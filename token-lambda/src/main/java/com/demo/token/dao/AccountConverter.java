package com.demo.token.dao;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class AccountConverter implements DynamoDBTypeConverter<String, List<AccountInfo>>{

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public String convert(List<AccountInfo> object) {
		System.out.println("object to convert : "+object);
		String string = gson.toJson(object).toString();
		System.out.println("Converted to string : "+string);
		return string;
	}

	@Override
	public List<AccountInfo> unconvert(String object) {
		List<AccountInfo> list = gson.fromJson(object, new TypeToken<List<AccountInfo>>(){}.getType());
		System.out.println("Converted to list : "+list);
		return list;
	}

}
