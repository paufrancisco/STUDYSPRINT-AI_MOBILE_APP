package com.jtdev.umak.Fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.CountDownTimer;

import com.jtdev.umak.PomodoroService;
import com.jtdev.umak.R;

public class PomodoroNotification extends Fragment {
    private Handler handler = new Handler(Looper.getMainLooper());

    private EditText studyTimeInput, restTimeInput, setsInput;
    private Button saveButton;
    private TextView pomodoroLabel, remainingTimeText, textView2, textView3, textView4;
    private CountDownTimer studyTimer, restTimer;

    public PomodoroNotification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        if (getActivity() != null) {
            getActivity().setTitle("Settings");
        }
        // Initialize TextViews and other views
        pomodoroLabel = rootView.findViewById(R.id.pomodoroLabel);
        remainingTimeText = rootView.findViewById(R.id.remainingTimeText);
        textView2 = rootView.findViewById(R.id.textView2);
        textView3 = rootView.findViewById(R.id.textView3);
        textView4 = rootView.findViewById(R.id.textView4);

        studyTimeInput = rootView.findViewById(R.id.studyTimeInput);
        restTimeInput = rootView.findViewById(R.id.restTimeInput);
        setsInput = rootView.findViewById(R.id.setsInput);
        saveButton = rootView.findViewById(R.id.saveButton);
        animateViews();

        saveButton.setOnClickListener(v -> startPomodoro());

        return rootView;
    }

    private void startPomodoro() {
        String studyTimeStr = studyTimeInput.getText().toString().trim();
        String restTimeStr = restTimeInput.getText().toString().trim();
        String setsStr = setsInput.getText().toString().trim();

        if (TextUtils.isEmpty(studyTimeStr)) studyTimeStr = "25";
        if (TextUtils.isEmpty(restTimeStr)) restTimeStr = "5";
        if (TextUtils.isEmpty(setsStr)) setsStr = "4";

        int studyTime = Integer.parseInt(studyTimeStr);
        int restTime = Integer.parseInt(restTimeStr);
        int sets = Integer.parseInt(setsStr);

        if (studyTime <= 0 || restTime <= 0 || sets <= 0) {
            Toast.makeText(getActivity(), "Please enter positive values", Toast.LENGTH_SHORT).show();
            return;
        }

        startPomodoroTimer(studyTime, restTime, sets);

        studyTimeInput.setText("");
        restTimeInput.setText("");
        setsInput.setText("");
    }

    public void startPomodoroTimer(final int studyTime, final int restTime, final int sets) {
        Intent serviceIntent = new Intent(getActivity(), PomodoroService.class);
        serviceIntent.putExtra("studyTime", studyTime);
        serviceIntent.putExtra("restTime", restTime);
        serviceIntent.putExtra("sets", sets);
        getActivity().startService(serviceIntent);
    }


    private void startPomodoroCycle(final int studyTime, final int restTime, final int sets, final int currentSet,
                                    final NotificationManager notificationManager, final String channelId) {
        if (currentSet <= sets) {
            studyTimer = new CountDownTimer(studyTime * 60000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (getView() != null) {
                        final int minutesRemaining = (int) (millisUntilFinished / 60000);
                        final int secondsRemaining = (int) (millisUntilFinished % 60000) / 1000;
                        final String remainingTime = String.format("%02d:%02d", minutesRemaining, secondsRemaining);

                        handler.post(() -> {
                            TextView remainingTimeText = getView().findViewById(R.id.remainingTimeText);
                            if (remainingTimeText != null) {
                                remainingTimeText.setText("Remaining Time: " + remainingTime);
                            }
                        });
                    }
                }

                @Override
                public void onFinish() {
                    sendNotification(notificationManager, channelId, "Study time is over! Take a break.", "Pomodoro Cycle " + currentSet);

                    restTimer = new CountDownTimer(restTime * 60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            final int minutesRemaining = (int) (millisUntilFinished / 60000);
                            final int secondsRemaining = (int) (millisUntilFinished % 60000) / 1000;
                            final String remainingTime = String.format("%02d:%02d", minutesRemaining, secondsRemaining);

                            handler.post(() -> {
                                TextView remainingTimeText = getView().findViewById(R.id.remainingTimeText);
                                if (remainingTimeText != null) {
                                    remainingTimeText.setText("Remaining Time: " + remainingTime);
                                }
                            });
                        }

                        @Override
                        public void onFinish() {
                            sendNotification(notificationManager, channelId, "Rest time is over! Get ready for the next cycle.", "Pomodoro Cycle " + currentSet);
                            startPomodoroCycle(studyTime, restTime, sets, currentSet + 1, notificationManager, channelId);
                        }
                    }.start();
                }
            }.start();
        } else {
            sendNotification(notificationManager, channelId, "All Pomodoro cycles completed! Well done!", "Pomodoro Timer");
        }
    }

    private void sendNotification(NotificationManager notificationManager, String channelId, String contentText, String title) {
        Notification notification = new Notification.Builder(getActivity(), channelId)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(0, notification);
    }

    private void animateViews() {
        ObjectAnimator fadeInLabel = ObjectAnimator.ofFloat(pomodoroLabel, "alpha", 0f, 1f);
        fadeInLabel.setDuration(1000);
        fadeInLabel.start();

        ObjectAnimator slideInButton = ObjectAnimator.ofFloat(saveButton, "translationY", 500f, 0f);
        slideInButton.setDuration(1000);
        slideInButton.start();

        ObjectAnimator slideInRemainingTime = ObjectAnimator.ofFloat(remainingTimeText, "translationX", -500f, 0f);
        slideInRemainingTime.setDuration(1000);
        slideInRemainingTime.start();

        ObjectAnimator fadeInStudyTime = ObjectAnimator.ofFloat(textView2, "alpha", 0f, 1f);
        fadeInStudyTime.setDuration(1000);
        fadeInStudyTime.start();

        ObjectAnimator fadeInRestTime = ObjectAnimator.ofFloat(textView3, "alpha", 0f, 1f);
        fadeInRestTime.setDuration(1000);
        fadeInRestTime.start();

        ObjectAnimator fadeInSets = ObjectAnimator.ofFloat(textView4, "alpha", 0f, 1f);
        fadeInSets.setDuration(1000);
        fadeInSets.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (studyTimer != null) {
            studyTimer.cancel();
        }
        if (restTimer != null) {
            restTimer.cancel();
        }
    }
}
