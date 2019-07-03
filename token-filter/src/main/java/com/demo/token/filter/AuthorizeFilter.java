package com.demo.token.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JOSEException;

@Component
public class AuthorizeFilter extends OncePerRequestFilter {
	private Log log = LogFactory.getLog(AuthorizeFilter.class);

	@Value("${policy.account.summary:}")
	private List<String> account_summary_policy;

	@Value("${policy.profile.summary:}")
	private List<String> profile_summary_policy;

	@Value("${policy.transaction.summary:}")
	private List<String> transaction_summary_policy;

	@Value("${policy.transaction.recent:}")
	private List<String> transaction_recent_policy;

	@Value("${policy.transaction.last:}")
	private List<String> transaction_last_policy;

	@Autowired
	private JWEValidator jweValidator;

	@Override 
	protected void doFilterInternal(HttpServletRequest request,  HttpServletResponse response, FilterChain filterChain) throws  ServletException, IOException { 
		try { 
			String encryptedJWT =  request.getHeader(HttpHeaders.AUTHORIZATION);
			log.info(String.format("Authorizing the token %s for URI %s URL %s Method %s ", encryptedJWT,request.getRequestURI(), request.getRequestURL(), request.getMethod()));

			JWTPayload jwtPayload = jweValidator.getJWTPayload(encryptedJWT);

			Map<String, List<String>> policyMap = generatePolicyMap();

			boolean allowed = jweValidator.checkPolicy(jwtPayload, request.getMethod(),	request.getRequestURI(), policyMap);
			log.info(jwtPayload.getUserName()+" access allowed ? "+allowed);


			if(!allowed) { 
				commitResponse(response,403,"Unauthorized access !!!"); 
				throw  new UnauthorizedException(); 
			}else {
				request.setAttribute("JWTPayload", jwtPayload); 
				response.addHeader(HttpHeaders.AUTHORIZATION, encryptedJWT);
				response.addHeader("Access-Control-Expose-Headers", "*");
				filterChain.doFilter(request,response); 
			} 

		} catch(UnauthorizedException |IllegalAccessException |NoSuchAlgorithmException |InvalidKeySpecException |ParseException |JOSEException e) {
			e.printStackTrace();
			commitResponse(response,403,"Unauthorized access !!!");
		} catch(Exception e)
		{ 
			e.printStackTrace(); 
			commitResponse(response,500,"Server error !!!"); 
		}
	}

	private Map<String, List<String>> generatePolicyMap() {
		Map<String, List<String>> policyMap = new HashMap<>();
		policyMap.put("account_summary", account_summary_policy);
		policyMap.put("profile_summary", profile_summary_policy);
		policyMap.put("transaction_summary", transaction_summary_policy);
		policyMap.put("transaction_recent", transaction_recent_policy);
		policyMap.put("transaction_last", transaction_last_policy);
		log.info("Policy map : "+policyMap);
		return policyMap;
	}

	private void commitResponse(HttpServletResponse response, int status, String msg){
		if(response.isCommitted()) 
			return;
		response.setStatus(status);
		response.setContentType("application/json");
		log.info("Exception caught : "+msg);
		try {
			PrintWriter pw = response.getWriter();
			pw.printf("{'Message' :'%s'}",msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}