package com.example.ddareungi.timer


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.ddareungi.R
import com.example.ddareungi.databinding.TimerFragBinding
import com.example.ddareungi.utils.setupSnackBar
import com.example.ddareungi.viewmodel.TimerViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.timer_frag.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TimerFragment : Fragment() {

    lateinit var uiUpdateReceiver: BroadcastReceiver

    lateinit var timerViewModel: TimerViewModel

    lateinit var binding: TimerFragBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        timerViewModel = activity?.run {
            ViewModelProviders.of(this)[TimerViewModel::class.java]
        }?: throw Exception("Invalid Activity")

        timerViewModel.setIsRunning(TimerService.getIsRunning())

        binding = TimerFragBinding.inflate(inflater, container, false)
            .apply {
                timerVM = timerViewModel
            }
        binding.lifecycleOwner = this.viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupSnackBar()
        setTimerRGCheckedListener()
        setTimerBtnClickListener()
    }

    private fun setTimerRGCheckedListener() {
        binding.timerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val countHour = timerViewModel.countHour.value

            if(timerViewModel.isRunning.value!!) {
                if((checkedId == R.id.one_hour_radio_button && countHour == 1) ||
                    (checkedId == R.id.two_hour_radio_button && countHour == 2)) {

                    timerViewModel.showSnackBarMessage("타이머가 이미 실행 중 입니다")
                    if(countHour == 1) {
                        one_hour_radio_button.isChecked = true
                    }
                    else {
                        two_hour_radio_button.isChecked = true
                    }
                }
            }
            else {
                if (checkedId == R.id.one_hour_radio_button) {
                    timerViewModel.setCountHour(1)
                }
                else if (checkedId == R.id.two_hour_radio_button) {
                    timerViewModel.setCountHour(2)
                }
            }
        }
    }

    private fun setTimerBtnClickListener() {
        binding.setTimerBtnClickListener{
            if(timerViewModel.isRunning.value!!) {
                stopTimer()
            }
            else {
                startTimer()
            }
            timerViewModel.toggleIsRunning()
        }
    }

    private fun setupSnackBar() {
        view?.setupSnackBar(this, timerViewModel.snackbarText, Snackbar.LENGTH_LONG)
    }

    private fun stopTimer() {
        val context = context ?: return

        context.unregisterReceiver(uiUpdateReceiver)
        context.stopService(Intent(context, TimerService::class.java))
    }

    private fun startTimer() {
        val context = context ?: return

        val intent = Intent(context, TimerService::class.java)
        intent.putExtra(TIME, timerViewModel.countHour.value)
        context.startService(intent)
        context.registerReceiver(uiUpdateReceiver, IntentFilter(TimerService.TIMER_ACTION))
    }

    private fun updateCountDown(time: String) {
        if (time != "done") {
            binding.timerTv.text = time
        } else {
            alarm()
            timerViewModel.toggleIsRunning()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TimerService.getIsRunning()

        uiUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent!!.getStringExtra(TimerService.COUNTDOWN_ID)
                updateCountDown(time!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (timerViewModel.isRunning.value!!)
            requireContext().registerReceiver(uiUpdateReceiver, IntentFilter(TimerService.TIMER_ACTION))

        (activity as AppCompatActivity).supportActionBar!!.show()
        (requireActivity()).appbar_title.text = "타이머"
    }

    override fun onPause() {
        super.onPause()
        if (timerViewModel.isRunning.value!!)
            requireContext().unregisterReceiver(uiUpdateReceiver)
    }

    companion object {
        const val TIME = "COUNT_DOWN_TIME"

        fun newInstance() = TimerFragment()
    }

}
