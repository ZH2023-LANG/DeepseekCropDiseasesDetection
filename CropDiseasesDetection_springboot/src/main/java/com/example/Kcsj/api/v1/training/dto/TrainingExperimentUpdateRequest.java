package com.example.Kcsj.api.v1.training.dto;

public class TrainingExperimentUpdateRequest {
    private String experimentName;
    private String datasetName;
    private String paramsJson;
    private String resultJson;
    private String remark;
    private Double map50;
    private Double map5095;
    private Double trainLoss;
    private Double valLoss;

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getMap50() {
        return map50;
    }

    public void setMap50(Double map50) {
        this.map50 = map50;
    }

    public Double getMap5095() {
        return map5095;
    }

    public void setMap5095(Double map5095) {
        this.map5095 = map5095;
    }

    public Double getTrainLoss() {
        return trainLoss;
    }

    public void setTrainLoss(Double trainLoss) {
        this.trainLoss = trainLoss;
    }

    public Double getValLoss() {
        return valLoss;
    }

    public void setValLoss(Double valLoss) {
        this.valLoss = valLoss;
    }
}
