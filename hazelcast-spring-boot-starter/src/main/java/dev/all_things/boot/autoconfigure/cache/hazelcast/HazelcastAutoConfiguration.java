package dev.all_things.boot.autoconfigure.cache.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * Provides auto-configuration for Hazelcast.
 */
@AutoConfiguration
@Import(HazelcastConfiguration.class)
@ConditionalOnClass(value = { HazelcastInstance.class })
public class HazelcastAutoConfiguration
{
}
