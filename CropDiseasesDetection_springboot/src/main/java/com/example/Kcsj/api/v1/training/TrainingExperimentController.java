package com.example.Kcsj.api.v1.training;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentCompareResponse;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentCreateRequest;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentUpdateRequest;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.entity.TrainingExperiment;
import com.example.Kcsj.service.TrainingExperimentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/training/experiments")
@Validated
public class TrainingExperimentController {
    private final TrainingExperimentService trainingExperimentService;

    public TrainingExperimentController(TrainingExperimentService trainingExperimentService) {
        this.trainingExperimentService = trainingExperimentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<TrainingExperiment> create(@Valid @RequestBody TrainingExperimentCreateRequest request) {
        return ApiResponse.success(trainingExperimentService.create(request));
    }

    @GetMapping("/{experimentNo}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<TrainingExperiment> getByNo(@PathVariable String experimentNo) {
        return ApiResponse.success(trainingExperimentService.getByNo(experimentNo));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Page<TrainingExperiment>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String experimentName,
        @RequestParam(required = false) String datasetName) {
        return ApiResponse.success(trainingExperimentService.list(pageNum, pageSize, experimentName, datasetName));
    }

    @PatchMapping("/{experimentNo}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<TrainingExperiment> update(@PathVariable String experimentNo,
                                                  @RequestBody TrainingExperimentUpdateRequest request) {
        return ApiResponse.success(trainingExperimentService.update(experimentNo, request));
    }

    @DeleteMapping("/{experimentNo}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Void> delete(@PathVariable String experimentNo) {
        trainingExperimentService.delete(experimentNo);
        return ApiResponse.success(null);
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<TrainingExperimentCompareResponse> compare(@RequestParam String leftNo,
                                                                  @RequestParam String rightNo) {
        return ApiResponse.success(trainingExperimentService.compare(leftNo, rightNo));
    }
}
