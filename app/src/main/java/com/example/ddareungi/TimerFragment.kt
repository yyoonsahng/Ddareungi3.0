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
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.example.ddareungi.MainActivity.Companion.timerStart
import com.example.ddareungi.MainActivity.Companion.timerStr
import com.example.ddareungi.MainActivity.Companion.timermin
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
    var mode="01"
    var count=60
    var isTimer=false
    var nowHour:String?=null
    //lateinit var nowHour:Int="01" //디폴트값이 1시간
    var nowMin:String?=null
    var isReload:Boolean?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mActivity=activity as MainActivity
        Log.i("timer","onActivityCreated에서 initlayout 실행")
        initLayout()
        timerSpinner.onItemSelectedListener = SpinnerSelectedListener()
        timerBtn.setOnClickListener {
            if(!isTimer){ //타이머 실행
                mActivity.runTimer(true,count)
                timerBtn.text="반납 완료"
                isTimer=true
            }
            else{
                mActivity.runTimer(false,count)
                nowHour="00"
                nowMin="00"
                timerBtn.text="대여 시작"
                isTimer=false
            }
        }
    }
    fun initLayout(){
        val mActivity=activity as MainActivity

        if(isTimer) {
            timerBtn.text = "반납 완료"
        }
        else {
            timerBtn.text = "대여 시작"
        }
         if(mActivity.tCount<=0){
            mActivity.runTimer(false,count)
            nowMin="00"
            nowHour=mode
        }
        Log.i("timer",nowHour+ " "+ nowMin)
        textHour.text = nowHour
        textMin.text = nowMin

    }
    fun setData(flag:Boolean,s1:String, s2: String){
        if(flag)
        isReload=true
        nowHour=s1
        nowMin=s2
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
    }

    inner class SpinnerSelectedListener: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val mActivity=activity as MainActivity
            if(position==0){
                Log.i("timer","selectmode/ pos 0 에서 initlayout 실행")
                mode="01"
                if(isReload==null){ //isReload에 값이 있음 -> reLoad 되었다는 의미 -> 1 보이면 안됨
                    nowHour="01"
                    nowMin="00"
                }
                count=5 //원래 60 인데 임의값
            }
            else{
                Log.i("timer","selectmode/ pos 2 에서 initlayout 실행")
                mode="02"
                if(isReload==null){
                    nowHour="02"
                    nowMin="00"
                }
                count=7
            }

            initLayout()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }



}
