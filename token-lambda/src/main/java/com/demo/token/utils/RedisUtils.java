package com.demo.token.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.demo.token.dao.AccountInfo;
import com.demo.token.dao.JWTPayload;
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

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplateSec;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, String> stringRedisTemplateSec;

	public void addToCache(UserInfo user) throws Exception {
		System.out.println("Redis Key : "+user.getRedisKey());
		setValue(user.getRedisKey(), token_value);
		System.out.println("Value from redis : "+getStringValue(user.getRedisKey()));
	}

	public void addAccountIdToCache(UserInfo user, String sessionId) {
		Map<String,String> map = new HashMap<>();
		if(user!=null && !user.getAccount().isEmpty()) {
			for(AccountInfo account : user.getAccount()) {
				map.put(account.getAccountId(), account.getAccountNumber());
			}
			setValue("account:"+sessionId, map);
			validateAccountFromRedis("account:"+sessionId);
		}
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

	public boolean removeFromCache(String redisKey, boolean foundInDiffRegion) {
		RedisTemplate<String,String> redisTemplate;
		if(!foundInDiffRegion) {
			redisTemplate = stringRedisTemplate;
		}else {
			redisTemplate = stringRedisTemplateSec;
		}
		String checkUser = redisTemplate.opsForValue().get( redisKey );

		System.out.println("User check : "+checkUser);
		if("valid".equals(checkUser)) {
			Boolean delete = redisTemplate.delete(redisKey);
			System.out.println("Key deleted from Redis : "+delete);
			return true;
		}else {
			System.out.println("User not found in cache.");
			return false;
		}
	}

	public String tokenValueFromRedis(JWTPayload payload, String region_current) {
		System.out.println("Key to Redis : "+payload.getRedisKey());
		String tokenValueFromRedis = null;

		if(region_current.equals(payload.getRegionLatest())) {
			tokenValueFromRedis = stringRedisTemplate.opsForValue().get(payload.getRedisKey());

			System.out.println("Value from Primary redis against token : "+tokenValueFromRedis);
		}
		else {
			tokenValueFromRedis = stringRedisTemplateSec.opsForValue().get(payload.getRedisKey());
			if(tokenValueFromRedis!=null) {
				payload.setFoundInDiffRegion(true);
				payload.setRegionLatest(region_current);
			}
			System.out.println("Value from Secondary redis against token : "+tokenValueFromRedis);
		}
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
		if(string==null) {
			log.info("Getting from secondary cache.");
			return stringRedisTemplateSec.opsForValue().get( key );
		}
		return string;
	}

	public Object getMapValue( final String key ) {
		log.info("Getting from primary cache.");
		Map<?, ?> map = mapRedisTemplate.opsForValue().get( key );
		if(map.isEmpty()) {
			log.info("Getting from secondary cache.");
			return mapRedisTemplateSec.opsForValue().get( key );
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public void validateAccountFromRedis(String key) {
		Map<String,String> mapValue = (Map<String, String>) getMapValue(key);
		System.out.println("Data from Redis : "+mapValue);

	}

}
