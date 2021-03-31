package org.spin.jpa.lin.impl;

import org.spin.core.util.BeanUtils;
import org.spin.core.util.LambdaUtils;
import org.spin.jpa.Prop;
import org.spin.jpa.PropImpl;
import org.spin.jpa.lin.Linu;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

public class LinuImpl<T> extends LinImpl<Linu<T>, CriteriaUpdate<?>> implements Linu<T> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LinuImpl(Class<?> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        criteria = cb.createCriteriaUpdate(domainClass);
        root = criteria.from((Class) domainClass);
    }

    public LinuImpl(Class<?> domainClass) {
        this(domainClass, null);
    }

    public LinuImpl(LinuImpl<T> parent, Class<?> domainClass) {
        super(parent, domainClass);
    }

    @Override
    public Linu<T> createChild(Class<?> domainClass) {
        return new LinuImpl<>(this, domainClass);
    }

    @Override
    public Linu<T> set(String attributeName, Object value) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        criteria.set(attributeName, value);
        return this;
    }

    @Override
    public Linu<T> set(Prop<T, ?> attribute, Object value) {
        @SuppressWarnings("unchecked")
        String p = attribute instanceof PropImpl ?
            ((PropImpl<T>) attribute).apply(null) :
            BeanUtils.toFieldName(LambdaUtils.resolveLambda(attribute).getImplMethodName());
        return set(p, value);
    }

    @Override
    public <Y> Linu<T> set(Path<Y> attribute, Expression<? extends Y> value) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        criteria.set(attribute, value);
        return this;
    }

    @Override
    public <Y, X extends Y> Linu<T> set(Path<Y> attribute, X value) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        criteria.set(attribute, value);
        return this;
    }

    @Override
    public <Y, X extends Y> Linu<T> set(SingularAttribute<? super Object, Y> attribute, X value) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        criteria.set(attribute, value);
        return this;
    }

    @Override
    public <Y> Linu<T> set(SingularAttribute<? super Object, Y> attribute, Expression<? extends Y> value) {
        if (!beforeMethodInvoke()) {
            return this;
        }
        criteria.set(attribute, value);
        return this;
    }

    @Override
    public int update() {
        if (parent != null) {
            applyPredicateToCriteria();
            return parent.update();
        }
        applyPredicateToCriteria();
        return em.createQuery(criteria).executeUpdate();
    }

    protected void applyPredicateToCriteria() {
        Predicate predicate = parsePredicate(junction);
        if (predicate != null) {
            if (sq != null) {
                sq.where(predicate);
            } else {
                criteria.where(predicate);
            }
        }
    }


}
