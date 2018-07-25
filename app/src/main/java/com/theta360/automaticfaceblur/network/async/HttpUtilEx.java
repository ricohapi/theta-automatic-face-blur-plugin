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

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.FilteredDataEmitter;
import com.koushikdutta.async.http.BodyDecoderException;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.HttpUtil;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.Protocol;
import com.koushikdutta.async.http.filter.ChunkedInputFilter;
import com.koushikdutta.async.http.filter.ContentLengthFilter;
import com.koushikdutta.async.http.filter.GZIPInputFilter;
import com.koushikdutta.async.http.filter.InflaterInputFilter;
import java.nio.charset.Charset;

/**
 * HttpUtilEx
 */
public class HttpUtilEx extends HttpUtil {
    public static DataEmitter getBodyDecoder(DataEmitter emitter, Protocol protocol,
            Headers headers, boolean server) {
        long _contentLength;
        try {
            _contentLength = Long.parseLong(headers.get("Content-Length"));
        } catch (Exception ex) {
            _contentLength = -1;
        }
        final long contentLength = _contentLength;
        if (-1 != contentLength) {
            if (contentLength < 0) {
                HttpUtilEx.EndEmitter ender = HttpUtilEx.EndEmitter.create(emitter.getServer(),
                        new BodyDecoderException(
                                "not using chunked encoding, and no content-length found."));
                ender.setDataEmitter(emitter);
                emitter = ender;
                return emitter;
            }
            if (contentLength == 0) {
                HttpUtilEx.EndEmitter ender = HttpUtilEx.EndEmitter
                        .create(emitter.getServer(), null);
                ender.setDataEmitter(emitter);
                emitter = ender;
                return emitter;
            }
            String charset = null;
            Multimap mm = Multimap.parseSemicolonDelimited(headers.get("Content-Type"));
            String cs;
            if (mm != null && null != (cs = mm.getString("charset")) && Charset.isSupported(cs)) {
                charset = cs;
            }
            final String finalCharset = charset;
            final ContentLengthFilter contentLengthWatcher = new ContentLengthFilter(
                    contentLength) {
                @Override
                public String charset() {
                    return finalCharset;
                }
            };
            contentLengthWatcher.setDataEmitter(emitter);
            emitter = contentLengthWatcher;
        } else if ("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
            ChunkedInputFilter chunker = new ChunkedInputFilter();
            chunker.setDataEmitter(emitter);
            emitter = chunker;
        } else {
            if ((server || protocol == Protocol.HTTP_1_1) && !"close"
                    .equalsIgnoreCase(headers.get("Connection"))) {
                // if this is the server, and the client has not indicated a request body, the client is done
                HttpUtilEx.EndEmitter ender = HttpUtilEx.EndEmitter
                        .create(emitter.getServer(), null);
                ender.setDataEmitter(emitter);
                emitter = ender;
                return emitter;
            }
        }

        if ("gzip".equals(headers.get("Content-Encoding"))) {
            GZIPInputFilter gunzipper = new GZIPInputFilter();
            gunzipper.setDataEmitter(emitter);
            emitter = gunzipper;
        } else if ("deflate".equals(headers.get("Content-Encoding"))) {
            InflaterInputFilter inflater = new InflaterInputFilter();
            inflater.setDataEmitter(emitter);
            emitter = inflater;
        }

        // conversely, if this is the client (http 1.0), and the server has not indicated a request body, we do not report
        // the close/end event until the server actually closes the connection.
        return emitter;
    }

    static class EndEmitter extends FilteredDataEmitter {
        private EndEmitter() {
        }

        public static HttpUtilEx.EndEmitter create(AsyncServer server, final Exception e) {
            final HttpUtilEx.EndEmitter ret = new HttpUtilEx.EndEmitter();
            // don't need to worry about any race conditions with post and this return value
            // since we are in the server thread.
            server.post(new Runnable() {
                @Override
                public void run() {
                    ret.report(e);
                }
            });
            return ret;
        }
    }
}
