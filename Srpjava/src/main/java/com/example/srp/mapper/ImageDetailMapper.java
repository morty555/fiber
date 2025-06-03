package com.example.srp.mapper;

import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.entity.ImageDetail;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageDetailMapper {
    Page<ImageDetailVo> pageQuery(ImageDetailQueryDto imageDetailQueryDto);

    @Insert("insert into image_detail(id,original_image_path,analyzed_image_path,image_detail,update_time,create_time,owner)"+"values"+"(#{id},#{originalImagePath},#{analyzedImagePath},#{imageDetail},#{updateTime},#{createTime},#{owner})")
    void addNewImageDetail(ImageDetail imageDetail);

    @Delete("delete from image_detail where id = #{id}")
    void deleteDetailById(Long id);
}
