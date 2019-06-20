package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.TokenAuthRequest;

public class ValidateLambda implements RequestHandler<TokenAuthRequest, AuthPolicy> {

	@Override
	public AuthPolicy handleRequest(TokenAuthRequest input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();

		/* arn format -> arn:aws:<resource>:<region>:<unique id>:function:<name of lambda invoked>  
		 * eg. arn:aws:lambda:us-east-2:161770494564:function:signon
		 * eg. arn:aws:dynamodb:us-east-2:161770494564:table/user_info
		 *  */
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);

		String principalId = "xxxx";

		String methodArn = input.getMethodArn();
		String[] arnPartials = methodArn.split(":");

		String region = arnPartials[3];
		String awsAccountId = arnPartials[4];
		String[] apiGatewayArnPartials = arnPartials[5].split("/");

		String restApiId = apiGatewayArnPartials[0];
		String stage = apiGatewayArnPartials[1];
		String invokedFunctionArn = context.getInvokedFunctionArn();
		String invokedLambda = invokedFunctionArn.split(":")[6];

		logger.log("invokedFunctionArn : "+invokedFunctionArn +", invokedLambda : "+invokedLambda);
		logger.log("Authorization token : "+input.getAuthorizationToken());

		//cred.setArn(invokedFunctionArn);
		try {
			if(authenticatorService!=null) {
				String jwtToken = input.getAuthorizationToken();
				if(authenticatorService.validate2(jwtToken, invokedFunctionArn)!=null) {
					closeAppContext(appContext);
					return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
		}
		closeAppContext(appContext);
		return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
	}

	private void closeAppContext(ApplicationContext appContext) {
		((AnnotationConfigApplicationContext) appContext).close();

	}



}
