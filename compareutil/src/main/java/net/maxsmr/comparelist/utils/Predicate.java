package net.maxsmr.comparelist.utils;

import net.maxsmr.comparelist.utils.support.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Predicate<V> {

    boolean apply(V element);

    class Methods {

        public static <V> boolean contains(Collection<V> elements, Predicate<V> predicate) {
            return findWithIndex(elements, predicate) != null;
        }

        public static <V> Pair<Integer, V> findWithIndex(Collection<V> elements, Predicate<V> predicate) {
            int targetIndex = -1;
            V result = null;
            if (elements != null) {
                int index = 0;
                for (V elem : elements) {
                    if (predicate.apply(elem)) {
                        result = elem;
                        targetIndex = index;
                        break;
                    }
                    index++;
                }
            }
            return targetIndex >= 0 ? new Pair<>(targetIndex, result) : null;
        }

        public static <V> V find(Collection<V> elements, Predicate<V> predicate) {
            Pair<Integer, V> result = findWithIndex(elements, predicate);
            return result != null ? result.second : null;
        }

        public static <V> Map<Integer, V> filterWithIndex(Collection<V> elements, Predicate<V>
                predicate) {
            Map<Integer, V> result = new LinkedHashMap<>();
            if (elements != null) {
                int index = 0;
                for (V elem : elements) {
                    if (predicate.apply(elem)) {
                        result.put(index, elem);
                    }
                    index++;
                }
            }
            return result;
        }

        public static <V> List<V> filter(Collection<V> elements, Predicate<V> predicate) {
            Map<Integer, V> map = filterWithIndex(elements, predicate);
            return entriesToValues(map.entrySet());
        }

        public static <K, V> List<K> entriesToKeys(Collection<Map.Entry<K, V>> entries) {
            List<K> result = new ArrayList<>();
            if (entries != null) {
                for (Map.Entry<K, V> e : entries) {
                    if (e != null) {
                        result.add(e.getKey());
                    }
                }
            }
            return result;
        }

        public static <K, V> List<V> entriesToValues(Collection<Map.Entry<K, V>> entries) {
            List<V> result = new ArrayList<>();
            if (entries != null) {
                for (Map.Entry<K, V> e : entries) {
                    if (e != null) {
                        result.add(e.getValue());
                    }
                }
            }
            return result;
        }
    }
}
