package com.demo.transaction.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.demo.transaction.doc.Transaction;

@Component
public class RedisUtils {

	@Value("${redis.timeout.interval}")	
	private long timeout_interval;

	@Autowired
	private RedisTemplate<String, Transaction> transactionRedisTemplate;

	@Autowired
	private RedisTemplate<String, Map<?,?>> mapRedisTemplate;

	@Autowired
	private RedisTemplate<String, String> stringRedisTemplate;

	public <T> void setValue( final String key, final T value ) {
		if(value instanceof Transaction) {
			transactionRedisTemplate.opsForValue().set( key, (Transaction) value);
			transactionRedisTemplate.expire(key, timeout_interval, TimeUnit.SECONDS);
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

	public Object getTransactionValue( final String key ) {
		return transactionRedisTemplate.opsForValue().get( key );
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
