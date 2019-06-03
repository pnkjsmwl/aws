package com.demo.token.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class DummyResponseLambda implements RequestHandler<String, String> {

	@Override
	public String handleRequest(String fromLambda, Context context) {

		return "Hello "+fromLambda;
	}

}
