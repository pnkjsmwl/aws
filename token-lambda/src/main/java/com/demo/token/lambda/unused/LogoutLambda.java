package com.demo.token.lambda.unused;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.TokenAuthRequest;

public class LogoutLambda implements RequestHandler<TokenAuthRequest, ResponseEntity<String>> {

	@Override
	public ResponseEntity<String> handleRequest(TokenAuthRequest input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		AuthenticatorServiceUnused authenticatorService = appContext.getBean(AuthenticatorServiceUnused.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		ResponseEntity<String> response = null;
		LambdaLogger logger = context.getLogger();

		if(authenticatorService!=null) {
			String jwtToken = input.getAuthorizationToken();
			Map<String, String> header = input.getHeaders();
			System.out.println(header);
			if(header!=null && header.get("Authorization")!=null) {
				jwtToken = header.get("Authorization");
			}
			System.out.println("Token from request : "+jwtToken);
			response = authenticatorService.logout(jwtToken, context);
			if(response!=null) 
				logger.log("Body : "+response.getBody());
		}
		((AnnotationConfigApplicationContext) appContext).close();
		return response;

	}

}
