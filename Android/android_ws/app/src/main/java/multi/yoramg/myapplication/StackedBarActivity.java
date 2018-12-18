package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * VisualFragment의 일자별 수면 성공율 List View에서 일자별 상세 그래프를 그리는 데 그 중
 * StackedBar를 그리는 객체
 */
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import multi.yoramg.charting.animation.Easing;
import multi.yoramg.charting.charts.BarChart;
import multi.yoramg.charting.charts.PieChart;
import multi.yoramg.charting.components.Legend;
import multi.yoramg.charting.components.XAxis;
import multi.yoramg.charting.components.XAxis.XAxisPosition;
import multi.yoramg.charting.components.YAxis;
import multi.yoramg.charting.data.BarData;
import multi.yoramg.charting.data.BarDataSet;
import multi.yoramg.charting.data.BarEntry;
import multi.yoramg.charting.data.Entry;
import multi.yoramg.charting.data.PieData;
import multi.yoramg.charting.data.PieDataSet;
import multi.yoramg.charting.data.PieEntry;
import multi.yoramg.charting.formatter.PercentFormatter;
import multi.yoramg.charting.formatter.StackedValueFormatter;
import multi.yoramg.charting.highlight.Highlight;
import multi.yoramg.charting.interfaces.datasets.IBarDataSet;
import multi.yoramg.charting.listener.OnChartValueSelectedListener;
import multi.yoramg.charting.utils.ColorTemplate;
import multi.yoramg.chart.MyValueFormatter;
import multi.yoramg.chart.DemoBase;
import multi.yoramg.charting.utils.MPPointF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

//OnSeekBarChangeListener
public class StackedBarActivity extends DemoBase implements OnChartValueSelectedListener {

    private BarChart barChart;
    private PieChart pieChart;
    private String todayDate;

    private SleepAmountList sleepAmountList;
    private SimpleDateFormat timeFormat;

    private float sleepRate;    //파이차트에서 수면비율을 구하기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_barchart);

        setTitle("SleepPatten Visualizing");

        sleepAmountList = SleepAmountList.getSleepAmountList();
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);

        Intent intent = getIntent();
        todayDate = (String)(intent.getExtras().get("today"));

        barChart = findViewById(R.id.barChart);
        barChart.setOnChartValueSelectedListener(this);

        barChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        barChart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);

        barChart.setDrawGridBackground(false);

        barChart.setDrawBarShadow(false);

        barChart.setDrawValueAboveBar(false);
        barChart.setHighlightFullBarEnabled(false);

        // change the position of the y-labels
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyValueFormatter("H"));
        //y축 갯수 지정
        leftAxis.setLabelCount(24);
        //y축 최솟값 지정
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        //y축 최댓값 지정
        leftAxis.setAxisMaximum(24f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();

        //Stacked Bar Chart에 수면, 비수면 시작, 종료 시점 파악 로직 시작
        HashMap<String, String> curDate = sleepAmountList.getDaySleepList().get(todayDate);
        TreeMap<String, String> dayTreeMap = new TreeMap<String, String>(curDate);
        Set<String> keyset = curDate.keySet();
        Iterator<String> keyiterator = dayTreeMap.keySet().iterator(); //키값 오름차순 정렬

        ArrayList<Float> sleepInfo = new ArrayList<>();
        String hasStartTime = "00:00:00";
        sleepRate = 0f;
        boolean isStartSleep = false;
        String curTime;
        String curState = null;
        while(keyiterator.hasNext()) {
            curTime = (String)keyiterator.next();
            curState = dayTreeMap.get(curTime);
            if(hasStartTime.equals("00:00:00")) {
                if(curState.equals("start")) {
                    isStartSleep = false;
                }else {
                    isStartSleep = true;
                }
            }
            try {
                Date startTime = timeFormat.parse(hasStartTime);
                Date endTime = timeFormat.parse(curTime);
                float hour = (endTime.getTime() - startTime.getTime()) / (1000*60*60f);
                if(curState.equals("stop")) { //종료시점의 상태가 수면 종료이면 구해진 시간은 수면 시간
                    sleepRate += hour;
                }

                sleepInfo.add((float)hour);
                hasStartTime = curTime;
            }catch(ParseException e) {}

        }

        float[] sleepInfoArr = new float[sleepInfo.size()];
        Iterator<Float> sleepIterator = sleepInfo.iterator();
        int w = 0;
        while(sleepIterator.hasNext()) {
            sleepInfoArr[w++] = sleepIterator.next();
        }
        //Stacked Bar Chart에 수면, 비수면 시작, 종료 시점 파악 로직 끝

        values.add(new BarEntry(
                0,
                sleepInfoArr,    // 수면 데이터 정보
                getResources().getDrawable(R.drawable.star)));

        BarDataSet set1;
        set1 = new BarDataSet(values, todayDate + " 수면 분석");
        set1.setDrawIcons(false);
        set1.setColors(getColors());
        if(isStartSleep) {  //처음 시작이 수면인지 비수면인지 파악
            set1.setStackLabels(new String[]{"Sleep", "Non-Sleep"});    // 가로축 이름 지정
        }else {
            set1.setStackLabels(new String[]{"Non-Sleep", "Sleep"});    // 가로축 이름 지정
        }


        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueFormatter(new StackedValueFormatter(false, "", 1));
        data.setValueTextColor(Color.WHITE);
        data.setDrawValues(false);

        barChart.setData(data);


        barChart.setFitBars(true);
        barChart.invalidate();

        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        // chart.setDrawLegend(false);
        /////////////////////////////////////////Pie Chart Start

        pieChart = findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setCenterTextTypeface(tfLight);
        pieChart.setCenterText(generateCenterSpannableText());

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);

        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);

        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        pieChart.setOnChartValueSelectedListener(this);

        pieChart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTypeface(tfRegular);
        pieChart.setEntryLabelTextSize(12f);

        setData();
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("하루 수면 비율 차트\ndeveloped by yoramG");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 12, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 12, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 12, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 12, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }

    private void setData() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry((float) sleepRate,
                "수면",
                getResources().getDrawable(R.drawable.star)));
        entries.add(new PieEntry((float) (24 - sleepRate),
                "비수면",
                getResources().getDrawable(R.drawable.star)));

        PieDataSet dataSet = new PieDataSet(entries, "하루 수면 비율");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(tfLight);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar, menu);
        getMenuInflater().inflate(R.menu.pie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(getApplicationContext(), "아이템 : " + item, Toast.LENGTH_LONG).show();
        switch (item.getItemId()) {
            case R.id.viewGithub: {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/StackedBarActivity.java"));
                startActivity(i);
                break;
            }
            case R.id.actionToggleValues: {
                List<IBarDataSet> sets = barChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    BarDataSet set = (BarDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                barChart.invalidate();
                break;
            }
            case R.id.actionToggleIcons: {
                List<IBarDataSet> sets = barChart.getData()
                        .getDataSets();

                for (IBarDataSet iSet : sets) {

                    BarDataSet set = (BarDataSet) iSet;
                    set.setDrawIcons(!set.isDrawIconsEnabled());
                }

                barChart.invalidate();
                break;
            }
            case R.id.actionToggleHighlight: {
                if (barChart.getData() != null) {
                    barChart.getData().setHighlightEnabled(!barChart.getData().isHighlightEnabled());
                    barChart.invalidate();
                }
                break;
            }
            case R.id.actionTogglePinch: {
                if (barChart.isPinchZoomEnabled())
                    barChart.setPinchZoom(false);
                else
                    barChart.setPinchZoom(true);

                barChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                barChart.setAutoScaleMinMaxEnabled(!barChart.isAutoScaleMinMaxEnabled());
                barChart.notifyDataSetChanged();
                break;
            }
            case R.id.actionToggleBarBorders: {
                for (IBarDataSet set : barChart.getData().getDataSets())
                    ((BarDataSet) set).setBarBorderWidth(set.getBarBorderWidth() == 1.f ? 0.f : 1.f);

                barChart.invalidate();
                break;
            }
            case R.id.animateX: {
                barChart.animateX(2000);
                break;
            }
            case R.id.animateY: {
                barChart.animateY(2000);
                break;
            }
            case R.id.animateXY: {

                barChart.animateXY(2000, 2000);
                break;
            }
            case R.id.actionSave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery();
                } else {
                    requestStoragePermission(barChart);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void saveToGallery() {
        //saveToGallery(barChart, "StackedBarActivity");
        //saveToGallery(pieChart, "PieChartActivity");
    }

//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;

        if(e instanceof BarEntry) { //바 차트
            BarEntry entry = (BarEntry) e;
            if (entry.getYVals() != null) {
                Toast.makeText(getApplicationContext(), "Value : " + entry.getYVals()[h.getStackIndex()], Toast.LENGTH_LONG).show();
                Log.i("VAL SELECTED", "Value: " + entry.getYVals()[h.getStackIndex()]);
            }else{
                Toast.makeText(getApplicationContext(), "Value : " + entry.getY(), Toast.LENGTH_LONG).show();
                Log.i("VAL SELECTED", "Value: " + entry.getY());
            }
        }else { //원형 차트
            Log.i("VAL SELECTED",
                    "Value: " + e.getY() + ", index: " + h.getX()
                            + ", DataSet index: " + h.getDataSetIndex());
        }
    }

    @Override
    public void onNothingSelected() {}

    private int[] getColors() {

        // have as many colors as stack-values per entry
        int[] colors = new int[2];

        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 2);

        return colors;
    }
}