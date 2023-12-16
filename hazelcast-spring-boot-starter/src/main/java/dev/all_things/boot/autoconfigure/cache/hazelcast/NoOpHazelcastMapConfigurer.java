package dev.all_things.boot.autoconfigure.cache.hazelcast;

import com.hazelcast.config.Config;

/**
 * No-op implementation of {@link HazelcastMapConfigurer}.
 * <p>
 * To be used as fallback when no other {@link HazelcastMapConfigurer} is provided.
 */
public class NoOpHazelcastMapConfigurer
		implements HazelcastMapConfigurer
{
	/**
	 * Does nothing.
	 */
	@Override
	public void configure(final Config config)
	{
		// No-op
	}
}