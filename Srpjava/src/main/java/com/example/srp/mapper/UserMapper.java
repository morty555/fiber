package com.example.srp.mapper;

import com.example.srp.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User getByUsername(String username);
}
