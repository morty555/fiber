package com.example.srp.mapper;

import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageDetailMapper {
    Page<ImageDetailVo> pageQuery(ImageDetailQueryDto imageDetailQueryDto);
}
