package com.demo.account.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import com.demo.token.filter.AuthorizeFilter;

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

	@Value("${jedis.timeout.interval}")	
	private long timeout_interval;

	@Value("${tokenExpiryInterval}")
	private long tokenExpiryInterval;

	@Value("${policy.account.summary}")
	private List<String> summary_Policy;

	@Value("${policy.account.current_balance}")
	private List<String> current_balance_Policy;

	@Value("${policy.account.due}")
	private List<String> due_Policy;

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

	@Bean
	public FilterRegistrationBean<AuthorizeFilter> registerTokenValidationFilter(AuthorizeFilter authorizeFilter)
	{
		FilterRegistrationBean<AuthorizeFilter> registrationBean = new FilterRegistrationBean<>();
		authorizeFilter.setTokenExpiryInterval(tokenExpiryInterval);
		authorizeFilter.setMap(getPolicy());
		registrationBean.setFilter(authorizeFilter);
		registrationBean.setOrder(1);
		return registrationBean;
	}


	public Map<String,List<String>> getPolicy(){
		Map<String,List<String>> policyMap = new HashMap<>();
		policyMap.put("account_summary", summary_Policy);
		policyMap.put("account_current_balance", current_balance_Policy);
		policyMap.put("account_due", due_Policy);
		return policyMap;
	}
}
