package org.spin.jpa.lin.impl;

import org.spin.core.Assert;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.ConstructorUtils;
import org.spin.core.util.LambdaUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.jpa.Prop;
import org.spin.jpa.PropImpl;
import org.spin.jpa.lin.Linq;
import org.spin.jpa.transform.ResultTransformer;
import org.spin.jpa.transform.impl.Transformers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

//@SuppressWarnings("unchecked")
public class LinqImpl<R> extends LinImpl<Linq<R, LinqImpl<R>>, CriteriaQuery<?>> implements Linq<R, LinqImpl<R>> {

    protected List<Order> orders = new ArrayList<>();
    protected boolean distinct;
    protected Class<?> resultClass;
    protected ResultTransformer resultTransformer;


    public LinqImpl(Class<?> domainClass) {
        this(domainClass, (EntityManager) null);
    }

    public LinqImpl(Class<?> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        criteria = cb.createQuery(domainClass);
        root = criteria.from(domainClass);
        resultClass = domainClass;
    }

    public LinqImpl(Class<?> domainClass, Class<R> resultClass) {
        this(domainClass, resultClass, null);
    }

    @SuppressWarnings("rawtypes")
    public LinqImpl(Class<?> domainClass, Class<R> resultClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        if (Tuple.class.isAssignableFrom(resultClass)) {
            criteria = cb.createTupleQuery();
            root = criteria.from(domainClass);
        } else if (Map.class.isAssignableFrom(resultClass)) {
            criteria = cb.createQuery(Object[].class);
            root = criteria.from(domainClass);
            resultTransformer = Transformers.ALIAS_TO_MAP;
            Set<?> attrs = em.getMetamodel().entity(domainClass).getDeclaredSingularAttributes();
            String[] selections = new String[attrs.size()];
            int i = 0;
            for (Object attr : attrs) {
                selections[i] = ((SingularAttribute) attr).getName();
                i++;
            }
            select(selections);
        } else {
            criteria = cb.createQuery(resultClass);
            root = criteria.from(domainClass);
        }
        this.resultClass = resultClass;
    }

    public LinqImpl(LinqImpl<R> parent, Class<?> domainClass) {
        super(parent, domainClass);
    }

    @Override
    public LinqImpl<R> selectAll() {
        if (!beforeMethodInvoke()) {
            return this;
        }

        if (resultClass == Map.class || resultClass == Tuple.class) {
            return this;
        }

        List<Selection<?>> selections = new LinkedList<>();
        ReflectionUtils.doWithFields(resultClass, field -> {
            if (!Modifier.isTransient(field.getModifiers())) {
                parseSelectionStr(selections, field.getName());
            }
        });

        return select(selections.toArray(new Selection<?>[0]));
    }


    @Override
    public LinqImpl<R> selectId() {
        return select(org.spin.jpa.R.getIdName(domainClass));
    }

    @Override
    public LinqImpl<R> select(String... selections) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        List<Selection<?>> list = new ArrayList<>(selections.length);
        for (String selection : selections) {
            parseSelectionStr(list, selection);
        }
        select(list.toArray(new Selection<?>[0]));
        return this;
    }

    @Override
    public LinqImpl<R> selectExclude(String... exclusion) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        HashSet<String> strings = org.spin.core.util.CollectionUtils.ofHashSet(exclusion);
        List<Selection<?>> selections = new LinkedList<>();
        ReflectionUtils.doWithFields(resultClass, field -> {
            if (!Modifier.isTransient(field.getModifiers()) && !strings.contains(field.getName())) {
                parseSelectionStr(selections, field.getName());
            }
        });
        return select(selections.toArray(new Selection<?>[0]));
    }

    @SafeVarargs
    public final LinqImpl<R> selectExclude(Prop<R, ?>... exclusion) {
        @SuppressWarnings("unchecked")
        String[] exclusions = Arrays.stream(exclusion).map(it -> it instanceof PropImpl ? ((PropImpl<R>) it).apply(null) :
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(it).getImplMethodName())).toArray(String[]::new);

        return select(exclusions);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final LinqImpl<R> select(Prop<R, ?>... selections) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        List<Selection<?>> list = new ArrayList<>(selections.length);
        for (Prop<R, ?> selection : selections) {
            parseSelectionStr(list, selection instanceof PropImpl ?
                ((PropImpl<R>) selection).apply(null) :
                BeanUtils.toFieldName(LambdaUtils.resolveLambda(selection).getImplMethodName()));
        }
        select(list.toArray(new Selection<?>[0]));
        return this;
    }

    @Override
    public LinqImpl<R> select(Object... selections) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        List<Selection<?>> list = new ArrayList<>(selections.length);
        for (Object selection : selections) {
            if (selection instanceof String) {
                parseSelectionStr(list, (String) selection);
            } else if (selection instanceof Selection) {
                list.add((Selection<?>) selection);
            }
        }
        select(list.toArray(new Selection<?>[0]));
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "ConstantConditions", "unchecked"})
    public final LinqImpl<R> select(Selection<?>... selections) {
        if (!beforeMethodInvoke()) {
            return this;
        }

        Assert.isTrue(sq == null || selections.length == 1, "selections can only have one in subquery! ");
        Assert.isTrue(sq == null || selections[0] instanceof Expression, "Elements in the selections must implement the " + Expression.class.getName() + " interface in subquery! ");
        Assert.isTrue(sq != null || criteria instanceof CriteriaQuery, "Not supported!");
        if (sq == null) {
            ((CriteriaQuery) criteria).multiselect(selections);
            aliases.clear();
        } else {
            sq.select((Expression) selections[0]);
        }
        for (Selection<?> selection : selections) {
            aliases.add(selection.getAlias());
        }

        return this;
    }

    @Override
    public boolean exist() {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.exist();
        }
        return count() > 0;
    }

    @Override
    public LinqImpl<R> exists(Class<?> domainClass) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        LinqImpl<R> lin = createChild(domainClass);
        lin.select(lin.root());
        add(cb.exists(lin.getSubquery()));
        return lin;
    }

    @Override
    public LinqImpl<R> notExists(Class<?> domainClass) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        LinqImpl<R> lin = createChild(domainClass);
        lin.select(lin.root());
        add(cb.not(cb.exists(lin.getSubquery())));
        return lin;
    }

    private void parseSelectionStr(List<Selection<?>> result, String selection) {
        String[] ps = selection.split("\\s*,\\s*");
        for (String p : ps) {
            String alias = p.trim();
            String[] pa = alias.split("\\s+[aA][sS]\\s+");
            if (pa.length > 1) {
                alias = pa[1];
            } else {
                pa = alias.split("\\s+");
                if (pa.length > 1) {
                    alias = pa[1];
                }
            }
            String[] paths = ps[0].split("\\.");
            Path<?> s = root.get(paths[0]);
            for (int i = 1; i < paths.length; i++) {
                s = s.get(paths[i]);
            }
            result.add(s.alias(alias));
        }
    }

    @Override
    public LinqImpl<R> distinct() {
        if (!beforeMethodInvoke()) {
            return this;
        }
        distinct = true;
        return this;
    }

    @Override
    public LinqImpl<R> groupBy(String... grouping) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        List<Expression<?>> expressions = new ArrayList<>();
        for (String property : grouping) {
            expressions.add(root.get(property));
        }
        if (sq != null) {
            sq.groupBy(expressions);
        } else {
            criteria.groupBy(expressions);
        }
        return this;
    }

    @Override
    public LinqImpl<R> desc(String... properties) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        for (String property : properties) {
            orders.add(cb.desc(root.get(property)));
        }
        return this;
    }

    @Override
    public LinqImpl<R> desc(Expression<?>... expressions) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        for (Expression<?> expression : expressions) {
            orders.add(cb.desc(expression));
        }
        return this;
    }

    @Override
    public LinqImpl<R> asc(String... properties) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        for (String property : properties) {
            orders.add(cb.asc(root.get(property)));
        }
        return this;
    }

    @Override
    public LinqImpl<R> asc(Expression<?>... expressions) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        for (Expression<?> expression : expressions) {
            orders.add(cb.asc(expression));
        }
        return this;
    }

    @Override
    public Optional<R> findOne() {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.findOne();
        }
        applyPredicateToCriteria(criteria);
        TypedQuery<?> query = createQuery();
        List<R> list = transform(query, 1);
        return Optional.ofNullable(CollectionUtils.first(list));
    }

    @Override
    public Optional<R> findFirst() {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.findOne();
        }
        applyPredicateToCriteria(criteria);
        TypedQuery<?> query = createQuery();
        List<R> list = transform(query, 2);
        return Optional.ofNullable(CollectionUtils.first(list));
    }

    @Override
    public List<R> list() {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.list();
        }
        applyPredicateToCriteria(criteria);
        return transform(createQuery(), 0);
    }

    @Override
    public Page<R> paging(Pageable pageable) {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.paging(pageable);
        }
        List<R> list;
        if (pageable == null) {
            list = list();
            return new PageImpl<>(list);
        } else {
            Sort sort = pageable.getSort();
            if (null != sort) {
                orders.addAll(QueryUtils.toOrders(sort, root, cb));
            }
            applyPredicateToCriteria(criteria);
            TypedQuery<?> query = createQuery();
            long offset = pageable.getOffset();
            query.setFirstResult((int) offset);
            query.setMaxResults(pageable.getPageSize());

            Long total = org.spin.jpa.R.count(criteria);
            List<R> content = Collections.emptyList();
            if (total > pageable.getOffset()) {
                content = transform(query, 0);
            }

            return new PageImpl<>(content, pageable, total);
        }
    }

    @Override
    public List<R> list(Pageable pageable) {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.list(pageable);
        }
        if (pageable == null) {
            return list();
        } else {
            Sort sort = pageable.getSort();
            orders.addAll(QueryUtils.toOrders(sort, root, cb));
            applyPredicateToCriteria(criteria);
            TypedQuery<?> query = createQuery();

            long offset = pageable.getOffset();
            query.setFirstResult((int) offset);
            query.setMaxResults(pageable.getPageSize());

            return transform(query, 0);
        }
    }

    @Override
    public List<R> list(int page, int size) {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.list(page, size);
        }
        applyPredicateToCriteria(criteria);
        TypedQuery<?> query = createQuery();

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return transform(query, 0);
    }

    private TypedQuery<?> createQuery() {

        try {
            Selection<?> selection = BeanUtils.getFieldValue(criteria, "queryStructure.selection");
            List<Selection<?>> selections;
            if (selection.isCompoundSelection()) {
                selections = selection.getCompoundSelectionItems();
            } else {
                selections = CollectionUtils.ofArrayList(selection);
            }

            Class<?>[] typeClass = new Class[selections.size()];
            for (int i = 0; i < selections.size(); i++) {
                typeClass[i] = selections.get(i).getJavaType();
            }

            Constructor<?> accessibleConstructor = ConstructorUtils.getAccessibleConstructor(resultClass, typeClass);
            if (null == accessibleConstructor) {
                BeanUtils.setFieldValue(criteria, "queryStructure.selection.isConstructor", false);
            }
        } catch (Exception ignore) {
            // do nothing
        }

        return em.createQuery(criteria);
    }

    @Override
    public Long count() {
        if (parent != null) {
            applyPredicateToCriteria(sq);
            return parent.count();
        }
        return org.spin.jpa.R.executeCountQuery(getCountQuery());
    }

    protected TypedQuery<Long> getCountQuery() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        criteria.getRoots().add(root);
        applyPredicateToCriteria(criteria);
        criteria.getOrderList().clear();
        if (distinct) {
            criteria.select(cb.countDistinct(root));
        } else {
            criteria.select(cb.count(root));
        }
        return em.createQuery(criteria);
    }

    protected void applyPredicateToCriteria(AbstractQuery<?> query) {
        Predicate predicate = parsePredicate(junction);
        if (predicate != null) {
            query.where(predicate);
        }

        predicate = parsePredicate(having);
        if (predicate != null) {
            query.having(predicate);
        }

        if (query instanceof CriteriaQuery) {
            if (!CollectionUtils.isEmpty(orders)) {
                ((CriteriaQuery<?>) query).orderBy(orders);
            }
        }
    }

    /**
     * 获取查询结果并转换
     *
     * @param query  查询
     * @param single 0-多条 1-唯一 2-第一条
     * @param <T>    结果泛型
     * @return 结果列表
     */
    @SuppressWarnings({"unchecked"})
    protected <T> List<T> transform(Query query, int single) {
        List<T> result;
        @SuppressWarnings("rawtypes")
        List tuples;
        if (Assert.inclusiveBetween(0, 2, single, "single不合法") > 0) {
            tuples = new ArrayList<>(1);
            query.setMaxResults(3 - single);
            tuples.add(1 == single ? query.getSingleResult() : CollectionUtils.first(query.getResultList()));
        } else {
            tuples = query.getResultList();
        }
        if (tuples.isEmpty() || resultClass.isInstance(tuples.get(0))) {
            return tuples;
        } else {
            result = new ArrayList<>(tuples.size());
            String[] aliases = this.aliases.toArray(new String[0]);
            if (resultTransformer == null) {
                if (resultClass == Map.class) {
                    resultTransformer = Transformers.ALIAS_TO_MAP;
                } else {
                    resultTransformer = Transformers.aliasToBean(resultClass);
                }
            }
            for (Object tuple : tuples) {
                if (tuple != null) {
                    if (tuple.getClass().isArray()) {
                        result.add((T) resultTransformer.transformTuple((Object[]) tuple, aliases));
                    } else {
                        result.add((T) resultTransformer.transformTuple(new Object[]{tuple}, aliases));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public LinqImpl<R> createChild(Class<?> domainClass) {
        return new LinqImpl<>(this, domainClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> LinqImpl<T> aliasToBean() {
        if (!beforeMethodInvoke()) {
            return (LinqImpl<T>) this;
        }
        criteria = cb.createQuery(Object[].class);
        root = criteria.from(domainClass);
        resultTransformer = Transformers.aliasToBean(domainClass);
        return (LinqImpl<T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> LinqImpl<T> aliasToBean(Class<T> resultClass) {
        if (!beforeMethodInvoke()) {
            return (LinqImpl<T>) this;
        }
        criteria = cb.createQuery(Object[].class);
        root = criteria.from(domainClass);
        this.resultClass = resultClass;
        resultTransformer = Transformers.aliasToBean(resultClass);
        return (LinqImpl<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LinqImpl<Map<String, Object>> aliasToMap() {
        if (!beforeMethodInvoke()) {
            return (LinqImpl<Map<String, Object>>) this;
        }
        criteria = cb.createQuery(Object[].class);
        root = criteria.from(domainClass);
        this.resultClass = Map.class;
        resultTransformer = Transformers.ALIAS_TO_MAP;
        return (LinqImpl<Map<String, Object>>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinqImpl<Tuple> aliasToTuple() {
        if (!beforeMethodInvoke()) {
            return (LinqImpl<Tuple>) this;
        }
        criteria = cb.createTupleQuery();
        root = criteria.from(domainClass);
        resultClass = Tuple.class;
        return (LinqImpl<Tuple>) this;
    }
}
