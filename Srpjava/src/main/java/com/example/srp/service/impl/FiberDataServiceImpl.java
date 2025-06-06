package com.example.srp.service.impl;

import com.example.srp.constant.AliyunPathConstant;
import com.example.srp.mapper.FiberDataMapper;
import com.example.srp.pojo.dto.FiberDataDto;
import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.vo.FiberDataVo;
import com.example.srp.result.PageResult;
import com.example.srp.service.FiberDataService;
import com.example.srp.utils.AliOssUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        PageHelper.startPage(imageDetailQueryAllDto.getPage(),imageDetailQueryAllDto.getPageSize());
        Page<FiberDataVo> page = fiberDataMapper.pageQueryAll(imageDetailQueryAllDto);
        //System.out.println(page);
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
}
