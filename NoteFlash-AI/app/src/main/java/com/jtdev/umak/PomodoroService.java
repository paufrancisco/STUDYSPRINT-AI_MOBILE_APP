package com.jtdev.umak;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jtdev.umak.R;

public class PomodoroService extends android.app.Service {
    private CountDownTimer studyTimer, restTimer;
    private NotificationManager notificationManager;
    private int studyTime = 25, restTime = 5, sets = 4, currentSet = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("pomodoro_channel", "Pomodoro Timer", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPomodoroCycle();
        return START_STICKY;
    }

    private void startPomodoroCycle() {
        studyTimer = new CountDownTimer(studyTime * 60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Send notifications or update UI
                sendNotification("Remaining Time: " + formatTime(millisUntilFinished), "Pomodoro Cycle " + currentSet);
            }

            @Override
            public void onFinish() {
                sendNotification("Study time is over! Take a break.", "Pomodoro Cycle " + currentSet);
                startRestTimer();
            }
        }.start();
    }

    private void startRestTimer() {
        restTimer = new CountDownTimer(restTime * 60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendNotification("Remaining Rest Time: " + formatTime(millisUntilFinished), "Pomodoro Cycle " + currentSet);
            }

            @Override
            public void onFinish() {
                sendNotification("Rest time is over! Get ready for the next cycle.", "Pomodoro Cycle " + currentSet);
                currentSet++;
                if (currentSet <= sets) {
                    startPomodoroCycle();
                } else {
                    stopSelf();
                }
            }
        }.start();
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 60000);
        int seconds = (int) (millis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void sendNotification(String contentText, String title) {
        Notification notification = new Notification.Builder(this, "pomodoro_channel")
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (studyTimer != null) {
            studyTimer.cancel();
        }
        if (restTimer != null) {
            restTimer.cancel();
        }
    }
}
