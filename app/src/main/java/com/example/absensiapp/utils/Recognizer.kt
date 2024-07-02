import android.graphics.Bitmap
import android.graphics.Rect
import com.example.absensiapp.AbsensiApp
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Recognizer(private val numThreads: Int? = null) {

    private lateinit var interpreter: Interpreter
    private lateinit var interpreterOptions: Interpreter.Options

    companion object {
        const val WIDTH = 112
        const val HEIGHT = 112
    }

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            interpreterOptions = Interpreter.Options()
            numThreads?.let {
                interpreterOptions.setNumThreads(it)
            }
            interpreter = Interpreter(loadModelFile(), interpreterOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetManager = AbsensiApp.getApp().assets
        val assetFileDescriptor = assetManager.openFd("mobile_face_net.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun imageToArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true)
        val intValues = IntArray(WIDTH * HEIGHT)
        resizedBitmap.getPixels(intValues, 0, WIDTH, 0, 0, WIDTH, HEIGHT)
        val floatValues = FloatArray(WIDTH * HEIGHT * 3)
        for (i in intValues.indices) {
            val value = intValues[i]
            floatValues[i * 3 + 0] = ((value shr 16 and 0xFF) - 127.5f) / 127.5f
            floatValues[i * 3 + 1] = ((value shr 8 and 0xFF) - 127.5f) / 127.5f
            floatValues[i * 3 + 2] = ((value and 0xFF) - 127.5f) / 127.5f
        }
        return arrayOf(Array(WIDTH) { Array(HEIGHT) { FloatArray(3) } }.also { array ->
            for (y in 0 until HEIGHT) {
                for (x in 0 until WIDTH) {
                    for (c in 0 until 3) {
                        array[y][x][c] = floatValues[(y * WIDTH + x) * 3 + c]
                    }
                }
            }
        })
    }

    fun recognize(bitmap: Bitmap, location: Rect): RecognitionEmbedding {
        val input = imageToArray(bitmap)
        val output = Array(1) { FloatArray(192) }

        interpreter.run(input, output)

        return RecognitionEmbedding(location, output[0])
    }
}

