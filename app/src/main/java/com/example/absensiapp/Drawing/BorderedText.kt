package com.example.absensiapp.Drawing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

/** A class that encapsulates the tedious bits of rendering legible, bordered text onto a canvas. */
class BorderedText(
    private val textSize: Int,
    interiorColor: Int,
    exteriorColor: Int
) {
    private val interiorPaint: Paint = Paint().apply {
        textSize = this@BorderedText.textSize.toFloat()
        color = interiorColor
        style = Paint.Style.FILL
        isAntiAlias = false
        alpha = 255
    }

    private val exteriorPaint: Paint = Paint().apply {
        textSize = this@BorderedText.textSize.toFloat()
        color = exteriorColor
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = this@BorderedText.textSize / 8f
        isAntiAlias = false
        alpha = 255
    }

    fun setTypeface(typeface: Typeface) {
        interiorPaint.typeface = typeface
        exteriorPaint.typeface = typeface
    }

    fun drawText(canvas: Canvas, posX: Float, posY: Float, text: String) {
        canvas.drawText(text, posX, posY, exteriorPaint)
        canvas.drawText(text, posX, posY, interiorPaint)
    }

    fun drawText(canvas: Canvas, posX: Float, posY: Float, text: String, bgPaint: Paint) {
        val width = exteriorPaint.measureText(text)
        val textSize = exteriorPaint.textSize
        val paint = Paint(bgPaint).apply {
            style = Paint.Style.FILL
            alpha = 160
        }
        canvas.drawRect(posX, posY + textSize, posX + width, posY, paint)
        canvas.drawText(text, posX, posY + textSize, interiorPaint)
    }

    fun drawLines(canvas: Canvas, posX: Float, posY: Float, lines: List<String>) {
        var lineNum = 0
        for (line in lines) {
            drawText(canvas, posX, posY - textSize * (lines.size - lineNum - 1), line)
            lineNum++
        }
    }

    fun setInteriorColor(color: Int) {
        interiorPaint.color = color
    }

    fun setExteriorColor(color: Int) {
        exteriorPaint.color = color
    }

    fun getTextSize(): Float {
        return textSize.toFloat()
    }

    fun setAlpha(alpha: Int) {
        interiorPaint.alpha = alpha
        exteriorPaint.alpha = alpha
    }

    fun getTextBounds(line: String, index: Int, count: Int, lineBounds: Rect) {
        interiorPaint.getTextBounds(line, index, count, lineBounds)
    }

    fun setTextAlign(align: Paint.Align) {
        interiorPaint.textAlign = align
        exteriorPaint.textAlign = align
    }
}
