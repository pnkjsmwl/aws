package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.Credentials;

public class LogoutLambda implements RequestHandler<Credentials, ResponseEntity<String>> {

	@Override
	public ResponseEntity<String> handleRequest(Credentials input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		ResponseEntity<String> response = null;
		LambdaLogger logger = context.getLogger();

		if(authenticatorService!=null) {

			response = authenticatorService.logout(input);
			if(response!=null) 
				logger.log("Body : "+response.getBody());
		}
		((AnnotationConfigApplicationContext) appContext).close();
		return response;

	}

}
