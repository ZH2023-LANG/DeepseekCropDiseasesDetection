package com.example.Kcsj.controller;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.common.Result;
import com.example.Kcsj.entity.CameraRecords;
import com.example.Kcsj.mapper.CameraRecordsMapper;
import com.example.Kcsj.security.SecurityUtils;
import com.example.Kcsj.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/cameraRecords")
public class CameraRecordsController {
    @Resource
    CameraRecordsMapper cameraRecordsMapper;
    @Resource
    AuditLogService auditLogService;

    @GetMapping("/all")
    public Result<?> GetAll() {
        return Result.success(cameraRecordsMapper.selectList(null));
    }
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable int id) {
        System.out.println(id);
        return Result.success(cameraRecordsMapper.selectById(id));
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "") String search1,
                              @RequestParam(defaultValue = "") String search3,
                              @RequestParam(defaultValue = "") String search2) {
        LambdaQueryWrapper<CameraRecords> wrapper = Wrappers.<CameraRecords>lambdaQuery();
        wrapper.orderByDesc(CameraRecords::getStartTime);
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(CameraRecords::getUsername, search);
        }
        if (StrUtil.isNotBlank(search1)) {
            wrapper.like(CameraRecords::getStartTime, search1);
        }
        if (StrUtil.isNotBlank(search2)) {
            wrapper.like(CameraRecords::getWeight, search2);
        }
        if (StrUtil.isNotBlank(search3)) {
            wrapper.like(CameraRecords::getConf, search3);
        }
        Page<CameraRecords> Page = cameraRecordsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(Page);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable int id) {
        cameraRecordsMapper.deleteById(id);
        auditLogService.tryLog(safeUserId(), "CAMERA_RECORD_DELETE", "CAMERA_RECORD", String.valueOf(id), "legacy delete");
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updates(@RequestBody CameraRecords cameraRecords) {
        cameraRecordsMapper.updateById(cameraRecords);
        return Result.success();
    }


    @PostMapping
    public Result<?> save(@RequestBody CameraRecords cameraRecords) {
        System.out.println(cameraRecords);
        cameraRecordsMapper.insert(cameraRecords);
        return Result.success();
    }

    private Integer safeUserId() {
        try {
            return SecurityUtils.currentUserId();
        } catch (Exception ignore) {
            return null;
        }
    }
}
