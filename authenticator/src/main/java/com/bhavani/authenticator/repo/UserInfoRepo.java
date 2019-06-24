package com.bhavani.authenticator.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bhavani.authenticator.dao.UserInfo;

@Repository
public interface UserInfoRepo extends MongoRepository<UserInfo, String> {

	public UserInfo findByUserName(String userName);
}
