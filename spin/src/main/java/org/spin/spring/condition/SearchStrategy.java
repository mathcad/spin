package org.spin.spring.condition;

/**
 * Some named search strategies for beans in the bean factory hierarchy.
 *
 * @author Dave Syer
 */
public enum SearchStrategy {

    /**
     * Search only the current context.
     */
    CURRENT,

    /**
     * Search all parents and ancestors, but not the current context.
     *
     * @deprecated as of 1.5 in favor of {@link SearchStrategy#ANCESTORS}
     */
    @Deprecated PARENTS,

    /**
     * Search all ancestors, but not the current context.
     */
    ANCESTORS,

    /**
     * Search the entire hierarchy.
     */
    ALL
}
