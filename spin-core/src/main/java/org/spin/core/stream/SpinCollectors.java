package org.spin.core.stream;

import org.spin.core.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 扩展的Java8 Stream收集器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/2/29</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class SpinCollectors {

    public static <E> Collector<Set<E>, Set<E>, Set<E>> mergeSet() {
        return Collector.of(HashSet::new, Set::addAll, CollectionUtils::mergeIntoLeft);
    }

    public static <E, R> Collector<Set<E>, Set<E>, R> mergeSet(Function<Set<E>, R> finisher) {
        return Collector.of(HashSet::new, Set::addAll, CollectionUtils::mergeIntoLeft, finisher);
    }

    public static <T, E> Collector<T, Set<E>, Set<E>> mergeToSet(Function<T, Set<E>> parser) {
        return Collector.of(HashSet::new, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft);
    }

    public static <T, E, R> Collector<T, Set<E>, R> mergeToSet(Function<T, Set<E>> parser, Function<Set<E>, R> finisher) {
        return Collector.of(HashSet::new, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft, finisher);
    }

    public static <E> Collector<List<E>, List<E>, List<E>> mergeList() {
        return Collector.of(LinkedList::new, List::addAll, CollectionUtils::mergeIntoLeft);
    }

    public static <E, R> Collector<List<E>, List<E>, R> mergeList(Function<List<E>, R> finisher) {
        return Collector.of(LinkedList::new, List::addAll, CollectionUtils::mergeIntoLeft, finisher);
    }

    public static <T, E> Collector<T, List<E>, List<E>> mergeToList(Function<T, List<E>> parser) {
        return Collector.of(LinkedList::new, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft);
    }

    public static <T, E, R> Collector<T, List<E>, R> mergeToList(Function<T, List<E>> parser, Function<List<E>, R> finisher) {
        return Collector.of(LinkedList::new, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft, finisher);
    }

    public static <T, E> Collector<T, Collection<E>, Collection<E>> merge(Supplier<Collection<E>> supplier, Function<T, List<E>> parser) {
        return Collector.of(supplier, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft);
    }

    public static <T, E, R> Collector<T, Collection<E>, R> merge(Supplier<Collection<E>> supplier, Function<T, List<E>> parser, Function<Collection<E>, R> finisher) {
        return Collector.of(supplier, (s, l) -> s.addAll(parser.apply(l)), CollectionUtils::mergeIntoLeft, finisher);
    }

    public static <T, K, A, P, D> Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier,
                                                                        Function<? super T, P> mapper,
                                                                        Collector<P, A, D> downstream) {
        return Collectors.groupingBy(classifier, HashMap::new, Collectors.mapping(mapper, downstream));
    }

    public static <R> Collector<CharSequence, StringBuilder, R> joining(Function<String, R> finisher) {
        return Collector.of(StringBuilder::new, StringBuilder::append,
            (r1, r2) -> {
                r1.append(r2);
                return r1;
            },

            ((Function<StringBuilder, String>) StringBuilder::toString).andThen(finisher));
    }

    public static <R> Collector<CharSequence, StringJoiner, R> joining(CharSequence delimiter, Function<String, R> finisher) {
        return joining(delimiter, "", "", finisher);
    }

    public static <R> Collector<CharSequence, StringJoiner, R> joining(CharSequence delimiter,
                                                                       CharSequence prefix,
                                                                       CharSequence suffix, Function<String, R> finisher) {
        return Collector.of(() -> new StringJoiner(delimiter, prefix, suffix),
            StringJoiner::add, StringJoiner::merge,
            ((Function<StringJoiner, String>) StringJoiner::toString).andThen(finisher));
    }
}
