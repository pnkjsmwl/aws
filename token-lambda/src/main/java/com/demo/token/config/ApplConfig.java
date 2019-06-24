package com.demo.token.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import redis.clients.jedis.Jedis;

@Configuration
@ComponentScan(basePackages= {"com.demo"})
@PropertySource("classpath:application.properties")
public class ApplConfig {

	@Value("${dynamodb.service.endpoint}")
	private String serviceEndpoint;

	@Value("${dynamodb.region}")
	private String region;

	@Value("${aws.access.key}")
	private String awsAccessKey;

	@Value("${aws.secret.key}")
	private String awsSecretKey;

	@Value("${redis.host}")
	private String redis_host;

	@Value("${redis.host.sec}")
	private String redis_host_sec;

	@Value("${redis.port}")
	private int redis_port;

	@Value("${redis.timeout}")
	private int redis_timeout;

	@Bean
	public static PropertySourcesPlaceholderConfigurer  propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =  new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
		return propertySourcesPlaceholderConfigurer;
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
		jedisClientConfiguration.connectTimeout(Duration.ofMillis(redis_timeout));// connection timeout in milliseconds

		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
		return jedisConFactory;
	}

	public JedisConnectionFactory jedisConnectionFactoryPrimarySecondary() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redis_host_sec);
		redisStandaloneConfiguration.setPort(redis_port);

		JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
		jedisClientConfiguration.connectTimeout(Duration.ofMillis(redis_timeout));// connection timeout in milliseconds

		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
		return jedisConFactory;
	}

	@Bean(name="jedis")
	@Primary
	public Jedis jedis() {
		System.out.println("Redis Timeout : "+redis_timeout);
		return new Jedis(redis_host, redis_port, redis_timeout);
	}

	@Bean(name="jedis_sec")
	public Jedis jedisSec() {
		System.out.println("Redis Timeout : "+redis_timeout);
		return new Jedis(redis_host_sec, redis_port, redis_timeout);
	}

	@Bean(name = "policy")
	public HashMap<String, List<String>> getPolicy() {
		HashMap<String,List<String>> policy = new HashMap<>();
		policy.put("GET/account/summary",Arrays.asList("ACCOUNTS_ROLE","ADMIN"));
		policy.put("GET/profile/email",Arrays.asList("PROFILE_ROLE","ADMIN"));
		policy.put("GET/profile/mobile",Arrays.asList("PROFILE_ROLE","ADMIN"));
		policy.put("GET/profile/address",Arrays.asList("PROFILE_ROLE","ADMIN"));
		return policy;
	}

	/*@Bean
	 Similar to a JDBCTemplate 
	public DynamoDBMapper dynamoDBMapper() {
		return new DynamoDBMapper(amazonDynamoDB());
	}

	public AmazonDynamoDB amazonDynamoDB() {
		System.out.println("DynamoDB Configuration : \n"+serviceEndpoint+"\n"+region+"\n"+awsAccessKey+"\n"+awsSecretKey+"\n");
		AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region);
		AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey));

		AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(endpointConfiguration)
				.withCredentials(credentialsProvider)
				.build();
		return amazonDynamoDB;
	}*/

	@Bean
	public DynamoDBMapper dynamoDBMapper() {

		EndpointConfiguration endpointConfiguration = new EndpointConfiguration(serviceEndpoint,region);
		System.out.println("creating dynamodb mapper...");
		AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(endpointConfiguration)
				.build();
		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDB);
		System.out.println("dynamodb mapper created.");
		return dynamoDBMapper;
	}

	@Bean
	public Gson gson() {
		return new GsonBuilder().setPrettyPrinting().create();
	}

}
