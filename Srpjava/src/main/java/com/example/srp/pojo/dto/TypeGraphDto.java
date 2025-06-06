package com.example.srp.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TypeGraphDto implements Serializable {
    private String type;
    private int count;
}
