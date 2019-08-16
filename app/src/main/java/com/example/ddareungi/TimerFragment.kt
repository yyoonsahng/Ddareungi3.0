package com.example.ddareungi


import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.example.ddareungi.MainActivity.Companion.activitystate
import com.example.ddareungi.MainActivity.Companion.selectHour
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

    var timerState=true//반납상태


    lateinit var timer:CountDownTimer
    lateinit var timerBtn: Button
    lateinit var timerTxt:TextView
    lateinit var spinner:Spinner
    var hour:Long=0L
    var secondsremaining:Long=0
    lateinit var oDialog: AlertDialog.Builder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activitystate=true
        super.onActivityCreated(savedInstanceState)
        timerBtn=activity!!.findViewById(R.id.timerBtn)
        timerTxt = activity!!.findViewById<TextView>(R.id.timerTxt)
        spinner = activity!!.findViewById<Spinner>(R.id.timerSpinner)
        spinner.onItemSelectedListener = SpinnerSelectedListener()
        init()

    }

    fun init(){

        oDialog=AlertDialog.Builder(this.requireContext(),android.R.style.Theme_DeviceDefault_Dialog_Alert)
        oDialog.setIcon(R.drawable.ddareungimark).setTitle("반납 안내").setMessage("대여 시간 15분 전입니다.").setPositiveButton("확인",null)

       timerBtn.setOnClickListener {

            if (timerState) {
                timerBtn.text = "반납 완료"
                timerState = false
                startTimer(hour)
                timerSpinner.isEnabled=false
            } else {
                timerBtn.text = "대여시작"
                timerState = true
                //timerTxt.text="0"+hour.toString()+":00"
                Log.i("tim_btnclick",timerTxt.text.toString())
                timer.cancel()
                timerSpinner.isEnabled=true
            }


        }
    }

    fun startTimer(rhour:Long){
        timer = object : CountDownTimer(3600000*rhour, 1000) {//rhour시간짜리
        override fun onTick(millisUntilFinished: Long) {
            secondsremaining=millisUntilFinished/1000
            updateUI(!timerState,selectHour)
            if(secondsremaining==3580L) {
                Log.i("alert", secondsremaining.toString())
                oDialog.show()
            }
        }


            override fun onFinish() {
                activitystate=false
                timerTxt.text = "00:00"
            }
        }.start()
    }

    fun alarm(){
        val notification= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val  ring= RingtoneManager.getRingtone(this.context, notification)
        Log.i("timer","ring")
        ring.play()

    }
    fun updateUI(state:Boolean,selectHour:Boolean){
        var h=hour-1
        if(!selectHour)
            h++

        if(state){
            timerBtn.text = "반납 완료"
            val hourUntilFinished=secondsremaining/3600
            val minutesUntilFinished=(secondsremaining-hourUntilFinished*3600)/60
            val minsStr=minutesUntilFinished.toString()
            timerTxt.text ="0$hourUntilFinished:${if(minsStr.length==2) minsStr else "0"+minsStr}"
            Log.i("tim_updateUI실행중",timerTxt.text.toString())
        }else {
            timerBtn.text = "대여시작"
            h++
            timerTxt.text="0"+h.toString()+":00"
            Log.i("tim_updateUI반납",timerTxt.text.toString())
        }


}

    override fun onResume() {
        super.onResume()
        updateUI(!timerState,selectHour)
    }

    inner class SpinnerSelectedListener: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            var spinnerStr=parent?.getItemAtPosition(position).toString()
            if(spinnerStr.length>3){
                timerTxt.text="00:00"

            }else{
                selectHour=true
                hour=spinnerStr.substring(0,1).toLong()
                timerTxt.text="0"+hour.toString()+":00"
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }



}
