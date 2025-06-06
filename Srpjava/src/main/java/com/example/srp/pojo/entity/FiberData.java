package com.example.srp.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiberData implements Serializable {
    private Long id;
    private String image;
    private String type;
    private String detail;

}
