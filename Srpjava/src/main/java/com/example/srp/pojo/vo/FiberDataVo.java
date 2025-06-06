package com.example.srp.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FiberDataVo implements Serializable {
    private long id;
    private String originalImage;
    private String detail;
    private String type;

}
