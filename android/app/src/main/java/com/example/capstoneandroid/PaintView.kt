package com.example.capstoneandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.capstoneandroid.stylusActivity.Companion.paintBrush
import com.example.capstoneandroid.stylusActivity.Companion.path

class PaintView : View{

    var params : ViewGroup.LayoutParams? = null

    companion object{
        var pathList = ArrayList<Path>()
        var colorList = ArrayList<Int>()
        var currentBrush = Color.BLACK;
    }

    constructor(context: Context) : super(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init(){
        paintBrush.isAntiAlias = true
        paintBrush.color = currentBrush
        paintBrush.style = Paint.Style.STROKE
        paintBrush.strokeJoin = Paint.Join.ROUND
        paintBrush.strokeWidth = 6f

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    var preCheckWait : Boolean = false

    override fun onTouchEvent(event: MotionEvent) : Boolean {
        var x = event.x
        var y = event.y

        var pointer_count = event.pointerCount // 현재 터치 발생한 포인터 수를 얻는다.
        if(pointer_count > 2) pointer_count = 2 //3개 이상의 포인트를 터치했더라도 2개까지만 처리를 한다.
        if(pointer_count == 2) {
            preCheckWait = true
        }/*
        if(pointer_count==1 && preCheckWait) {
            preCheckWait= false
            return false
        }*/
        when(event.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_DOWN -> {
                if (pointer_count == 1 && !preCheckWait) {  //한 개 포인트에 대한 DOWN을 얻을 때,
                    path.moveTo(x,y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> { // zoom in, zoom out 후에 바로 선이 그어지지 않도록 체크포인트 구성.
                if (pointer_count==1 && preCheckWait) {
                    preCheckWait= false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (pointer_count == 1 && !preCheckWait) {  //한 개 포인트에 대한 DOWN을 얻을 때,
                    path.lineTo(x, y)
                    pathList.add(path)
                    colorList.add(currentBrush)
                }
            }
            else -> return false
        }
        postInvalidate()
        return false;
    }


    override fun onDraw(canvas: Canvas){
        super.onDraw(canvas)
        for(i in pathList.indices){
            paintBrush.setColor(colorList[i])
            canvas.drawPath(pathList[i], paintBrush)
            invalidate()
        }
    }
}