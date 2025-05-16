package com.example.srp.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVo implements Serializable {

    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("主键值")
    private Long id;
    @ApiModelProperty("jwt验证")
    private String token;
}
