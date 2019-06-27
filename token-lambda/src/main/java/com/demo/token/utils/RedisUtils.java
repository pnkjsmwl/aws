package com.demo.token.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.demo.token.dao.UserInfo;

@Component
public class RedisUtils {
	private Log log = LogFactory.getLog(RedisUtils.class);

	@Value("${token_value}")
	private String token_value;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	public void addToCache(UserInfo user) throws Exception {
		System.out.println("Redis Key : "+user.getRedisKey());
		setValue(user.getRedisKey(), token_value);
		System.out.println("Value from redis : "+getStringValue(user.getRedisKey()));
	}

	public void addAccountIdToCache(Map<String,String> map, String sessionId) {
		String key = "account:"+sessionId;
		setValue(key, map);
		validateAccountFromRedis(key);
	}

	@SuppressWarnings("unchecked")
	public String getAccount(String key, String accountId) {
		String redisKey = "account:"+key.split(":")[1];
		System.out.println("Account Key : "+redisKey);
		Map<String,String> map = (Map<String, String>) getMapValue(redisKey);
		System.out.println("Map from Redis : "+map);

		String accountNumber = map.get(accountId);
		System.out.println("Account Number : "+accountNumber);

		return accountNumber;
	}

	@SuppressWarnings("unchecked")
	public Map<String,String> getAccountMap(String key, String accountId) {
		String redisKey = "account:"+key.split(":")[1];
		System.out.println("Account Key : "+redisKey);
		Map<String,String> map = (Map<String, String>) getMapValue(redisKey);
		System.out.println("Map from Redis : "+map);
		return map;
	}

	public boolean removeFromCache(String redisKey, boolean foundInDiffRegion) {
		/*RedisTemplate<String,String> redisTemplate;
		if(!foundInDiffRegion) {
			redisTemplate = stringRedisTemplate;
		}else {
			redisTemplate = stringRedisTemplateSec;
		}*/

		String checkUser = stringRedisTemplate.opsForValue().get( redisKey );

		System.out.println("User check : "+checkUser);
		if("valid".equals(checkUser)) {
			Boolean delete = stringRedisTemplate.delete(redisKey);
			System.out.println("Key deleted from Redis : "+delete);
			return true;
		}else {
			System.out.println("User not found in cache.");
			return false;
		}
	}

	public String tokenValueFromRedis(String key, String region_current) {
		System.out.println("Key to Redis : "+key);
		String tokenValueFromRedis = null;

		tokenValueFromRedis = stringRedisTemplate.opsForValue().get(key);
		System.out.println("Value from Primary redis against token : "+tokenValueFromRedis);

		return tokenValueFromRedis;
	}

	public <T> void setValue( final String key, final T value ) {
		if(value instanceof Map) {
			mapRedisTemplate.opsForValue().set( key, (Map<?, ?>) value);
		}
		else if(value instanceof String) {
			stringRedisTemplate.opsForValue().set( key, (String) value);
		}
	}

	public Object getStringValue( final String key ) {
		log.info("Getting from primary cache.");
		String string = stringRedisTemplate.opsForValue().get( key );
		/*if(string==null) {
			log.info("Getting from secondary cache.");
			return stringRedisTemplateSec.opsForValue().get( key );
		}*/
		return string;
	}

	public Object getMapValue( final String key ) {
		log.info("Getting from primary cache.");
		Map<?, ?> map = mapRedisTemplate.opsForValue().get( key );
		/*if(map.isEmpty()) {
			log.info("Getting from secondary cache.");
			return mapRedisTemplateSec.opsForValue().get( key );
		}*/
		return map;
	}

	@SuppressWarnings("unchecked")
	public void validateAccountFromRedis(String key) {
		Map<String,String> mapValue = (Map<String, String>) getMapValue(key);
		System.out.println("Data from Redis : "+mapValue);

	}

}
