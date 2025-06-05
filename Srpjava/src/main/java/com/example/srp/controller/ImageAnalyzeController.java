package com.example.srp.controller;

import com.example.srp.constant.AliyunPathConstant;
import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.Result;
import com.example.srp.service.ImageDetailService;
import com.example.srp.utils.AliOssUtil;
import com.example.srp.utils.Base64ToMultipartUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fiber")
@Slf4j
public class ImageAnalyzeController {

    @Autowired
    private ImageDetailService imageDetailService;



    @PostMapping("/analyze")
    public Result<ImageDetailVo> analyzeImage(@RequestParam("file") MultipartFile file) {
        log.info("图片分析结果：",file);


        ImageDetailVo imageDetailVo = new ImageDetailVo();
        ImageDetailDto dtoresult  = imageDetailService.analyze(file);

        if(dtoresult == null){
            return Result.error("文件上传失败!");
        }
        BeanUtils.copyProperties(dtoresult,imageDetailVo);
        return Result.success(imageDetailVo);

}



}
