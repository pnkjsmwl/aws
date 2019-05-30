package com.demo.token.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

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

	@Value("${spring.redis.host}")
	private String redis_host;

	@Value("${spring.redis.port}")
	private int redis_port;

	@Bean
	public Jedis jedis() {
		return new Jedis(redis_host, redis_port);
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

	@Bean
	/* Similar to a JDBCTemplate */
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
	}

	public DynamoDB dynamoDB() {
		EndpointConfiguration endpointConfiguration = new EndpointConfiguration(serviceEndpoint,region);
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(endpointConfiguration)
				.build());
		return dynamoDB;
	}


}
