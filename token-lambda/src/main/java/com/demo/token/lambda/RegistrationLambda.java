package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.UserInfo;

public class RegistrationLambda implements RequestHandler<UserInfo, ResponseEntity<?>> {

	
	@Override
	public ResponseEntity<?> handleRequest(UserInfo userInfo, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);

		ResponseEntity<UserInfo> respEntity = authenticatorService.register(userInfo);
		((AnnotationConfigApplicationContext) appContext).close();
		return respEntity;
	}

}
