package com.example.srp.service;

import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.result.PageResult;

public interface ImageDetailService {
  PageResult pageQuery(ImageDetailQueryDto imageDetailQueryDto);

}
