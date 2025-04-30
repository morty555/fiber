package com.example.srp.controller;

import com.example.srp.constant.JwtClaimsConstant;
import com.example.srp.pojo.dto.UserLoginDto;
import com.example.srp.pojo.entity.User;
import com.example.srp.properties.JwtProperties;
import com.example.srp.result.Result;
import com.example.srp.service.UserService;
import com.example.srp.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.srp.pojo.vo.UserLoginVo;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/")
@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 登录
     *
     * @param userLoginDto
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVo> test(@RequestBody UserLoginDto userLoginDto){
         log.info("员工登陆:{}",userLoginDto);
         User user = userService.login(userLoginDto);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        UserLoginVo userLoginVo = UserLoginVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();

        System.out.println(userLoginVo);
        return Result.success(userLoginVo);

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
