package com.example.Kcsj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentCompareResponse;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentCreateRequest;
import com.example.Kcsj.api.v1.training.dto.TrainingExperimentUpdateRequest;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.entity.TrainingExperiment;
import com.example.Kcsj.mapper.TrainingExperimentMapper;
import com.example.Kcsj.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TrainingExperimentService {
    private final TrainingExperimentMapper trainingExperimentMapper;
    private final AuditLogService auditLogService;

    public TrainingExperimentService(TrainingExperimentMapper trainingExperimentMapper,
                                     AuditLogService auditLogService) {
        this.trainingExperimentMapper = trainingExperimentMapper;
        this.auditLogService = auditLogService;
    }

    public TrainingExperiment create(TrainingExperimentCreateRequest request) {
        TrainingExperiment experiment = new TrainingExperiment();
        experiment.setExperimentNo("EXP" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        experiment.setExperimentName(request.getExperimentName());
        experiment.setDatasetName(request.getDatasetName());
        experiment.setParamsJson(request.getParamsJson());
        experiment.setResultJson(defaultIfNull(request.getResultJson(), "{}"));
        experiment.setRemark(defaultIfNull(request.getRemark(), ""));
        experiment.setOwnerId(SecurityUtils.currentUserId());
        experiment.setMap50(request.getMap50());
        experiment.setMap5095(request.getMap5095());
        experiment.setTrainLoss(request.getTrainLoss());
        experiment.setValLoss(request.getValLoss());
        experiment.setCreatedAt(LocalDateTime.now());
        experiment.setUpdatedAt(LocalDateTime.now());
        trainingExperimentMapper.insert(experiment);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "EXPERIMENT_CREATE", "TRAINING_EXPERIMENT", experiment.getExperimentNo(), request.getExperimentName());
        return experiment;
    }

    public TrainingExperiment getByNo(String experimentNo) {
        TrainingExperiment experiment = trainingExperimentMapper.selectByExperimentNo(experimentNo);
        if (experiment == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "experiment not found");
        }
        checkOwner(experiment);
        return experiment;
    }

    public Page<TrainingExperiment> list(int pageNum, int pageSize, String experimentName, String datasetName) {
        LambdaQueryWrapper<TrainingExperiment> wrapper = new LambdaQueryWrapper<TrainingExperiment>();
        if (!SecurityUtils.isAdmin()) {
            wrapper.eq(TrainingExperiment::getOwnerId, SecurityUtils.currentUserId());
        }
        if (StringUtils.hasText(experimentName)) {
            wrapper.like(TrainingExperiment::getExperimentName, experimentName);
        }
        if (StringUtils.hasText(datasetName)) {
            wrapper.like(TrainingExperiment::getDatasetName, datasetName);
        }
        wrapper.orderByDesc(TrainingExperiment::getCreatedAt);
        return trainingExperimentMapper.selectPage(new Page<TrainingExperiment>(pageNum, pageSize), wrapper);
    }

    public TrainingExperiment update(String experimentNo, TrainingExperimentUpdateRequest request) {
        TrainingExperiment experiment = getByNo(experimentNo);
        if (request.getExperimentName() != null) {
            experiment.setExperimentName(request.getExperimentName());
        }
        if (request.getDatasetName() != null) {
            experiment.setDatasetName(request.getDatasetName());
        }
        if (request.getParamsJson() != null) {
            experiment.setParamsJson(request.getParamsJson());
        }
        if (request.getResultJson() != null) {
            experiment.setResultJson(request.getResultJson());
        }
        if (request.getRemark() != null) {
            experiment.setRemark(request.getRemark());
        }
        if (request.getMap50() != null) {
            experiment.setMap50(request.getMap50());
        }
        if (request.getMap5095() != null) {
            experiment.setMap5095(request.getMap5095());
        }
        if (request.getTrainLoss() != null) {
            experiment.setTrainLoss(request.getTrainLoss());
        }
        if (request.getValLoss() != null) {
            experiment.setValLoss(request.getValLoss());
        }
        experiment.setUpdatedAt(LocalDateTime.now());
        trainingExperimentMapper.updateById(experiment);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "EXPERIMENT_UPDATE", "TRAINING_EXPERIMENT", experimentNo, "update");
        return experiment;
    }

    public void delete(String experimentNo) {
        TrainingExperiment experiment = getByNo(experimentNo);
        trainingExperimentMapper.deleteById(experiment.getId());
        auditLogService.tryLog(SecurityUtils.currentUserId(), "EXPERIMENT_DELETE", "TRAINING_EXPERIMENT", experimentNo, "delete");
    }

    public TrainingExperimentCompareResponse compare(String leftNo, String rightNo) {
        TrainingExperiment left = getByNo(leftNo);
        TrainingExperiment right = getByNo(rightNo);
        TrainingExperimentCompareResponse response = new TrainingExperimentCompareResponse();
        response.setLeft(left);
        response.setRight(right);
        TrainingExperimentCompareResponse.Diff diff = new TrainingExperimentCompareResponse.Diff();
        diff.setMap50Delta(delta(left.getMap50(), right.getMap50()));
        diff.setMap5095Delta(delta(left.getMap5095(), right.getMap5095()));
        diff.setTrainLossDelta(delta(left.getTrainLoss(), right.getTrainLoss()));
        diff.setValLossDelta(delta(left.getValLoss(), right.getValLoss()));
        response.setDiff(diff);
        return response;
    }

    private Double delta(Double left, Double right) {
        if (left == null || right == null) {
            return null;
        }
        return left - right;
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void checkOwner(TrainingExperiment experiment) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!experiment.getOwnerId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "no permission for this experiment");
        }
    }
}
