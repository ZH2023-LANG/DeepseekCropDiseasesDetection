package com.example.Kcsj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.model.dto.ModelCreateRequest;
import com.example.Kcsj.api.v1.model.dto.ModelUpdateRequest;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.entity.ModelRegistry;
import com.example.Kcsj.mapper.ModelRegistryMapper;
import com.example.Kcsj.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class ModelRegistryService {
    private final ModelRegistryMapper modelRegistryMapper;
    private final AuditLogService auditLogService;

    public ModelRegistryService(ModelRegistryMapper modelRegistryMapper, AuditLogService auditLogService) {
        this.modelRegistryMapper = modelRegistryMapper;
        this.auditLogService = auditLogService;
    }

    public ModelRegistry create(ModelCreateRequest request) {
        LambdaQueryWrapper<ModelRegistry> uniqueCheck = new LambdaQueryWrapper<ModelRegistry>()
            .eq(ModelRegistry::getModelName, request.getModelName())
            .eq(ModelRegistry::getVersion, request.getVersion());
        if (modelRegistryMapper.selectCount(uniqueCheck) > 0) {
            throw new ApiException(ErrorCode.CONFLICT, "modelName + version already exists");
        }

        ModelRegistry model = new ModelRegistry();
        model.setModelName(request.getModelName());
        model.setVersion(request.getVersion());
        model.setFilePath(request.getFilePath());
        model.setFileSize(request.getFileSize());
        model.setSha256(request.getSha256());
        model.setFrameworkType(request.getFrameworkType());
        model.setEnabled(defaultZeroOne(request.getEnabled(), 1));
        model.setIsDefault(defaultZeroOne(request.getIsDefault(), 0));
        model.setRemark(request.getRemark());
        model.setHealthStatus("UNKNOWN");
        model.setHealthMessage("not checked");
        model.setCreatedBy(SecurityUtils.currentUserId());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.insert(model);
        if (model.getIsDefault() == 1) {
            setDefault(model.getId());
        }
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_CREATE", "MODEL", String.valueOf(model.getId()), model.getModelName());
        return model;
    }

    public ModelRegistry getById(Long id) {
        ModelRegistry model = modelRegistryMapper.selectById(id);
        if (model == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "model not found");
        }
        return model;
    }

    public Page<ModelRegistry> list(int pageNum, int pageSize, String modelName) {
        LambdaQueryWrapper<ModelRegistry> wrapper = new LambdaQueryWrapper<ModelRegistry>();
        if (StringUtils.hasText(modelName)) {
            wrapper.like(ModelRegistry::getModelName, modelName);
        }
        wrapper.orderByDesc(ModelRegistry::getCreatedAt);
        return modelRegistryMapper.selectPage(new Page<ModelRegistry>(pageNum, pageSize), wrapper);
    }

    public ModelRegistry update(Long id, ModelUpdateRequest request) {
        ModelRegistry model = getById(id);
        if (StringUtils.hasText(request.getFilePath())) {
            model.setFilePath(request.getFilePath());
        }
        if (request.getFileSize() != null) {
            model.setFileSize(request.getFileSize());
        }
        if (StringUtils.hasText(request.getSha256())) {
            model.setSha256(request.getSha256());
        }
        if (StringUtils.hasText(request.getFrameworkType())) {
            model.setFrameworkType(request.getFrameworkType());
        }
        if (request.getEnabled() != null) {
            model.setEnabled(defaultZeroOne(request.getEnabled(), model.getEnabled()));
        }
        if (request.getIsDefault() != null) {
            model.setIsDefault(defaultZeroOne(request.getIsDefault(), model.getIsDefault()));
        }
        if (request.getRemark() != null) {
            model.setRemark(request.getRemark());
        }
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.updateById(model);
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            setDefault(id);
        }
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_UPDATE", "MODEL", String.valueOf(id), "update");
        return model;
    }

    public void enable(Long id) {
        ModelRegistry model = getById(id);
        model.setEnabled(1);
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.updateById(model);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_ENABLE", "MODEL", String.valueOf(id), "enable");
    }

    public void disable(Long id) {
        ModelRegistry model = getById(id);
        model.setEnabled(0);
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.updateById(model);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_DISABLE", "MODEL", String.valueOf(id), "disable");
    }

    public void setDefault(Long id) {
        ModelRegistry model = getById(id);
        Page<ModelRegistry> page = modelRegistryMapper.selectPage(new Page<ModelRegistry>(1, 1000), new LambdaQueryWrapper<ModelRegistry>());
        for (ModelRegistry m : page.getRecords()) {
            if (m.getIsDefault() != null && m.getIsDefault() == 1 && !m.getId().equals(id)) {
                m.setIsDefault(0);
                m.setUpdatedAt(LocalDateTime.now());
                modelRegistryMapper.updateById(m);
            }
        }
        model.setIsDefault(1);
        model.setEnabled(1);
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.updateById(model);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_SET_DEFAULT", "MODEL", String.valueOf(id), "set default");
    }

    public ModelRegistry healthCheck(Long id) {
        ModelRegistry model = getById(id);
        File file = new File(model.getFilePath());
        if (!file.exists()) {
            model.setHealthStatus("DOWN");
            model.setHealthMessage("file not exists");
        } else if (!file.isFile()) {
            model.setHealthStatus("DOWN");
            model.setHealthMessage("path is not a file");
        } else if (!file.canRead()) {
            model.setHealthStatus("DOWN");
            model.setHealthMessage("file cannot read");
        } else {
            model.setHealthStatus("UP");
            model.setHealthMessage("file exists and readable");
        }
        model.setUpdatedAt(LocalDateTime.now());
        modelRegistryMapper.updateById(model);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "MODEL_HEALTH_CHECK", "MODEL", String.valueOf(id), model.getHealthStatus());
        return model;
    }

    private int defaultZeroOne(Integer value, Integer fallback) {
        if (value == null) {
            return fallback == null ? 0 : fallback;
        }
        return value == 0 ? 0 : 1;
    }
}
