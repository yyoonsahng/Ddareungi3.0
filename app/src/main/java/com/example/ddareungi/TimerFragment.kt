package com.example.ddareungi


import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
         val timerTxt=activity!!.findViewById<TextView>(R.id.timerTxt)
        val spinner=activity!!.findViewById<Spinner>(R.id.timerSpinner)
         val timer= object:CountDownTimer(3600000,1000){
             override fun onTick(millisUntilFinished: Long) {
                 timerTxt.text=MainActivity.timerStr
             }

             override fun onFinish() {
                 Log.v("timerstate","onfinish")
                            timerTxt.text=MainActivity.timerStr
             }
         }



        timerBtn.setOnClickListener {

            if(timerState){
                timerBtn.text="반납 완료"
                timerState=false
                timer.start()

            }else{
                timerBtn.text="대여시작"
                timerState=true
            }


        }



    }



}
