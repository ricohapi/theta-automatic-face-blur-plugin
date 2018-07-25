package com.theta360.automaticfaceblur.network.model.commands;

/**
 * CommandsName
 */
public enum CommandsName {
    TAKE_PICTURE("camera.takePicture"),
    GET_STATUS("camera.getStatus"),
    COMMANDS_EXECUTE("camera.commands.execute"),
    SET_OPTIONS("camera.setOptions"),
    GET_OPTIONS("camera.getOptions"),
    GET_LIVE_PREVIEW("camera.getLivePreview"),
    START_LIVE_PREVIEW("camera.startLivePreview"),
    UNKNOWN("unknown"),;

    private final String mCommands;

    CommandsName(final String commands) {
        this.mCommands = commands;
    }

    public static CommandsName getValue(final String name) {
        for (CommandsName commandsName : CommandsName.values()) {
            if (commandsName.toString().equals(name)) {
                return commandsName;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.mCommands;
    }
}
