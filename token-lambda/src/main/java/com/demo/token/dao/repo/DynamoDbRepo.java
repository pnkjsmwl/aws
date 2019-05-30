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
		dynamoDBMapper.save(user);
		return dynamoDBMapper.load(UserInfo.class,user.getId());
	}



}
