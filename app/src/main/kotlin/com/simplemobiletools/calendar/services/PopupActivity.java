package com.simplemobiletools.calendar.services;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.simplemobiletools.calendar.R;
import com.simplemobiletools.calendar.activities.SimpleActivity;
import com.simplemobiletools.calendar.helpers.Config;


public class PopupActivity extends SimpleActivity implements OnClickListener {

    private Button Confirm1, Confirm2, Confirm3;//환인, 취소 버튼 선언
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //팝업창에서 위에 어플 타이틀라인 지우는 거
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //이 문단이 팝업창이 무조건 제일 앞에 뜨도록 해주는 역할 입니다.
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_popup);

        setContent();
    }

    //버튼 두개를 나타내서 누르면 반응하는 부분입니다
    private void setContent(){
        Confirm1 = (Button)findViewById(R.id.btn_1);
        Confirm2 = (Button)findViewById(R.id.btn_2);
        Confirm3 = (Button)findViewById(R.id.btn_3);

        Confirm1.setOnClickListener(this);
        Confirm2.setOnClickListener(this);
        Confirm3.setOnClickListener(this);
    }


    //버튼을 눌럿을때 지금은 바로 팝업창이 없어지게 되어 잇습니다.
    public void onClick(View v){
        Config t1 = new Config(this);
        int temp = t1.getDefaultDelayAlarmTime2Value();
        switch (v.getId()){
            case R.id.btn_1://확인버튼 눌렀을때
                //확인버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                this.finish();
                break;
            case R.id.btn_2://취소버튼 눌렀을때
                //취소버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                temp = temp + -2;
                t1.setDefaultDelayAlarmTime2Value(temp);
                this.finish();
                break;
            case R.id.btn_3://취소버튼 눌렀을때
                //취소버튼을 눌럿을때 일어날일 을 여기서 구현하거나 디비에 값을 저장하면 될꺼 같아요
                temp = temp + 3;
                t1.setDefaultDelayAlarmTime2Value(temp);
                this.finish();
                break;
            default:
                break;
        }
    }

    //바깥 클릭시 안닫히게함
    public boolean onTouchEcent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }


    //안드로이드 백버튼 막기
    public void onBackPressed(){
        return;
    }

}
