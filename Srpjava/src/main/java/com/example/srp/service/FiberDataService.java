package com.example.srp.service;

import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

public interface FiberDataService {
    void deleteFiberData(Long id);

    void addNewRecord(MultipartFile file, String detail);

    PageResult pageQueryAll(ImageDetailQueryAllDto imageDetailQueryAllDto);
}
