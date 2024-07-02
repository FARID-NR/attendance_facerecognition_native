package com.example.absensiapp.Face_Recognition

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import com.example.absensiapp.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteFaceRecognition private constructor(context: Context) : FaceClassifier {

    companion object {
        private const val OUTPUT_SIZE = 192
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val WIDTH = 112
        private const val HEIGHT = 112

        @Throws(IOException::class)
        private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
            val fileDescriptor = assets.openFd(modelFilename)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

        @Throws(IOException::class)
        suspend fun create(
            assetManager: AssetManager,
            modelFilename: String,
            inputSize: Int,
            isQuantized: Boolean,
            context: Context
        ): FaceClassifier {
            return TFLiteFaceRecognition(context).apply {
                this.inputSize = inputSize

                try {
                    tfLite = Interpreter(loadModelFile(assetManager, modelFilename))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

                this.isModelQuantized = isQuantized
                val numBytesPerChannel = if (isQuantized) 1 else 4
                imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * numBytesPerChannel)
                imgData.order(ByteOrder.nativeOrder())
                intValues = IntArray(inputSize * inputSize)
            }
        }
    }

    private var isModelQuantized = false
    private var inputSize = 0
    private lateinit var intValues: IntArray
    private lateinit var embeddings: Array<FloatArray>
    private lateinit var imgData: ByteBuffer
    private lateinit var tfLite: Interpreter
    private val registered = HashMap<String, FaceClassifier.Recognition>()

    interface ApiService {
        @POST("api/update-profile")
        suspend fun insertFace(@Body requestBody: FaceRequest)
    }

    data class FaceRequest(val name: String, val embedding: String)

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    override fun register(name: String, rec: FaceClassifier.Recognition) {
        CoroutineScope(Dispatchers.IO).launch {
            insertFaceToDatabase(name, rec.embedding as FloatArray)
            registered[name] = rec
        }
    }

    private fun findNearest(emb: FloatArray): Pair<String, Float>? {
        var ret: Pair<String, Float>? = null
        for ((name, value) in registered) {
            val knownEmb = (value.embedding as Array<FloatArray>)[0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = kotlin.math.sqrt(distance)
            if (ret == null || distance < ret.second) {
                ret = Pair(name, distance)
            }
        }
        return ret
    }

    private fun imageToArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true)
        val intValues = IntArray(WIDTH * HEIGHT)
        resizedBitmap.getPixels(intValues, 0, WIDTH, 0, 0, WIDTH, HEIGHT)
        val floatValues = FloatArray(WIDTH * HEIGHT * 3)
        for (i in intValues.indices) {
            val value = intValues[i]
            floatValues[i * 3 + 0] = ((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD
            floatValues[i * 3 + 1] = ((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD
            floatValues[i * 3 + 2] = ((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD
        }
        return Array(1) { Array(WIDTH) { Array(HEIGHT) { FloatArray(3) } }.also { array ->
            for (y in 0 until HEIGHT) {
                for (x in 0 until WIDTH) {
                    for (c in 0 until 3) {
                        array[y][x][c] = floatValues[(y * WIDTH + x) * 3 + c]
                    }
                }
            }
        }}
    }

    override fun recognizeImage(bitmap: Bitmap, storeExtra: Boolean): FaceClassifier.Recognition {
        val inputArray = imageToArray(bitmap)
        val outputArray = Array(1) { FloatArray(OUTPUT_SIZE) }

        tfLite.run(inputArray, outputArray)

        var distance = Float.MAX_VALUE
        var id = "0"
        var label = "?"

        if (registered.isNotEmpty()) {
            val nearest = findNearest(outputArray[0])
            if (nearest != null) {
                val name = nearest.first
                label = name
                distance = nearest.second
            }
        }

        val rec = FaceClassifier.Recognition(
            id,
            label,
            distance,
            RectF()
        )

        if (storeExtra) {
            rec.embedding = outputArray[0]
        }

        return rec
    }

    private suspend fun insertFaceToDatabase(name: String, embedding: FloatArray) {
        withContext(Dispatchers.IO) {
            val embeddingString = embedding.joinToString(",")
            val request = FaceRequest(name, embeddingString)
            apiService.insertFace(request)
        }
    }
}