/**
 * Copyright 2018 Ricoh Company, Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.automaticfaceblur.task;

import android.os.AsyncTask;

import com.theta360.automaticfaceblur.network.HttpConnector;
import com.theta360.automaticfaceblur.network.model.values.Errors;

public class SetCaptureModeTask extends AsyncTask<Void, Void, String> {
    private static final String STREAMING = "streaming";
    private static final String VIDEO = "video";
    private static final String SERVICE_UNAVAILABLE = "Service Unavailable.";
    private Callback mCallback;
    private HttpConnector mCamera;
    private String mCaptureMode;
    private int mExposureDelay;
    private boolean mIsStart;

    public SetCaptureModeTask(Callback callback, String captureMode, int exposureDelay, boolean isStart) {
        this.mCallback = callback;
        this.mCaptureMode = captureMode;
        this.mExposureDelay = exposureDelay;
        this.mIsStart = isStart;
    }


    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(Void... params) {
        if (mCaptureMode.equals(STREAMING)) {
            mCaptureMode = VIDEO;
        }
        mCamera = new HttpConnector();
        int exposureDelay;
        if (mIsStart) {
            exposureDelay = 0;
        } else {
            exposureDelay = mExposureDelay;
        }
        if (mCaptureMode.equals(STREAMING)) {
            mCaptureMode = VIDEO;
        }
        String errorMessage = mCamera.setCaptureMode(mCaptureMode);
        if (errorMessage != null) {
            mCallback.onSetCaptureModeFailed(Errors.UNEXPECTED);
        }
        return mCamera.setExposureDelay(exposureDelay);
    }

    @Override
    protected void onPostExecute(String errorMessage) {
        Errors errors;
        if (errorMessage == null) {
            mCallback.onSetExposureDelay();
        } else {
            if (errorMessage.equals(SERVICE_UNAVAILABLE)) {
                errors = Errors.SERVICE_UNAVAILABLE;
            } else {
                errors = Errors.UNEXPECTED;
            }
            mCallback.onSetExposureDelayFailed(errors);
        }
    }

    @Override
    protected void onCancelled(String result) {
        mCamera = null;
    }

    public interface Callback {
        void onSetExposureDelay();

        void onSetExposureDelayFailed(Errors errors);

        void onSetCaptureModeFailed(Errors errors);
    }

}
