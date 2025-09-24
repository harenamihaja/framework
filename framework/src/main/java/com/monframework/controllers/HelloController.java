package com.monframework.controllers;

import com.monframework.annotations.WebMapping;

public class HelloController {
    @WebMapping("/hello")
    public String sayHello() {
        return "Bonjour depuis le framework !";
    }
}  
