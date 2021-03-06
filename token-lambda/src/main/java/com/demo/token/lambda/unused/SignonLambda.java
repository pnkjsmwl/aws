package com.demo.token.lambda.unused;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.Credentials;

public class SignonLambda implements RequestHandler<Credentials, ResponseEntity<String>> {

	public ResponseEntity<String> handleRequest(Credentials cred, Context context) {
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
		LambdaLogger logger = context.getLogger();

		/* arn format -> arn:aws:<resource>:<region>:<unique id>:function:<name of lambda invoked>  
		 * eg. arn:aws:lambda:us-east-2:161770494564:function:signon
		 * eg. arn:aws:dynamodb:us-east-2:161770494564:table/user_info
		 *  */
		String invokedFunctionArn = context.getInvokedFunctionArn();
		ResponseEntity<String> response = null;
		String invokedLambda = invokedFunctionArn.split(":")[6];
		logger.log("invokedFunctionArn : "+invokedFunctionArn +", invokedLambda : "+invokedLambda);

		AuthenticatorServiceUnused authenticatorService = appContext.getBean(AuthenticatorServiceUnused.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		cred.setArn(invokedFunctionArn);
		if(authenticatorService!=null) {

			response = authenticatorService.signon(cred);
			if(response!=null) 
				logger.log("Body : "+response.getBody());
		}
		((AnnotationConfigApplicationContext) appContext).close();
		return response;
	}
}
