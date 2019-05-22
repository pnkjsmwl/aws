package com.bhavani.authenticator.dao;

import lombok.Data;

@Data
public class Caller{
    private String name;
    private String resource;
    private String httpVerb;
}