package com.example.absensiapp.Drawing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.TypedValue
import com.example.absensiapp.Face_Recognition.FaceClassifier
import com.example.absensiapp.LifeFeed.ImageUtils
import java.util.*

class MultiBoxTracker(context: Context) {
    private val screenRects = mutableListOf<Pair<Float, RectF>>()
    private val availableColors = LinkedList<Int>()
    private val trackedObjects = mutableListOf<TrackedRecognition>()
    private val boxPaint = Paint()
    private var frameToCanvasMatrix: Matrix? = null
    private var frameWidth = 0
    private var frameHeight = 0
    private var sensorOrientation = 0

    init {
        for (color in COLORS) {
            availableColors.add(color)
        }

        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 10.0f
        boxPaint.strokeCap = Paint.Cap.ROUND
        boxPaint.strokeJoin = Paint.Join.ROUND
        boxPaint.strokeMiter = 100f
    }

    @Synchronized
    fun setFrameConfiguration(width: Int, height: Int, sensorOrientation: Int) {
        frameWidth = width
        frameHeight = height
        this.sensorOrientation = sensorOrientation
    }

    @Synchronized
    fun drawDebug(canvas: Canvas) {
        val boxPaint = Paint().apply {
            color = Color.RED
            alpha = 200
            style = Paint.Style.STROKE
        }

        for (detection in screenRects) {
            val rect = detection.second
            canvas.drawRect(rect, boxPaint)
        }
    }

    @Synchronized
    fun trackResults(results: List<FaceClassifier.Recognition>, timestamp: Long) {
        processResults(results)
    }

    private fun getFrameToCanvasMatrix(): Matrix {
        return frameToCanvasMatrix ?: Matrix()
    }

    @Synchronized
    fun draw(canvas: Canvas) {
        val rotated = sensorOrientation % 180 == 90
        val multiplier = minOf(
            canvas.height / (if (rotated) frameWidth else frameHeight).toFloat(),
            canvas.width / (if (rotated) frameHeight else frameWidth).toFloat()
        )
        frameToCanvasMatrix = ImageUtils.getTransformationMatrix(
            frameWidth,
            frameHeight,
            (multiplier * (if (rotated) frameHeight else frameWidth)).toInt(),
            (multiplier * (if (rotated) frameWidth else frameHeight)).toInt(),
            sensorOrientation,
            false
        )

        for (recognition in trackedObjects) {
            val trackedPos = RectF(recognition.location)

            getFrameToCanvasMatrix().mapRect(trackedPos)
            boxPaint.color = recognition.color

            // Gambar kotak deteksi wajah tanpa judul
            canvas.drawRect(trackedPos, boxPaint)
        }
    }

    private fun processResults(results: List<FaceClassifier.Recognition>) {
        val rectsToTrack = LinkedList<Pair<Float, FaceClassifier.Recognition>>()

        screenRects.clear()
        val rgbFrameToScreen = Matrix(getFrameToCanvasMatrix())

        for (result in results) {
            if (result.location == null) {
                continue
            }
            val detectionFrameRect = RectF(result.location)

            val detectionScreenRect = RectF()
            rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect)

            screenRects.add(Pair(result.distance ?: 0f, detectionScreenRect))

            if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
                continue
            }

            rectsToTrack.add(Pair(result.distance ?: 0f, result))
        }

        trackedObjects.clear()
        if (rectsToTrack.isEmpty()) {
            return
        }

        for (potential in rectsToTrack) {
            val trackedRecognition = TrackedRecognition()
            trackedRecognition.detectionConfidence = potential.first
            trackedRecognition.location = RectF(potential.second.location)
            trackedRecognition.color = COLORS[trackedObjects.size]
            trackedObjects.add(trackedRecognition)

            if (trackedObjects.size >= COLORS.size) {
                break
            }
        }
    }

    private class TrackedRecognition {
        var location: RectF? = null
        var detectionConfidence: Float = 0f
        var color: Int = 0
    }

    companion object {
        private const val MIN_SIZE = 16.0f
        private val COLORS = intArrayOf(
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.WHITE,
            Color.parseColor("#55FF55"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FF8888"),
            Color.parseColor("#AAAAFF"),
            Color.parseColor("#FFFFAA"),
            Color.parseColor("#55AAAA"),
            Color.parseColor("#AA33AA"),
            Color.parseColor("#0D0068")
        )
    }
}