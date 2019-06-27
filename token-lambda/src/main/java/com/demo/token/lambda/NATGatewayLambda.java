package com.demo.token.lambda;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.dao.Credentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NATGatewayLambda implements RequestHandler<Credentials, String> {

	@SuppressWarnings("rawtypes")
	@Override
	public String handleRequest(Credentials cred, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Input : "+cred.getUserName());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		String url = "https://api.github.com/users/"+cred.getUserName();
		ResponseEntity<Map> resp = new RestTemplate().getForEntity(url, Map.class);
		System.out.println("Git Response : "+gson.toJson(resp.getBody()));
		return gson.toJson(resp.getBody());
	}

}
