package com.example.absensiapp.LifeFeed

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

/** Utility class for manipulating images. */
object ImageUtils {
    // This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
    // are normalized to eight bits.
    private const val kMaxChannelValue = 262143

    /**
     * Utility method to compute the allocated size in bytes of a YUV420SP image of the given
     * dimensions.
     */
    fun getYUVByteSize(width: Int, height: Int): Int {
        // The luminance plane requires 1 byte per pixel.
        val ySize = width * height

        // The UV plane works on 2x2 blocks, so dimensions with odd size must be rounded up.
        // Each 2x2 block takes 2 bytes to encode, one each for U and V.
        val uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2

        return ySize + uvSize
    }

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap The bitmap to save.
     */
    fun saveBitmap(bitmap: Bitmap) {
        saveBitmap(bitmap, "preview.png")
    }

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap The bitmap to save.
     * @param filename The location to save the bitmap to.
     */
    fun saveBitmap(bitmap: Bitmap, filename: String) {
        val root = Environment.getExternalStorageDirectory().absolutePath + File.separator + "tensorflow"
        //LOGGER.i("Saving %dx%d bitmap to %s.", bitmap.getWidth(), bitmap.getHeight(), root)
        val myDir = File(root)

        if (!myDir.mkdirs()) {
            //LOGGER.i("Make dir failed")
        }

        val fname = filename
        val file = File(myDir, fname)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            //LOGGER.e(e, "Exception!")
        }
    }

    fun convertYUV420SPToARGB8888(input: ByteArray, width: Int, height: Int, output: IntArray) {
        val frameSize = width * height
        for (j in 0 until height) {
            var uvp = frameSize + (j shr 1) * width
            var u = 0
            var v = 0

            for (i in 0 until width) {
                val y = 0xff and input[j * width + i].toInt()
                if (i and 1 == 0) {
                    v = 0xff and input[uvp++].toInt()
                    u = 0xff and input[uvp++].toInt()
                }

                output[j * width + i] = YUV2RGB(y, u, v)
            }
        }
    }

    private fun YUV2RGB(y: Int, u: Int, v: Int): Int {
        var y = y
        var u = u
        var v = v
        y = if (y - 16 < 0) 0 else y - 16
        u -= 128
        v -= 128

        val y1192 = 1192 * y
        var r = y1192 + 1634 * v
        var g = y1192 - 833 * v - 400 * u
        var b = y1192 + 2066 * u

        r = if (r > kMaxChannelValue) kMaxChannelValue else if (r < 0) 0 else r
        g = if (g > kMaxChannelValue) kMaxChannelValue else if (g < 0) 0 else g
        b = if (b > kMaxChannelValue) kMaxChannelValue else if (b < 0) 0 else b

        return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
    }

    fun convertYUV420ToARGB8888(
        yData: ByteArray,
        uData: ByteArray,
        vData: ByteArray,
        width: Int,
        height: Int,
        yRowStride: Int,
        uvRowStride: Int,
        uvPixelStride: Int,
        out: IntArray
    ) {
        var yp = 0
        for (j in 0 until height) {
            val pY = yRowStride * j
            val pUV = uvRowStride * (j shr 1)

            for (i in 0 until width) {
                val uvOffset = pUV + (i shr 1) * uvPixelStride

                out[yp++] = YUV2RGB(
                    0xff and yData[pY + i].toInt(),
                    0xff and uData[uvOffset].toInt(),
                    0xff and vData[uvOffset].toInt()
                )
            }
        }
    }

    fun getTransformationMatrix(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int,
        applyRotation: Int,
        maintainAspectRatio: Boolean
    ): Matrix {
        val matrix = Matrix()

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                //LOGGER.w("Rotation of %d % 90 != 0", applyRotation)
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

        val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAspectRatio) {
                val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }

        if (applyRotation != 0) {
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }

        return matrix
    }
}
