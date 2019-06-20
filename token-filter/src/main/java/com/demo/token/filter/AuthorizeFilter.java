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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JOSEException;

@Component
public class AuthorizeFilter extends OncePerRequestFilter {
	private Log log = LogFactory.getLog(AuthorizeFilter.class);
	private Map<String, List<String>> map = new HashMap<>();
	private long tokenExpiryInterval;

	@Override 
	protected void doFilterInternal(HttpServletRequest request,  HttpServletResponse response, FilterChain filterChain) throws  ServletException, IOException { 
		JWEValidator jweValidator = new JWEValidator(map, tokenExpiryInterval);
		try { 
			String encryptedJWT =  request.getHeader(HttpHeaders.AUTHORIZATION);
			log.info(String.format("Authorizing the token %s for URI %s URL %s Method %s ", encryptedJWT,request.getRequestURI(), request.getRequestURL(), request.getMethod()));

			JWTPayload jwtPayload = jweValidator.getJWTPayload(encryptedJWT);

			boolean allowed = jweValidator.checkPolicy(jwtPayload, request.getMethod(),	request.getRequestURI());
			log.info(jwtPayload.getUserName()+" access allowed ? "+allowed);


			if(!allowed) { 
				commitResponse(response,403,"Unauthorized access !!!"); 
				throw  new UnauthorizedException(); 
			}else {
				/*
				 * MultiValueMap<String, String> headers = new HttpHeaders();
				 * headers.add("Authorization", encryptedJWT);
				 */
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

	public Map<String, List<String>> getMap() {
		return map;
	}
	public void setMap(Map<String, List<String>> map) {
		this.map = map;
	}
	public long getTokenExpiryInterval() {
		return tokenExpiryInterval;
	}
	public void setTokenExpiryInterval(long tokenExpiryInterval) {
		this.tokenExpiryInterval = tokenExpiryInterval;
	}

}