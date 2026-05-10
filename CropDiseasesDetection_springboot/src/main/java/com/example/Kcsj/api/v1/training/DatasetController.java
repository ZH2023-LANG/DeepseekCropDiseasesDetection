package com.example.Kcsj.api.v1.training;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.training.dto.DatasetImportCreateRequest;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.entity.DatasetImportRecord;
import com.example.Kcsj.service.TrainingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/datasets")
@Validated
public class DatasetController {
    private final TrainingService trainingService;

    public DatasetController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/import-records")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DatasetImportRecord> createImportRecord(@Valid @RequestBody DatasetImportCreateRequest request) {
        return ApiResponse.success(trainingService.createDatasetImportRecord(request));
    }

    @GetMapping("/import-records")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Page<DatasetImportRecord>> listImportRecords(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String datasetName) {
        return ApiResponse.success(trainingService.listDatasetImportRecords(pageNum, pageSize, datasetName));
    }
}
