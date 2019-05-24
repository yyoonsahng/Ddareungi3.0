package com.example.timer2

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var hour=1
    var btnState=true //시작 버튼
    enum class TimerState{
        Stopped,Paused,Running
    }
    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds=0L
    private var timerState=TimerState.Stopped
    private var secondsRemaining=0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        timerSpinner.onItemSelectedListener=SpinnerSelectedListener()
        timerBtn.setOnClickListener {
            if(btnState) { //시작버튼 클릭

                startTimer()
                timerState = TimerState.Running
                btnState=false
                timerBtn.text="반납 완료"
            //    timerBtn.setBackgroundResource(R.drawable.btn22)
                timerPrgbar.visibility=View.VISIBLE
            }else{
                timer.cancel()
                finishTimer()
                btnState=true
                timerBtn.text="대여 시작"
               // timerBtn.setBackgroundResource(R.drawable.btn11)
                timerPrgbar.visibility=View.INVISIBLE
            }
        }


    }

    override fun onResume() {
        super.onResume()
        initTimer()
    }

    override fun onPause() {
        super.onPause()
        if(timerState==TimerState.Running){
            timer.cancel()
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timerState,this)

    }

    private fun initTimer(){

        timerState= PrefUtil.getTimerState(this)
        if(timerState==TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()
        secondsRemaining=if(timerState==TimerState.Running)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        if(timerState==TimerState.Running)
            startTimer()

    }

   private fun finishTimer(){
        timerState=TimerState.Stopped
        setNewTimerLength()
        timerPrgbar.progress=0
        PrefUtil.setSecondsRemaining(timerLengthSeconds,this)
        secondsRemaining=timerLengthSeconds
        updateCountdownUI()
    }
    private  fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = finishTimer()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()

    }
    fun setNewTimerLength(){
        val lengthMin= PrefUtil.getTimerLength(this,hour)
        timerLengthSeconds=(lengthMin*60L)
        timerPrgbar.max=timerLengthSeconds.toInt()

    }
    fun setPreviousTimerLength(){
        timerLengthSeconds= PrefUtil.getPreviousTimerLengthSeconds(this)
        timerPrgbar.max=timerLengthSeconds.toInt()

    }
    fun updateCountdownUI(){
        val minUntilFinish=secondsRemaining/60
        val secUntilFinish=secondsRemaining-minUntilFinish*60
        val secStr=secUntilFinish.toString()
        timerTxt.text="$minUntilFinish:${
        if(secStr.length==2)secStr
        else "0"+secStr}"
        timerPrgbar.progress=(timerLengthSeconds-secondsRemaining).toInt()
    }

    inner class SpinnerSelectedListener:AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val str=parent?.getItemAtPosition(position).toString()
            if(str.length>3)hour=1
            else {
                hour = parent?.getItemAtPosition(position).toString().substring(0, 1).toInt()
            }
            timerTxt.text="0"+hour+":00"
            initTimer()
        }
    }

}
