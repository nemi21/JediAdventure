package com.nehemiah.jediadventure.state;

public class GameState {

    public enum TrainingApproach {
        NOT_CHOSEN,
        OBSERVE_FIRST,
        TRUST_THE_FORCE
    }

    private TrainingApproach trainingApproach;
    private boolean trainingCompleted;

    public GameState() {
        reset();
    }

    public void reset() {
        trainingApproach =
                TrainingApproach.NOT_CHOSEN;

        trainingCompleted = false;
    }

    public TrainingApproach getTrainingApproach() {
        return trainingApproach;
    }

    public void setTrainingApproach(
            TrainingApproach trainingApproach) {

        this.trainingApproach = trainingApproach;
    }

    public boolean isTrainingCompleted() {
        return trainingCompleted;
    }

    public void completeTraining() {
        trainingCompleted = true;
    }
}