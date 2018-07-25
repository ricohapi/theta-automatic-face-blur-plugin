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

import com.google.gson.GsonBuilder;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.theta360.automaticfaceblur.network.HttpConnector;
import com.theta360.automaticfaceblur.network.model.requests.CommandsRequest;
import com.theta360.automaticfaceblur.network.model.values.Errors;

import timber.log.Timber;

/**
 * SetOptionsTask
 */
public class SetOptionsTask extends AsyncTask<Void, Void, String> {
    private Callback mCallback;
    private AsyncHttpServerResponse mResponse;
    private CommandsRequest mCommandsRequest;

    /**
     * Constructor of SetOptionsTask.
     *
     * @param callback executed
     */
    public SetOptionsTask(Callback callback, AsyncHttpServerResponse response,
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
    }

    /**
     * Set option.
     *
     * @return Shooting request results
     */
    @Override
    protected String doInBackground(Void... aVoid) {
        String commands = new GsonBuilder().create().toJson(mCommandsRequest);
        return new HttpConnector().setOptions(commands);
    }

    /**
     * Notify error.
     *
     * @param errorMessage
     */
    @Override
    protected void onPostExecute(String errorMessage) {
        Errors errors = null;
        if (errorMessage != null) {
            errors = Errors.UNEXPECTED;
            Timber.d("setOption: %s", errorMessage);
        }
        mCallback.onSendCommand(mResponse, mCommandsRequest, errors);
    }

    /**
     * Interface of Callback.
     */
    public interface Callback {
        void onSendCommand(AsyncHttpServerResponse response, CommandsRequest commandsRequest, Errors errors);
    }
}
