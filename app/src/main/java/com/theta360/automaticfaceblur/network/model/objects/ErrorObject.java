package com.theta360.automaticfaceblur.network.model.objects;

import com.google.gson.annotations.SerializedName;
import com.theta360.automaticfaceblur.network.model.values.Errors;

/**
 * ErrorObject
 */
public class ErrorObject {
    @SerializedName("code")
    String mCode;

    @SerializedName("message")
    String mMessage;

    public ErrorObject(Errors errors) {
        this.mCode = errors.getCode();
        this.mMessage = errors.getMessage();
    }
}
