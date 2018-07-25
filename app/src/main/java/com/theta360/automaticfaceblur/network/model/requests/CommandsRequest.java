package com.theta360.automaticfaceblur.network.model.requests;

import com.google.gson.annotations.SerializedName;
import com.theta360.automaticfaceblur.network.model.commands.CommandsName;

/**
 * CommandsRequest
 */
public class CommandsRequest {
    @SerializedName("name")
    String mName;

    @SerializedName("parameters")
    Object mParameters;

    private CommandsRequest(CommandsName commandsName) {
        this.mName = commandsName.toString();
    }

    private CommandsRequest(CommandsName commandsName, Object parameters) {
        this.mName = commandsName.toString();
        this.mParameters = parameters;
    }

    public static CommandsRequest cameraTakePicture() {
        CommandsRequest commandsRequest = new CommandsRequest(CommandsName.TAKE_PICTURE);
        return commandsRequest;
    }

    public CommandsName getCommandsName() {
        return CommandsName.getValue(mName);
    }

}
