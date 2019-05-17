package com.bhavani.authenticator.service;

import java.util.HashMap;

import com.bhavani.authenticator.dao.Credentials;
import com.bhavani.authenticator.dao.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticatorService {
    @Autowired
    @Qualifier(value = "userData")
    private HashMap<String, UserInfo> userdata;

    @RequestMapping(value="/signon",method=RequestMethod.POST)
    public String signon(@RequestBody Credentials credentials){
        if(userdata.containsKey(credentials.getUserName())){
               if(userdata.get(credentials.getUserName()).getPassword().equals(credentials.getPassword())){
                return "{'Message':'Login Successful'}";
               } else{
                return "{'Error':'Invalid Password'}";       
               }
        }else{
            return "{'Error':'Invalid Username'}";
        }
    }


}