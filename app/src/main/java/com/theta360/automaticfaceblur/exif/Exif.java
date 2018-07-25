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
package com.theta360.automaticfaceblur.exif;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Exif
 */
public class Exif {
    public Exif() {
    }

    /**
     * Copy metadata.
     *
     * @param fileInUrl url of image which has metadata to be copied.
     * @param fileOutUrl url of image which needs to be pasted metadata.
     * @return true: succeed in copy metadata; false: fail to copy metadata.
     */
    public static boolean copyMetadata(String fileInUrl, String fileOutUrl) {
        String fileTmpUrl = fileOutUrl.replace(".JPG", "_tmp.JPG");

        try (RandomAccessFile fileIn = new RandomAccessFile(fileInUrl, "rw");
                RandomAccessFile fileOut = new RandomAccessFile(fileOutUrl, "rw");
                RandomAccessFile fileTmp = new RandomAccessFile(fileTmpUrl, "rw")) {
            byte[] headExif = new byte[4];
            fileIn.read(headExif);

            byte[] lengthExif = new byte[2];
            fileIn.read(lengthExif);
            int size = ByteBuffer.wrap(lengthExif).getShort();
            byte[] exif = new byte[size - 2];
            fileIn.read(exif);

            byte[] headXmp = new byte[2];
            fileIn.read(headXmp);

            byte[] lengthXmp = new byte[2];
            fileIn.read(lengthXmp);
            size = ByteBuffer.wrap(lengthXmp).getShort();
            byte[] xmp = new byte[size - 2];
            fileIn.read(xmp);

            fileOut.skipBytes(4);
            short le = fileOut.readShort();
            fileOut.skipBytes(le - 2);
            size = (int) (fileOut.length() - fileOut.getFilePointer());
            byte[] image = new byte[size];
            fileOut.read(image);

            fileTmp.write(headExif);
            fileTmp.write(lengthExif);
            fileTmp.write(exif);
            fileTmp.write(headXmp);
            fileTmp.write(lengthXmp);
            fileTmp.write(xmp);
            fileTmp.write(image);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File fileOut = new File(fileOutUrl);
        File fileTmp = new File(fileTmpUrl);
        fileOut.delete();
        fileTmp.renameTo(fileOut);

        return true;
    }
}
