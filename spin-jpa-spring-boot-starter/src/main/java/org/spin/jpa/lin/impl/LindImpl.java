package org.spin.jpa.lin.impl;

import org.spin.jpa.lin.Lind;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;

public class LindImpl<D> extends LinImpl<Lind<D>, CriteriaDelete<?>> implements Lind<D> {

    public LindImpl(Class<D> domainClass) {
        this(domainClass, null);
    }

    public LindImpl(Class<D> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        CriteriaDelete<D> criteriaDelete = cb.createCriteriaDelete(domainClass);
        criteria = criteriaDelete;
        root = criteriaDelete.from(domainClass);
    }

    public LindImpl(LindImpl<D> parent, Class<?> domainClass) {
        super(parent, domainClass);
    }

    @Override
    public LindImpl<D> createChild(Class<?> domainClass) {
        return new LindImpl<>(this, domainClass);
    }

    @Override
    public int delete() {
        if (parent != null) {
            applyPredicateToCriteria();
            return parent.delete();
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
