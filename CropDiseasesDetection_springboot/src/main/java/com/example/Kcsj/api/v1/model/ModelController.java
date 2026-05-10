package com.example.Kcsj.api.v1.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.model.dto.ModelCreateRequest;
import com.example.Kcsj.api.v1.model.dto.ModelUpdateRequest;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.entity.ModelRegistry;
import com.example.Kcsj.service.ModelRegistryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/v1/models")
@Validated
public class ModelController {
    private final ModelRegistryService modelRegistryService;

    public ModelController(ModelRegistryService modelRegistryService) {
        this.modelRegistryService = modelRegistryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModelRegistry> create(@Valid @RequestBody ModelCreateRequest request) {
        return ApiResponse.success(modelRegistryService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<Page<ModelRegistry>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String modelName) {
        return ApiResponse.success(modelRegistryService.list(pageNum, pageSize, modelName));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMON')")
    public ApiResponse<ModelRegistry> getById(@PathVariable Long id) {
        return ApiResponse.success(modelRegistryService.getById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModelRegistry> update(@PathVariable Long id, @RequestBody ModelUpdateRequest request) {
        return ApiResponse.success(modelRegistryService.update(id, request));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        modelRegistryService.enable(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        modelRegistryService.disable(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        modelRegistryService.setDefault(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/health-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModelRegistry> healthCheck(@PathVariable Long id) {
        return ApiResponse.success(modelRegistryService.healthCheck(id));
    }
}
