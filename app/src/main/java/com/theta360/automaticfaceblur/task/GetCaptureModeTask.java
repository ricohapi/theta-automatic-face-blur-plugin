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

import org.json.JSONException;
import org.json.JSONObject;

public class GetCaptureModeTask extends AsyncTask<Void, Void, JSONObject> {
    private Callback mCallback;
    private HttpConnector mCamera;

    public GetCaptureModeTask(Callback callback) {
        this.mCallback = callback;
    }


    @Override
    protected void onPreExecute() {

    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        mCamera = new HttpConnector();
        return mCamera.getCaptureMode();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            mCallback.onGetCaptureMode(result);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    @Override
    protected void onCancelled(JSONObject result) {
        mCamera = null;
    }

    public interface Callback {
        void onGetCaptureMode(JSONObject result) throws JSONException;
    }
}
