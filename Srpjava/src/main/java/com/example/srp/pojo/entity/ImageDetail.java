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
public class ImageDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String originalImagePath;

    private String analyzedImagePath;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    private String ImageDetail;
}
