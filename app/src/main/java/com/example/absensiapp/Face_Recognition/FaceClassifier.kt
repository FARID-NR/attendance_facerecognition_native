package com.example.absensiapp.Face_Recognition

import android.graphics.Bitmap
import android.graphics.RectF

/** Generic interface for interacting with different recognition engines. */
interface FaceClassifier {

    fun register(name: String, recognition: Recognition)

    fun recognizeImage(bitmap: Bitmap, getExtra: Boolean): Recognition

    class Recognition(
        val id: String? = null,
        val title: String?,
        val distance: Float? = null,
        var location: RectF? = null,
        var embedding: Any? = null,
        var crop: Bitmap? = null
    ) {

        constructor(title: String?, embedding: Any?) : this(
            id = null,
            title = title,
            distance = null,
            location = null,
            embedding = embedding,
            crop = null
        )

        fun updateEmbedding(extra: Any?) {
            this.embedding = extra
        }

        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }

            if (title != null) {
                resultString += "$title "
            }

            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f)
            }

            if (location != null) {
                resultString += "$location "
            }

            return resultString.trim()
        }
    }
}
