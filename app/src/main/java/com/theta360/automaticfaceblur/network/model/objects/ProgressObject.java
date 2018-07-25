package com.theta360.automaticfaceblur.network.model.objects;

import com.google.gson.annotations.SerializedName;

/**
 * ProgressObject
 */
public class ProgressObject {
    @SerializedName("completion")
    Number mCompletion;

    public ProgressObject(Number completion) {
        this.mCompletion = completion;
    }

    public Number getCompletion() {
        return mCompletion;
    }
}
