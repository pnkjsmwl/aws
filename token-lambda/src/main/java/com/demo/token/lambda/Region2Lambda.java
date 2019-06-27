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
import com.demo.token.dao.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Region2Lambda implements RequestHandler<APIGatewayProxyRequest, APIGatewayProxyResponse> {

	@Override
	public APIGatewayProxyResponse handleRequest(APIGatewayProxyRequest input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();
		Response resp = new Response();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String token = input.getHeaders().get("Authorization")!= null? input.getHeaders().get("Authorization") : input.getHeaders().get("authorization");
		String action = input.getHeaders().get("Action")!= null? input.getHeaders().get("Action") : input.getHeaders().get("action");
		logger.log("Authorization token : "+token);
		logger.log("Action to be performed : "+action);
		String invokedFunctionArn = context.getInvokedFunctionArn();
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		logger.log("invokedFunctionArn : "+invokedFunctionArn);
		try {
			if(authenticatorService!=null) {
				resp = authenticatorService.performActionDiffRegion(invokedFunctionArn, input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeAppContext(appContext);
		return new APIGatewayProxyResponse(200, null, gson.toJson(resp), true);
	}

	private void closeAppContext(ApplicationContext appContext) {
		((AnnotationConfigApplicationContext) appContext).close();
	}
}
