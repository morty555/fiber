package com.example.srp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/")
@RestController
public class testController {
    @PostMapping("/login")
    public void test(@RequestBody String username){
         System.out.println(username);
    }

    @GetMapping("/")
    public void test2(@RequestParam String username){
        System.out.println(username+"get请求");
    }

    @PostMapping("analyze/result")
    public void test3(@RequestBody String image){
        System.out.println("testsuccess");
    }
}
