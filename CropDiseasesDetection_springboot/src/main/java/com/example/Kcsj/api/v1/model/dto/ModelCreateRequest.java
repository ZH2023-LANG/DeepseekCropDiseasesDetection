package com.example.Kcsj.api.v1.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ModelCreateRequest {
    @NotBlank(message = "modelName不能为空")
    private String modelName;

    @NotBlank(message = "version不能为空")
    private String version;

    @NotBlank(message = "filePath不能为空")
    private String filePath;

    @NotBlank(message = "frameworkType不能为空")
    private String frameworkType;

    @NotNull(message = "enabled不能为空")
    private Integer enabled;

    private Integer isDefault;
    private Long fileSize;
    private String sha256;
    private String remark;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFrameworkType() {
        return frameworkType;
    }

    public void setFrameworkType(String frameworkType) {
        this.frameworkType = frameworkType;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
