package com.bhavani.cutomerprofile.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bhavani.cutomerprofile.dao.Caller;
import com.bhavani.cutomerprofile.dao.JWTPayload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnExpression("${authorizer.enabled}==true")
public class AuthorizeFilter extends OncePerRequestFilter {
    private Log log = LogFactory.getLog(AuthorizeFilter.class);

    @Value("${spring.application.name}")
    String appName;

    @Value("${endpoints.authorize}")
    String authorizerEndpoint;

    RestTemplate authorizerRestTemplate = new RestTemplate();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String encryptedJWT = request.getHeader("Authorization");
            String resource = request.getRequestURI();
            String httpVerb = request.getMethod();
            log.info(String.format("Authorizing the token %s for request %s %s : ", encryptedJWT, httpVerb, resource));
            Caller caller = new Caller();
            caller.setName(appName);
            caller.setResource(resource);
            caller.setHttpVerb(httpVerb);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Authorization", encryptedJWT);
            RequestEntity<Caller> reqEntity= new RequestEntity<>(caller, headers, HttpMethod.POST, new URI(authorizerEndpoint));
            ResponseEntity<JWTPayload> resEntity = authorizerRestTemplate.exchange(reqEntity, JWTPayload.class);
            request.setAttribute("JWTPayload", resEntity.getBody());
            filterChain.doFilter(request, response);
        } catch(RestClientResponseException e){
            commitResponse(response,403,"Unauthorized access !!!");
        } catch (URISyntaxException e) {
            commitResponse(response,400,"Bad Request !!!");
            e.printStackTrace();
        } catch(Exception e){
            commitResponse(response,500,"Server error !!!");
        }
        
    }

    private void commitResponse(HttpServletResponse response, int status, String msg){
        if(response.isCommitted()) return;
        response.setStatus(status);
        response.setContentType("application/json");
        try{
            PrintWriter pw = response.getWriter();
            pw.printf("{'Message' :'%s'}",msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}