package com.example.Kcsj.api.v1.training.dto;

import javax.validation.constraints.NotBlank;

public class TrainingJobCreateRequest {
    @NotBlank(message = "jobName不能为空")
    private String jobName;

    @NotBlank(message = "datasetName不能为空")
    private String datasetName;

    @NotBlank(message = "trainParamsJson不能为空")
    private String trainParamsJson;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getTrainParamsJson() {
        return trainParamsJson;
    }

    public void setTrainParamsJson(String trainParamsJson) {
        this.trainParamsJson = trainParamsJson;
    }
}
