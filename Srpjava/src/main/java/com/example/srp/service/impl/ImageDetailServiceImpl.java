package com.example.srp.service.impl;

import com.example.srp.constant.AliyunPathConstant;
import com.example.srp.mapper.ImageDetailMapper;
import com.example.srp.pojo.dto.*;
import com.example.srp.pojo.entity.ImageDetail;
import com.example.srp.pojo.vo.FiberDataVo;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.PageResult;
import com.example.srp.service.ImageDetailService;
import com.example.srp.utils.AliOssUtil;
import com.example.srp.utils.Base64ToMultipartUtil;
import com.example.srp.utils.ThreadLocalUtil;
import com.example.srp.utils.TiffConverterUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.srp.constant.ImageConstant.CATEGORY;
import static com.example.srp.constant.ImageConstant.CONFIDENCE;

@Service
@Slf4j
public class ImageDetailServiceImpl implements ImageDetailService{

    @Autowired
    private ImageDetailMapper imageDetailMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    public ImageDetailDto analyze(MultipartFile file) {
        ImageDetailDto dto = new ImageDetailDto();
        Long currentId = ThreadLocalUtil.getCurrentId();
        // 上传原始图片
       String originalImagePath = uploadAliOss(file, AliyunPathConstant.ORIGINAL_IMAGE);
        //String originalImagePath = uploadAliOss(file, AliyunPathConstant.FIBERDATA_IMAGE);

        try {
            // 生成 Base64 原图字符串
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            dto.setOriginalImage(base64Image);

            RestTemplate restTemplate = new RestTemplate(); //java发送请求
            String pythonUrl = "http://localhost:5001/analyze";


            // 用 Multipart/form-data 发送文件给 Python 服务
            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();//构造 multipart/form-data 的请求体
            //用字节模拟文件上传	把 byte[] 变成 Flask 能识别的“文件”
            //如果此处直接使用base64 问题：1.base64 比原始图片大 30% 左右 2.Python 接收后还要额外解码 3.若你想复用 multipart 接口或未来扩展为表单上传，这种方式更通用
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            bodyMap.add("image", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, requestEntity, Map.class); //也可以用自定义类接收返回数据
            // ----------------------------------------------

            // 解析返回结果（你现有代码）
            Map result = response.getBody();
            ArrayList<String> stats = (ArrayList<String>) result.get("stats");
            String unit = (String) result.get("unit");
            Double upp = (Double) result.get("upp");
            Double confidence = (Double) result.get("confidence");
            String category = (String) result.get("category");

            String analyzedImage = new String();
            // base64转 MultipartFile，上传分析图（你现有代码）
            try {
                 analyzedImage = (String) result.get("image_base64");
            }
            catch (Exception e) {
                log.error("获取分析图失败", e);
                throw new RuntimeException("获取分析图失败，image_base64 字段无效");
            }

            MultipartFile analyzedFile = base64ToMultipart(analyzedImage);
            String analyzedImagePath = uploadAliOss(analyzedFile, AliyunPathConstant.ANALYZED_IMAGE);

            dto.setOwner(currentId);
            dto.setAnalyzedImage(analyzedImage);
            dto.setAnalyzedImagePath(analyzedImagePath);
            dto.setCreateTime(LocalDateTime.now());
            dto.setUpdateTime(LocalDateTime.now());
            dto.setImageDetail(CATEGORY+category+","+CONFIDENCE+confidence+","+"长宽比分析："+stats + "," +","+upp);
            dto.setOriginalImagePath(originalImagePath);

            ImageDetailReturnDto returnDto = new ImageDetailReturnDto();
            BeanUtils.copyProperties(dto, returnDto);

            System.out.print("图片分析:"+dto.getImageDetail());
            ImageDetail imageDetail = new ImageDetail();
            BeanUtils.copyProperties(dto, imageDetail);
            imageDetailMapper.addNewImageDetail(imageDetail);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件上传失败");
        }

        return dto;
    }


    //分页查询结果
    @Override
    public PageResult pageQuery(ImageDetailQueryDto imageDetailQueryDto) {
        PageHelper.startPage(imageDetailQueryDto.getPage(), imageDetailQueryDto.getPageSize());
        imageDetailQueryDto.setOwner(ThreadLocalUtil.getCurrentId());
        Page<ImageDetailVo> page = imageDetailMapper.pageQuery(imageDetailQueryDto);

        for (ImageDetailVo vo : page) {
            String imagePath = vo.getOriginalImagePath();
            if (imagePath != null && imagePath.endsWith(".tiff")) {
                try {
                    // 下载 TIFF 文件
                    URL url = new URL(imagePath);
                    File tiffFile = File.createTempFile("input_", ".tiff");
                    try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(tiffFile)) {
                        in.transferTo(out);
                    }

                    // 转换为 PNG
                    File pngFile = TiffConverterUtil.convertTiffToPng(tiffFile);

                    // 上传到 OSS
                    String newImagePath = upload(pngFile, "image-detail" ,vo.getId());

                    // 替换路径
                    vo.setOriginalImagePath(newImagePath);

                    // 清理临时文件
                    tiffFile.delete();
                    pngFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.warn("TIFF 转换失败: {}", imagePath);
                }
            }
        }

        return new PageResult(page.getTotal(), page.getResult());
    }


    @Override
    public void deleteDetail(Long id) {
         imageDetailMapper.deleteDetailById(id);
    }


    private String upload(File file, String pathPrefix, Long id) {
        try {
            String objectName = pathPrefix + id + ".png";

            // 如果已存在，直接返回URL
            if (aliOssUtil.exists(objectName)) {
                return aliOssUtil.generateUrl(objectName);
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            return aliOssUtil.upload(bytes, objectName);
        } catch (IOException e) {
            e.printStackTrace();
            return "WrongPath!";
        }
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