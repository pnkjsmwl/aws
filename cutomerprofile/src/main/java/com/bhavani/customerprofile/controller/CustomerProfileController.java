package com.bhavani.cutomerprofile.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.bhavani.cutomerprofile.dao.CustomerProfile;
import com.bhavani.cutomerprofile.dao.JWTPayload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerProfileController{
    private Log log = LogFactory.getLog(CustomerProfileController.class);

    @Autowired
    @Qualifier("profileData")
    HashMap<String,CustomerProfile> profileData;

    @RequestMapping(value="/profile/email", method=RequestMethod.GET)
    public ResponseEntity<String> getEmail(HttpServletRequest request){
        log.info("Inside get email !!!");
        JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");
        return ResponseEntity.ok().body(String.format("{\"Email\": \"%s\"}",profileData.get(jwtPayload.getSubject()).getEmail()));
    }

    @RequestMapping(value="/profile/mobile", method=RequestMethod.GET)
    public ResponseEntity<String> getMobile(HttpServletRequest request){
        log.info("Inside get mobile !!!");
        JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");
        return ResponseEntity.ok().body(String.format("{\"Mobile\": \"%s\"}",profileData.get(jwtPayload.getSubject()).getMobile()));
    }

    @RequestMapping(value="/profile/address", method=RequestMethod.GET)
    public ResponseEntity<String> getAddress(HttpServletRequest request){
        log.info("Inside get address !!!");
        JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");
        return ResponseEntity.ok().body(String.format("{\"Address\": \"%s\"}",profileData.get(jwtPayload.getSubject()).getAddress()));
    }



    
}