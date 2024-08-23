# Hazelcast Spring Boot Starters

Spring Boot starters for configuring Hazelcast server and client instances.

## System Requirements

1. JDK 17
2. Spring Boot 3.x

## Usage

Add the following dependency to your `pom.xml` file:

```xml
<dependency>
	<groupId>dev.all-things.boot</groupId>
	<artifactId>hazelcast-spring-boot-starter</artifactId>
	<version>1.1.0-SNAPSHOT</version>
</dependency>
```

## Configuration Properties

### Common Properties

| Property                                    | Description                                                           |
|---------------------------------------------|-----------------------------------------------------------------------|
| `application.cache.hazelcast.mode`          | Whether to configure Hazelcast instance in 'client' or 'server' mode. |
| `application.cache.hazelcast.cluster-name`  | Name of the Hazelcast cluster.                                        |
| `application.cache.hazelcast.instance-name` | Name of the Hazelcast instance.                                       |

### Client Properties

Set `application.cache.hazelcast.mode = client` to use following properties.

| Property                                                   | Description                                                   |
|------------------------------------------------------------|---------------------------------------------------------------|
| `application.cache.hazelcast.client.server-addresses`      | Comma-separated list of server addresses.                     |
| `application.cache.hazelcast.client.connection-timeout`    | Timeout value for nodes to accept client connection requests. | 
| `application.cache.hazelcast.client.smart-routing.enabled` | Whether to enable smart-routing.                              | 

### Server Properties

Set `application.cache.hazelcast.mode = server` to use following properties.

| Property                                                          | Description                                                  |
|-------------------------------------------------------------------|--------------------------------------------------------------|
| `application.cache.hazelcast.server.port`                         | Hazelcast server port.                                       |
| `application.cache.hazelcast.server.primary-address`              | Hazelcast primary server address.                            |
| `application.cache.hazelcast.server.secondary-addresses`          | Alternate network addresses for server to bind to.           |
| `application.cache.hazelcast.server.port-auto-increment.enabled`  | Whether to enable port auto-increment.                       |
| `application.cache.hazelcast.server.cluster.enabled`              | Whether to enable clustering mode.                           |
| `application.cache.hazelcast.server.cluster.members`              | Comma-separated list of well-known cluster members.          |
| `application.cache.hazelcast.server.multicast.enabled`            | Whether to enable multicast clustering mode.                 |
| `application.cache.hazelcast.server.multicast.group-name`         | Name of the multicast group.                                 |
| `application.cache.hazelcast.server.multicast.port`               | Multicast port.                                              |
| `application.cache.hazelcast.server.multicast.trusted-interfaces` | Comma-separated list of trusted network interfaces.          |
| `application.cache.hazelcast.server.multicast.time-to-live`       | Time that a node should wait for a valid multicast response. |
| `application.cache.hazelcast.server.multicast.timeout`            | Time to live for multicast packets.                          |

Considering the variety of practical scenarios, each comma-separated value in
`application.cache.hazelcast.server.cluster.members` can be specified in different format.

| Format     | Example                                                               |
|------------|-----------------------------------------------------------------------|
| Standard   | `10.10.20.20:5701,10.10.20.20:5702,10.10.20.20:5703`                  |
| Port-range | `10.10.20.20:[5701-5703]` or `10.10.20.20:[5701-5702;5703;5709-5712]` |
| Combined   | `10.10.10.20:5701,10.10.20.20:[5701-5702;5703;5709-5712]`             |

### Standalone Server Configuration

Minimal standalone server configuration requires following properties in your `application.properties` file -

```properties
# Hazelcast properties
application.cache.hazelcast.mode = server
application.cache.hazelcast.cluster-name = dev
application.cache.hazelcast.instance-name = dev-node-1
application.cache.hazelcast.server.primary-address = 127.0.0.1
application.cache.hazelcast.server.port = 5701
```

### Clustered Server Configuration

Minimal clustered server configuration requires following properties in your `application.properties` file -

```properties
# Hazelcast properties
application.cache.hazelcast.mode = server
application.cache.hazelcast.cluster-name = dev
application.cache.hazelcast.instance-name = dev-node-1
application.cache.hazelcast.server.primary-address = 127.0.0.1
application.cache.hazelcast.server.port = 5701
application.cache.hazelcast.server.cluster.enabled = true
application.cache.hazelcast.server.cluster.members = 127.0.0.1:5702
```

### Client Configuration

Minimal client configuration requires following properties in your `application.properties` file -

```properties
# Hazelcast properties
application.cache.hazelcast.mode = client
application.cache.hazelcast.cluster-name = dev
application.cache.hazelcast.instance-name = dev-node-1
application.cache.hazelcast.client.server-addresses = 127.0.0.1:5701
```

## Custom Cache Configuration

You can configure Hazelcast caches by implementing `HazelcastMapConfigurer` interface and registering it as a bean.

```java
@Bean
public HazelcastMapConfigurer hazelcastMapConfigurer()
{
	return config ->
	{
		// Configures cache for name 'cache-1' with 1-hour duration and 1000 entries
		createMapConfig(config, "cache-1", Duration.ofHours(1), 1000);

		// Configures cache for name 'cache-2' with 1-hour duration which will be cleared if 60% of heap is used
		createMapConfig(config, "cache-2", Duration.ofHours(1), MaxSizePolicy.USED_HEAP_PERCENTAGE, 60);
	};
}
```