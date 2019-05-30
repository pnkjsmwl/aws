package com.demo.token.lambda;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.demo.token.authenticator.service.AuthenticatorService;
import com.demo.token.config.ApplConfig;
import com.demo.token.dao.Caller;

public class ValidateToken implements RequestHandler<Caller, ResponseEntity<?>> {

	private static final ApplicationContext appContext = new AnnotationConfigApplicationContext(ApplConfig.class);
	private static final String VALIDATE = "validate";

	@Override
	public ResponseEntity<?> handleRequest(Caller caller, Context context) {
		LambdaLogger logger = context.getLogger();


		/* arn format -> arn:aws:<resource>:<region>:<unique id>:function:<name of lambda invoked>  
		 * eg. arn:aws:lambda:us-east-2:161770494564:function:signon
		 * eg. arn:aws:dynamodb:us-east-2:161770494564:table/user_info
		 *  */
		String invokedFunctionArn = context.getInvokedFunctionArn();
		ResponseEntity<?> response = null;
		String invokedLambda = invokedFunctionArn.split(":")[6];
		logger.log("invokedFunctionArn : "+invokedFunctionArn +", invokedLambda : "+invokedLambda);
		logger.log("Headers : "+caller.getHeaders());
		AuthenticatorService authenticatorService = appContext.getBean(AuthenticatorService.class);
		System.out.println("authenticatorService from appContext : "+authenticatorService);
		//cred.setArn(invokedFunctionArn);
		if(authenticatorService!=null) {

			if(VALIDATE.equals(invokedLambda)) {
				String jwtToken = caller.getHeaders().get("Authorization");
				response = authenticatorService.validate(jwtToken , caller);
				if(response!=null) 
					logger.log("Body : "+response.getBody());
			}
		}
		return response;
	}

}
