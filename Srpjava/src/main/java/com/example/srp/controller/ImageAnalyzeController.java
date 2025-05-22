package com.example.srp.controller;

import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.Result;
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

            RestTemplate restTemplate = new RestTemplate();
            String pythonUrl = "http://localhost:5001/analyze";

            // 构建请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image", base64Image);

            // 发送 POST 请求到 Python
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, entity, Map.class);

            // 解析返回值
            Map result = response.getBody();
            String analysis = (String) result.get("analysis");
            Integer edgePixelCount = (Integer) result.get("edge_pixel_count");
            System.out.println(analysis+"+"+result);

            // 这里根据业务，给dto设置其他字段
            dto.setCreateTime(LocalDateTime.now());
            dto.setUpdateTime(LocalDateTime.now());
            dto.setImageDetail(analysis);
            dto.setAnalyzedImage(base64Image);
            imageDetailMapper.addNewImageDetail(dto);
            BeanUtils.copyProperties(dto,imageDetailVo);

            return Result.success(imageDetailVo);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件解析失败");
        }
    }
}
