package dev.all_things.boot.autoconfigure.cache.hazelcast;

import java.time.Duration;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Hazelcast.
 */
@ConfigurationProperties(prefix = "application.cache.hazelcast")
public class HazelcastProperties
{
	private static final Logger logger = LogManager.getLogger(HazelcastProperties.class);

	/**
	 * Whether to configure Hazelcast instance in 'client' or 'server' mode.
	 * <p>
	 * By default, Hazelcast is configured in 'server' mode.
	 */
	private String mode = "server";

	/**
	 * Name of the Hazelcast cluster.
	 */
	private String clusterName;

	/**
	 * Name of this Hazelcast client / server instance.
	 */
	private String instanceName;

	/**
	 * Hazelcast client configuration.
	 */
	private final Client client = new Client();

	/**
	 * Hazelcast server configuration.
	 */
	private final Server server = new Server();

	public String getMode()
	{
		return this.mode;
	}

	public void setMode(final String mode)
	{
		this.mode = mode;
	}

	public String getClusterName()
	{
		return this.clusterName;
	}

	public void setClusterName(final String clusterName)
	{
		this.clusterName = clusterName;
	}

	public String getInstanceName()
	{
		return this.instanceName;
	}

	public void setInstanceName(final String instanceName)
	{
		this.instanceName = instanceName;
	}

	public Client getClient()
	{
		return this.client;
	}

	public Server getServer()
	{
		return this.server;
	}

	/**
	 * Configuration properties for configuring Hazelcast client instance.
	 */
	public static class Client
	{
		/**
		 * Comma-separated list of server addresses to which the Hazelcast client will connect
		 * e.g. 10.10.20.20:5701,10.10.20.30:5701
		 */
		private final List<String> serverAddresses = new ArrayList<>();

		/**
		 * Timeout value for nodes to accept client connection requests.
		 * Default value is 5 seconds.
		 */
		private Duration connectionTimeout = Duration.ofSeconds(5);

		/**
		 * Hazelcast smart-routing configuration properties.
		 */
		private SmartRouting smartRouting = new SmartRouting();

		public List<String> getServerAddresses()
		{
			return this.serverAddresses;
		}

		public Duration getConnectionTimeout()
		{
			return this.connectionTimeout;
		}

		public void setConnectionTimeout(final Duration connectionTimeout)
		{
			this.connectionTimeout = connectionTimeout;
		}

		public SmartRouting getSmartRouting()
		{
			return this.smartRouting;
		}

		public void setSmartRouting(final SmartRouting smartRouting)
		{
			this.smartRouting = smartRouting;
		}

		/**
		 * Configuration properties for Hazelcast smart-routing.
		 */
		public static class SmartRouting
		{
			/**
			 * Whether to enable smart-routing. Default value is true.
			 */
			private Boolean enabled = true;

			public Boolean getEnabled()
			{
				return this.enabled;
			}

			public void setEnabled(final Boolean enabled)
			{
				this.enabled = enabled;
			}
		}
	}

	/**
	 * Configuration properties for configuring Hazelcast server instance.
	 */
	public static class Server
	{
		/**
		 * Hazelcast server port. Default value is 5701.
		 */
		private Integer port = 5701;

		/**
		 * Hazelcast primary server address.
		 * This address will be broadcast to other cluster members.
		 */
		private String primaryAddress = "127.0.0.1";

		/**
		 * Comma-separated list of alternate network addresses to which Hazelcast server will bind
		 * in addition to the 'primaryAddress'.
		 */
		private final List<String> secondaryAddresses = new ArrayList<>();

		/**
		 * Hazelcast port auto-increment configuration properties.
		 */
		private final PortAutoIncrement portAutoIncrement = new PortAutoIncrement();

		/**
		 * Hazelcast clustering configuration properties.
		 */
		private final Cluster cluster = new Cluster();

		/**
		 * Hazelcast multicast configuration properties.
		 */
		private final Multicast multicast = new Multicast();

		public Integer getPort()
		{
			return this.port;
		}

		public void setPort(final Integer port)
		{
			this.port = port;
		}

		public String getPrimaryAddress()
		{
			return this.primaryAddress;
		}

		public void setPrimaryAddress(final String primaryAddress)
		{
			this.primaryAddress = primaryAddress;
		}

		public List<String> getSecondaryAddresses()
		{
			return this.secondaryAddresses;
		}

		public PortAutoIncrement getPortAutoIncrement()
		{
			return this.portAutoIncrement;
		}

		public Cluster getCluster()
		{
			return this.cluster;
		}

		public Multicast getMulticast()
		{
			return this.multicast;
		}

		public static class PortAutoIncrement
		{
			/**
			 * Whether to enable port auto-increment. Default value is false.
			 */
			private Boolean enabled = false;

			public Boolean getEnabled()
			{
				return this.enabled;
			}

			public void setEnabled(final Boolean enabled)
			{
				this.enabled = enabled;
			}
		}

		/**
		 * Configuration properties for Hazelcast clustering.
		 */
		public static class Cluster
		{
			/**
			 * Whether to enable clustering mode. Default value is false.
			 */
			private Boolean enabled = false;

			/**
			 * Comma-separated list of well-known cluster members.
			 * See README.md for supported formats.
			 */
			private List<String> members = new ArrayList<>();

			public Boolean getEnabled()
			{
				return this.enabled;
			}

			public void setEnabled(final Boolean enabled)
			{
				this.enabled = enabled;
			}

			public List<String> getMembers()
			{
				return this.members;
			}

			public void setMembers(final List<String> members)
			{
				this.members = members;
			}
		}

		/**
		 * Configuration properties for Hazelcast clustering.
		 */
		public static class Multicast
		{
			/**
			 * Whether to enable multicast clustering mode. Default value is false.
			 */
			private Boolean enabled = false;

			/**
			 * Name of the multicast group.
			 */
			private String groupName = "multicastGroup";

			/**
			 * Multicast port. Default is 5710.
			 */
			private Integer port = 5710;

			/**
			 * Comma-separated list of trusted network interfaces e.g. 10.10.20.*,10.10.30.*.
			 * Wildcard '*' can be used to trust the entire subnet range.
			 */
			private Set<String> trustedInterfaces = new HashSet<>();

			/**
			 * Time that a node should wait for a valid multicast response.
			 * Default is 5 seconds.
			 */
			private Duration timeout = Duration.ofSeconds(5);

			/**
			 * Time to live for multicast packets; a value between 0..255.
			 * Default value is 32.
			 */
			private Integer timeToLive = 32;

			public Boolean getEnabled()
			{
				return this.enabled;
			}

			public void setEnabled(final Boolean enabled)
			{
				this.enabled = enabled;
			}

			public String getGroupName()
			{
				return this.groupName;
			}

			public void setGroupName(final String groupName)
			{
				this.groupName = groupName;
			}

			public Integer getPort()
			{
				return this.port;
			}

			public void setPort(final Integer port)
			{
				this.port = port;
			}

			public Set<String> getTrustedInterfaces()
			{
				return this.trustedInterfaces;
			}

			public void setTrustedInterfaces(final Set<String> trustedInterfaces)
			{
				this.trustedInterfaces = trustedInterfaces;
			}

			public Duration getTimeout()
			{
				return this.timeout;
			}

			public void setTimeout(final Duration timeout)
			{
				this.timeout = timeout;
			}

			public Integer getTimeToLive()
			{
				return this.timeToLive;
			}

			public void setTimeToLive(final Integer timeToLive)
			{
				this.timeToLive = timeToLive;
			}
		}
	}
}
