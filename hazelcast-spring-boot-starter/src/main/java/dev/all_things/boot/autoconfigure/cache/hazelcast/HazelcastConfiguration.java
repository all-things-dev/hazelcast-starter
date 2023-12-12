package dev.all_things.boot.autoconfigure.cache.hazelcast;

import java.util.*;
import java.util.stream.IntStream;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

/**
 * Configures {@link HazelcastInstance} with provided properties.
 * <p>
 * 'proxyTargetClass' needs to be set to 'true' when caching mode is {@link AdviceMode#PROXY}.
 * Default configuration ('proxyTargetClass = false') produces JDK proxy classes.
 * This is incompatible with {@link ApplicationContext#getBean(Class)}.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HazelcastProperties.class)
@EnableCaching(mode = AdviceMode.PROXY, proxyTargetClass = true)
public class HazelcastConfiguration
{
	private static final Logger logger = LogManager.getLogger(HazelcastConfiguration.class);

	/**
	 * Creates a server instance of {@link HazelcastInstance} for caching.
	 * This instance will be used by cache manager application level caching.
	 *
	 * @param hazelcastProperties for configuring {@link HazelcastInstance}.
	 * @return {@link HazelcastInstance} instance customized according to {@link HazelcastProperties}.
	 */
	@Bean(name = "hazelcastInstance", destroyMethod = "shutdown")
	@ConditionalOnMissingBean(HazelcastInstance.class)
	@ConditionalOnProperty(prefix = "application.cache.hazelcast", name = "mode", havingValue = "client")
	public HazelcastInstance hazelcastClientInstance(final HazelcastProperties hazelcastProperties)
	{
		System.setProperty("hazelcast.phone.home.enabled", "false");

		final HazelcastProperties.Client properties = hazelcastProperties.getClient();
		final ClientConfig config = new ClientConfig();

		config.setClusterName(hazelcastProperties.getClusterName()); // Configuring the name of the cluster to which a client will connect
		config.setInstanceName(hazelcastProperties.getInstanceName()); // Configuring the name of the instance to which a client will connect
		config.setProperty(ClusterProperty.LOGGING_TYPE.getName(), "slf4j"); // Configuring Hazelcast to use SLF4J logging

		// Configuring client connection retry properties
		config.getConnectionStrategyConfig()
			  .getConnectionRetryConfig()
			  .setClusterConnectTimeoutMillis(Long.MAX_VALUE); // Retry connecting to cluster indefinitely

		// Configuration network properties
		final ClientNetworkConfig networkConfig = config.getNetworkConfig();

		networkConfig.setAddresses(properties.getServerAddresses()) // Configuring addresses of servers in cluster
					 .setSmartRouting(properties.getSmartRouting().getEnabled()) // Enabling / disabling smart routing of requests to nearest server
					 .setConnectionTimeout((int) properties.getConnectionTimeout().toMillis()); // Configuring client connection timeout

		final HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(config);

		logger.info("Hazelcast client instance created : {}", hazelcastInstance.getName());

		return hazelcastInstance;
	}

	/**
	 * Creates a server instance of {@link HazelcastInstance} for caching.
	 * This instance will be used by cache manager application level caching.
	 *
	 * @param hazelcastProperties for configuring {@link HazelcastInstance}.
	 * @return {@link HazelcastInstance} instance customized according to {@link HazelcastProperties}.
	 */
	@Bean(name = "hazelcastInstance", destroyMethod = "shutdown")
	@ConditionalOnMissingBean(HazelcastInstance.class)
	@ConditionalOnProperty(prefix = "application.cache.hazelcast", name = "mode", havingValue = "server", matchIfMissing = true)
	public HazelcastInstance hazelcastServerInstance(final HazelcastProperties hazelcastProperties)
	{
		System.setProperty("hazelcast.phone.home.enabled", "false");

		final HazelcastProperties.Server properties = hazelcastProperties.getServer();
		final Config config = new Config();

		config.setClusterName(hazelcastProperties.getClusterName()); // Configuring network wide cluster name
		config.setInstanceName(hazelcastProperties.getInstanceName()); // Configuring cluster wide instance name
		config.setProperty(ClusterProperty.LOGGING_TYPE.getName(), "slf4j"); // Configuring Hazelcast to use SLF4J logging

		// Configuration network properties
		final NetworkConfig networkConfig = config.getNetworkConfig();
		final List<String> secondaryAddresses = properties.getSecondaryAddresses();

		networkConfig.setPort(properties.getPort()) // Configuring server port
					 .setPublicAddress(properties.getPrimaryAddress())
					 .setPortAutoIncrement(properties.getPortAutoIncrement().getEnabled()) // Configuring port auto-increment
					 .getInterfaces()
					 .setEnabled(!secondaryAddresses.isEmpty()) // Enabling server interfaces
					 .setInterfaces(secondaryAddresses); // Configuring secondary addresses to bind to.

		final JoinConfig join = networkConfig.getJoin();

		// Configuring multicast properties
		final HazelcastProperties.Server.Multicast multicast = properties.getMulticast();

		// Disabling auto-detection of join configuration
		join.getAutoDetectionConfig().setEnabled(false);

		join.getMulticastConfig()
			.setEnabled(multicast.getEnabled()) // Enabling / disabling clustering mode
			.setMulticastGroup(multicast.getGroupName())
			.setMulticastPort(multicast.getPort())
			.setTrustedInterfaces(multicast.getTrustedInterfaces())
			.setMulticastTimeoutSeconds((int) multicast.getTimeout().toSeconds())
			.setMulticastTimeToLive(multicast.getTimeToLive());

		// Configuring clustering properties
		final HazelcastProperties.Server.Cluster cluster = properties.getCluster();

		join.getTcpIpConfig()
			.setEnabled(cluster.getEnabled()) // Enabling support for well-known members, if specified
			.setMembers(createMembers(properties)); // Setting well-known members of the cluster

		final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

		logger.info("Hazelcast server instance created : {}", hazelcastInstance.getName());

		return hazelcastInstance;
	}

	/**
	 * Creates a member list from grouped configuration format.
	 *
	 * @param server user configured Hazelcast server properties.
	 * @return list of cluster members.
	 */
	public static List<String> createMembers(final HazelcastProperties.Server server)
	{
		final String primaryMember = server.getPrimaryAddress() + ":" + server.getPort();

		return server.getCluster().getMembers().stream().map(HazelcastConfiguration::createMembers)
					 .flatMap(Collection::stream).filter(member -> !member.equals(primaryMember))
					 .toList();
	}

	/**
	 * Creates a member list from grouped configuration format.
	 *
	 * @param member grouped member addresses.
	 * @return list of cluster members.
	 */
	public static List<String> createMembers(final String member)
	{
		if (!member.contains("["))
		{
			// Handles simple member addresses like 127.0.0.1:5701
			return Collections.singletonList(member);
		}

		final String address = member.substring(0, member.indexOf(":"));
		final String[] portGroups = member.substring(member.indexOf("[") + 1, member.indexOf("]")).split(";");

		return Arrays.stream(portGroups).map(portGroup -> createMembers(address, portGroup))
					 .flatMap(Collection::stream).toList();
	}

	/**
	 * Creates a member list from grouped configuration format.
	 *
	 * @param address   address of the member.
	 * @param portGroup grouped ports.
	 * @return list of cluster members.
	 */
	public static List<String> createMembers(final String address, final String portGroup)
	{
		final String[] ports = portGroup.split("-");

		if (ports.length == 1)
		{
			// Handles explicit port definitions e.g. 5701
			return createMembers(address, ports);
		}

		// Handles port ranges e.g. 5701-5702
		final String[] memberPorts = IntStream.rangeClosed(Integer.parseInt(ports[0]), Integer.parseInt(ports[1]))
											  .boxed().map(String::valueOf).toArray(String[]::new);

		return createMembers(address, memberPorts);
	}

	/**
	 * Creates a member list from grouped configuration format.
	 *
	 * @param address address of the member.
	 * @param ports   ports to be combined with {address} for creating member address.
	 * @return list of cluster members.
	 */
	public static List<String> createMembers(final String address, final String[] ports)
	{
		return Arrays.stream(ports).map(port -> address + ":" + port).toList();
	}

	/**
	 * Creates an instance of {@link CacheManager} for caching.
	 *
	 * @param hazelcastInstance pre-configured instance of {@link HazelcastInstance}.
	 * @return Customized instance of {@link CacheManager}.
	 */
	@Bean(name = "cacheManager")
	@ConditionalOnMissingBean(CacheManager.class)
	public CacheManager cacheManager(final @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance)
	{
		return new HazelcastCacheManager(hazelcastInstance);
	}
}
