package com.theta360.automaticfaceblur.network.model.values;

public enum Status {
    SHOOTING("shooting"),
    BLURRING("blurring"),
    IDLE("idle"),;

    private final String mStatus;

    Status(String status) {
        this.mStatus = status;
    }

    @Override
    public String toString() {
        return this.mStatus;
    }
}
