package org.spin.jpa.strategy;

import javax.persistence.EntityManagerFactory;

public interface GetEntityManagerFactoryStrategy {

    EntityManagerFactory getEntityManagerFactory(Class<?> domainClass);

}
