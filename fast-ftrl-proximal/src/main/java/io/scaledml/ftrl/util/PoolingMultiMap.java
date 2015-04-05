package io.scaledml.ftrl.util;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class PoolingMultiMap<K, V> {
    private Supplier<V> valueFactory;
    private Function<K, K> cloneFactory;
    private V[] emptyArray;
    private Object2ObjectMap<K, V[]> elements;
    private Object2IntMap<K> sizes;

    public PoolingMultiMap(Supplier<V> valueFactory, Function<K, K> cloneFactory, V[] emptyArray) {
        this.valueFactory = valueFactory;
        this.cloneFactory = cloneFactory;
        this.emptyArray = emptyArray;
        elements = new Object2ObjectOpenHashMap<>();
        sizes = new Object2IntOpenHashMap<>();
    }

    public V appendNextValue(K key) {
        if (!elements.containsKey(key)) {
            K newKey = cloneFactory.apply(key);
            elements.put(newKey, ObjectArrays.grow(emptyArray, 64));
            sizes.put(newKey, 0);
        }
        int currentSize = sizes.getInt(key);
        V[] currentValues = elements.get(key);
        if (currentValues.length == currentSize) {
            elements.put(key, ObjectArrays.grow(currentValues, currentValues.length + 64));
            currentValues = elements.get(key);
        }
        if (currentValues[currentSize] == null) {
            currentValues[currentSize] = valueFactory.get();
        }
        sizes.put(key, currentSize + 1);
        return currentValues[currentSize];
    }

    public void clear() {
        for (K key : sizes.keySet()) {
            sizes.put(key, 0);
        }
    }

    public Iterable<K> keys() {
        return Iterables.filter(elements.keySet(), (K k) -> sizes.getInt(k) > 0);
    }

    public Iterable<V> getValues(K key) {
        return Arrays.asList(elements.get(key)).subList(0, sizes.getInt(key));
    }
}
