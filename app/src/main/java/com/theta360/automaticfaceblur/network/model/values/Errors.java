package com.theta360.automaticfaceblur.network.model.values;

/**
 * Errors
 */
public enum Errors {
    UNKNOWN_COMMAND("unknownCommand", "Command executed is unknown.", -1),
    DISABLED_COMMAND("disabledCommand", "Command executed is currently disabled.", 0),
    MISSING_PARAMETER("missingParameter", "Any required parameter is not specified.", 1),
    INVALID_PARAMETER_NAME("invalidParameterName",
            "Any input parameter or option name is unrecognized or supported.", -1),
    INVALID_SESSION_ID("invalidSessionId", "The sessionId is invalid.", -1),
    INVALID_PARAMETER_VALUE("invalidParameterValue",
            "Any input parameter or option name is recognized, but its value is invalid.", 2),
    TOO_MANY_PARAMETERS("tooManyParameters", "Too many parameters are set.", -1),
    NO_FREE_SPACE("noFreeSpace", "Free space unavailable.", -1),
    CORRUPTED_FILE("corruptedFile", "The file is corrupt.", -1),
    CAMERA_IN_EXCLUSIVE_USE("cameraInExclusiveUse",
            "States is already in exclusive use, new session can’t be started.", -1),
    POWER_OFF_SEQUENCE_RUNNING("powerOffSequenceRunning", "Power off sequence running.", 3),
    INVALID_FILE_FORMAT("invalidFileFormat", "File format is invalid.", 4),
    SERVICE_UNAVAILABLE("serviceUnavailable", "Service Unavailable.", 5),
    CANCELED_SHOOTING("canceledShooting", "shooting is canceled.", -1),
    CANCELED_CONVERSION("canceledConversion", "conversion is canceled.", -1),
    DEVICE_BUSY("deviceBusy", "Device Busy.", 6),
    UPLOAD_ERROR("uploadError", "Camera failed to upload the file.", -1),
    UNEXPECTED("unexpected", "unexpected error occurred.", 7),;

    private final String mCode;
    private final String mMessage;
    private final int mCodeBle;

    Errors(final String code, final String message, final int codeBle) {
        this.mCode = code;
        this.mMessage = message;
        this.mCodeBle = codeBle;
    }

    public static Errors getValue(int _error) {
        for (Errors errors : values()) {
            if (errors.getBleValue() == _error) {
                return errors;
            }
        }
        return null;
    }

    public String getCode() {
        return this.mCode;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public int getBleValue() {
        return this.mCodeBle;
    }
}
