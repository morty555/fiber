package com.example.srp.service.impl;

import com.example.srp.constant.AliyunPathConstant;
import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.entity.ImageDetail;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.PageResult;
import com.example.srp.result.Result;
import com.example.srp.service.ImageDetailService;
import com.example.srp.utils.AliOssUtil;
import com.example.srp.utils.Base64ToMultipartUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jaxb.core.v2.TODO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageDetailServiceImpl implements ImageDetailService{

    @Autowired
    private ImageDetailMapper imageDetailMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    public ImageDetailDto analyze(MultipartFile file) {
        ImageDetailDto dto = new ImageDetailDto();

        //上传原始图片
        String originalImagePath = uploadAliOss(file, AliyunPathConstant.ORIGINAL_IMAGE);

        try {
            // 将图片文件转成Base64字符串
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            dto.setOriginalImage(base64Image);

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

            //传回python识别后的图片与识别结果
            // 解析返回值
            Map result = response.getBody();
            String analysis = (String) result.get("analysis");
            Integer edgePixelCount = (Integer) result.get("edge_pixel_count");

            //上传分析后的图片
            String analyzedImage = (String) result.get("analyzed_image");
            MultipartFile analyzedFile = base64ToMultipart(analyzedImage);
            String analyzedImagePath = uploadAliOss(analyzedFile, AliyunPathConstant.ANALYZED_IMAGE);

            // 保存路径和 Base64 到 dto
            dto.setAnalyzedImage(analyzedImage);
            dto.setAnalyzedImagePath(analyzedImagePath);

            System.out.println(result);

            // 这里根据业务，给dto设置其他字段
            dto.setCreateTime(LocalDateTime.now());
            dto.setUpdateTime(LocalDateTime.now());
            dto.setImageDetail(analysis+","+edgePixelCount);
            dto.setOriginalImagePath(originalImagePath);

            ImageDetail imageDetail = new ImageDetail();
            BeanUtils.copyProperties(dto, imageDetail);
            //将oss存储地址等信息保存
            imageDetailMapper.addNewImageDetail(imageDetail);


        } catch (IOException e) {
            e.printStackTrace();
           System.out.println("文件上传失败");
        }

        return dto;
    }

    //分页查询结果
    public PageResult pageQuery(ImageDetailQueryDto imageDetailQueryDto) {
        PageHelper.startPage(imageDetailQueryDto.getPage(),imageDetailQueryDto.getPageSize());
        Page<ImageDetailVo> page = imageDetailMapper.pageQuery(imageDetailQueryDto);
        return new PageResult(page.getTotal(),page.getResult());
    }

    public String uploadAliOss(MultipartFile file,String path) {
        String filePath;
        try {
            //原始文件名
            /*首先通过file.getOriginalFilename()获取原始文件名*/
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀   dfdfdf.png
            /*然后通过originalFilename.lastIndexOf(".")获取文件名的后缀。*/
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称
            /*使用UUID.randomUUID().toString()生成一个随机的文件名，并将后缀拼接在文件名后面，构造出新的文件名。*/
            String objectName = path+ UUID.randomUUID().toString() + extension;

            //文件的请求路径
            /*然后，调用aliOssUtil.upload方法将文件上传到OSS，并获取文件的请求路径。*/
            filePath = aliOssUtil.upload(file.getBytes(), objectName);

            return filePath;
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }
        return "WrongPath!";
    }

    public MultipartFile base64ToMultipart(String base64) {
        String[] parts = base64.split(",");
        String base64Data = parts.length > 1 ? parts[1] : parts[0];
        byte[] data = Base64.getDecoder().decode(base64Data);
        return new Base64ToMultipartUtil(data, "image.png", "image/png");
    }
}
