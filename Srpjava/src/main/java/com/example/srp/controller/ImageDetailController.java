package com.example.srp.controller;

import com.example.srp.pojo.dto.FiberDataDto;
import com.example.srp.pojo.dto.ImageDetailDto;
import com.example.srp.pojo.dto.ImageDetailQueryAllDto;
import com.example.srp.pojo.dto.ImageDetailQueryDto;
import com.example.srp.pojo.vo.FiberDataVo;
import com.example.srp.pojo.vo.ImageDetailVo;
import com.example.srp.result.PageResult;
import com.example.srp.result.Result;
import com.example.srp.service.FiberDataService;
import com.example.srp.service.ImageDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/function")
public class ImageDetailController {


    @Autowired
    private ImageDetailService imageDetailService;

    @Autowired
    private FiberDataService fiberDataService;

    @PostMapping("/history")
    public Result<PageResult> getImageDetail(@RequestBody  ImageDetailQueryDto imageDetailQueryDto){
        log.info("图片分析历史记录：{}",imageDetailQueryDto);
        PageResult pageResult = imageDetailService.pageQuery(imageDetailQueryDto);
        System.out.print(pageResult);
        return Result.success(pageResult);

    }

    @DeleteMapping("/history/{id}")
    public Result deleteDetail(@PathVariable Long id){
        log.info("删除记录{}",id);
        imageDetailService.deleteDetail(id);
        return Result.success();

    }

    @PostMapping("/fiberdata")
    public Result<PageResult> getAllImageDetail(@RequestBody ImageDetailQueryAllDto imageDetailQueryAllDto){
        log.info("纤维数据集：{}",imageDetailQueryAllDto);
        PageResult pageResult = fiberDataService.pageQueryAll(imageDetailQueryAllDto);
        System.out.print(pageResult);
         return Result.success(pageResult);
    }

    @PostMapping("/addFiberdata")
    public Result addNewRecord(@RequestParam("file") MultipartFile file,@RequestParam("detail") String detail){
        log.info("添加新纪录：{}",detail);
       fiberDataService.addNewRecord(file,detail);
        return Result.success();
    }

    @DeleteMapping("/fiber/{id}")
    public Result deleteFiberData(@PathVariable Long id){
        log.info("删除记录：{}",id);
        fiberDataService.deleteFiberData(id);
        return Result.success();
    }
}
