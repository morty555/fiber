package com.example.srp.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String username;
    private String password;
    private String image;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int status;
}
