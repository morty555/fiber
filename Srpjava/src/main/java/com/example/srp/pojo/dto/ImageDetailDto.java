package com.example.srp.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ImageDetailDto implements Serializable {

    private long id;
    private String image;
    private String analyzedImage;
    private String imageDetail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
