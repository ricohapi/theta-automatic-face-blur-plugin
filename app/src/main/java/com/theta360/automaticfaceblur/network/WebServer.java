/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.theta360.automaticfaceblur.network;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.theta360.automaticfaceblur.network.async.AsyncHttpServerEx;
import com.theta360.automaticfaceblur.network.model.commands.CommandsName;
import com.theta360.automaticfaceblur.network.model.objects.ErrorObject;
import com.theta360.automaticfaceblur.network.model.requests.CommandsRequest;
import com.theta360.automaticfaceblur.network.model.responses.CommandsResponse;
import com.theta360.automaticfaceblur.network.model.responses.StatusResponse;
import com.theta360.automaticfaceblur.network.model.values.Errors;
import com.theta360.automaticfaceblur.network.model.values.State;

import org.json.JSONObject;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * WebServer
 */
public class WebServer {
    private static final int HTTP_PORT = 8888;
    private static final String COMMANDS = "/blur/commands/execute";
    private static final String PREVIEW_HTML = "index.html";
    private static final String TAKE_PICTURE_SCRIPT = "js/shooting.js";
    private static final String SET_OPTIONS_SCRIPT = "js/settings.js";
    private static final String GET_LIVE_PREVIEW_SCRIPT = "js/live-preview.js";
    private static final String JQUERY_SCRIPT = "js/jquery-1.12.2.min.js";
    private static final String THREE_SCRIPT = "js/three.min.js";
    private static final String ORBIT_CONTROL_SCRIPT = "js/ThetaControls.js";
    private static final String SPHERE_SCRIPT = "js/sphere.js";
    private static final String COMMON_CSS = "css/plugin.css";
    private static final String SLICK_SCRIPT = "js/slick.min.js";
    private static final String CLIENT_SCRIPT = "js/face-blur-client.js";
    private static final String JQUERY_1_8_3_SCRIPT = "js/jquery-1.8.3.min.js";
    private static final String BTN_MODE_AUTO = "img/btn-mode-auto-normal.png";
    private static final String BTN_MODE_ISO = "img/btn-mode-iso-normal.png";
    private static final String BTN_MODE_MANUAL = "img/btn-mode-manual-normal.png";
    private static final String BTN_MODE_SHUTTER = "img/btn-mode-shutter-normal.png";
    private static final String BTN_SHUTTER = "img/btn-shutte-normal.png";
    private static final String ICONWB_AUTO_CURRENT = "img/iconwb-auto-current.png";
    private static final String ICONWB_CLOUD_CURRENT = "img/iconwb-cloud-current.png";
    private static final String ICONWB_FLUORESCENT_D_CURRENT = "img/iconwb-fluorescent-d-current.png";
    private static final String ICONWB_FLUORESCENT_L_CURRENT = "img/iconwb-fluorescent-l-current.png";
    private static final String ICONWB_FLUORESCENT_N_CURRENT = "img/iconwb-fluorescent-n-current.png";
    private static final String ICONWB_FLUORESCENT_W_CURRENT = "img/iconwb-fluorescent-w-current.png";
    private static final String ICONWB_INCANDESCENT_1_CURRENT = "img/iconwb-incandescent-1-current.png";
    private static final String ICONWB_INCANDESCENT_2_CURRENT = "img/iconwb-incandescent-2-current.png";
    private static final String ICONWB_SHADE_CURRENT = "img/iconwb-shade-current.png";
    private static final String ICONWB_SUN_CURRENT = "img/iconwb-sun-current.png";

    private static final String HOST = "Host";
    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String CONTENT_TYPE_JPEG = "image/jpeg";
    private static final String NOSNIFF = "nosniff";
    private final Handler mWebDisconnectionHandler = new Handler();
    private Context mContext;
    private AsyncServer mAsyncServer;
    private AsyncHttpServerEx mAsyncHttpServer;
    private Callback mCallback;
    private String mHost;
    private int mAccessCounter = 0;
    private Runnable mWebDisconnectionRunnable = null;
    private Set<AsyncSocket> mFileAccessSocketSet = new HashSet<>();

    private HttpServerRequestCallback mCommandsRequestCallback = new HttpServerRequestCallback() {
        @Override
        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
            mHost = request.getHeaders().get(HOST);
            if (request.getBody() instanceof JSONObjectBody) {
                try {
                    JSONObject json = ((JSONObjectBody) request.getBody()).get();
                    CommandsRequest commandsRequest = new GsonBuilder().create().fromJson(
                            json.toString(), CommandsRequest.class);

                    CommandsName commandsName = commandsRequest.getCommandsName();
                    if (commandsName != null) {
                        mCallback.commandsRequest(response, commandsRequest);
                    } else {
                        sendMissingParameter(response, CommandsName.COMMANDS_EXECUTE);
                    }
                } catch (Exception e) {
                    sendUnknownCommand(response);
                }
            } else {
                sendUnknownCommand(response);
            }
        }
    };

    private AsyncHttpServerEx.Callback mHttpServerCallback = new AsyncHttpServerEx.Callback() {
        @Override
        public void isConnection(boolean isConnection, AsyncSocket socket) {
            if (mWebDisconnectionRunnable != null) {
                mWebDisconnectionHandler.removeCallbacks(mWebDisconnectionRunnable);
                mWebDisconnectionRunnable = null;
            }
            mWebDisconnectionHandler.removeCallbacksAndMessages(null);

            if (isConnection) {
                mAccessCounter++;
                Timber.d("connect web server. ctr=%d", mAccessCounter);
            } else {
                if (mAccessCounter > 0) {
                    mAccessCounter--;
                }
                mFileAccessSocketSet.remove(socket);

                Timber.d("disconnect web server. ctr=%d", mAccessCounter);
                Timber.d("fileAccess num=%d", mFileAccessSocketSet.size());

                if (mFileAccessSocketSet.size() == 0) {
                    Timber.d("fileAccess finished.");
                }

                mWebDisconnectionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mAccessCounter == 0) {
                            Timber.d("access finished.");
                        }
                    }
                };
                //file is transferred several times therefore interval below 500ms will be ignored
                mWebDisconnectionHandler.postDelayed(mWebDisconnectionRunnable, 500);
            }
        }
    };

    /**
     * Instantiates a new Web server.
     *
     * @param context the context
     * @param inetAddress the ip
     */
    public WebServer(@NonNull Context context, InetAddress inetAddress,
            @NonNull Callback callback) {
        this.mContext = context;
        this.mCallback = callback;

        mAsyncHttpServer = new AsyncHttpServerEx(mHttpServerCallback);
        mAsyncHttpServer.post(COMMANDS, mCommandsRequestCallback);
        mAsyncHttpServer.directory(mContext, "/", PREVIEW_HTML);
        mAsyncHttpServer.directory(mContext, "/" + TAKE_PICTURE_SCRIPT, TAKE_PICTURE_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + SET_OPTIONS_SCRIPT, SET_OPTIONS_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + GET_LIVE_PREVIEW_SCRIPT, GET_LIVE_PREVIEW_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + JQUERY_SCRIPT, JQUERY_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + THREE_SCRIPT, THREE_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + ORBIT_CONTROL_SCRIPT, ORBIT_CONTROL_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + SPHERE_SCRIPT, SPHERE_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + COMMON_CSS, COMMON_CSS);
        mAsyncHttpServer.directory(mContext, "/" + JQUERY_1_8_3_SCRIPT, JQUERY_1_8_3_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + SLICK_SCRIPT, SLICK_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + CLIENT_SCRIPT, CLIENT_SCRIPT);
        mAsyncHttpServer.directory(mContext, "/" + BTN_MODE_AUTO, BTN_MODE_AUTO);
        mAsyncHttpServer.directory(mContext, "/" + BTN_MODE_ISO, BTN_MODE_ISO);
        mAsyncHttpServer.directory(mContext, "/" + BTN_MODE_MANUAL, BTN_MODE_MANUAL);
        mAsyncHttpServer.directory(mContext, "/" + BTN_MODE_SHUTTER, BTN_MODE_SHUTTER);
        mAsyncHttpServer.directory(mContext, "/" + BTN_SHUTTER, BTN_SHUTTER);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_AUTO_CURRENT, ICONWB_AUTO_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_CLOUD_CURRENT, ICONWB_CLOUD_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_FLUORESCENT_D_CURRENT, ICONWB_FLUORESCENT_D_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_FLUORESCENT_L_CURRENT, ICONWB_FLUORESCENT_L_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_FLUORESCENT_N_CURRENT, ICONWB_FLUORESCENT_N_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_FLUORESCENT_W_CURRENT, ICONWB_FLUORESCENT_W_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_INCANDESCENT_1_CURRENT, ICONWB_INCANDESCENT_1_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_INCANDESCENT_2_CURRENT, ICONWB_INCANDESCENT_2_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_SHADE_CURRENT, ICONWB_SHADE_CURRENT);
        mAsyncHttpServer.directory(mContext, "/" + ICONWB_SUN_CURRENT, ICONWB_SUN_CURRENT);
        mAsyncServer = new AsyncServer();
        mAsyncServer.listen(inetAddress, HTTP_PORT, mAsyncHttpServer.getListenCallback());
    }

    public void stop() {
        if (mAsyncHttpServer != null) {
            mAsyncHttpServer.stop();
            mAsyncHttpServer = null;
        }
        if (mAsyncServer != null) {
            mAsyncServer.stop();
            mAsyncServer = null;
        }
    }

    public void sendCommandsResponse(@NonNull AsyncHttpServerResponse response,
            @NonNull CommandsResponse commandsResponse) {
        String json = new GsonBuilder().create().toJson(commandsResponse);

        if (commandsResponse.getProgress() != null) {
            Number completion = 100; //commandsResponse.getProgress().getCompletion();

            json = replaceCompletion(json, completion);
        }

        sendJson(response, json);
    }

    public void sendStatus(@NonNull AsyncHttpServerResponse response, @NonNull StatusResponse statusResponse) {
        sendJson(response, new GsonBuilder().create().toJson(statusResponse));
    }

    public void sendGetOptionsResponse(@NonNull AsyncHttpServerResponse response,
            @NonNull String optionsResponse) {
        sendJson(response, optionsResponse);
    }

    public void sendPreviewPicture(@NonNull AsyncHttpServerResponse response, byte[] data) {
        response.send(CONTENT_TYPE_JPEG, data);
    }

    public void sendUnknownCommand(@NonNull AsyncHttpServerResponse response) {
        CommandsResponse commandsResponse = new CommandsResponse(CommandsName.UNKNOWN, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.UNKNOWN_COMMAND));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(400);
        sendJson(res, json);
    }

    public void sendDisabledCommand(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.DISABLED_COMMAND));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(403);
        sendJson(res, json);
    }

    public void sendMissingParameter(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.MISSING_PARAMETER));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(400);
        sendJson(res, json);
    }

    public void sendInvalidParameterValue(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.INVALID_PARAMETER_VALUE));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(400);
        sendJson(res, json);
    }

    public void sendServiceUnavailable(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.SERVICE_UNAVAILABLE));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(503);
        sendJson(res, json);
    }

    public void sendNoFreeSpace(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.NO_FREE_SPACE));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(403);
        sendJson(res, json);
    }

    public void sendError(@NonNull AsyncHttpServerResponse response, Errors errors,
            CommandsName commandsName) {
        switch (errors) {
            case DISABLED_COMMAND:
                sendDisabledCommand(response, commandsName);
                break;
            case INVALID_PARAMETER_VALUE:
                sendInvalidParameterValue(response, commandsName);
                break;
            case NO_FREE_SPACE:
                sendNoFreeSpace(response, commandsName);
                break;
            case SERVICE_UNAVAILABLE:
                sendServiceUnavailable(response, commandsName);
                break;
            case UNEXPECTED:
                sendUnexpected(response, commandsName);
                break;
        }
    }

    private void sendUnexpected(@NonNull AsyncHttpServerResponse response,
            CommandsName commandsName) {
        CommandsResponse commandsResponse = new CommandsResponse(commandsName, State.ERROR);
        commandsResponse.setError(new ErrorObject(Errors.UNEXPECTED));
        String json = new GsonBuilder().create().toJson(commandsResponse);

        AsyncHttpServerResponse res = response.code(503);
        sendJson(res, json);
    }

    private void sendJson(@NonNull AsyncHttpServerResponse response, String json) {
        response.getHeaders().set(CONTENT_TYPE_OPTIONS, NOSNIFF);
        response.send(APPLICATION_JSON, json);
    }

    private String replaceCompletion(String json, Number completion) {
        if (completion.floatValue() == 0.0) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String decimal = decimalFormat.format(completion);
            json = json.replace(completion.toString(), decimal);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String decimal = decimalFormat.format(completion);
            json = json.replace(completion.toString(), decimal);
        }

        return json;
    }

    public interface Callback {
        void commandsRequest(AsyncHttpServerResponse response, CommandsRequest commandsRequest);
    }
}
