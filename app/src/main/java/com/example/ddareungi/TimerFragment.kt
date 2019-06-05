package com.example.ddareungi


import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timer.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class TimerFragment : Fragment() {

    var timerView: View? = null
    var hour=1
    var btnState=true //시작 버튼 (반납 상태)
    enum class TimerState{
        Stopped,Paused,Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds=0L
    private var timerState=TimerState.Stopped
    private var secondsRemaining=0L
    private var minsRemaining=0L
    private lateinit var notification: Uri
    private lateinit var  ringtone: Ringtone
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onStart() {
        super.onStart()
        Log.v("status", "onStart")
    }

    override fun onDetach() {
        super.onDetach()
        Log.v("status", "onDetach")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "타이머"
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        timerSpinner.onItemSelectedListener=SpinnerSelectedListener()
        timerBtn.setOnClickListener {
            if(btnState) { //시작버튼 클릭

                startTimer()
                timerState = TimerState.Running
                btnState=false
                timerBtn.text="반납 완료"
                timerPrgbar.visibility=View.VISIBLE
            }else{//반납완료 버튼 클릭
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
        Log.v("status", "onResume")
    }

    override fun onPause() {
        //옆 프레그먼트로 이동할 때
        super.onPause()
        Log.v("status", "onPause")
        if(timerState==TimerState.Running){
            timer.cancel()
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds,activity!!.applicationContext)
        PrefUtil.setSecondsRemaining(secondsRemaining,activity!!.applicationContext)
        PrefUtil.setTimerState(timerState,activity!!.applicationContext)

    }

    private fun initTimer(){

        timerState= PrefUtil.getTimerState(activity!!.applicationContext)
        if(timerState==TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()
        secondsRemaining=if(timerState==TimerState.Running)
            PrefUtil.getSecondsRemaining(activity!!.applicationContext)
        else
            timerLengthSeconds

        if(timerState==TimerState.Running)
            startTimer()

    }

    private fun finishTimer(){
        timerState=TimerState.Stopped
        setNewTimerLength()
        timerPrgbar.progress=0
        PrefUtil.setSecondsRemaining(timerLengthSeconds,activity!!.applicationContext)
        secondsRemaining=timerLengthSeconds
        //updateCountdownUI()
        timerTxt.text="00:00"

    }
    private  fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 60000, 60000) {
            override fun onFinish() = finishTimer()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000 //남은 초
                //  minsRemaining=secondsRemaining/60
                updateCountdownUI()
                Log.v("timer",secondsRemaining.toString())
                if(secondsRemaining.toInt()==3539){//50분남은거
                    Toast.makeText(activity!!.applicationContext,"onTick15", Toast.LENGTH_SHORT).show()
                    alramPlay()
                }
            }
        }.start()

    }
    fun setNewTimerLength(){

        val lengthMin= PrefUtil.getTimerLength(activity!!.applicationContext,hour)
        Log.v("lengthMin",lengthMin.toString())
        timerLengthSeconds=(lengthMin*60L)
        timerPrgbar.max=timerLengthSeconds.toInt()

    }
    fun setPreviousTimerLength(){
        timerLengthSeconds= PrefUtil.getPreviousTimerLengthSeconds(activity!!.applicationContext)
        timerPrgbar.max=timerLengthSeconds.toInt()

    }
    fun updateCountdownUI(){
        /*val minUntilFinish=secondsRemaining/60-secondsRemaining*60
        val secUntilFinish=secondsRemaining-minUntilFinish*60
        val hourStr=minUntilFinish/60
        val minUntilFinishStr=secondsRemaining/60-hourStr*60
        val minStr=minUntilFinishStr.toString()*/
        Log.v("secondsRemaining",secondsRemaining.toString())
        var secUntilFinish=secondsRemaining.toInt()
        val hour=(secUntilFinish/3600).toInt()
        secUntilFinish-=hour*3600
        val hourStr=hour.toString()
        val min=(secUntilFinish/60).toInt()
        minsRemaining=min.toLong()
        secUntilFinish-=min*60
        val minStr=min.toString()

        /* val secStr=secUntilFinish.toString()
         timerTxt.text="$minUntilFinish:${
         if(secStr.length==2)secStr
         else "0"+secStr}"*/

        // timerTxt.text="0"+"$hourStr:${if(minStr.length==2)minStr
        //else{"0"+minStr}}"

        timerTxt.text="0"+"$hourStr:${
        if(minStr.length>=2)minStr
        else "0"+minStr}"

        timerPrgbar.progress=(timerLengthSeconds-secondsRemaining).toInt()


    }

    inner class SpinnerSelectedListener: AdapterView.OnItemSelectedListener{
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

    fun alramPlay(){
        Log.v("alarm","50")
        notification= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone= RingtoneManager.getRingtone(activity!!.applicationContext,notification)
        ringtone.play()
    }
}
