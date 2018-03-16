package org.spin.spring.condition;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Condition} that checks for the presence or absence of specific beans.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Jakub Kubrynski
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
@Order
class OnBeanCondition extends SpringBootCondition implements ConfigurationCondition {

    private static final String[] NO_BEANS = {};

    /**
     * Bean definition attribute name for factory beans to signal their product type (if
     * known and it can't be deduced from the factory meta class).
     */
    public static final String FACTORY_BEAN_OBJECT_TYPE = BeanTypeRegistry.FACTORY_BEAN_OBJECT_TYPE;

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage matchMessage = ConditionMessage.empty();
        if (metadata.isAnnotated(ConditionalOnBean.class.getName())) {
            BeanSearchSpec spec = new BeanSearchSpec(context, metadata,
                ConditionalOnBean.class);
            List<String> matching = getMatchingBeans(context, spec);
            if (matching.isEmpty()) {
                return ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnBean.class, spec)
                        .didNotFind("any beans").atAll());
            }
            matchMessage = matchMessage.andCondition(ConditionalOnBean.class, spec)
                .found("meta", "beans").items(ConditionMessage.Style.QUOTE, matching);
        }
        if (metadata.isAnnotated(ConditionalOnSingleCandidate.class.getName())) {
            BeanSearchSpec spec = new SingleCandidateBeanSearchSpec(context, metadata,
                ConditionalOnSingleCandidate.class);
            List<String> matching = getMatchingBeans(context, spec);
            if (matching.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage
                    .forCondition(ConditionalOnSingleCandidate.class, spec)
                    .didNotFind("any beans").atAll());
            } else if (!hasSingleAutowireCandidate(context.getBeanFactory(), matching,
                spec.getStrategy() == SearchStrategy.ALL)) {
                return ConditionOutcome.noMatch(ConditionMessage
                    .forCondition(ConditionalOnSingleCandidate.class, spec)
                    .didNotFind("a primary meta from beans")
                    .items(ConditionMessage.Style.QUOTE, matching));
            }
            matchMessage = matchMessage
                .andCondition(ConditionalOnSingleCandidate.class, spec)
                .found("a primary meta from beans").items(ConditionMessage.Style.QUOTE, matching);
        }
        if (metadata.isAnnotated(ConditionalOnMissingBean.class.getName())) {
            BeanSearchSpec spec = new BeanSearchSpec(context, metadata,
                ConditionalOnMissingBean.class);
            List<String> matching = getMatchingBeans(context, spec);
            if (!matching.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage
                    .forCondition(ConditionalOnMissingBean.class, spec)
                    .found("meta", "beans").items(ConditionMessage.Style.QUOTE, matching));
            }
            matchMessage = matchMessage.andCondition(ConditionalOnMissingBean.class, spec)
                .didNotFind("any beans").atAll();
        }
        return ConditionOutcome.match(matchMessage);
    }

    @SuppressWarnings("deprecation")
    private List<String> getMatchingBeans(ConditionContext context,
                                          BeanSearchSpec beans) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beans.getStrategy() == SearchStrategy.PARENTS
            || beans.getStrategy() == SearchStrategy.ANCESTORS) {
            BeanFactory parent = beanFactory.getParentBeanFactory();
            Assert.isInstanceOf(ConfigurableListableBeanFactory.class, parent,
                "Unable to use SearchStrategy.PARENTS");
            beanFactory = (ConfigurableListableBeanFactory) parent;
        }
        if (beanFactory == null) {
            return Collections.emptyList();
        }
        List<String> beanNames = new ArrayList<>();
        boolean considerHierarchy = beans.getStrategy() != SearchStrategy.CURRENT;
        for (String type : beans.getTypes()) {
            beanNames.addAll(getBeanNamesForType(beanFactory, type,
                context.getClassLoader(), considerHierarchy));
        }
        for (String ignoredType : beans.getIgnoredTypes()) {
            beanNames.removeAll(getBeanNamesForType(beanFactory, ignoredType,
                context.getClassLoader(), considerHierarchy));
        }
        for (String annotation : beans.getAnnotations()) {
            beanNames.addAll(Arrays.asList(getBeanNamesForAnnotation(beanFactory,
                annotation, context.getClassLoader(), considerHierarchy)));
        }
        for (String beanName : beans.getNames()) {
            if (containsBean(beanFactory, beanName, considerHierarchy)) {
                beanNames.add(beanName);
            }
        }
        return beanNames;
    }

    private boolean containsBean(ConfigurableListableBeanFactory beanFactory,
                                 String beanName, boolean considerHierarchy) {
        if (considerHierarchy) {
            return beanFactory.containsBean(beanName);
        }
        return beanFactory.containsLocalBean(beanName);
    }

    private Collection<String> getBeanNamesForType(ListableBeanFactory beanFactory,
                                                   String type, ClassLoader classLoader, boolean considerHierarchy)
        throws LinkageError {
        try {
            Set<String> result = new LinkedHashSet<>();
            collectBeanNamesForType(result, beanFactory,
                ClassUtils.forName(type, classLoader), considerHierarchy);
            return result;
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return Collections.emptySet();
        }
    }

    private void collectBeanNamesForType(Set<String> result,
                                         ListableBeanFactory beanFactory, Class<?> type, boolean considerHierarchy) {
        result.addAll(BeanTypeRegistry.get(beanFactory).getNamesForType(type));
        if (considerHierarchy && beanFactory instanceof HierarchicalBeanFactory) {
            BeanFactory parent = ((HierarchicalBeanFactory) beanFactory)
                .getParentBeanFactory();
            if (parent instanceof ListableBeanFactory) {
                collectBeanNamesForType(result, (ListableBeanFactory) parent, type,
                    true);
            }
        }
    }

    private String[] getBeanNamesForAnnotation(
        ConfigurableListableBeanFactory beanFactory, String type,
        ClassLoader classLoader, boolean considerHierarchy) throws LinkageError {
        String[] result;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> typeClass = (Class<? extends Annotation>) ClassUtils
                .forName(type, classLoader);
            result = beanFactory.getBeanNamesForAnnotation(typeClass);
            if (considerHierarchy) {
                if (beanFactory
                    .getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
                    String[] parentResult = getBeanNamesForAnnotation(
                        (ConfigurableListableBeanFactory) beanFactory
                            .getParentBeanFactory(),
                        type, classLoader, true);
                    List<String> resultList = new ArrayList<>();
                    resultList.addAll(Arrays.asList(result));
                    for (String beanName : parentResult) {
                        if (!resultList.contains(beanName)
                            && !beanFactory.containsLocalBean(beanName)) {
                            resultList.add(beanName);
                        }
                    }
                    result = StringUtils.toStringArray(resultList);
                }
            }
            return result;
        } catch (ClassNotFoundException ex) {
            return NO_BEANS;
        }
    }

    private boolean hasSingleAutowireCandidate(
        ConfigurableListableBeanFactory beanFactory, List<String> beanNames,
        boolean considerHierarchy) {
        return (beanNames.size() == 1
            || getPrimaryBeans(beanFactory, beanNames, considerHierarchy)
            .size() == 1);
    }

    private List<String> getPrimaryBeans(ConfigurableListableBeanFactory beanFactory,
                                         List<String> beanNames, boolean considerHierarchy) {
        List<String> primaryBeans = new ArrayList<>();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = findBeanDefinition(beanFactory, beanName,
                considerHierarchy);
            if (beanDefinition != null && beanDefinition.isPrimary()) {
                primaryBeans.add(beanName);
            }
        }
        return primaryBeans;
    }

    private BeanDefinition findBeanDefinition(ConfigurableListableBeanFactory beanFactory,
                                              String beanName, boolean considerHierarchy) {
        if (beanFactory.containsBeanDefinition(beanName)) {
            return beanFactory.getBeanDefinition(beanName);
        }
        if (considerHierarchy && beanFactory
            .getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
            return findBeanDefinition(((ConfigurableListableBeanFactory) beanFactory
                .getParentBeanFactory()), beanName, true);
        }
        return null;

    }

    private static class BeanSearchSpec {

        private final Class<?> annotationType;

        private final List<String> names = new ArrayList<>();

        private final List<String> types = new ArrayList<>();

        private final List<String> annotations = new ArrayList<>();

        private final List<String> ignoredTypes = new ArrayList<>();

        private final SearchStrategy strategy;

        BeanSearchSpec(ConditionContext context, AnnotatedTypeMetadata metadata,
                       Class<?> annotationType) {
            this.annotationType = annotationType;
            MultiValueMap<String, Object> attributes = metadata
                .getAllAnnotationAttributes(annotationType.getName(), true);
            collect(attributes, "name", this.names);
            collect(attributes, "value", this.types);
            collect(attributes, "type", this.types);
            collect(attributes, "annotation", this.annotations);
            collect(attributes, "ignored", this.ignoredTypes);
            collect(attributes, "ignoredType", this.ignoredTypes);
            this.strategy = (SearchStrategy) metadata
                .getAnnotationAttributes(annotationType.getName()).get("search");
            BeanTypeDeductionException deductionException = null;
            try {
                if (this.types.isEmpty() && this.names.isEmpty()) {
                    addDeducedBeanType(context, metadata, this.types);
                }
            } catch (BeanTypeDeductionException ex) {
                deductionException = ex;
            }
            validate(deductionException);
        }

        protected void validate(BeanTypeDeductionException ex) {
            if (!hasAtLeastOne(this.types, this.names, this.annotations)) {
                String message = annotationName()
                    + " did not specify a meta using type, name or annotation";
                if (ex == null) {
                    throw new IllegalStateException(message);
                }
                throw new IllegalStateException(message + " and the attempt to deduce"
                    + " the meta's type failed", ex);
            }
        }

        private boolean hasAtLeastOne(List<?>... lists) {
            for (List<?> list : lists) {
                if (!list.isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        protected String annotationName() {
            return "@" + ClassUtils.getShortName(this.annotationType);
        }

        protected void collect(MultiValueMap<String, Object> attributes, String key,
                               List<String> destination) {
            List<?> values = attributes.get(key);
            if (values != null) {
                for (Object value : values) {
                    if (value instanceof String[]) {
                        Collections.addAll(destination, (String[]) value);
                    } else {
                        destination.add((String) value);
                    }
                }
            }
        }

        private void addDeducedBeanType(ConditionContext context,
                                        AnnotatedTypeMetadata metadata, final List<String> beanTypes) {
            if (metadata instanceof MethodMetadata
                && metadata.isAnnotated(Bean.class.getName())) {
                addDeducedBeanTypeForBeanMethod(context, (MethodMetadata) metadata,
                    beanTypes);
            }
        }

        private void addDeducedBeanTypeForBeanMethod(ConditionContext context,
                                                     MethodMetadata metadata, final List<String> beanTypes) {
            try {
                // We should be safe to load at this point since we are in the
                // REGISTER_BEAN phase
                Class<?> returnType = ClassUtils.forName(metadata.getReturnTypeName(),
                    context.getClassLoader());
                beanTypes.add(returnType.getName());
            } catch (Throwable ex) {
                throw new BeanTypeDeductionException(metadata.getDeclaringClassName(),
                    metadata.getMethodName(), ex);
            }
        }

        public SearchStrategy getStrategy() {
            return (this.strategy != null ? this.strategy : SearchStrategy.ALL);
        }

        public List<String> getNames() {
            return this.names;
        }

        public List<String> getTypes() {
            return this.types;
        }

        public List<String> getAnnotations() {
            return this.annotations;
        }

        public List<String> getIgnoredTypes() {
            return this.ignoredTypes;
        }

        @Override
        public String toString() {
            StringBuilder string = new StringBuilder();
            string.append("(");
            if (!this.names.isEmpty()) {
                string.append("names: ");
                string.append(StringUtils.collectionToCommaDelimitedString(this.names));
                if (!this.types.isEmpty()) {
                    string.append("; ");
                }
            }
            if (!this.types.isEmpty()) {
                string.append("types: ");
                string.append(StringUtils.collectionToCommaDelimitedString(this.types));
            }
            string.append("; SearchStrategy: ");
            string.append(this.strategy.toString().toLowerCase());
            string.append(")");
            return string.toString();
        }

    }

    static final class BeanTypeDeductionException extends RuntimeException {

        private BeanTypeDeductionException(String className, String beanMethodName,
                                           Throwable cause) {
            super("Failed to deduce meta type for " + className + "." + beanMethodName,
                cause);
        }
    }

    private static class SingleCandidateBeanSearchSpec extends BeanSearchSpec {

        SingleCandidateBeanSearchSpec(ConditionContext context,
                                      AnnotatedTypeMetadata metadata, Class<?> annotationType) {
            super(context, metadata, annotationType);
        }

        @Override
        protected void collect(MultiValueMap<String, Object> attributes, String key,
                               List<String> destination) {
            super.collect(attributes, key, destination);
            destination.removeAll(Arrays.asList("", Object.class.getName()));
        }

        @Override
        protected void validate(BeanTypeDeductionException ex) {
            Assert.isTrue(getTypes().size() == 1, annotationName() + " annotations must "
                + "specify only one type (got " + getTypes() + ")");
        }

    }
}
