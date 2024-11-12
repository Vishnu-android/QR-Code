package com.example.qrhub

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ScannerOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val boxPaint: Paint = Paint()
    private val backgroundPaint: Paint = Paint()

    private val boxRect = RectF(100f, 300f, 600f, 800f) // Example box position (adjust as needed)
    private var linePosition = boxRect.top  // Start position of the line (at the top of the box)

    init {
        // Initialize the paint for the box (white with cyan corners)
        boxPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }

        // Initialize the paint for the dark background outside the box
        backgroundPaint.apply {
            color = Color.parseColor("#60000000") // Semi-transparent black
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Start the line animation
        startLineAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the darkened background first
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Now draw the clear (bright) area inside the box (we "cut out" this area from the dark background)
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(boxRect, clearPaint)

        // Draw the scanning box with white border and cyan corners
        canvas.drawRect(boxRect, boxPaint)

        // Draw the animated line moving vertically within the box
        drawAnimatedLine(canvas)
    }

    private fun drawAnimatedLine(canvas: Canvas) {
        val linePaint = Paint().apply {
            color = Color.GREEN // Line color (can be customized)
            strokeWidth = 4f // Line width
            isAntiAlias = true
        }

        // Draw the line at the current position
        canvas.drawLine(boxRect.left, linePosition, boxRect.right, linePosition, linePaint)
    }

    private fun startLineAnimation() {
        // Use a handler to post updates continuously
        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                // Move the line position down by 10 pixels, and reset to the top if it goes out of bounds
                linePosition += 5f
                if (linePosition > boxRect.bottom) {
                    linePosition = boxRect.top
                }

                // Redraw the view
                postInvalidate()

                // Keep the animation going every 16ms (60 FPS)
                handler.postDelayed(this, 16)
            }
        }

        // Start the animation by posting the runnable
        handler.post(runnable)
    }
}
