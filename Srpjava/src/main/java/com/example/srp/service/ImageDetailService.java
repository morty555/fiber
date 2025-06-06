package com.example.srp.service;

import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.FiberDataVo;
import com.example.srp.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageDetailService {
   ImageDetailDto analyze(MultipartFile file) ;



    PageResult pageQuery(ImageDetailQueryDto imageDetailQueryDto);

    void deleteDetail(Long id);


}
