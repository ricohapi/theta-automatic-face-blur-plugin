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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * HTTP connection to device
 */
public class HttpConnector {
    private final static long CHECK_STATUS_PERIOD_MS = 50;
    private final static String IP_ADDRESS = "127.0.0.1:8080";
    private static final String IMAGE = "image";

    private Timer mCheckStatusTimer = null;
    private HttpEventListener mHttpEventListener = null;

    /**
     * Constructor
     *
     */
    public HttpConnector() {
    }

    /**
     * Take photo<p> After shooting, the status is checked for each {@link
     * HttpConnector#CHECK_STATUS_PERIOD_MS} and the listener notifies you of the status.
     *
     * @param listener Post-shooting event listener
     * @return Shooting request results
     */
    public ShootResult takePicture(HttpEventListener listener) {
        ShootResult result = ShootResult.FAIL_DEVICE_BUSY;

        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        mHttpEventListener = listener;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("name", "camera.takePicture");

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");
            String commandId = output.getString("id");

            if (status.equals("inProgress")) {
                mCheckStatusTimer = new Timer(true);
                CapturedTimerTask capturedTimerTask = new CapturedTimerTask();
                capturedTimerTask.setCommandId(commandId);
                mCheckStatusTimer.scheduleAtFixedRate(capturedTimerTask, CHECK_STATUS_PERIOD_MS,
                        CHECK_STATUS_PERIOD_MS);
                result = ShootResult.SUCCESS;
            } else if (status.equals("done")) {
                JSONObject results = output.getJSONObject("results");
                String lastFileId = results.getString("fileUri");

                mHttpEventListener.onObjectChanged(lastFileId);
                mHttpEventListener.onCompleted();
                result = ShootResult.SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = ShootResult.FAIL_DEVICE_BUSY;
        } catch (JSONException e) {
            e.printStackTrace();
            result = ShootResult.FAIL_DEVICE_BUSY;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    /**
     * Check still image shooting status
     *
     * @param commandId Command ID for shooting still images
     * @return ID of saved file (null is returned if the file is not saved)
     */
    private String checkCaptureStatus(String commandId) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/status");
        JSONObject input = new JSONObject();
        String responseData;
        String capturedFileId = null;
        InputStream is = null;

        try {
            // send HTTP POST
            input.put("id", commandId);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("done")) {
                JSONObject results = output.getJSONObject("results");
                capturedFileId = results.getString("fileUrl");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return capturedFileId;
    }

    /**
     * Set still image as shooting mode
     *
     * @return Error message (null is returned if successful)
     */
    public String setCaptureMode(String captureMode) {
        String errorMessage;

        try {
            // send HTTP POST
            JSONObject input = new JSONObject();
            input.put("name", "camera.setOptions");
            JSONObject parameters = new JSONObject();
            JSONObject options = new JSONObject();
            options.put("captureMode", captureMode);
            parameters.put("options", options);
            input.put("parameters", parameters);

            errorMessage = setOptions(input.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        }

        return errorMessage;
    }

    /**
     * Set shooting options
     * @param commands
     * @return errorMessage
     */
    public String setOptions(String commands) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        String responseData;
        String errorMessage = null;
        InputStream is = null;

        try {
//          send HTTP POST
            JSONObject json = new JSONObject(commands);
            Timber.d("json %s", json);

            OutputStream os = postConnection.getOutputStream();
            os.write(json.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return errorMessage;
    }

    /**
     * Set shooting options
     * @param commands
     * @return responseData
     */
    public String getOptions(String commands) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        String responseData = null;
        String errorMessage = null;
        InputStream is = null;

        try {
//          send HTTP POST
            JSONObject json = new JSONObject(commands);
            Timber.d("json %s", json);

            OutputStream os = postConnection.getOutputStream();
            os.write(json.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseData;
    }

    /**
     * Set exposure delay
     *
     * @return errorMessage
     */
    public String setExposureDelay(int exposureDelay) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        String responseData;
        String errorMessage = null;
        InputStream is = null;

        try {
//          send HTTP POST
            JSONObject input = new JSONObject();
            input.put("name", "camera.setOptions");
            JSONObject parameters = new JSONObject();
            JSONObject options = new JSONObject();
            options.put("exposureDelay", exposureDelay);
            parameters.put("options", options);
            input.put("parameters", parameters);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return errorMessage;
    }

    /**
     * Generate connection destination URL
     *
     * @param path Path
     * @return URL
     */
    private String createUrl(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(IP_ADDRESS);
        sb.append(path);

        return sb.toString();
    }

    /**
     * Generate HTTP connection
     *
     * @param method Method
     * @param path   Path
     * @return HTTP Connection instance
     */
    private HttpURLConnection createHttpConnection(String method, String path) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(createUrl(path));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);

            if (method.equals("POST")) {
                connection.setRequestMethod(method);
                connection.setDoOutput(true);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Convert input stream to string
     *
     * @param is InputStream
     * @return String
     * @throws IOException IO error
     */
    private String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String lineData;
        while ((lineData = br.readLine()) != null) {
            sb.append(lineData);
        }
        br.close();
        return sb.toString();
    }

    /**
     * ShootResult
     */
    public enum ShootResult {
        SUCCESS, FAIL_CAMERA_DISCONNECTED, FAIL_STORE_FULL, FAIL_DEVICE_BUSY
    }

    /**
     * CapturedTimerTask
     */
    private class CapturedTimerTask extends TimerTask {
        private String mCommandId;

        public void setCommandId(String commandId) {
            mCommandId = commandId;
        }

        @Override
        public void run() {
            String capturedFileId = checkCaptureStatus(mCommandId);

            if (capturedFileId != null) {
                mHttpEventListener.onCheckStatus(true);
                mCheckStatusTimer.cancel();
                mHttpEventListener.onObjectChanged(capturedFileId);
                mHttpEventListener.onCompleted();
            } else {
                mHttpEventListener.onCheckStatus(false);
            }
        }
    }

    /**
     * Acquire live view stream
     * @return Stream for receiving data
     * @throws IOException
     */
    public InputStream getLivePreview() throws IOException, JSONException {

        // set capture mode to image
        setCaptureMode(IMAGE);

        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        InputStream is;

        try {
            // send HTTP POST
            input.put("name", "camera.getLivePreview");

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = null;
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            throw e;
        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }

        return is;
    }

    public long getRemainingSpaces() {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        String errorMessage = null;
        InputStream is = null;
        long remainingSpace = -1;
        try {
            // send HTTP POST
            input.put("name", "camera.getOptions");
            JSONObject parameters = new JSONObject();
            JSONArray optionNames = new JSONArray();
            optionNames.put("remainingSpace");
            parameters.put("optionNames", optionNames);
            input.put("parameters", parameters);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            remainingSpace = output.getJSONObject("results").getJSONObject("options").getLong("remainingSpace");

            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return remainingSpace;
    }

    public int getExposureDelay() {
        String optionName = "exposureDelay";
        JSONObject options = getOptionsJSON(optionName);
        int exposureDelay = 0;
        try {
            exposureDelay = options.getInt(optionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return exposureDelay;
    }

    public String getCaptureMode() {
        String optionName = "captureMode";
        JSONObject options = getOptionsJSON(optionName);
        String captureMode = null;
        try {
            captureMode = options.getString(optionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return captureMode;
    }

    private JSONObject getOptionsJSON(String optionName) {
        HttpURLConnection postConnection = createHttpConnection("POST", "/osc/commands/execute");
        JSONObject input = new JSONObject();
        String responseData;
        String errorMessage = null;
        InputStream is = null;
        JSONObject options = null;

        try {
            // send HTTP POST
            input.put("name", "camera.getOptions");
            JSONObject parameters = new JSONObject();
            JSONArray optionNames = new JSONArray();
            optionNames.put(optionName);
            parameters.put("optionNames", optionNames);
            input.put("parameters", parameters);

            OutputStream os = postConnection.getOutputStream();
            os.write(input.toString().getBytes());
            postConnection.connect();
            os.flush();
            os.close();

            is = postConnection.getInputStream();
            responseData = InputStreamToString(is);

            // parse JSON data
            JSONObject output = new JSONObject(responseData);
            options = output.getJSONObject("results").getJSONObject("options");

            String status = output.getString("state");

            if (status.equals("error")) {
                JSONObject errors = output.getJSONObject("error");
                errorMessage = errors.getString("message");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = e.toString();
            InputStream es = postConnection.getErrorStream();
            try {
                if (es != null) {
                    String errorData = InputStreamToString(es);
                    JSONObject output = new JSONObject(errorData);
                    JSONObject errors = output.getJSONObject("error");
                    errorMessage = errors.getString("message");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } finally {
                if (es != null) {
                    try {
                        es.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return options;
    }
}
