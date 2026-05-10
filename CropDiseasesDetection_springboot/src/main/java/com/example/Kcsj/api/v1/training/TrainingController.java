package com.example.Kcsj.api.v1.training;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.training.dto.TrainingJobCreateRequest;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.entity.TrainingJob;
import com.example.Kcsj.entity.TrainingMetric;
import com.example.Kcsj.service.TrainingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/training")
@Validated
public class TrainingController {
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrainingJob> createJob(@Valid @RequestBody TrainingJobCreateRequest request) {
        return ApiResponse.success(trainingService.createTrainingJob(request));
    }

    @GetMapping("/jobs/{jobNo}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<TrainingJob> getJob(@PathVariable String jobNo) {
        return ApiResponse.success(trainingService.getTrainingJob(jobNo));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Page<TrainingJob>> listJobs(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String status) {
        return ApiResponse.success(trainingService.listTrainingJobs(pageNum, pageSize, status));
    }

    @GetMapping("/jobs/{jobNo}/metrics")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<List<TrainingMetric>> listMetrics(@PathVariable String jobNo) {
        return ApiResponse.success(trainingService.listTrainingMetrics(jobNo));
    }

    @GetMapping("/jobs/{jobNo}/report")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Map<String, Object>> report(@PathVariable String jobNo) {
        return ApiResponse.success(trainingService.getTrainingReport(jobNo));
    }
}
