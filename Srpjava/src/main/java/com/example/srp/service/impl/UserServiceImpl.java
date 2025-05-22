package com.example.srp.service.impl;

import com.example.srp.constant.AccountLockedException;
import com.example.srp.constant.MessageConstant;
import com.example.srp.constant.StatusConstant;
import com.example.srp.exception.AccountExistedException;
import com.example.srp.exception.AccountNotFoundException;
import com.example.srp.exception.PasswordErrorException;
import com.example.srp.mapper.UserMapper;
import com.example.srp.pojo.dto.UserLoginDto;
import com.example.srp.pojo.entity.User;
import com.example.srp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;


    public User login(UserLoginDto userLoginDto) {
        String username = userLoginDto.getUsername();
        String password = DigestUtils.md5DigestAsHex(userLoginDto.getPassword().getBytes());
        User user = userMapper.getByUsername(username);
        if(user==null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FIND);
        }

       // password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(user.getPassword())) {

            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        return user;
    }


    public User register(UserLoginDto userLoginDto) {
        User user;
        String username = userLoginDto.getUsername();
        String password = userLoginDto.getPassword();
        user = userMapper.getByUsername(username);
        if(user!=null){
            throw new AccountExistedException(MessageConstant.ACCOUNT_EXISTED);
        }
        else{
            user.setUsername(username);
            user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
            user.setImage("");
            user.setStatus(StatusConstant.ENABLE);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
//        user.builder()
//                .password(DigestUtils.md5DigestAsHex(password.getBytes()))
//                .username(username)
//                .createTime(LocalDateTime.now())
//                .updateTime(LocalDateTime.now())
//                .image("")
//                .status(StatusConstant.ENABLE)
//                .build();
            userMapper.register(user);
        }


        return user;
    }

}
