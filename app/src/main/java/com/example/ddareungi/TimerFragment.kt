package com.example.ddareungi


import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
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
import com.example.ddareungi.MainActivity.Companion.timerMin
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
    var hour=0
    /*
    private lateinit var notification:Uri
    private  lateinit var  ring:Ringtone
*/
    lateinit var onTimePickerSetListener:OnTimePickerSetListener


    //fragment->MainActivity 대여시간 선택 전달

    interface OnTimePickerSetListener{
        fun onTimePickerSet(hour:Int);
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
          activitystate=true
        super.onActivityCreated(savedInstanceState)
        timerBtn=activity!!.findViewById(R.id.timerBtn)
        timerTxt = activity!!.findViewById<TextView>(R.id.timerTxt)
        val spinner = activity!!.findViewById<Spinner>(R.id.timerSpinner)
        spinner.onItemSelectedListener = SpinnerSelectedListener()
       // updateUI(!timerState)
        init()
        Log.v("timer","oncreated")


    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onTimePickerSetListener=context as OnTimePickerSetListener
    }





    fun init(){

        timer = object : CountDownTimer(3600000, 1000) {//1시간짜리
        override fun onTick(millisUntilFinished: Long) {


            updateUI(!timerState,selectHour)



        }


            override fun onFinish() {
                activitystate=false
                Log.v("timerstate", "onfinish")
                // timerTxt.text=MainActivity.timerStr
                timerTxt.text = "00:00"
                Log.i("tim_onFinish","00:00")
            }
        }



        timerBtn.setOnClickListener {

            if (timerState) {

                timerBtn.text = "반납 완료"
                timerState = false
               timerMin=59
                timer.start()
                timerSpinner.isEnabled=false
              //  timerStart=true
            } else {
                timerBtn.text = "대여시작"
                timerState = true
                //timerTxt.text="0"+hour.toString()+":00"
                Log.i("tim_btnclick",timerTxt.text.toString())
              //  timermin=59
                timer.cancel()
                timerSpinner.isEnabled=true
            }


        }
    }

    fun updateUI(state:Boolean,selectHour:Boolean){
        var h=hour-1
        if(!selectHour)
              h++

        Log.v("timer updateUI",h.toString())
        if(state){//실행 중
            timerBtn.text = "반납 완료"
            val timerStr="0"+h.toString()+":"+timerMin
            timerTxt.text =timerStr
            Log.i("tim_updateUI실행중",timerTxt.text.toString())
        }else {
            timerBtn.text = "대여시작"
            h++
             timerTxt.text="0"+h.toString()+":00"
            Log.i("tim_updateUI반납",timerTxt.text.toString())
        }
    }
    override fun onDetach() {
        super.onDetach()
        Log.v("timer","onDetache")
        if(timerState){//타이머 상관없엉
            Log.v("timer","타이머 상관없어용")
        }else{//타이머 작동 중
            Log.v("timer","타이머 작동 중")

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
                Log.v("timer","대여시간을 선택하세요")
            }else{
                selectHour=true
                hour=spinnerStr.substring(0,1).toInt()
                 onTimePickerSetListener.onTimePickerSet(spinnerStr.substring(0,1).toInt())

            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }



}