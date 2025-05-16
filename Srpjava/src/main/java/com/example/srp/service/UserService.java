package com.example.srp.service;

import com.example.srp.pojo.dto.UserLoginDto;
import com.example.srp.pojo.entity.User;

public interface UserService {
    User login(UserLoginDto userLoginDto);

    void register(UserLoginDto userLoginDto);
}
