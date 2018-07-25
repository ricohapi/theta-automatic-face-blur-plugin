package com.theta360.automaticfaceblur.network.model.responses;

import com.google.gson.annotations.SerializedName;
import com.theta360.automaticfaceblur.network.model.commands.CommandsName;
import com.theta360.automaticfaceblur.network.model.objects.ErrorObject;
import com.theta360.automaticfaceblur.network.model.objects.ProgressObject;
import com.theta360.automaticfaceblur.network.model.values.State;

/**
 * CommandsResponse
 */
public class CommandsResponse {
    @SerializedName("name")
    String mName;

    @SerializedName("state")
    String mState;

    @SerializedName("id")
    String mId;

    @SerializedName("error")
    ErrorObject mError;

    @SerializedName("progress")
    ProgressObject mProgress;

    public CommandsResponse(CommandsName commandsName, State state) {
        this.mName = commandsName.toString();
        this.mState = state.toString();
    }

    public String getName() {
        return this.mName;
    }

    public String getState() {
        return this.mState;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public ErrorObject getError() {
        return this.mError;
    }

    public void setError(ErrorObject error) {
        this.mError = error;
    }

    public ProgressObject getProgress() {
        return this.mProgress;
    }

    public void setProgress(ProgressObject progress) {
        this.mProgress = progress;
    }
}
