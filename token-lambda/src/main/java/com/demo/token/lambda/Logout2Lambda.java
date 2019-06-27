package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.APIGatewayProxyRequest;
import com.demo.token.dao.APIGatewayProxyResponse;

public class Logout2Lambda implements RequestHandler<APIGatewayProxyRequest, APIGatewayProxyResponse> {

	@Override
	public APIGatewayProxyResponse handleRequest(APIGatewayProxyRequest input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();
		APIGatewayProxyResponse response = new APIGatewayProxyResponse();
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		if(authenticatorService!=null) {
			String token = input.getHeaders().get("Authorization");
			logger.log("Token from request : "+token);
			response = authenticatorService.logout2(token, context);
			if(response!=null) 
				logger.log("Body : "+response.getBody());
		}
		((AnnotationConfigApplicationContext) appContext).close();
		return response;
	}

}
