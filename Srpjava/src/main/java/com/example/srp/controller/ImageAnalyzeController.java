package com.example.srp.controller;

import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/fiber")
@Slf4j
public class ImageAnalyzeController {

    @Autowired
    private ImageDetailMapper imageDetailMapper;

    @PostMapping("/analyze")
    public Result<ImageDetailVo> analyzeImage(@RequestParam("file") MultipartFile file) {
        log.info("图片分析结果：",file);
        ImageDetailDto dto = new ImageDetailDto();
        ImageDetailVo imageDetailVo = new ImageDetailVo();
        try {
            // 将图片文件转成Base64字符串
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            dto.setImage(base64Image);

            // 这里你可以根据业务，给dto设置其他字段
            dto.setCreateTime(LocalDateTime.now());
            dto.setUpdateTime(LocalDateTime.now());
            dto.setImageDetail("图片分析结果描述"); // 自定义填充
            imageDetailMapper.addNewImageDetail(dto);
            BeanUtils.copyProperties(dto,imageDetailVo);

            return Result.success(imageDetailVo);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件解析失败");
        }
    }
}
