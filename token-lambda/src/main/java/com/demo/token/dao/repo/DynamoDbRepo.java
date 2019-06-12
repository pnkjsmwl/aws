package com.demo.token.dao.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.demo.token.dao.UserInfo;

@Repository
public class DynamoDbRepo {


	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	public UserInfo save(UserInfo user) {

		/*Item item = new Item()
				.withString("userName", user.getUserName())
				.withString("password", user.getPassword())
				.withString("accountNumber", user.getAccountNumber())
				.withString("role", user.getRole());

		PutItemSpec itemSpec = new PutItemSpec().withItem(item);
		PutItemOutcome putItem = dynamoDB.getTable("user_info").putItem(itemSpec);
		PutItemResult putItemResult = putItem.getPutItemResult();
		System.out.println("putItemResult : "+putItemResult);
		return user; */
		System.out.println("Persisting user...");
		
		dynamoDBMapper.save(user);
		
		System.out.println("User persisted !!");
		
		UserInfo userFromDb = dynamoDBMapper.load(UserInfo.class, user.getUserName());
		
		System.out.println("User loaded from DB !!");
		/*DynamoDBQueryExpression<UserInfo> query = new DynamoDBQueryExpression<UserInfo>()
				.withHashKeyValues(user);

		List<UserInfo> userFromDb = dynamoDBMapper.query(UserInfo.class, query);
		System.out.println("User from DB : "+userFromDb.get(0).getUserName());
		return userFromDb.get(0);*/
		
		return userFromDb;
	}

	public UserInfo getUserByUserName(String userName) {
		System.out.println("dynamoDBMapper : "+dynamoDBMapper);
		UserInfo userFromDb = dynamoDBMapper.load(UserInfo.class, userName);
		System.out.println("User loaded from DB !!");
		return userFromDb;
	}
	
	/*public UserInfo findAllUsers(UserInfo user) {

		PutItemSpec itemSpec = new PutItemSpec().withItem(item);
		PutItemOutcome putItem = dynamoDB.getTable("user_info").putItem(itemSpec);
		PutItemResult putItemResult = putItem.getPutItemResult();
		System.out.println("putItemResult : "+putItemResult);
		return user;
	}
	 */

}
