package com.example.srp.mapper;

import com.example.srp.pojo.dto.FiberDataDto;
import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.vo.FiberDataVo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FiberDataMapper {
    @Insert("insert into fiber_data(original_image,detail)"+"values"+"(#{image},#{detail})")
    void addNewFiberData(FiberDataDto dto);

    Page<FiberDataVo> pageQueryAll(ImageDetailQueryAllDto imageDetailQueryAllDto);

    @Delete("delete from fiber_data where id = #{id}")
    void deleteById(Long id);

    Page<FiberDataVo> pageQuerySimilarImageDetail(String imageDetailQueryAllDto);
}
