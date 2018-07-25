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
import com.theta360.automaticfaceblur.network.model.requests.CommandsRequest;
import com.theta360.automaticfaceblur.network.model.values.Errors;
import com.theta360.automaticfaceblur.view.MJpegInputStream;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * ShowLiveViewTask
 */

public class ShowLiveViewTask extends AsyncTask<String, String, MJpegInputStream> {

    Callback mCallback;
    private AsyncHttpServerResponse mResponse;
    private CommandsRequest mCommandsRequest;

    /**
     * Constructor of ShowLiveViewTask.
     *
     * @param callback ShowLiveViewTask Callback
     * @param response AsyncHttpServerResponse response
     * @param commandsRequest CommandsRequest
     */
    public ShowLiveViewTask(ShowLiveViewTask.Callback callback, AsyncHttpServerResponse response,
            CommandsRequest commandsRequest) {
        this.mCallback = callback;
        this.mResponse = response;
        this.mCommandsRequest = commandsRequest;
    }

    /**
     * Get live preview MJpegInputStream.
     *
     * @param ipAddress ipAddress
     * @return mjis
     */
    @Override
    protected MJpegInputStream doInBackground(String... ipAddress) {
        MJpegInputStream mjis = null;
        final int MAX_RETRY_COUNT = 20;

        for (int retryCount = 0; retryCount < MAX_RETRY_COUNT; retryCount++) {
            try {
                publishProgress("start Live view");
                HttpConnector camera = new HttpConnector();
                InputStream is = camera.getLivePreview();
                mjis = new MJpegInputStream(is);
                retryCount = MAX_RETRY_COUNT;
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (JSONException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return mjis;
    }

    /**
     * Do live preview.
     *
     * @param mJpegInputStream MJpegInputStream
     */
    @Override
    protected void onPostExecute(MJpegInputStream mJpegInputStream) {
        if (mJpegInputStream != null) {
            mCallback.onLivePreview(mJpegInputStream, mResponse, mCommandsRequest, null);

        } else {
            mCallback.onLivePreview(null, mResponse, mCommandsRequest, Errors.UNEXPECTED);
        }
    }

    /**
     * Interface of Callback.
     */
    public interface Callback {
        void onLivePreview(MJpegInputStream mJpegInputStream, AsyncHttpServerResponse response,
                CommandsRequest commandsRequest,
                Errors errors);
    }
}
