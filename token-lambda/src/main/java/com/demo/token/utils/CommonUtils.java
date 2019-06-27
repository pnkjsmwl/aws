package com.demo.token.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.demo.token.dao.AccountInfo;
import com.demo.token.dao.Response;
import com.demo.token.dao.UserInfo;

@Component
public class CommonUtils {

	public Response generateResponse(UserInfo user) {

		Response response = new Response();
		response.setMessage("Login Successful");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if(user!=null && user.getAccount()!=null) {
			for(AccountInfo account : user.getAccount()) {
				Map<String, String> map = new HashMap<>();
				map.put("accountId", account.getAccountId());
				map.put("accountNumber", getLastNDigit(account.getAccountNumber(), 4));
				list.add(map);
			}
		}
		response.setAccount(list);
		return response;
	}


	public UserInfo generateAccountId(UserInfo user) {
		if(user!=null && user.getAccount()!=null) {
			System.out.println("AccountId -> AccountNumber");
			for(AccountInfo account : user.getAccount()) {
				account.setAccountId(UUID.randomUUID().toString());
				System.out.println(account.getAccountId()+" -> "+account.getAccountNumber());
			}
		}
		return user;
	}

	private String getLastNDigit(String number, int digits) {
		StringBuilder s = new StringBuilder(number); 
		IntStream.rangeClosed(0, number.length()-digits-1).forEach(n -> s.setCharAt(n, '*'));
		return s.toString();
	}
}
