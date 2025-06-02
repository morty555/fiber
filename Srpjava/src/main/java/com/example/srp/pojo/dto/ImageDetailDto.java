package com.example.srp.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ImageDetailDto implements Serializable {

    private long id;
    private String originalImage;
    private String analyzedImage;
    private String originalImagePath;
    private String analyzedImagePath;
    private String imageDetail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private long owner;

}
