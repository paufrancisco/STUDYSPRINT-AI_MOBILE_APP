package com.jtdev.umak.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.jtdev.umak.DatabaseHelper;
import com.jtdev.umak.R;
import java.util.ArrayList;

public class Statistics extends Fragment {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView recommendationTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
    }
    private void animateTextView(TextView textView) {
        textView.setAlpha(0f);
        textView.animate()
                .alpha(1f)
                .setDuration(2000)
                .setStartDelay(500)
                .start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        recommendationTextView = view.findViewById(R.id.recommendationTextView);

        if (getActivity() != null) {
            getActivity().setTitle("Statistics");
        }

        loadStatistics();
        return view;
    }

    private void loadStatistics() {
        int totalCompleted = dbHelper.getTotalEasyCardCount();
        int totalCards = dbHelper.getTotalCardCount();
        int hardCount = dbHelper.getTotalHardCardCount();
        int mediumCount = dbHelper.getTotalMediumCardCount();
        int easyCount = dbHelper.getTotalEasyCardCount();

        int completionPercentage = 0;
        if (totalCards > 0) {
            completionPercentage = (totalCompleted * 100) / totalCards;
        }

        setupPieChart(completionPercentage);
        setupBarChart(hardCount, mediumCount, easyCount);
        generateRecommendation(hardCount, mediumCount, easyCount, completionPercentage);
        animateTextView(recommendationTextView);
    }

    private void setupPieChart(int completionPercentage) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completionPercentage, "Completed"));
        entries.add(new PieEntry(100 - completionPercentage, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "Completion");
        dataSet.setColors(getResources().getColor(R.color.button_background), getResources().getColor(R.color.active));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter());

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(getResources().getColor(R.color.black));

        pieChart.animateY(1000, Easing.EaseInOutCubic);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(14f);
        pieChart.getLegend().setTextColor(getResources().getColor(R.color.black));

        pieChart.invalidate();
    }

    private void setupBarChart(int hardCount, int mediumCount, int easyCount) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, hardCount));
        barEntries.add(new BarEntry(1f, mediumCount));
        barEntries.add(new BarEntry(2f, easyCount));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Card Difficulty");
        barDataSet.setColor(getResources().getColor(R.color.button_background));
        barDataSet.setValueTextColor(getResources().getColor(R.color.black));
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Hard", "Medium", "Easy"}));

        YAxis leftYAxis = barChart.getAxisLeft();
        leftYAxis.setGranularity(1f);
        leftYAxis.setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        barChart.setDrawBarShadow(false);
        barChart.setFitBars(true);

        barChart.animateY(1000, Easing.EaseInOutCubic);

        barChart.invalidate();
    }

    private void generateRecommendation(int hardCount, int mediumCount, int easyCount, int completionPercentage) {
        String recommendation;

        if (completionPercentage == 100) {
            recommendation = "Congratulations! You've completed all your cards!";
        } else if (hardCount > mediumCount && hardCount > easyCount) {
            recommendation = "Focus on reviewing the hard cards to improve your understanding. Consider using the Pomodoro timer to break down your study sessions.";
        } else if (mediumCount > hardCount && mediumCount > easyCount) {
            recommendation = "You are doing well! Focus on medium-level cards to refine your knowledge. The Pomodoro timer can help you stay focused and take short breaks.";
        } else {
            recommendation = "Great progress! Keep reviewing the easy cards to reinforce your knowledge. Use the Pomodoro timer to keep a steady study pace.";
        }

        recommendationTextView.setText(recommendation);
        animateTextView(recommendationTextView);
    }

}
