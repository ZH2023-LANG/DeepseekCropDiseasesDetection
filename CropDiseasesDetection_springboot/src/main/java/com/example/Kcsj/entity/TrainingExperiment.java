package com.example.Kcsj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("training_experiment")
public class TrainingExperiment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String experimentNo;
    private String experimentName;
    private String datasetName;
    private String paramsJson;
    private String resultJson;
    private String remark;
    private Integer ownerId;
    private Double map50;
    private Double map5095;
    private Double trainLoss;
    private Double valLoss;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExperimentNo() {
        return experimentNo;
    }

    public void setExperimentNo(String experimentNo) {
        this.experimentNo = experimentNo;
    }

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

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
