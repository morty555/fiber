package com.example.srp.pojo.dto;

import lombok.Data;

@Data
public class ImageDetailQueryAllDto {
    private int page;

    private int pageSize;

    private String search;

    private String sortOrder;

    private String sortField;
}
