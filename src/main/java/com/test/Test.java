package com.test;

import com.spring.MyApplicationContext;
import com.test.service.UserService;

public class Test {
    public static void main(String[] args) {
        MyApplicationContext context = new MyApplicationContext(AppConfig.class);
        UserService userService =(UserService) context.getBean("userService");
        userService.add();
        userService.test();
    }
}
