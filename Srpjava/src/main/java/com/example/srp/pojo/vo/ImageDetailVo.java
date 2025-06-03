package com.example.srp.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ImageDetailVo implements Serializable {
    private long id;
    private String originalImagePath;
    private String analyzedImagePath;
    private String imageDetail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
