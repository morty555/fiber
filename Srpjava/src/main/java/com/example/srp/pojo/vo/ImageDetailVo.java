package com.example.srp.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageDetailVo implements Serializable {
    private String image;
    private String analyzedImage;
    private String imageDetail;
}
