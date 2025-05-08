package com.example.srp.service.impl;

import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.PageResult;
import com.example.srp.service.ImageDetailService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageDetailServiceImpl implements ImageDetailService {

    @Autowired
    private ImageDetailMapper imageDetailMapper;


    @Override
    public PageResult pageQuery(ImageDetailQueryDto imageDetailQueryDto) {
        PageHelper.startPage(imageDetailQueryDto.getPage(),imageDetailQueryDto.getPageSize());
        Page<ImageDetailVo> page = imageDetailMapper.pageQuery(imageDetailQueryDto);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
