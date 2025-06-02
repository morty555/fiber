package com.example.srp.pojo.dto;

import lombok.Data;

@Data
public class ImageDetailQueryDto {
    private int page;

    private int pageSize;

    private String search;

    private String sortOrder;

    private String sortField;

    private Long owner;
}
