package com.example.Kcsj.api.v1.task.dto;

import javax.validation.constraints.NotBlank;

public class BatchImageTaskCreateRequest {
    @NotBlank(message = "weight不能为空")
    private String weight;

    @NotBlank(message = "imgFolderUrl不能为空")
    private String imgFolderUrl;

    @NotBlank(message = "conf不能为空")
    private String conf;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImgFolderUrl() {
        return imgFolderUrl;
    }

    public void setImgFolderUrl(String imgFolderUrl) {
        this.imgFolderUrl = imgFolderUrl;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }
}
