package com.example.srp.service.impl;

import com.example.srp.constant.AliyunPathConstant;
import com.example.srp.mapper.FiberDataMapper;
import com.example.srp.pojo.dto.FiberDataDto;
import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.dto.TypeGraphDto;
import com.example.srp.pojo.vo.DailyCountVo;
import com.example.srp.pojo.vo.FiberDataVo;
import com.example.srp.pojo.vo.TypeGraphVo;
import com.example.srp.result.PageResult;
import com.example.srp.service.FiberDataService;
import com.example.srp.utils.AliOssUtil;
import com.example.srp.utils.TiffConverterUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
public class FiberDataServiceImpl implements FiberDataService {
    @Autowired
    private FiberDataMapper fiberDataMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public void deleteFiberData(Long id) {
        fiberDataMapper.deleteById(id);
    }

    @Override
    public void addNewRecord(MultipartFile file, String detail) {
        String originalImagePath = uploadAliOss(file, AliyunPathConstant.FIBERDATA_IMAGE);
        FiberDataDto dto = new FiberDataDto();
        dto.setImage(originalImagePath);
        dto.setDetail(detail);
        fiberDataMapper.addNewFiberData(dto);

    }


    @Override
    public PageResult pageQueryAll(ImageDetailQueryAllDto imageDetailQueryAllDto) {
        PageHelper.startPage(imageDetailQueryAllDto.getPage(), imageDetailQueryAllDto.getPageSize());

        Page<FiberDataVo> page = fiberDataMapper.pageQueryAll(imageDetailQueryAllDto);

        for (FiberDataVo vo : page) {
            String imagePath = vo.getOriginalImage();
            if (imagePath != null && imagePath.endsWith(".tiff")) {
                try {
                    // 下载 TIFF 文件
                    URL url = new URL(imagePath);
                    File tiffFile = File.createTempFile("input_", ".tiff");
                    try (InputStream in = url.openStream();
                         FileOutputStream out = new FileOutputStream(tiffFile)) {
                        in.transferTo(out);
                    }

                    // 转换为 PNG
                    File pngFile = TiffConverterUtil.convertTiffToPng(tiffFile);

                    // 上传到 OSS（替换为你自己的上传逻辑）
                    String newImageUrl = upload(pngFile, "fiberdata-image/", vo.getId());



                    // 替换路径
                    vo.setOriginalImage(newImageUrl);

                    // 清理临时文件
                    tiffFile.delete();
                    pngFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                    // 可以选择设置默认图片路径或者保留 TIFF
                }
            }
        }

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult pageQuerySimilarImageDetail(MultipartFile file, int pageNum, int pageSize) {
        try {
            byte[] bytes = file.getBytes();

            RestTemplate restTemplate = new RestTemplate();
            String pythonUrl = "http://localhost:5001/analyze";

            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
            bodyMap.add("image", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, requestEntity, Map.class);

            Map result = response.getBody();

            if (result == null) {
                throw new RuntimeException("Python 返回结果为空");
            }

            String category = (String) result.get("category");
            String[] parts = category.split("\\s+", 2);  // 按第一个空格分成两部分
            String type = parts[1];

            //System.out.print(type);
            if (type == null || type.isEmpty()) {
                throw new RuntimeException("未从 Python 返回中识别出类型");
            }

            //  开始分页
            PageHelper.startPage(pageNum, pageSize);

            //  查询分页数据
            List<FiberDataVo> similarImages = fiberDataMapper.pageQuerySimilarImageDetail(type);

            //  获取分页结果
            PageInfo<FiberDataVo> pageInfo = new PageInfo<>(similarImages);

            PageResult pageResult = new PageResult();
            pageResult.setTotal(pageInfo.getTotal());
            pageResult.setRecords(pageInfo.getList());

            return pageResult;

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败", e);
        }
    }

    @Override
    public List<TypeGraphVo> getTypeCounts() {
        List<TypeGraphVo> list  = fiberDataMapper.countType();
        return list;
    }

    @Override
    public List<DailyCountVo> dailyCount() {
        List<DailyCountVo> dailyCountVos = fiberDataMapper.dailyCount();
        return dailyCountVos;
    }


    private String upload(File file, String pathPrefix, Long id) {
        try {
            String objectName =AliyunPathConstant.FIBERDATA_IMAGE+ pathPrefix + id + ".png";

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
}
