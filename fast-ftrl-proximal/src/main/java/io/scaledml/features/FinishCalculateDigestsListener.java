package io.scaledml.features;

import com.clearspring.analytics.stream.quantile.TDigest;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class FinishCalculateDigestsListener {
    private Long2ObjectMap<TDigest> digests = new Long2ObjectOpenHashMap<>();

    public synchronized void finishCalculateDigests(Long2ObjectMap<TDigest> eachDigests) {
        for (long index : eachDigests.keySet()) {
            if (!digests.containsKey(index)) {
                digests.put(index, eachDigests.get(index));
            } else {
                digests.get(index).add(eachDigests.get(index));
            }
        }
    }
}
