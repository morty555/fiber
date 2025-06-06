package com.example.srp.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DailyCountVo implements Serializable {
    private String date;
    private int count;
}
