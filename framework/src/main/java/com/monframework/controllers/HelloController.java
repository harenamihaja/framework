package com.monframework.controllers;

import com.monframework.annotations.UrlMapping;

public class HelloController {
    @UrlMapping(url = "/")
    public String sayHello() {
        return "Bonjour depuis le framework !";
    }
}  
