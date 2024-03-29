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
package com.theta360.automaticfaceblur;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.theta360.automaticfaceblur.network.WebServer;
import com.theta360.automaticfaceblur.network.model.commands.CommandsName;
import com.theta360.automaticfaceblur.network.model.objects.ProgressObject;
import com.theta360.automaticfaceblur.network.model.requests.CommandsRequest;
import com.theta360.automaticfaceblur.network.model.responses.CommandsResponse;
import com.theta360.automaticfaceblur.network.model.responses.StatusResponse;
import com.theta360.automaticfaceblur.network.model.values.Errors;
import com.theta360.automaticfaceblur.network.model.values.State;
import com.theta360.automaticfaceblur.network.model.values.Status;
import com.theta360.automaticfaceblur.task.GetCaptureModeTask;
import com.theta360.automaticfaceblur.task.GetExposureDelayTask;
import com.theta360.automaticfaceblur.task.GetOptionsTask;
import com.theta360.automaticfaceblur.task.GetRemainingSpaceTask;
import com.theta360.automaticfaceblur.task.ImageProcessorTask;
import com.theta360.automaticfaceblur.task.SetCaptureModeTask;
import com.theta360.automaticfaceblur.task.SetOptionsTask;
import com.theta360.automaticfaceblur.task.ShowLiveViewTask;
import com.theta360.automaticfaceblur.task.TakePictureTask;
import com.theta360.automaticfaceblur.task.TakePictureTask.Callback;
import com.theta360.automaticfaceblur.task.UpdatePreviewTask;
import com.theta360.automaticfaceblur.view.MJpegInputStream;
import com.theta360.automaticfaceblur.view.MyProgressDialog;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.activity.ThetaInfo;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.values.OledDisplay;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;
import com.theta360.pluginlibrary.values.TextArea;
import com.theta360.pluginlibrary.values.ThetaModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static com.theta360.pluginlibrary.values.ThetaModel.THETA_X;
import static com.theta360.pluginlibrary.values.ThetaModel.isZ1Model;

/**
 * MainActivity
 */
public class MainActivity extends PluginActivity {
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();
    public static final String ACTION_FINISH_UPDATING ="ACTION_ASYNC_FINISH_UPDATING";
    private TakePictureTask mTakePictureTask;
    private ImageProcessorTask mImageProcessorTask;
    private byte[] mPreviewByteArray;
    private SetOptionsTask mSetOptionsTask;
    private GetOptionsTask mGetOptionsTask;
    private WebServer mWebServer;
    private UpdatePreviewTask mUpdatePreviewTask;
    private String mCaptureMode;
    private int mExposureDelay;
    private static final String IMAGE = "image";
    private boolean mIsStarted;
    private boolean mIsChanged;
    private boolean mIsEnded;
    private JSONObject mJsonRecord = null;
    static final String URL = "http://localhost:8888";
    private WebView webView;
    private boolean takePictureComplete = false;

    /**
     * Set a KeyCallback when onCreate executes.
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.BLACK);

        setKeyCallback(new KeyCallback() {
            /**
             * Receive the shutter key down when it is not during taking picture task or
             * processing image task.
             * @param keyCode code of key
             * @param keyEvent event of key
             */
            @Override
            public void onKeyDown(int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (mTakePictureTask == null && mImageProcessorTask == null) {
                        if (mUpdatePreviewTask != null) {
                            mUpdatePreviewTask.cancel(false);
                        }
                        mTakePictureTask = new TakePictureTask(mTakePictureTaskCallback, null,
                                null);
                        mTakePictureTask.execute();
                    }
                }
            }

            @Override
            public void onKeyUp(int i, KeyEvent keyEvent) {

            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD || keyCode == KeyReceiver.KEYCODE_FUNCTION) {
//                    if(isXCameraModel()) {
                        if (mTakePictureTask != null) {
                            mTakePictureTask.cancel(true);
                            mTakePictureTask = null;
                        }
                        if (mImageProcessorTask != null) {
                            mImageProcessorTask.cancel(true);
                            mImageProcessorTask = null;
                        }
                        if (mUpdatePreviewTask != null) {
                            mUpdatePreviewTask.cancel(false);
                            mUpdatePreviewTask = null;
                        }

                        mWebServer.stop();

                        finishTask();
//                    }
                }
            }
        });

        if (isXCameraModel()) {
            Intent intent = new Intent("com.theta360.plugin.ACTION_PLUGIN_WEBAPI_CAMERA_OPEN");
            getBaseContext().sendBroadcast(intent);
        } else {
            if (isZ1Model()) {
                notificationOledDisplaySet(OledDisplay.DISPLAY_BASIC);
            }
        }
        new GetRemainingSpaceTask(mGetRemainingSpaceTaskCallback).execute();
        new GetCaptureModeTask(mGetCaptureModeTaskCallback).execute();

        mIsStarted = true;
        mIsEnded = false;
        mJsonRecord = new JSONObject();
    }

    /**
     * Control LEDs when onResume executes.
     */
    @Override
    protected void onResume() {
        Timber.d("onResume");
        super.onResume();
        controlLedOnCreate();
        mWebServer = new WebServer(getApplicationContext(), null, mWebServerCallback);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient()); // WebViewを設定する
        webView.getSettings().setJavaScriptEnabled(true); // JavaScriptを有効にする
        webView.loadUrl(URL); // URLを読み込む
    }

    /**
     * Cancel async tasks and stop the web server when onPause executes.
     */
    @Override
    protected void onPause() {
        if (mTakePictureTask != null) {
            mTakePictureTask.cancel(true);
            mTakePictureTask = null;
        }
        if (mImageProcessorTask != null) {
            mImageProcessorTask.cancel(true);
            mImageProcessorTask = null;
        }
        if (mUpdatePreviewTask != null) {
            mUpdatePreviewTask.cancel(false);
            mUpdatePreviewTask = null;
        }

        setAutoClose(false);

        super.onPause();
    }

    /**
     * TakePictureTask Callback.
     */
    TakePictureTask.Callback mTakePictureTaskCallback = new Callback() {
        @Override
        public void onPreExecute() {
            setAutoClose(false);
        }

        @Override
        public void onPictureGenerated(String fileUrl) {
            if (!TextUtils.isEmpty(fileUrl)) {
                notificationAudioOpen();
                if(isXCameraModel()) {
                    MyProgressDialog pd = MyProgressDialog.newInstance();
                    pd.show(getFragmentManager(), "Processing");
                } else {
                    if (isZ1Model()) {
                        notificationOledDisplaySet(OledDisplay.DISPLAY_PLUGIN);
                        Map<TextArea, String> output = new HashMap<>();
                        output.put(TextArea.MIDDLE, "Processing");
                        output.put(TextArea.BOTTOM, "");
                        notificationOledTextShow(output);
                    } else {
                        notificationLedBlink(LedTarget.LED4, LedColor.BLUE, 1000);
                    }
                }
                mImageProcessorTask = new ImageProcessorTask(mImageProcessorTaskCallback);
                mImageProcessorTask.execute(fileUrl);
            } else {
                notificationError(getResources().getString(R.string.take_picture_error));
            }
            mTakePictureTask = null;
        }

        @Override
        public void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest,
                                  Errors errors) {
            if (mWebServer != null && response != null && commandsRequest != null) {
                CommandsName commandsName = commandsRequest.getCommandsName();
                if (errors == null) {
                    CommandsResponse commandsResponse = new CommandsResponse(commandsName,
                            State.IN_PROGRESS);
                    commandsResponse.setProgress(new ProgressObject(0.00));
                    mWebServer.sendCommandsResponse(response, commandsResponse);
                } else {
                    mWebServer.sendError(response, errors, commandsName);
                }
            }
            if (errors != null) {
                notificationError(errors.getMessage());
            }
        }

        @Override
        public void onCompleted() {
            if(isXCameraModel()) {
                setAutoClose(false);
            } else {
                setAutoClose(true);
            }
        }

        @Override
        public void onTakePictureFailed() {
            notificationError(getResources().getString(R.string.error));
            setAutoClose(true);
        }
    };

    /**
     * SetOptionsTask Callback.
     */
    SetOptionsTask.Callback mSetOptionsTaskCallback = new SetOptionsTask.Callback() {
        @Override
        public void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest,
                                  Errors errors) {
            if (mWebServer != null && response != null && commandsRequest != null) {
                CommandsName commandsName = commandsRequest.getCommandsName();
                if (errors == null) {
                    CommandsResponse commandsResponse = new CommandsResponse(commandsName,
                            State.DONE);
                    mWebServer.sendCommandsResponse(response, commandsResponse);
                } else {
                    mWebServer.sendError(response, errors, commandsName);
                }
                mSetOptionsTask = null;
            }
        }
    };

    /**
     * GetOptionsTask Callback.
     */
    GetOptionsTask.Callback mGetOptionsTaskCallback = new GetOptionsTask.Callback() {
        @Override
        public void onSendCommand(String responseData, AsyncHttpServerResponse response,
                                  CommandsRequest commandsRequest,
                                  Errors errors) {
            if (mWebServer != null && response != null && commandsRequest != null) {
                CommandsName commandsName = commandsRequest.getCommandsName();
                if (errors == null) {
                    mWebServer.sendGetOptionsResponse(response, responseData);
                } else {
                    mWebServer.sendError(response, errors, commandsName);
                }
                mGetOptionsTask = null;
            }
        }
    };

    /**
     * ShowLiveViewTask Callback.
     */
    ShowLiveViewTask.Callback mShowLiveViewTaskCallback = new ShowLiveViewTask.Callback() {
        @Override
        public void onLivePreview(MJpegInputStream mJpegInputStream,
                                  AsyncHttpServerResponse response, CommandsRequest commandsRequest,
                                  Errors errors) {
            CommandsName commandsName = CommandsName.START_LIVE_PREVIEW;
            if (errors == null) {
                if (mUpdatePreviewTask != null) {
                    mUpdatePreviewTask.cancel(false);
                }

                mUpdatePreviewTask = new UpdatePreviewTask(mSendPreviewTaskCallback,
                        mJpegInputStream);
                mUpdatePreviewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                CommandsResponse commandsResponse = new CommandsResponse(commandsName,
                        State.DONE);
                mWebServer.sendCommandsResponse(response, commandsResponse);
            } else {
                mWebServer.sendError(response, errors, commandsName);
            }
        }
    };

    /**
     * UpdatePreviewTask Callback.
     */
    public UpdatePreviewTask.Callback mSendPreviewTaskCallback = new UpdatePreviewTask.Callback() {
        @Override
        public void updatePreview(byte[] previewByteArray) {
            mPreviewByteArray = previewByteArray;
        }

        @Override
        public void onCancelled() {
            mUpdatePreviewTask = null;
        }
    };

    /**
     * ImageProcessorTask Callback.
     */
    private ImageProcessorTask.Callback mImageProcessorTaskCallback = new ImageProcessorTask.Callback() {
        @Override
        public void onSuccess(Map<String, String> fileUrlMap) {
            String fileUrl = fileUrlMap.get(ImageProcessorTask.ORIGINAL_FILE_KEY);
            Matcher notBlurredFileMatcher = Pattern.compile("/DCIM.*").matcher(fileUrl);
            if (notBlurredFileMatcher.find()) {
                fileUrl = notBlurredFileMatcher.group();
            }

            Matcher blurredMatcher = Pattern.compile("/DCIM.*")
                    .matcher(fileUrlMap.get(ImageProcessorTask.BLURRED_FILE_KEY));
            if (blurredMatcher.find()) {
                String formattedFileUrl = blurredMatcher.group();
                Timber.d(formattedFileUrl);
                Timber.d(fileUrl);
                String[] fileUrls = new String[]{formattedFileUrl, fileUrl};
                notificationDatabaseUpdate(fileUrls);
            }
            mImageProcessorTask = null;
            notificationAudioClose();
            takePictureComplete = true;
            if(isXCameraModel()) {
                closeDialog();
            } else {
                if (isZ1Model()) {
                    notificationOledDisplaySet(OledDisplay.DISPLAY_BASIC);
                } else {
                    notificationLedShow(LedTarget.LED4);
                }
            }
        }

        @Override
        public void onError(boolean isCancelled) {
            mImageProcessorTask = null;
            if (isCancelled) {
                notificationLedShow(LedTarget.LED4);
            } else {
                notificationError(getResources().getString(R.string.error));
            }
        }
    };

    private GetRemainingSpaceTask.Callback mGetRemainingSpaceTaskCallback = new GetRemainingSpaceTask.Callback() {
        @Override
        public void onStorageFew() {
            notificationLedShow(LedTarget.LED8);
        }
        @Override
        public void onStorageVeryFew() {
            notificationLedBlink(LedTarget.LED8, null, 2000);
        }
        @Override
        public void onStorageEnough() {
            notificationLedHide(LedTarget.LED8);
        }
        @Override
        public void onError() {
            notificationError("error");
        }
    };

    private SetCaptureModeTask.Callback mSetCaptureModeTaskCallback = new SetCaptureModeTask.Callback() {
        @Override
        public void onSetExposureDelay() {
            if (isXCameraModel()) {
                setAutoClose(false);
            } else {
                setAutoClose(true);
            }
        }

        @Override
        public void onSetExposureDelayFailed(Errors errors) {
            if (errors != null) {
                notificationError(errors.getMessage());
            }
        }

        @Override
        public void onSetCaptureModeFailed(Errors errors) {
            if (errors != null) {
                notificationError(errors.getMessage());
            }
        }
    };

    private GetCaptureModeTask.Callback mGetCaptureModeTaskCallback = new GetCaptureModeTask.Callback() {
        @Override
        public void onGetCaptureMode(JSONObject json){
            try {
                if(json != null) {
                    JSONObject result = json.getJSONObject("results");
                    mCaptureMode = result.getJSONObject("options").getString("captureMode");
                    if(mCaptureMode.equals("image")) {
                        mJsonRecord = result;
                        if(!result.getJSONObject("options").isNull("fileFormat")) {
                            if(result.getJSONObject("options").getJSONObject("fileFormat").getInt("width") == 11008
                                    && result.getJSONObject("options").getJSONObject("fileFormat").getInt("height") == 5504) {
                                mIsChanged = true;
                            }
                        }
                    }
                }
            } catch (JSONException jsonException) {
                mCaptureMode = "image";
                jsonException.printStackTrace();
            }
            new GetExposureDelayTask(mGetExposureDelayTaskCallback).execute();
        }
    };

    private GetExposureDelayTask.Callback mGetExposureDelayTaskCallback = new GetExposureDelayTask.Callback() {
        @Override
        public void onGetExposureDelay(int exposureDelay) {
            mExposureDelay = exposureDelay;
            new SetCaptureModeTask(mSetCaptureModeTaskCallback, IMAGE, 0, null, mIsStarted, mIsChanged, mIsEnded).execute();
            mIsStarted = false;
        }
    };

    /**
     * WebServer Callback
     */
    private WebServer.Callback mWebServerCallback = new WebServer.Callback() {

        @Override
        public void commandsRequest(AsyncHttpServerResponse response,
                                    CommandsRequest commandsRequest) {
            CommandsName commandsName = commandsRequest.getCommandsName();
            Timber.d("commandsName : %s", commandsName.toString());
            switch (commandsName) {
                case TAKE_PICTURE:
                    if (mTakePictureTask == null && mImageProcessorTask == null
                            && mSetOptionsTask == null && mGetOptionsTask == null) {
                        if (mUpdatePreviewTask != null) {
                            mUpdatePreviewTask.cancel(false);
                        }
                        mTakePictureTask = new TakePictureTask(mTakePictureTaskCallback, response,
                                commandsRequest);
                        mTakePictureTask.execute();
                    } else {
                        mWebServer.sendError(response, Errors.DEVICE_BUSY, commandsName);
                        mTakePictureTask = null;
                    }
                    break;
                case SET_OPTIONS:
                    if (mTakePictureTask == null && mImageProcessorTask == null
                            && mSetOptionsTask == null) {
                        mSetOptionsTask = new SetOptionsTask(mSetOptionsTaskCallback, response,
                                commandsRequest);
                        mSetOptionsTask.execute();
                    } else {
                        mWebServer.sendError(response, Errors.DEVICE_BUSY, commandsName);
                        mSetOptionsTask = null;
                    }
                    break;
                case GET_OPTIONS:
                    if (mTakePictureTask == null && mImageProcessorTask == null
                            && mGetOptionsTask == null) {
                        mGetOptionsTask = new GetOptionsTask(mGetOptionsTaskCallback, response,
                                commandsRequest);
                        mGetOptionsTask.execute();
                    } else {
                        mWebServer.sendError(response, Errors.DEVICE_BUSY, commandsName);
                        mGetOptionsTask = null;
                    }
                    break;
                case GET_LIVE_PREVIEW:
                    mWebServer.sendPreviewPicture(response, mPreviewByteArray);
                    break;
                case START_LIVE_PREVIEW:
                    new ShowLiveViewTask(mShowLiveViewTaskCallback, response,
                            commandsRequest).execute();
                    break;
                case GET_STATUS:
                    if (mTakePictureTask == null && mImageProcessorTask == null) {
                        mWebServer.sendStatus(response, new StatusResponse(Status.IDLE));
                        if(takePictureComplete) {
                            takePictureComplete = false;
                            new ShowLiveViewTask(mShowLiveViewTaskCallback, response, commandsRequest).execute();
                        }
                    } else if (mImageProcessorTask == null) {
                        mWebServer.sendStatus(response, new StatusResponse(Status.SHOOTING));
                    } else if (mTakePictureTask == null) {
                        mWebServer.sendStatus(response, new StatusResponse(Status.BLURRING));
                    } else {
                        mWebServer.sendError(response, Errors.DEVICE_BUSY, commandsName);
                    }
                    break;
                default:
                    mWebServer.sendUnknownCommand(response);
                    break;
            }
        }
    };

    /**
     * Control led when onCreate executes.
     */
    private void controlLedOnCreate() {
        notificationLedShow(LedTarget.LED4);
        notificationLedHide(LedTarget.LED5);
        notificationLedHide(LedTarget.LED6);
        notificationLedHide(LedTarget.LED7);
        notificationLedHide(LedTarget.LED8);
    }

    private void closeDialog() {
        Intent intent = new Intent();
        intent.setAction(ACTION_FINISH_UPDATING);
        sendBroadcast(intent);
    }

    private void finishTask() {
        mWebServer.stop();
        
        if(isXCameraModel()) {
            if (mCaptureMode.equals(IMAGE)) {
                new SetCaptureModeTask(mSetCaptureModeTaskCallback, mCaptureMode, mExposureDelay, mJsonRecord, mIsStarted, mIsChanged, true).execute();
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isXCameraModel()) {
                    Intent intent = new Intent("com.theta360.plugin.ACTION_PLUGIN_WEBAPI_CAMERA_CLOSE");
                    getBaseContext().sendBroadcast(intent);
                }
                Process.killProcess(Process.myPid());
            }
        }, 2000);
    }

    public static Boolean isXCameraModel() {
        ThetaModel model = ThetaModel.getValue(ThetaInfo.getThetaModelName());
        return model != THETA_X ? false : true;
    }

}
