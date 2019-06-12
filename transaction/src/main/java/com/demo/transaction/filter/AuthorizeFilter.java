package com.demo.transaction.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

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

import com.demo.transaction.doc.JWTPayload;
import com.demo.transaction.exception.UnauthorizedException;
import com.demo.transaction.utils.JWEValidator;
import com.nimbusds.jose.JOSEException;

import redis.clients.jedis.Jedis;

@Component
public class AuthorizeFilter extends OncePerRequestFilter {
	private Log log = LogFactory.getLog(AuthorizeFilter.class);

	@Value("${spring.application.name}")
	String appName;

	@Value("${token_message}")
	String token_message;

	private Jedis jedis;
	private JWEValidator jweValidator;

	@Autowired 
	public AuthorizeFilter(Jedis jedis, JWEValidator jweValidator) {
		this.jedis = jedis;
		this.jweValidator = jweValidator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			String encryptedJWT =  request.getHeader(HttpHeaders.AUTHORIZATION);
			log.info(String.format("Authorizing the token %s for URI %s URL %s Method %s ", encryptedJWT, request.getRequestURI(), request.getRequestURL(), request.getMethod()));

			JWTPayload jwtPayload = jweValidator.getJWTPayload(encryptedJWT);
			String jedisValue = jedis.get(jwtPayload.getUserName());
			log.info("Jedis Key/Value : "+jwtPayload.getUserName()+"/"+jedisValue);

			boolean allowed = jweValidator.checkPolicy(jwtPayload, request.getMethod(), request.getRequestURI());
			log.info(jwtPayload.getUserName()+" access allowed ? "+allowed);

			if(!token_message.equals(jedisValue) || !allowed) {
				commitResponse(response,403,"Unauthorized access !!!");
				throw new UnauthorizedException();
			}else {
				/*
				 * MultiValueMap<String, String> headers = new HttpHeaders();
				 * headers.add("Authorization", encryptedJWT);
				 */
				request.setAttribute("JWTPayload", jwtPayload);
				response.addHeader(HttpHeaders.AUTHORIZATION, encryptedJWT);
				filterChain.doFilter(request, response);
			} 
		} 
		catch(UnauthorizedException |IllegalAccessException |NoSuchAlgorithmException |InvalidKeySpecException |ParseException |JOSEException e) {
			e.printStackTrace();
			commitResponse(response,403,"Unauthorized access !!!");
		} catch(Exception e) {
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

}