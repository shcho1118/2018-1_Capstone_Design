package com.simplemobiletools.calendar.activities

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.extensions.*
import kotlinx.android.synthetic.main.activity_popup.*

class PopupActivity2 : SimpleActivity(), View.OnClickListener {

    private var Confirm1:Button? = null
    private var Confirm2:Button? = null
    private var Confirm3:Button? = null
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        //팝업창에서 위에 어플 타이틀라인 지우는 거
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        //이 문단이 팝업창이 무조건 제일 앞에 뜨도록 해주는 역할 입니다.
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.7f
        window.attributes = layoutParams
        setContentView(R.layout.activity_popup)

        setContent()
    }

    private fun setContent() {

        btn_1.setOnClickListener{ view ->
            println("clicked")
        }
        btn_2.setOnClickListener{ view ->
            println("clicked")
        }
        btn_3.setOnClickListener{ view ->
            println("clicked")
        }
       // Confirm1 = findViewById<View>(R.id.btn_1).setOnClickListener(this) as Button
        //Confirm2 = findViewById<View>(R.id.btn_2).setOnClickListener(this) as Button
       // Confirm3 = findViewById<View>(R.id.btn_3).setOnClickListener(this) as Button

        //Confirm1.setOnClickListener(this)
        //Confirm2.setOnClickListener(this)
        //Confirm3.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_1//확인버튼 눌렀을때
            -> {
                //확인버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                config.defaultDelayAlarmTime2Value = config.defaultDelayAlarmTime2Value + 0
                this.finish()
            }
            R.id.btn_2//취소버튼 눌렀을때
            -> {
                //취소버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                config.defaultDelayAlarmTime2Value = config.defaultDelayAlarmTime2Value + -2
                this.finish()
            }
            R.id.btn_3//취소버튼 눌렀을때
            -> {
                //취소버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                config.defaultDelayAlarmTime2Value = config.defaultDelayAlarmTime2Value + 3
                this.finish()
            }
            else -> {
            }
        }
    }

    //바깥 클릭시 안닫히게함
    fun onTouchEcent(event: MotionEvent): Boolean {
        return if (event.action == MotionEvent.ACTION_OUTSIDE) {
            false
        } else true
    }

    //안드로이드 백버튼 막기
    override fun onBackPressed() {
        return
    }


    fun changeDelay2(changeValue : Int){
        try {
            config.defaultDelayAlarmTime2Value = config.defaultDelayAlarmTime2Value + changeValue
        } catch(e : Exception){
            Log.e("Popup", "문제: " + e!!)
            //Log.e("Popup", "콘피그 값 : " + config.defaultDelayAlarmTime2Value)
        }
    }
}
