package com.example.newcommand2

import android.content.Context
import android.graphics.Paint

import android.graphics.Point

import android.view.MotionEvent

import android.graphics.Canvas
import java.util.ArrayList
import android.util.AttributeSet
import android.graphics.Color
import android.view.View
import com.example.newcommand2.utils.log


class FilledProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        //typeface = Typeface.create("", Typeface.BOLD)
    }

    private var radius = 0.0f                  // Radius of the circle.



    init {
        isClickable = true


    }

    override fun performClick(): Boolean {
        // Give default click listeners priority and perform accessibility/autofill events.
        // Also calls onClickListener() to handle further subclass customizations.
        //if (super.performClick()) return true

        // Rotates between each of the different selection
        // states on each click.
        //fanSpeed = fanSpeed.next()
       // updateContentDescription()
        // Redraw the view.
       // invalidate()
        return true
    }
    var WIDTH  = 0
    var HEIGHT = 0
    /**
     * This is called during layout when the size of this view has changed. If
     * the view was just added to the view hierarchy, it is called with the old
     * values of 0. The code determines the drawing bounds for the custom view.
     *
     * @param width    Current width of this view.
     * @param height    Current height of this view.
     * @param oldWidth Old width of this view.
     * @param oldHeight Old height of this view.
     */
    var LEVEL = 50F

    fun setNewLevel(level : Float) {
        if (level < 1f ){
            //min 0 max 100
            LEVEL = HEIGHT * ( level )
        }else {
            //min 0 max 100
            LEVEL = HEIGHT * ( level / 100F )
        }


        log("IIIIIIIIIIIIIIINNNNIIT ${HEIGHT} ${LEVEL}  ${level}")
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        // Calculate the radius from the smaller of the width and height.
       //radius = (min(width, height) / 2.0 * 0.8).toFloat()
        WIDTH  = width
        HEIGHT = height
    }

    /**
     * Renders view content: an outer circle to serve as the "dial",
     * and a smaller black circle to server as the indicator.
     * The position of the indicator is based on fanSpeed.
     *
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set dial background color based on the selection.
//        paint.color = Color.GRAY
//        canvas.drawRect(0F,600F,450F,1200F,paint)
        paint.color = Color.GRAY
        canvas.drawRect(0F,0F,WIDTH.toFloat(),HEIGHT.toFloat(),paint)

        paint.color = Color.RED
        canvas.drawRect(0F,HEIGHT-LEVEL,WIDTH.toFloat(),HEIGHT.toFloat(),paint)

        //canvas


        //canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        // Draw the indicator circle.
        invalidate()
    }

}