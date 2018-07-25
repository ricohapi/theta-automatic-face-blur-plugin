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
import com.theta360.automaticfaceblur.view.MJpegInputStream;
import java.io.IOException;
import timber.log.Timber;

/**
 * UpdatePreviewTask
 */
public class UpdatePreviewTask extends AsyncTask<Void, Void, Void> {
    private Callback mCallback;
    private MJpegInputStream mMJpegInputStream;

    /**
     * Constructor of UpdatePreviewTask.
     */
    public UpdatePreviewTask(Callback callback, MJpegInputStream mJpegInputStream) {
        mCallback = callback;
        mMJpegInputStream = mJpegInputStream;
    }

    /**
     * Update preview.
     */
    @Override
    protected Void doInBackground(Void... aVoid) {
        while (mMJpegInputStream != null && !isCancelled()) {
            try {
                mCallback.updatePreview(mMJpegInputStream.readMJpegFrame());
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
            }
        }

        if (mMJpegInputStream != null && isCancelled()) {
            try {
                mMJpegInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Cancel the process.
     */
    @Override
    protected void onCancelled() {
        Timber.d("onCancelled");
        mCallback.onCancelled();
    }

    /**
     * Interface of Callback.
     */
    public interface Callback {
        void updatePreview(byte[] previewByteArray);

        void onCancelled();
    }
}
