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
package com.theta360.automaticfaceblur.task;

import android.os.AsyncTask;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.theta360.automaticfaceblur.network.HttpConnector;
import com.theta360.automaticfaceblur.network.HttpEventListener;
import com.theta360.automaticfaceblur.network.model.requests.CommandsRequest;
import com.theta360.automaticfaceblur.network.model.values.Errors;
import timber.log.Timber;

/**
 * TakePictureTask
 */
public class TakePictureTask extends AsyncTask<Void, Void, HttpConnector.ShootResult> {
    private Callback mCallback;
    private AsyncHttpServerResponse mResponse;
    private CommandsRequest mCommandsRequest;

    /**
     * Constructor of TakePictureTask.
     *
     * @param callback executed
     */
    public TakePictureTask(Callback callback, AsyncHttpServerResponse response,
            CommandsRequest commandsRequest) {
        this.mCallback = callback;
        this.mResponse = response;
        this.mCommandsRequest = commandsRequest;
    }

    /**
     * Setup the task.
     */
    @Override
    protected void onPreExecute() {
        mCallback.onPreExecute();
    }

    /**
     * Set post view listener and take picture.
     *
     * @param params params
     * @return Shooting request results
     */
    @Override
    protected HttpConnector.ShootResult doInBackground(Void... params) {
        CaptureListener postviewListener = new CaptureListener();
        HttpConnector camera = new HttpConnector();

        return camera.takePicture(postviewListener);
    }

    /**
     * Notify the result of shoot result.
     *
     * @param result shooting result
     */
    @Override
    protected void onPostExecute(HttpConnector.ShootResult result) {
        Errors errors = null;
        if (result == HttpConnector.ShootResult.FAIL_CAMERA_DISCONNECTED) {
            Timber.d("takePicture:FAIL_CAMERA_DISCONNECTED");
            errors = Errors.UNEXPECTED;
        } else if (result == HttpConnector.ShootResult.FAIL_STORE_FULL) {
            Timber.d("takePicture:FAIL_STORE_FULL");
            errors = Errors.NO_FREE_SPACE;
        } else if (result == HttpConnector.ShootResult.FAIL_DEVICE_BUSY) {
            Timber.d("takePicture:FAIL_DEVICE_BUSY");
            errors = Errors.SERVICE_UNAVAILABLE;
        } else if (result == HttpConnector.ShootResult.SUCCESS) {
            Timber.d("takePicture:SUCCESS");
        }
        mCallback.onSendCommand(mResponse, mCommandsRequest, errors);
    }

    @Override
    protected void onCancelled() {
        isCancelled();
    }

    /**
     * CaptureListener.
     */
    private class CaptureListener implements HttpEventListener {
        /**
         * check the status of take picture.
         *
         * @param newStatus true:Update available, false;No update available
         */
        @Override
        public void onCheckStatus(boolean newStatus) {
            if (newStatus) {
                Timber.d("takePicture:FINISHED");
            } else {
                Timber.d("takePicture:IN PROGRESS");
            }
        }

        /**
         * Call callback when take picture finished.
         *
         * @param latestCapturedFileId ID of saved file
         */
        @Override
        public void onObjectChanged(String latestCapturedFileId) {
            if (!isCancelled()) {
                mCallback.onPictureGenerated(latestCapturedFileId);
            }
        }

        /**
         * Notify when completed.
         */
        @Override
        public void onCompleted() {
            Timber.d("CaptureComplete");
            mCallback.onCompleted();
        }

        /**
         * Notify when error occurred.
         *
         * @param errorMessage error message
         */
        @Override
        public void onError(String errorMessage) {
            Timber.d("CaptureError %s", errorMessage);
            mCallback.onTakePictureFailed();
        }
    }

    /**
     * Interface of Callback.
     */
    public interface Callback {

        /**
         * Called before doInBackground() and notify that the take picture command will be sent.
         */
        void onPreExecute();

        /**
         * Called when take picture command is send and get the response.
         * @param response
         * @param commandsRequest
         * @param errors
         */
        void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest,
                Errors errors);

        /**
         * Called when captured file is generated.
         *
         * @param fileUrl path of the captured file
         */
        void onPictureGenerated(String fileUrl);

        /**
         * Called when take picture completed.
         */
        void onCompleted();

        /**
         * Called when error occurred.
         *
         */
        void onTakePictureFailed();

    }

}
