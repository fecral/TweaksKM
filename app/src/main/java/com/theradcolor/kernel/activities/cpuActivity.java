package com.theradcolor.kernel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.theradcolor.kernel.R;
import com.theradcolor.kernel.utils.kernel.cpu.CPU;

import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

@SuppressLint("SetTextI18n")
public class cpuActivity extends AppCompatActivity {

    LineChartView mChart, nChart;
    List<PointValue> bigValues = new ArrayList<>();
    List<PointValue> littleValues = new ArrayList<>();
    int maxNumberOfPoints = 8;

    TextView bigUsage, littleUsage;
    TextView bigGov, bigMinFreq, bigMaxFreq, litGov, litMinFreq, litMaxFreq;
    CPU cpu;
    float[] mCPUUsages;
    boolean[] mCPUStates;
    int[] mCPUFreqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        mChart = (LineChartView) findViewById(R.id.bigCpu);
        nChart = (LineChartView) findViewById(R.id.littleCpu);

        cpu = new CPU();
        bigUsage = findViewById(R.id.big_cpu_usage);
        littleUsage = findViewById(R.id.little_cpu_usage);
        bigGov = findViewById(R.id.tv_cpu_big_gov);
        bigMinFreq = findViewById(R.id.tv_cpu_big_min_freq);
        bigMaxFreq = findViewById(R.id.tv_cpu_big_max_freq);
        litGov = findViewById(R.id.tv_cpu_lit_gov);
        litMinFreq = findViewById(R.id.tv_cpu_lit_min_freq);
        litMaxFreq = findViewById(R.id.tv_cpu_lit_max_freq);

        mChart.setInteractive(true);
        mChart.setZoomEnabled(false);
        mChart.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);

        nChart.setInteractive(true);
        nChart.setZoomEnabled(false);
        nChart.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);

        List<Line> lines = new ArrayList<>();
        Line line = new Line();
        line.setHasLines(true);
        line.setHasPoints(false);
        line.setColor(getResources().getColor(R.color.colorAccent));
        line.setFilled(true);

        lines.add(line);
        LineChartData data = new LineChartData(lines);
        data.setAxisXBottom(null);
        data.setAxisYLeft(null);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mChart.setLineChartData(data);
        nChart.setLineChartData(data);
        uiInit();
        bigGraph();
        littleGraph();
    }

    public void uiInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bigGov.setText(cpu.getGovernor(cpu.getBigCpu(), true));
                bigMinFreq.setText(cpu.getMinFreq( true) / 1000 +getResources().getString(R.string.mhz));
                bigMaxFreq.setText(cpu.getMaxFreq(true) / 1000 +getResources().getString(R.string.mhz));

                litGov.setText(cpu.getGovernor(cpu.getLITTLECpu(), true));
                litMinFreq.setText(cpu.getMinFreq(cpu.getLITTLECpu(), true) / 1000 +getResources().getString(R.string.mhz));
                litMaxFreq.setText(cpu.getMaxFreq(cpu.getLITTLECpu(), true) / 1000 +getResources().getString(R.string.mhz));
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void bigGraph() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 999; ++i) {
                    try {
                        mCPUUsages = cpu.getCpuUsage();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mCPUStates = new boolean[cpu.getCpuCount()];
                    for (int s = 0; s < mCPUStates.length; s++) {
                        mCPUStates[s] = !cpu.isOffline(s);
                    }
                    mCPUFreqs = new int[cpu.getCpuCount()];
                    for (int c = 0; c < mCPUFreqs.length; c++) {
                        mCPUFreqs[c] = cpu.getCurFreq(c);
                    }

                    LineChartData bigData = mChart.getLineChartData();
                    bigValues.add(new PointValue(i, refreshUsages(mCPUUsages, cpu.getBigCpuRange(), mCPUStates)));

                    Log.d("CPU B", ""+refreshUsages(mCPUUsages, cpu.getBigCpuRange(), mCPUStates));
                    bigUsage.setText((int)refreshUsages(mCPUUsages, cpu.getBigCpuRange(), mCPUStates)
                            +getResources().getString(R.string.percent));

                    bigData.getLines().get(0).setValues(new ArrayList<>(bigValues));

                    mChart.setLineChartData(bigData);
                    bigViewPort();
                }
            }
        }).start();
    }

    private void littleGraph() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 999; ++i) {
                    try {
                        mCPUUsages = cpu.getCpuUsage();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mCPUStates = new boolean[cpu.getCpuCount()];
                    for (int s = 0; s < mCPUStates.length; s++) {
                        mCPUStates[s] = !cpu.isOffline(s);
                    }
                    mCPUFreqs = new int[cpu.getCpuCount()];
                    for (int c = 0; c < mCPUFreqs.length; c++) {
                        mCPUFreqs[c] = cpu.getCurFreq(c);
                    }
                    LineChartData littleData = nChart.getLineChartData();
                    littleValues.add(new PointValue(i, refreshUsages(mCPUUsages, cpu.getLITTLECpuRange(), mCPUStates)));

                    Log.d("CPU L", ""+refreshUsages(mCPUUsages, cpu.getLITTLECpuRange(), mCPUStates));
                    littleUsage.setText((int)refreshUsages(mCPUUsages, cpu.getLITTLECpuRange(), mCPUStates)
                            +getResources().getString(R.string.percent));

                    littleData.getLines().get(0).setValues(new ArrayList<>(littleValues));

                    nChart.setLineChartData(littleData);
                    littleViewPort();
                }
            }
        }).start();
    }

    private void bigViewPort() {
        int mSize = bigValues.size();
        if (mSize > maxNumberOfPoints) {
            final Viewport viewport1 = new Viewport(mChart.getMaximumViewport());
            viewport1.left = mSize - 8;
            mChart.setCurrentViewport(viewport1);
        }
    }

    private void littleViewPort() {
        int nSize = littleValues.size();
        if (nSize > maxNumberOfPoints) {
            final Viewport viewport = new Viewport(nChart.getMaximumViewport());
            viewport.left = nSize - 8;
            nChart.setCurrentViewport(viewport);
        }
    }

    private float refreshUsages(float[] usages, List<Integer> cores, boolean[] coreStates) {
        float graph = 0;
        float average = 0;
        int size = 0;
        for (int core : cores) {
            if (core + 1 < usages.length) {
                if (coreStates[core]) {
                    average += usages[core + 1];
                }
                size++;
            }
        }
        average /= size;
        graph = Math.round(average);
        return graph;
    }

}