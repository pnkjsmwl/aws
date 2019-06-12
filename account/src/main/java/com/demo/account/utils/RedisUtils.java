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
import com.demo.account.filter.AuthorizeFilter;

@Component
public class RedisUtils {
	private Log log = LogFactory.getLog(RedisUtils.class);
	
	@Value("${jedis.timeout.interval}")	
	private long timeout_interval;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Account> accountRedisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplate;

	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;
	
	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, Account> accountRedisTemplateSec;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, Map<?,?>> mapRedisTemplateSec;

	@Autowired
	@Qualifier("redisTemplateSec")
	private RedisTemplate<String, String> stringRedisTemplateSec;
	
	

	public <T> void setValue( final String key, final T value ) {
		if(value instanceof Account) {
			accountRedisTemplate.opsForValue().set( key, (Account) value);
			accountRedisTemplate.expire(key, timeout_interval, TimeUnit.SECONDS);
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
		Account account = accountRedisTemplate.opsForValue().get( key );
		if(account==null) {
			log.info("Getting from secondary cache.");
			account = accountRedisTemplateSec.opsForValue().get( key );
		}
		return account;
	}

	public Object getMapValue( final String key ) {
		return mapRedisTemplate.opsForValue().get( key );
	}

	public Object getStringValue( final String key ) {
		return stringRedisTemplate.opsForValue().get( key );
	}

	/*
	 * public <T extends Serializable> T toRedis(String key, T value) throws
	 * IOException { //jedis.setex(key, timeout_interval, stringify(value)); return
	 * value; }
	 * 
	 * public <T extends Serializable> T fromRedis(String key, TypeReference<T>
	 * valueType){
	 * 
	 * String fromRedis = jedis.get(key); return objectify(fromRedis, valueType); }
	 * 
	 * public static String stringify(Object object) { ObjectMapper jackson = new
	 * ObjectMapper(); jackson.setSerializationInclusion(Include.NON_NULL); try {
	 * return jackson.writeValueAsString(object); } catch (Exception ex) {
	 * log.fatal("Error while creating json."); } return null; }
	 * 
	 * public static <T extends Serializable> T objectify(String content,
	 * TypeReference<T> valueType) { try { ObjectMapper mapper = new ObjectMapper();
	 * return mapper.readValue(content, valueType); } catch (Exception e) {
	 * log.fatal("Error while reading json."); return null; } }
	 */

	/*
	 * 
	 * private static String toString( Serializable o ) throws IOException {
	 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream
	 * oos = new ObjectOutputStream( baos ); oos.writeObject( o ); oos.close();
	 * return Base64.getEncoder().encodeToString(baos.toByteArray()); }
	 * 
	 * private static Object fromString(String s) throws IOException ,
	 * ClassNotFoundException { byte [] data = Base64.getDecoder().decode(s);
	 * ObjectInputStream ois = new ObjectInputStream(new
	 * ByteArrayInputStream(data)); Object o = ois.readObject(); ois.close(); return
	 * o; }
	 */

}
