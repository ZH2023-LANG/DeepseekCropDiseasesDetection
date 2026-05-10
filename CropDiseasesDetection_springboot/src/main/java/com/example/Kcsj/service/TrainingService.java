package com.example.Kcsj.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.training.dto.DatasetImportCreateRequest;
import com.example.Kcsj.api.v1.training.dto.TrainingJobCreateRequest;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.entity.DatasetImportRecord;
import com.example.Kcsj.entity.TrainingArtifact;
import com.example.Kcsj.entity.TrainingJob;
import com.example.Kcsj.entity.TrainingMetric;
import com.example.Kcsj.mapper.DatasetImportRecordMapper;
import com.example.Kcsj.mapper.TrainingArtifactMapper;
import com.example.Kcsj.mapper.TrainingJobMapper;
import com.example.Kcsj.mapper.TrainingMetricMapper;
import com.example.Kcsj.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrainingService {
    private final DatasetImportRecordMapper datasetImportRecordMapper;
    private final TrainingJobMapper trainingJobMapper;
    private final TrainingMetricMapper trainingMetricMapper;
    private final TrainingArtifactMapper trainingArtifactMapper;
    private final AuditLogService auditLogService;

    public TrainingService(DatasetImportRecordMapper datasetImportRecordMapper,
                           TrainingJobMapper trainingJobMapper,
                           TrainingMetricMapper trainingMetricMapper,
                           TrainingArtifactMapper trainingArtifactMapper,
                           AuditLogService auditLogService) {
        this.datasetImportRecordMapper = datasetImportRecordMapper;
        this.trainingJobMapper = trainingJobMapper;
        this.trainingMetricMapper = trainingMetricMapper;
        this.trainingArtifactMapper = trainingArtifactMapper;
        this.auditLogService = auditLogService;
    }

    public DatasetImportRecord createDatasetImportRecord(DatasetImportCreateRequest request) {
        DatasetImportRecord record = new DatasetImportRecord();
        record.setDatasetName(request.getDatasetName());
        record.setSourceType(request.getSourceType());
        record.setSourcePath(request.getSourcePath());
        record.setSampleCount(request.getSampleCount());
        record.setClassCount(request.getClassCount());
        record.setImportedBy(SecurityUtils.currentUserId());
        record.setCreatedAt(LocalDateTime.now());
        datasetImportRecordMapper.insert(record);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "DATASET_IMPORT", "DATASET", String.valueOf(record.getId()), record.getDatasetName());
        return record;
    }

    public Page<DatasetImportRecord> listDatasetImportRecords(int pageNum, int pageSize, String datasetName) {
        LambdaQueryWrapper<DatasetImportRecord> wrapper = new LambdaQueryWrapper<DatasetImportRecord>();
        if (StringUtils.hasText(datasetName)) {
            wrapper.like(DatasetImportRecord::getDatasetName, datasetName);
        }
        wrapper.orderByDesc(DatasetImportRecord::getCreatedAt);
        return datasetImportRecordMapper.selectPage(new Page<DatasetImportRecord>(pageNum, pageSize), wrapper);
    }

    public TrainingJob createTrainingJob(TrainingJobCreateRequest request) {
        TrainingJob job = new TrainingJob();
        job.setJobNo("JOB" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        job.setJobName(request.getJobName());
        job.setDatasetName(request.getDatasetName());
        job.setStatus("PENDING");
        job.setOwnerId(SecurityUtils.currentUserId());
        job.setTrainParamsJson(request.getTrainParamsJson());
        job.setResultSummaryJson("{}");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        trainingJobMapper.insert(job);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "TRAINING_CREATE", "TRAINING_JOB", job.getJobNo(), request.getJobName());
        return job;
    }

    public TrainingJob getTrainingJob(String jobNo) {
        TrainingJob job = trainingJobMapper.selectByJobNo(jobNo);
        if (job == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "training job not found");
        }
        checkOwner(job);
        return job;
    }

    public Page<TrainingJob> listTrainingJobs(int pageNum, int pageSize, String status) {
        LambdaQueryWrapper<TrainingJob> wrapper = new LambdaQueryWrapper<TrainingJob>();
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(TrainingJob::getOwnerId, SecurityUtils.currentUserId());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TrainingJob::getStatus, status);
        }
        wrapper.orderByDesc(TrainingJob::getCreatedAt);
        return trainingJobMapper.selectPage(new Page<TrainingJob>(pageNum, pageSize), wrapper);
    }

    public List<TrainingMetric> listTrainingMetrics(String jobNo) {
        TrainingJob job = getTrainingJob(jobNo);
        LambdaQueryWrapper<TrainingMetric> wrapper = new LambdaQueryWrapper<TrainingMetric>()
            .eq(TrainingMetric::getJobNo, job.getJobNo())
            .orderByAsc(TrainingMetric::getEpochNo);
        return trainingMetricMapper.selectList(wrapper);
    }

    public Map<String, Object> getTrainingReport(String jobNo) {
        TrainingJob job = getTrainingJob(jobNo);
        List<TrainingMetric> metrics = listTrainingMetrics(jobNo);
        List<TrainingArtifact> artifacts = trainingArtifactMapper.selectList(
            new LambdaQueryWrapper<TrainingArtifact>().eq(TrainingArtifact::getJobNo, jobNo).orderByAsc(TrainingArtifact::getId)
        );
        Map<String, Object> report = new HashMap<String, Object>();
        report.put("jobNo", job.getJobNo());
        report.put("jobName", job.getJobName());
        report.put("status", job.getStatus());
        report.put("datasetName", job.getDatasetName());
        report.put("ownerId", job.getOwnerId());
        report.put("trainParams", parseToObject(job.getTrainParamsJson()));
        report.put("resultSummary", parseToObject(job.getResultSummaryJson()));
        report.put("metrics", metrics);
        report.put("artifacts", artifacts);
        report.put("createdAt", job.getCreatedAt());
        report.put("startedAt", job.getStartedAt());
        report.put("finishedAt", job.getFinishedAt());
        return report;
    }

    private Object parseToObject(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<String, Object>();
        }
        try {
            return JSON.parse(json);
        } catch (Exception ignore) {
            JSONObject fallback = new JSONObject();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private void checkOwner(TrainingJob job) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!job.getOwnerId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "no permission for this training job");
        }
    }
}
