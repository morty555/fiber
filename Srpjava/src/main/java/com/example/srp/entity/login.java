package com.example.srp.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class login implements Serializable {
    @ApiModelProperty("用户名")
    private String username;
}
