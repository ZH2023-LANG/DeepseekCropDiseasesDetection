package com.example.Kcsj.api.v1.training.dto;

import com.example.Kcsj.entity.TrainingExperiment;

public class TrainingExperimentCompareResponse {
    private TrainingExperiment left;
    private TrainingExperiment right;
    private Diff diff;

    public TrainingExperiment getLeft() {
        return left;
    }

    public void setLeft(TrainingExperiment left) {
        this.left = left;
    }

    public TrainingExperiment getRight() {
        return right;
    }

    public void setRight(TrainingExperiment right) {
        this.right = right;
    }

    public Diff getDiff() {
        return diff;
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }

    public static class Diff {
        private Double map50Delta;
        private Double map5095Delta;
        private Double trainLossDelta;
        private Double valLossDelta;

        public Double getMap50Delta() {
            return map50Delta;
        }

        public void setMap50Delta(Double map50Delta) {
            this.map50Delta = map50Delta;
        }

        public Double getMap5095Delta() {
            return map5095Delta;
        }

        public void setMap5095Delta(Double map5095Delta) {
            this.map5095Delta = map5095Delta;
        }

        public Double getTrainLossDelta() {
            return trainLossDelta;
        }

        public void setTrainLossDelta(Double trainLossDelta) {
            this.trainLossDelta = trainLossDelta;
        }

        public Double getValLossDelta() {
            return valLossDelta;
        }

        public void setValLossDelta(Double valLossDelta) {
            this.valLossDelta = valLossDelta;
        }
    }
}
