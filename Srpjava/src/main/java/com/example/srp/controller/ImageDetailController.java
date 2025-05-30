package com.example.srp.controller;

import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.PageResult;
import com.example.srp.result.Result;
import com.example.srp.service.ImageDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/function")
public class ImageDetailController {


    @Autowired
    private ImageDetailService imageDetailService;

    @PostMapping("/history")
    public Result<PageResult> getImageDetail(@RequestBody  ImageDetailQueryDto imageDetailQueryDto){
        log.info("图片分析历史记录：{}",imageDetailQueryDto);
        PageResult pageResult = imageDetailService.pageQuery(imageDetailQueryDto);
        return Result.success(pageResult);

    }
}
