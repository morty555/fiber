package com.example.srp.service;

import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.vo.DailyCountVo;
import com.example.srp.pojo.vo.TypeGraphVo;
import com.example.srp.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FiberDataService {
    void deleteFiberData(Long id);

    void addNewRecord(MultipartFile file, String detail);

    PageResult pageQueryAll(ImageDetailQueryAllDto imageDetailQueryAllDto);

    PageResult pageQuerySimilarImageDetail(MultipartFile file,int pageNo, int pageSize);

    List<TypeGraphVo> getTypeCounts();

    List<DailyCountVo> dailyCount();
}
