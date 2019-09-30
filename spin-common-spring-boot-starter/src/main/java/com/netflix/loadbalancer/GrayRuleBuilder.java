package com.netflix.loadbalancer;

public class GrayRuleBuilder {

    public static CompositePredicate createCompositePredicate(IRule rule) {
        ZoneAvoidancePredicate zonePredicate = new ZoneAvoidancePredicate(rule);
        AvailabilityPredicate availabilityPredicate = new AvailabilityPredicate(rule);
        GrayAvailablePredicate grayAvailablePredicate = new GrayAvailablePredicate(rule);
        return CompositePredicate.withPredicates(zonePredicate, availabilityPredicate, grayAvailablePredicate)
            .addFallbackPredicate(grayAvailablePredicate)
            .addFallbackPredicate(AbstractServerPredicate.alwaysTrue())
            .build();
    }
}
