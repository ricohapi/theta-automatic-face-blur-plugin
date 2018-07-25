package com.theta360.automaticfaceblur.network.model.responses;

import com.google.gson.annotations.SerializedName;
import com.theta360.automaticfaceblur.network.model.values.Status;

public class StatusResponse {
    @SerializedName("status")
    String mStatus;

    public StatusResponse(Status status) {
        this.mStatus = status.toString();
    }

    public String getStatus() {
        return this.mStatus;
    }
}
