package com.example.ddareungi.timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.ddareungi.MainActivity
import com.example.ddareungi.R


class TimerService : Service(){

    lateinit var timer: CountDownTimer
    var limitTime: Long = 10000
    var notifText = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isRunning = true
        limitTime = intent!!.getIntExtra(TimerFragment.TIME, 1) * MILLS_PER_HOUR - 5 * MILLS_PER_MIN

        if(limitTime == 55 * MILLS_PER_MIN) {
            notifText = "55분 00초"
        } else {
            notifText = "1시간 55분 00초"
        }

        startForeground(NOTIF_ID, getNotification(notifText))

        timer = object: CountDownTimer(limitTime, 10L) {

            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val sec = seconds % 60
                val min = (seconds % 3600) / 60
                val hour = seconds / 3600
                val i = Intent(TIMER_ACTION)
                i.putExtra(COUNTDOWN_ID, String.format("%d:%02d:%02d", hour, min, sec))
                sendBroadcast(i)

                if(hour > 0)
                    notifText = String.format("%d시간 %02d분 %02d초", hour, min, sec)
                else
                    notifText = String.format("%02d분 %02d초", min, sec)

                updateNotification(notifText)
            }

            override fun onFinish() {
                val i = Intent(TIMER_ACTION)
                i.putExtra(COUNTDOWN_ID, "done")

                sendBroadcast(i)
                stopSelf()
            }
        }
        timer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        isRunning = false
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotification(text: String): Notification {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                NotificationChannel("timer notification", "Timer notification", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Builder(this, "timer notification")
            .setSmallIcon(R.drawable.ic_ddareungi_logo)
            .setContentTitle("남은 대여 시간")
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun updateNotification(text: String) {
        val notification = getNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, notification)
    }

    companion object {
        const val TIMER_ACTION = "TIMER_UPDATED"
        const val COUNTDOWN_ID = "COUNTDOWN_ID"
        const val NOTIF_ID = 9
        private const val MILLS_PER_HOUR = 3_600_000L
        private const val MILLS_PER_MIN = 60_000L
        private var isRunning = false

        fun getIsRunning() = isRunning
    }


}
