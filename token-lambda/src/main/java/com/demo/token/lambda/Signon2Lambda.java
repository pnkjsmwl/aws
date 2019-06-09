package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.APIGatewayProxyResponse;
import com.demo.token.dao.Credentials;

public class Signon2Lambda implements RequestHandler<Credentials, APIGatewayProxyResponse> {

	@Override
	public APIGatewayProxyResponse handleRequest(Credentials cred, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();
		String invokedFunctionArn = context.getInvokedFunctionArn();
		APIGatewayProxyResponse response = new APIGatewayProxyResponse();
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		cred.setArn(invokedFunctionArn);
		if(authenticatorService!=null) {

			response = authenticatorService.signon2(cred);
			if(response!=null) 
				logger.log("Response : "+response);
		}
		((AnnotationConfigApplicationContext) appContext).close();
		return response;
	}

}
