package com.test.service;

import com.spring.Autowired;
import com.spring.Component;
import com.spring.Scope;

@Component("userService")
//@Scope("prototype")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void add() {
        System.out.println("userService add...");
    }

    public void test() {
        System.out.println(orderService);
    }
}
