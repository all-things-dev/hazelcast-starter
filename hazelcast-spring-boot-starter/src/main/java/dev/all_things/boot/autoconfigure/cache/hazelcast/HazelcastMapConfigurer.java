package dev.all_things.boot.autoconfigure.cache.hazelcast;

import java.time.Duration;

import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * Provides a way create Hazelcast {@link IMap} with custom configuration.
 */
@FunctionalInterface
public interface HazelcastMapConfigurer
{
	/**
	 * Configures a {@link MapConfig} instance with custom configuration.
	 *
	 * @param config {@link Config} to which custom {@link MapConfig} will be added.
	 */
	public void configure(final Config config);

	/**
	 * Creates and configures a {@link MapConfig} instance with provided configuration values.
	 *
	 * @param config        configuration for {@link HazelcastInstance}.
	 * @param cacheName     the name of the cache being configured.
	 * @param cacheDuration maximum duration for which values will be cached.
	 * @param maxCacheSize  maximum size of {@link IMap} being configured.
	 */
	public static void createMapConfig(final Config config, final String cacheName, final Duration cacheDuration, final int maxCacheSize)
	{
		createMapConfig(config, cacheName, cacheDuration, MaxSizePolicy.PER_NODE, maxCacheSize);
	}

	/**
	 * Creates and configures a {@link MapConfig} instance with provided configuration values.
	 *
	 * @param config        configuration for {@link HazelcastInstance}.
	 * @param cacheName     the name of the cache being configured.
	 * @param cacheDuration maximum duration for which values will be cached.
	 * @param maxSizePolicy interpretation of the {maxCacheSize} value e.g. memory size, entry count, etc.
	 * @param maxCacheSize  maximum size of {@link IMap} being configured.
	 */
	public static void createMapConfig(final Config config, final String cacheName, final Duration cacheDuration, final MaxSizePolicy maxSizePolicy, final int maxCacheSize)
	{
		final MapConfig mapConfig = new MapConfig(cacheName);

		// Configuring cache retention duration
		mapConfig.setTimeToLiveSeconds((int) cacheDuration.toSeconds());

		// Configuring cache eviction policy
		mapConfig.getEvictionConfig()

				 // Interpretation of the {maxCacheSize} value e.g. memory size, entry count, etc.
				 .setMaxSizePolicy(maxSizePolicy)

				 // Configuration cache max size to avoid {OutOfMemoryError} by limited map size
				 .setSize(maxCacheSize)

				 // Least recently used entries will be evicted first from the cache
				 .setEvictionPolicy(EvictionPolicy.LRU);

		// Adding created {MapConfig} to {HazelcastInstance} configuration.
		config.addMapConfig(mapConfig);
	}


}
