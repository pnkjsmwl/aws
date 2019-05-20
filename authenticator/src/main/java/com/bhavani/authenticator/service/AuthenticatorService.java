package com.bhavani.authenticator.service;

import java.util.HashMap;

import com.bhavani.authenticator.dao.Credentials;
import com.bhavani.authenticator.dao.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticatorService {
    @Autowired
    @Qualifier(value = "userData")
    private HashMap<String, UserInfo> userdata;

    @Autowired
    private JWEGenerator jweGenerator;

    @RequestMapping(value = "/signon", method = RequestMethod.POST)
    public ResponseEntity<String> signon(@RequestBody Credentials credentials) {
        try {
            if (userdata.containsKey(credentials.getUserName())) {
                if (userdata.get(credentials.getUserName()).getPassword().equals(credentials.getPassword())) {
                    String encryptedJWT = jweGenerator.generateJWE(null, userdata.get(credentials.getUserName()));
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
                    return ResponseEntity.ok().headers(headers).body("{'Message':'Login Successful'}");

                } else {
                    return ResponseEntity.badRequest().body("{'Error':'Invalid Password'}");
                }
            } else {
                return ResponseEntity.badRequest().body("{'Error':'Invalid Username'}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{'OOPS!!!  Error Occurred'}");
        }

    }
}