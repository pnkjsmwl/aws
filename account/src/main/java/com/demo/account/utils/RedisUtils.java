package com.demo.account.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.demo.account.doc.Account;

@Component
public class RedisUtils {
	private Log log = LogFactory.getLog(RedisUtils.class);

	@Value("${jedis.timeout.interval}")	
	private long timeout_interval;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Account> objectRedisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, Account> objectRedisTemplateSec;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplateSec;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, String> stringRedisTemplateSec;



	public <T> void setValue( final String key, final T value ) {
		if(value instanceof Account) {
			objectRedisTemplate.opsForValue().set( key, (Account) value);
			objectRedisTemplate.expire(key, timeout_interval, TimeUnit.SECONDS);
		}
		else if(value instanceof Map) {
			mapRedisTemplate.opsForValue().set( key, (Map<?, ?>) value);
			mapRedisTemplate.expire(key, timeout_interval, TimeUnit.SECONDS);
		}
		else if(value instanceof String) {
			stringRedisTemplate.opsForValue().set( key, (String) value);
			stringRedisTemplate.expire(key, timeout_interval, TimeUnit.SECONDS);
		}
	}

	public Object getAccountValue( final String key ) {
		log.info("Getting from primary cache.");
		Account account = objectRedisTemplate.opsForValue().get( key );
		if(account==null) {
			log.info("Getting from secondary cache.");
			account = objectRedisTemplateSec.opsForValue().get( key );
		}
		return account;
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

	public Object getStringValue( final String key ) {
		log.info("Getting from primary cache.");
		String string = stringRedisTemplate.opsForValue().get( key );
		if(string==null) {
			log.info("Getting from secondary cache.");
			return stringRedisTemplate.opsForValue().get( key );
		}
		return string;
	}

}
