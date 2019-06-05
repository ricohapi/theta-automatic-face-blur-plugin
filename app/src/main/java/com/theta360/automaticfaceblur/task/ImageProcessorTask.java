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

import static com.theta360.automaticfaceblur.MainActivity.DCIM;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.theta360.automaticfaceblur.Face;
import com.theta360.automaticfaceblur.exif.Exif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Process input image and return blurred image.
 */
public class ImageProcessorTask extends AsyncTask<String, Void, Map<String, String>> {
    //Divide the equirectangular image into similar four parts, the rightmost x coordinate of the leftmost part.
    private int mRightmostOfLeftImage;
    private static final double ONE_QUARTER_OF_EQUI = 0.25;
    //Divide the equirectangular image into similar four, the leftmost x coordinate of the rightmost part.
    private int mLeftmostOfRightImage;
    private static final double THREE_QUARTERS_OF_EQUI = 0.75;
    //Maximum of faces can be detected.
    private static final int MAX_FACE = 256;
    public static final String BLURRED_FILE_KEY = "blurred_file_url";
    public static final String ORIGINAL_FILE_KEY = "original_file_url";
    private Bitmap mBitmapToDetectFace;
    private Bitmap mBitmapToBlur;
    private Callback mCallback;

    /**
     * Constructor of ImageProcessorTask.
     *
     * @param callback callback
     */
    public ImageProcessorTask(@NonNull Callback callback) {
        this.mCallback = callback;
    }

    /**
     * Setup the task.
     */
    @Override
    protected void onPreExecute() {

    }

    /**
     * If the bitmap is blurred and its metadata is reserved successfully, returns file path of blurred file.
     *
     * @param params path of file took by http communication
     * @return file path of blurred file.
     */
    @Override
    protected Map<String, String> doInBackground(String... params) {
        Matcher matcher = Pattern.compile("/\\d{3}RICOH.*").matcher(params[0]);
        if (matcher.find()) {
            String fileUrl = DCIM + matcher.group();
            try {
                long start = System.currentTimeMillis();
                Bitmap bitmap = blurInputFile(fileUrl);
                if (!isCancelled()) {
                    String blurredFileUrl = fileUrl.replace("/R", "/B");
                    if (bitmap != null) {
                        try (FileOutputStream fos = new FileOutputStream(blurredFileUrl)) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        }

                        long end = System.currentTimeMillis();
                        Timber.d("blur : %d ms", (end - start));
                    }

                    File blurredFile = new File(blurredFileUrl);
                    File file = new File(fileUrl);
                    if (Exif.copyMetadata(fileUrl, blurredFileUrl)) {
//                        file.delete();
                        Timber.d("fileUrl = %s", blurredFileUrl);
                        Map<String, String> map = new HashMap<>();
                        map.put(BLURRED_FILE_KEY, blurredFileUrl);
                        map.put(ORIGINAL_FILE_KEY, fileUrl);
                        return map;
                    } else {
                        blurredFile.delete();
                    }
                }
            } catch (IOException e) {
                Timber.d(e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Map<String, String> result) {
        if (result != null) {
            mCallback.onSuccess(result);
        } else {
            mCallback.onError(false);
        }
    }

    @Override
    protected void onCancelled() {
        mCallback.onError(true);
    }

    /**
     * Blur faces in the equirectangular image.
     *
     * @param fileUrl path of file to blur
     * @return processed bitmaps
     */
    private Bitmap blurInputFile(@NonNull String fileUrl) throws IOException {
        long start = System.currentTimeMillis();
        inputFile(fileUrl);
        long now = System.currentTimeMillis();
        Timber.d("inputFile : %d", now - start);
        blurFaces();
        now = System.currentTimeMillis();
        Timber.d("blurFaceInEqui : %d", now - start);
        blurFacesOnSides();
        now = System.currentTimeMillis();
        Timber.d("blurFaceEquiTwoEdges : %d", now - start);
        return mBitmapToBlur;
    }

    /**
     * Make bitmap from the file path.
     *
     * @param fileUrl path of file in DCIM
     */
    private void inputFile(String fileUrl) throws IOException {
        if (!isCancelled()) {
            try (FileInputStream fileInputStream = new FileInputStream(fileUrl)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                //To make Android API FaceDetector effective, Bitmap.Config.RGB_565 is used.
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                mBitmapToDetectFace = BitmapFactory.decodeStream(fileInputStream, null, options);
                mRightmostOfLeftImage = getRightmostOfLeftImage();
                Timber.d("mRightmostOfLeftImage %d", mRightmostOfLeftImage);
                mLeftmostOfRightImage = getLeftmostOfRightImage();
                Timber.d("mLeftmostOfRightImage %d", mLeftmostOfRightImage);
            }

            try (FileInputStream fileInputStream = new FileInputStream(fileUrl)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                //To remain image quality, Bitmap.Config.ARGB_8888 is used.
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                mBitmapToBlur = BitmapFactory.decodeStream(fileInputStream, null, options);
            }
        }
    }

    /**
     * Detect faces but segmented faces in the equirectangular image and blur.
     */
    private void blurFaces() {
        if (!isCancelled()) {
            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE];
            FaceDetector faceDetector = new FaceDetector(mBitmapToDetectFace.getWidth(),
                    mBitmapToDetectFace.getHeight(),
                    MAX_FACE);
            int faceIsFound = faceDetector.findFaces(mBitmapToDetectFace, faces);

            for (int index = 0; index < faceIsFound; index++) {
                PointF point = new PointF();
                faces[index].getMidPoint(point);
                if (isCancelled()) {
                    return;
                }
                //Blur a square with width: 3 times the binocular distance, height: 4.5 times the binocular distance to the face.
                int width = (int) faces[index].eyesDistance() * 3;
                int height = (int) (faces[index].eyesDistance() * 4.5);
                int blurStartX = (int) (point.x - width / 2);
                int blurStartY = (int) (point.y - height / 2);

                //Make coordinate not exceed the size of bitmap.
                if (blurStartX >= 0 && blurStartY >= 0 &&
                        blurStartX + width <= mBitmapToDetectFace.getWidth()
                        && blurStartY + height <= mBitmapToDetectFace.getHeight()) {
                    blur(blurStartX, blurStartY, width, height);
                }
            }
        }
    }

    /**
     * Blur the designated area by the calculated binocular coordinates.
     */
    private void blurFacesOnSides() {
        ArrayList<Face> faceList = calculateCoordinateOfEyes();

        for (Face face : faceList) {
            if (isCancelled()) {
                return;
            }
            //Blur a square with width: 1.5 times the binocular distance, height: 4.5 times the binocular distance to each eye.
            int width = (int) (face.getEyeDistance() * 1.5);
            int height = (int) (face.getEyeDistance() * 4.5);
            int leftEyeBlurStartX = (int) (face.getLeftEyeX() - width / 2);
            int leftEyeBlurStartY = (int) (face.getLeftEyeY() - height / 2);
            int rightEyeBlurStartX = (int) (face.getRightEyeX() - width / 2);
            int rightEyeBlurStartY = (int) (face.getRightEyeY() - height / 2);

            if (leftEyeBlurStartX < 0) {
                leftEyeBlurStartX = 0;
            }

            //If start x coordinate of left eye on the right side of the bitmap and blur to draw will exceed the width of bitmap
            //or start x coordinate of right eye will exceed the width of bitmap, adjust coordinate.
            if ((leftEyeBlurStartX >= mLeftmostOfRightImage
                    && leftEyeBlurStartX + width >= mBitmapToDetectFace.getWidth())
                    || rightEyeBlurStartX < 0) {
                //Remove gap between blur and the edge of bitmap.
                while (width % 32 != 0) {
                    width++;
                }
                leftEyeBlurStartX = mBitmapToDetectFace.getWidth() - width;
            }

            //If start x coordinate of right eye on the left side of the bitmap and start x coordinate of left eye on the right side of the bitmap
            //or start x coordinate of right eye will exceed the width of bitmap, adjust coordinate.
            if (rightEyeBlurStartX < 0 || (rightEyeBlurStartX <= mRightmostOfLeftImage
                    && leftEyeBlurStartX >= mLeftmostOfRightImage)) {
                rightEyeBlurStartX = 0;
            }

            //If start x coordinate of right eye on the right side of the bitmap and blur to draw will exceed the width of bitmap,
            //adjust coordinate.
            if (rightEyeBlurStartX >= mLeftmostOfRightImage
                    && rightEyeBlurStartX + width >= mBitmapToDetectFace.getWidth()) {
                //Remove gap between blur and the edge of bitmap.
                while (width % 32 != 0) {
                    width++;
                }
                rightEyeBlurStartX = mBitmapToDetectFace.getWidth() - width;
            }

            //If blur to draw will exceed the height of bitmap, adjust coordinate.
            if (leftEyeBlurStartY + height >= mBitmapToDetectFace.getHeight()) {
                leftEyeBlurStartY = mBitmapToDetectFace.getHeight() - height;
                rightEyeBlurStartY = mBitmapToDetectFace.getHeight() - height;
            }

            blur(leftEyeBlurStartX, leftEyeBlurStartY, width, height);
            blur(rightEyeBlurStartX, rightEyeBlurStartY, width, height);
        }
    }

    /**
     * ArrayList of faces that detected by FaceDetector API on sides of the equirectangular image.
     */
    private ArrayList<Face> calculateCoordinateOfEyes() {
        ArrayList<Face> faceList = new ArrayList<>();
        if (!isCancelled()) {
            //Cut both sides of the equirectangular picture which the size is a quarter of it.
            int trimmingWidth = (int) (mBitmapToDetectFace.getWidth() * 0.25);
            int trimmingHeight = mBitmapToDetectFace.getHeight();

            Bitmap leftTrimmingImage =
                    Bitmap.createBitmap(mBitmapToDetectFace, 0, 0, trimmingWidth, trimmingHeight,
                            null, true);
            Bitmap rightTrimmingImage =
                    Bitmap.createBitmap(mBitmapToDetectFace,
                            mBitmapToDetectFace.getWidth() - trimmingWidth, 0,
                            trimmingWidth, trimmingHeight, null, true);

            //Decide the size of composited bitmap.
            int compositeWidth = trimmingWidth * 2;
            int compositeHeight = trimmingHeight;
            //Composite the two cut bitmap.
            Bitmap compositedImage = Bitmap
                    .createBitmap(compositeWidth, compositeHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(compositedImage);
            canvas.drawBitmap(mBitmapToDetectFace, 0, 0, null);
            canvas.drawBitmap(leftTrimmingImage, trimmingWidth, 0, null);
            canvas.drawBitmap(rightTrimmingImage, 0, 0, null);

            //Detect faces in the composited bitmap.
            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE];
            FaceDetector faceDetector = new FaceDetector(compositedImage.getWidth(),
                    compositedImage.getHeight(), MAX_FACE);
            int faceIsFound = faceDetector.findFaces(compositedImage, faces);
            int middleOfCompositeImage = mRightmostOfLeftImage;

            //Calculate coordinate of left eye and right eye.
            for (int index = 0; index < faceIsFound; index++) {
                PointF point = new PointF();
                faces[index].getMidPoint(point);

                float halfOfEyesDistance = faces[index].eyesDistance() / 2;
                float leftEyeX = point.x - halfOfEyesDistance;
                float rightEyeX = point.x + halfOfEyesDistance;

                //Calculate the binocular coordinates in the original equirectangular image.
                if (leftEyeX < middleOfCompositeImage && rightEyeX < middleOfCompositeImage) {
                    leftEyeX += middleOfCompositeImage * 3;
                    rightEyeX += middleOfCompositeImage * 3;
                } else if (leftEyeX > middleOfCompositeImage
                        && rightEyeX > middleOfCompositeImage) {
                    leftEyeX -= middleOfCompositeImage;
                    rightEyeX -= middleOfCompositeImage;
                } else {
                    leftEyeX += middleOfCompositeImage * 3;
                    rightEyeX -= middleOfCompositeImage;
                }
                Face face = new Face(leftEyeX, point.y, rightEyeX, point.y, halfOfEyesDistance * 2);
                faceList.add(face);
            }
        }
        return faceList;
    }

    /**
     * Blur the designated area.
     *
     * @param blurStartX start X coordinate of the blur
     * @param blurStartY start Y coordinate of the blur
     * @param width      width of the blur
     * @param height     height of the blur
     */
    private void blur(int blurStartX, int blurStartY, int width, int height) {
        if (!isCancelled()) {
            Bitmap blurToDraw = Bitmap
                    .createBitmap(mBitmapToBlur, blurStartX, blurStartY, width, height);
            Canvas canvas = new Canvas(mBitmapToBlur);
            Paint paint = new Paint();
            paint.setAntiAlias(false);
            paint.setDither(true);

            int dot = 32;
            int square = dot * dot;
            for (int i = 0; i < width / dot; i++) {
                for (int j = 0; j < height / dot; j++) {
                    if (isCancelled()) {
                        return;
                    }

                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (int k = 0; k < dot; k++) {
                        for (int l = 0; l < dot; l++) {
                            int dotColor = blurToDraw.getPixel(i * dot + k, j
                                    * dot + l);
                            r += Color.red(dotColor);
                            g += Color.green(dotColor);
                            b += Color.blue(dotColor);
                        }
                    }
                    r = r / square;
                    g = g / square;
                    b = b / square;
                    for (int k = 0; k < dot; k++) {
                        for (int l = 0; l < dot; l++) {
                            blurToDraw.setPixel(i * dot + k, j * dot + l,
                                    Color.rgb(r, g, b));
                        }
                    }
                }
            }
            canvas.drawBitmap(blurToDraw, blurStartX, blurStartY, paint);
        }
    }

    private int getRightmostOfLeftImage() {
        return (int) (mBitmapToDetectFace.getWidth() * ONE_QUARTER_OF_EQUI);
    }

    private int getLeftmostOfRightImage() {
        return (int) (mBitmapToDetectFace.getWidth() * THREE_QUARTERS_OF_EQUI);
    }

    /**
     * Interface of Callback.
     */
    public interface Callback {
        /**
         * Notify when succeed.
         */
        void onSuccess(Map<String, String> fileUrlMap);

        /**
         * Notify when error occurred.
         *
         * @param isCancelled is cancelled or not
         */
        void onError(boolean isCancelled);
    }
}
