package com.theta360.automaticfaceblur.network.model.values;

/**
 * State
 */
public enum State {
    DONE("done"),
    IN_PROGRESS("inProgress"),
    ERROR("error"),;

    private final String mState;

    State(final String state) {
        this.mState = state;
    }

    @Override
    public String toString() {
        return this.mState;
    }
}
