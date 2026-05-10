package com.example.Kcsj.api.v1.training.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DatasetImportCreateRequest {
    @NotBlank(message = "datasetName不能为空")
    private String datasetName;

    @NotBlank(message = "sourceType不能为空")
    private String sourceType;

    @NotBlank(message = "sourcePath不能为空")
    private String sourcePath;

    @NotNull(message = "sampleCount不能为空")
    @Min(value = 0, message = "sampleCount不能小于0")
    private Integer sampleCount;

    @NotNull(message = "classCount不能为空")
    @Min(value = 1, message = "classCount至少1")
    private Integer classCount;

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getClassCount() {
        return classCount;
    }

    public void setClassCount(Integer classCount) {
        this.classCount = classCount;
    }
}
