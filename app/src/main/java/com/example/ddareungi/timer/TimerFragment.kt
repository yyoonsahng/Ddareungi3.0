package com.example.ddareungi.timer


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.*
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.ddareungi.R
import kotlinx.android.synthetic.main.fragment_timer.view.*

class TimerFragment : Fragment() {

    var timerState = true//반납상태


    lateinit var timer: CountDownTimer
    lateinit var timerBtn: AppCompatButton
    lateinit var timerTxt: TextView
    lateinit var spinner: Spinner
    var hour: Long = 0L
    var secondsremaining: Long = 0
    lateinit var oDialog: AlertDialog.Builder

    lateinit var oneHourBtn: RadioButton
    lateinit var twoHourBtn: RadioButton
    lateinit var timerRadioGroup: ViewGroup
    lateinit var timerTextView: TextView
    lateinit var uiUpdateReceiver: BroadcastReceiver
    var countHour = 1
    var isTimerRunning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_timer, container, false)

        with(root) {
            timerTextView = findViewById(R.id.timer_text_view)
            timerRadioGroup = (findViewById<RadioGroup>(R.id.timer_radio_group)).apply {
                setOnCheckedChangeListener { _, checkedId ->
                    if(!isTimerRunning) {
                        if (checkedId == R.id.one_hour_radio_button) countHour = 1
                        else if (checkedId == R.id.two_hour_radio_button) countHour = 2
                        showLeftTime()
                    }
                    else{
                        if((checkedId==R.id.one_hour_radio_button && countHour==2)
                            || (checkedId==R.id.two_hour_radio_button && countHour==1) )
                            Toast.makeText(context,"타이머가 이미 실행중입니다.\n반납완료를 먼저 눌러주세요",Toast.LENGTH_SHORT).show()
                        if(countHour==1) one_hour_radio_button.isChecked=true else two_hour_radio_button.isChecked=true


                    }
                }
            }
            timerBtn = (findViewById<AppCompatButton>(R.id.timer_button)).apply {
                setOnClickListener {
                    if (isTimerRunning) {
                        stopTimer()
                        text = "대여 시작"
                    } else {
                        startTimer()
                        text = "반납 완료"
                    }
                }
                if (isTimerRunning)
                    text = "반납 완료"
                else
                    text = "대여 시작"
            }
        }

        return root
    }

    private fun showLeftTime() {
        if (!isTimerRunning) {
            if (countHour == 1) {
                timerTextView.text = resources.getString(R.string.one_hour)
            } else if (countHour == 2) {
                timerTextView.text = resources.getString(R.string.two_hour)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTimerRunning = TimerService.getIsRunning()

        uiUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent!!.getStringExtra(TimerService.COUNTDOWN_ID)
                updateCountDown(time)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTimerRunning)
            requireContext().registerReceiver(uiUpdateReceiver, IntentFilter(TimerService.TIMER_ACTION))
        else
            showLeftTime()
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning)
            requireContext().unregisterReceiver(uiUpdateReceiver)
    }

    private fun stopTimer() {
        val context = context ?: return

        context.unregisterReceiver(uiUpdateReceiver)
        context.stopService(Intent(context, TimerService::class.java))
        isTimerRunning = false
        showLeftTime()
    }

    private fun startTimer() {
        val context = context ?: return

        val intent = Intent(context, TimerService::class.java)
        intent.putExtra(TIME, countHour)
        context.startService(intent)
        isTimerRunning = true
        context.registerReceiver(uiUpdateReceiver, IntentFilter(TimerService.TIMER_ACTION))
    }

    private fun updateCountDown(time: String) {
        if (time != "done") {
            timerTextView.text = time
        } else {
            alarm()
            isTimerRunning = false
            showLeftTime()
        }
    }


    private fun alarm() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ring = RingtoneManager.getRingtone(this.context, notification)
        ring.play()

        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(2000)
        }
    }

    companion object {
        const val TIME = "COUNT_DOWN_TIME"
    }


}
