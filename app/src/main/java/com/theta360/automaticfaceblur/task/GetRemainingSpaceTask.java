package com.theta360.automaticfaceblur.task;

import android.os.AsyncTask;

import com.theta360.automaticfaceblur.network.HttpConnector;

public class GetRemainingSpaceTask extends AsyncTask<Void, Void, Long> {

    private static final long THRESHOLD_VERY_FEW = 2 * 1073741824L; // 2GB
    private static final long THRESHOLD_FEW = 5 * 1073741824L; // 5GB

    private Callback mCallback;
    private HttpConnector mCamera;

    public GetRemainingSpaceTask(Callback callback) {
        this.mCallback = callback;
    }


    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Long doInBackground(Void... params) {
        mCamera = new HttpConnector();
        return mCamera.getRemainingSpaces();
    }

    @Override
    protected void onPostExecute(Long remainingSpace) {
        if (remainingSpace == -1) {
            mCallback.onError();
        } else if (remainingSpace <= THRESHOLD_VERY_FEW) {
            mCallback.onStorageVeryFew();
        } else if ((THRESHOLD_VERY_FEW < remainingSpace) && (remainingSpace <= THRESHOLD_FEW)) {
            mCallback.onStorageFew();
        } else {
            mCallback.onStorageEnough();
        }
    }

    @Override
    protected void onCancelled(Long result) {
        mCamera = null;
    }

    public interface Callback {
        void onStorageFew();

        void onStorageVeryFew();

        void onStorageEnough();

        void onError();
    }

}
