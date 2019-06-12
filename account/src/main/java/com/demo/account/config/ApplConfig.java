package com.demo.account.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplConfig {
	//private Log log = LogFactory.getLog(ApplConfig.class);

	@Value("${REDIS_HOST:localhost}")
	private String redis_host;

	@Value("${REDIS_PORT:6380}")
	private int redis_port;

	@Value("${REDIS_HOST_SEC:localhost}")
	private String redis_host_sec;

	@Value("${REDIS_PORT:6379}")
	private int redis_port_sec;

	@Value("${REDIS_TIMEOUT:3000}")
	private int redis_timeout;

	@Value("${policy.account.summary}")
	private List<String> summary_Policy;

	@Value("${jedis.timeout.interval}")	
	private long timeout_interval;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean(name="redisTemplate")
	@Primary
	public <T> RedisTemplate<String, T> redisTemplate() {
		RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactoryPrimary());
		redisTemplate.setEnableTransactionSupport(true);
		return redisTemplate;
	}

	@Bean(name="redisTemplateSec")
	public <T> RedisTemplate<String, T> redisTemplateSecondary() {
		RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactoryPrimarySecondary());
		redisTemplate.setEnableTransactionSupport(true); 
		return redisTemplate; 
	}

	public JedisConnectionFactory jedisConnectionFactoryPrimary() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redis_host);
		redisStandaloneConfiguration.setPort(redis_port);

		JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
		jedisClientConfiguration.connectTimeout(Duration.ofSeconds(timeout_interval));// connection timeout in seconds

		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
		return jedisConFactory;
	}

	public JedisConnectionFactory jedisConnectionFactoryPrimarySecondary() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redis_host_sec);
		redisStandaloneConfiguration.setPort(redis_port_sec);

		JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
		jedisClientConfiguration.connectTimeout(Duration.ofSeconds(timeout_interval));// connection timeout in seconds

		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
		return jedisConFactory;
	}

	/*
	 * @Bean public Jedis jedis() {
	 * log.info("Redis HOST:PORT : "+redis_host+":"+redis_port); Jedis jedis = new
	 * Jedis(redis_host, redis_port, redis_timeout);
	 * 
	 * log.info(jedis.set("abc", "This is dummy.")); log.info(jedis.get("abc"));
	 * 
	 * log.info("summary_Policy : "+summary_Policy); return jedis; }
	 */
}
