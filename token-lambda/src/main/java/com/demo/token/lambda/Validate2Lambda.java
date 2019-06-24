package com.demo.token.lambda;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.TokenAuthRequest;

public class Validate2Lambda implements RequestHandler<TokenAuthRequest, Map<String, Object>> {

	private String newJWTToken, region;
	@Override
	public Map<String, Object> handleRequest(TokenAuthRequest input, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();

		/* arn format -> arn:aws:<resource>:<region>:<unique id>:function:<name of lambda invoked>  
		 * eg. arn:aws:lambda:us-east-2:161770494564:function:signon
		 * eg. arn:aws:dynamodb:us-east-2:161770494564:table/user_info
		 *  */
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);

		String principalId = "xxxx";

		System.out.println("AccountId : "+input.getHeaders().get("AccountId"));

		String methodArn = input.getMethodArn();

		String invokedFunctionArn = context.getInvokedFunctionArn();
		String invokedLambda = invokedFunctionArn.split(":")[6];
		region = invokedFunctionArn.split(":")[3];

		logger.log("invokedFunctionArn : "+invokedFunctionArn +", invokedLambda : "+invokedLambda);
		logger.log("Authorization token : "+input.getAuthorizationToken());

		//cred.setArn(invokedFunctionArn);
		try {
			if(authenticatorService!=null) {
				String jwtToken = input.getAuthorizationToken();
				newJWTToken  = authenticatorService.validate2(jwtToken, invokedFunctionArn);

				System.out.println("Old token : "+jwtToken);
				System.out.println("New Token : "+newJWTToken);

				closeAppContext(appContext);
				return generatePolicy(principalId, "Allow", methodArn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return generatePolicy(principalId, "Deny", methodArn);
		}
		closeAppContext(appContext);
		return generatePolicy(principalId, "Deny", methodArn);
	}

	private void closeAppContext(ApplicationContext appContext) {
		((AnnotationConfigApplicationContext) appContext).close();

	}

	private Map<String, Object> generatePolicy(String principalId, String effect, String resource) {
		Map<String, Object> authResponse = new HashMap<>();
		authResponse.put("principalId", principalId);

		Map<String, Object> policyDocument = new HashMap<>();
		policyDocument.put("Version", "2012-10-17");
		Map<String, String> statementOne = new HashMap<>();
		statementOne.put("Action", "execute-api:Invoke");
		statementOne.put("Effect", effect);
		statementOne.put("Resource", resource);
		policyDocument.put("Statement", new Object[] {statementOne});
		authResponse.put("policyDocument", policyDocument);

		if ("Allow".equals(effect)) {
			Map<String, Object> context = new HashMap<>();
			context.put("token", newJWTToken);
			context.put("region", region);
			authResponse.put("context", context);
		}
		return authResponse;
	}



}
