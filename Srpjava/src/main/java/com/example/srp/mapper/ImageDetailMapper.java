package com.example.srp.mapper;

import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageDetailMapper {
    Page<ImageDetailVo> pageQuery(ImageDetailQueryDto imageDetailQueryDto);

    @Insert("insert into image_detail(id,image,analyzed_image,detail,update_time,create_time)"+"values"+"(#{id},#{image},#{analyzedImage},#{imageDetail},#{updateTime},#{createTime})")
    void addNewImageDetail(ImageDetailDto dto);
}
