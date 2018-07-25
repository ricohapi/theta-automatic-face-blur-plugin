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
package com.theta360.automaticfaceblur.network.async;

import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.LineEmitter;
import com.koushikdutta.async.LineEmitter.StringCallback;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.HttpUtil;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.Protocol;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequestImpl;
import com.koushikdutta.async.http.server.UnknownRequestBody;
import java.util.regex.Matcher;

/**
 * AsyncHttpServerRequestEx
 */
public class AsyncHttpServerRequestEx extends AsyncHttpServerRequestImpl {
    private String statusLine;
    private Headers mRawHeaders = new Headers();
    AsyncSocket mSocket;
    Matcher mMatcher;

    public String getStatusLine() {
        return statusLine;
    }

    private CompletedCallback mReporter = new CompletedCallback() {
        @Override
        public void onCompleted(Exception error) {
            AsyncHttpServerRequestEx.this.onCompleted(error);
        }
    };

    @Override
    public void onCompleted(Exception e) {
//        if (mBody != null)
//            mBody.onCompleted(e);
        report(e);
    }

    protected void onNotHttp() {
        System.out.println("not http!");
    }

    protected AsyncHttpRequestBody onUnknownBody(Headers headers) {
        return null;
    }

    StringCallback mHeaderCallback = new StringCallback() {
        @Override
        public void onStringAvailable(String s) {
            try {
                if (statusLine == null) {
                    statusLine = s;
                    if (!statusLine.contains("HTTP/")) {
                        onNotHttp();
                        mSocket.setDataCallback(null);
                    }
                } else if (!"\r".equals(s)) {
                    mRawHeaders.addLine(s);
                } else {
                    DataEmitter emitter = HttpUtilEx
                            .getBodyDecoder(mSocket, Protocol.HTTP_1_1, mRawHeaders, true);
//                    emitter.setEndCallback(mReporter);
                    mBody = HttpUtil.getBody(emitter, mReporter, mRawHeaders);
                    if (mBody == null) {
                        mBody = onUnknownBody(mRawHeaders);
                        if (mBody == null) {
                            mBody = new UnknownRequestBody(mRawHeaders.get("Content-Type"));
                        }
                    }
                    mBody.parse(emitter, mReporter);
                    onHeadersReceived();
                }
            } catch (Exception ex) {
                onCompleted(ex);
            }
        }
    };

    public String method;

    @Override
    public String getMethod() {
        return method;
    }

    void setSocket(AsyncSocket socket) {
        mSocket = socket;

        LineEmitter liner = new LineEmitter();
        mSocket.setDataCallback(liner);
        liner.setLineCallback(mHeaderCallback);
        mSocket.setEndCallback(new NullCompletedCallback());
    }

    @Override
    public AsyncSocket getSocket() {
        return mSocket;
    }

    @Override
    public Headers getHeaders() {
        return mRawHeaders;
    }

    @Override
    public void setDataCallback(DataCallback callback) {
        mSocket.setDataCallback(callback);
    }

    @Override
    public DataCallback getDataCallback() {
        return mSocket.getDataCallback();
    }

    @Override
    public boolean isChunked() {
        return mSocket.isChunked();
    }

    @Override
    public Matcher getMatcher() {
        return mMatcher;
    }

    AsyncHttpRequestBody mBody;

    @Override
    public AsyncHttpRequestBody getBody() {
        return mBody;
    }

    @Override
    public void pause() {
        mSocket.pause();
    }

    @Override
    public void resume() {
        mSocket.resume();
    }

    @Override
    public boolean isPaused() {
        return mSocket.isPaused();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void onHeadersReceived() {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public Multimap getQuery() {
        return null;
    }
}
