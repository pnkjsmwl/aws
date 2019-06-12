package com.demo.transaction.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplConfig {
	//private Log log = LogFactory.getLog(ApplConfig.class);

	@Value("${REDIS_HOST:localhost}")
	private String redis_host;

	@Value("${REDIS_PORT:6379}")
	private int redis_port;

	@Value("${REDIS_TIMEOUT:3000}")
	private int redis_timeout;

	@Value("${policy.account.summary}")
	private List<String> summary_Policy;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public <T> RedisTemplate<String, T> redisTemplate() {
		RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setEnableTransactionSupport(true);
		return redisTemplate;
	}
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redis_host, redis_port);
		return new JedisConnectionFactory(redisStandaloneConfiguration);
	}
	/*
	 * @Bean public Jedis jedis() {
	 * log.info("Redis HOST:PORT : "+redis_host+":"+redis_port); Jedis jedis = new
	 * Jedis(redis_host, redis_port, redis_timeout); log.info(jedis.set("abc",
	 * "This is dummy.")); log.info(jedis.get("abc"));
	 * log.info("summary_Policy : "+summary_Policy); return jedis; }
	 */

}
