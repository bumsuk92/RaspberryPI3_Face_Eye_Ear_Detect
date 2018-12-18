package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * 핵심 UI를 담당하는 MainActivity
 */
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import static android.support.v4.view.PagerAdapter.POSITION_NONE;

public class MainActivity extends AppCompatActivity implements IClient, View.OnClickListener, VisualFragment.ISleepViewListener {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;    // Tab에 따라 화면을 보여주는 변수
    private Button mypageButton, logoutButton;
    private TextView nameText, userRecommand;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private Socket socket = null;
    private Thread worker;
    private FloatingActionButton autoSleepMode;
    private UserItem user;
    private Client client;

    private final MyHandler mHandler = new MyHandler(this);
    private ViewPagerAdapter adapter;

    private SleepViewAdapter sleepViewAdapter;
    private SleepAmountList sleepAmountList;
    private SimpleDateFormat timeFormat;

    private int userMonth;
    private long userRecommandSleepAmount;
    private boolean isWaitMessage;
    private boolean logoutSuccessed;
    private boolean activityMove;



//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.i("아오~~~~~~~~~~~~~~~~~~~~~", "종료");
//        if(!activityMove) {
//            client.stop();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //자동 수면 유도 기능 ON/OFF Floating Button 구현
        autoSleepMode = (FloatingActionButton) findViewById(R.id.autoSleepMode);
        user = UserItem.getUser();
        isWaitMessage = false;  //메세지 여러번 대기 하지 않도록 하는 flag
        activityMove = false;

        //오늘 날짜를 통해 아기의 개월 수 파악
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        Calendar calendar = Calendar.getInstance();

        String today = simpleDateFormat.format(calendar.getTime());
        Date startDate;
        Date endDate;
        long diffDay;
        try{
            startDate = simpleDateFormat.parse(user.getBirth());
            endDate = simpleDateFormat.parse(today);

            diffDay = (endDate.getTime() - startDate.getTime()) / (24*60*60*1000);

            userMonth = (int)diffDay / 30;
        }catch(ParseException e){
            e.printStackTrace();
        }
        //오늘 날짜를 통해 아기의 개월 수 파악 종료
        userRecommand = (TextView) findViewById(R.id.userRecommand);

        //개월 수에 따른 권장 수면량 구하기 시작
        if(userMonth >= 0 && userMonth <= 3) {// 0 ~ 3개월
            userRecommandSleepAmount = 16*60*60L;
            userRecommand.setText("권장 16시간");
        }else if(userMonth >= 4 && userMonth <= 11) {//4 ~ 11개월
            userRecommandSleepAmount = 14*60*60L;
            userRecommand.setText("권장 14시간");
        }else if(userMonth >= 12) {//12개월 이상
            userRecommandSleepAmount = 13*60*60L;
            userRecommand.setText("권장 13시간");
        }
        //개월 수에 따른 권장 수면량 구하기 종료

        sleepAmountList = SleepAmountList.getSleepAmountList();

        client = Client.getClient();
        socket = client.getSocket();
        if(socket.isClosed()) {
            client.connectToServer();
        }

        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();

        logoutButton = (Button) findViewById(R.id.logoutButton);
        mypageButton = (Button) findViewById(R.id.mypageButton);
        nameText = (TextView) findViewById(R.id.userName);


        autoSleepMode.setOnClickListener(this);
        mypageButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);



        // 사용자 이름과 개월 수 표시
        nameText.setText(user.getName() + " " + userMonth + "개월");

        //스마트 아기 침대의 자동수면유도기능의 ON/OFF 상태에 맞춰 버튼 상태 동기화
        if(user.getAutoSleepModeStatus().equals("on")) {
            //Toast.makeText(getApplicationContext(), "on으로 셋팅..", Toast.LENGTH_LONG).show();
            autoSleepMode.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.autoSleepModeOn)));
        }else {
            //Toast.makeText(getApplicationContext(), "off로 셋팅..", Toast.LENGTH_LONG).show();
            autoSleepMode.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.autoSleepModeOff)));
        }
        //로그인 성공 후 이름, 생일, 스마트침대 자동 수면 유도 기능 ON/OFF 상태값 받아오기 끝


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sleepViewAdapter = new SleepViewAdapter();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        //질식상황일 경우 화면 CCTV로 변경
        if(user.isDanger()) {
            Toast.makeText(getApplicationContext(), "질식상황!!!", Toast.LENGTH_LONG).show();
            viewPager.setCurrentItem(1);
            user.setDanger(false);
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        adapter.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if(message.what == 2) { //자동 수면 유도 on
                    activity.user.setAutoSleepModeStatus("on");
                    activity.autoSleepMode.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.autoSleepModeOn)));
                    Toast.makeText(activity.getApplicationContext(), "on으로 셋팅..", Toast.LENGTH_LONG).show();
                }else if(message.what == 3) { //자동 수면 유도 off
                    activity.user.setAutoSleepModeStatus("off");
                    activity.autoSleepMode.setBackgroundTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.autoSleepModeOff)));
                    Toast.makeText(activity.getApplicationContext(), "off으로 셋팅..", Toast.LENGTH_LONG).show();
                }else if(message.what == 21) {
                    Bundle bundle = message.getData();
                    String[] sleepDataObjects = bundle.getString("data").split("@");
                    if(!sleepDataObjects[0].equals("")) {
                        for(String sleepDataObject : sleepDataObjects) {
                            String[] sleepData = sleepDataObject.split("#");
                            if(!activity.sleepAmountList.getDaySleepList().containsKey(sleepData[1])) {  //날짜가 존재하지 않는 경우
                                activity.sleepAmountList.getDaySleepList().put(sleepData[1], new HashMap<String, String>());
                                activity.sleepAmountList.getDaySleepList().get(sleepData[1]).put("complete", "false");//해당 날짜 수면양 계산 필요
                            }

                            if(!activity.sleepAmountList.getDaySleepList().containsKey(sleepData[2])) { //해당 날짜에 해당 시간이 존재하지 않는 경우
                                //해당 날짜에 시간과 상태 추가
                                activity.sleepAmountList.getDaySleepList().get(sleepData[1]).put(sleepData[2], sleepData[0]);
                                activity.sleepAmountList.getDaySleepList().get(sleepData[1]).put("complete", "false");  //해당 날짜 수면양 계산 필요
                            }
                            //서버에서 이미 받은 시간은 안받기위한 효율을 위해 저장
                            activity.sleepAmountList.setLastDate(sleepData[1]);
                            activity.sleepAmountList.setLastTime(sleepData[2]);
                            //Log.i(this.getClass().getName(), "수면 데이터 왔다 : " + sleepData[0] + sleepData[1] + sleepData[2]);
                            //activity.sleepViewAdapter.addItem(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.logo), sleepData[0], sleepData[1], sleepData[2]);
                        }


                        TreeMap<String, HashMap<String, String>>  dateTreeMap = new TreeMap<String, HashMap<String, String>>(activity.sleepAmountList.getDaySleepList());
                        Set<String> dateKeyset = activity.sleepAmountList.getDaySleepList().keySet();
                        Iterator<String> dateKeyiterator = dateTreeMap.descendingKeySet().iterator(); //키값 내림차순 정렬
                        String curKey;
                        while(dateKeyiterator.hasNext()) {
                            curKey = (String)dateKeyiterator.next();
                            HashMap<String, String> curDate= dateTreeMap.get(curKey);

                            //수면양 계산이 이미 끝난 경우 효율을 위해 skip
                            if(curDate.get("complete").equals("true")) continue;

                            TreeMap<String, String>  dayTreeMap = new TreeMap<String, String>(curDate);
                            Set<String> keyset = curDate.keySet();
                            Iterator<String> keyiterator = dayTreeMap.keySet().iterator(); //키값 오름차순 정렬

                            String hasStartTime = "00:00:00";
                            //하루 수면양
                            long sleepAmount = 0L;
                            String curTime = null;
                            String curState = null;
                            while(keyiterator.hasNext()) {
                                curTime = (String)keyiterator.next();
                                curState = dayTreeMap.get(curTime);

                                if(curState.equals("stop") && hasStartTime != null) {   //수면 종료상태이면서 수면 시작 시간이 존재하는 경우
                                    try{
                                        Date startTime = activity.timeFormat.parse(hasStartTime);
                                        Log.i(this.getClass().getName(), "수면 데이터 startTime : " + startTime);
                                        Date endTime = activity.timeFormat.parse(curTime);
                                        Log.i(this.getClass().getName(), "수면 데이터 endTime : " + endTime);
                                        long sec = (endTime.getTime() - startTime.getTime()) / 1000;
                                        sleepAmount += sec;
                                        Log.i(this.getClass().getName(), "수면 데이터 더해진다 : " + sec);
                                    }catch (ParseException e) {}

                                    hasStartTime = null;
                                }
                                if(curState.equals("start")) {   //수면 시작상태
                                    hasStartTime = curTime;
                                }
                            }

                            if(curState != null && curState.equals("start")) {  //수면시작하고 하루가 끝날 경우 하루 끝에서 수면시작시간만큼 수면으로 판단
                                try{
                                    //activity.sleepAmountList.getDaySleepList().get(curTime).put("23:59:59", "stop"); //그래프 도식화 시 23:59:59초까지 잔걸로 판단하도록 하기위해 데이터 추가
                                    Date startTime = activity.timeFormat.parse(hasStartTime);
                                    Log.i(this.getClass().getName(), "마지막이 수면 시작인 데이터 startTime : " + startTime);

                                    Date endTime = activity.timeFormat.parse("23:59:59");
                                    Log.i(this.getClass().getName(), "마지막이 수면 시작인  데이터 endTime : " + endTime);
                                    long sec = (endTime.getTime() - startTime.getTime()) / 1000;
                                    sleepAmount += sec;
                                    Log.i(this.getClass().getName(), "마지막이 수면 시작인  데이터 더해진다 : " + sec);
                                }catch (ParseException e) {}
                            }

                            //수면 계산 완료상태로 수정
                            curDate.put("complete", "true");
                            Log.i(this.getClass().getName(), "수면 아기 개월 수 : " + activity.userMonth);
                            Log.i(this.getClass().getName(), "수면 양 : " + sleepAmount + "권장 수면 양 : " + activity.userRecommandSleepAmount);
                            double successRate = ((double)sleepAmount / activity.userRecommandSleepAmount) * 100;
                            Log.i(this.getClass().getName(), "수면 성공율 나왔다 : " + successRate);
                            Log.i(this.getClass().getName(), "수면 날짜 : " + curKey + "수면 성공 : " + String.format("%.2f",successRate));

                            activity.sleepViewAdapter.addItem(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.logo), curKey, "수면 성공율 : " + String.format("%.2f",successRate) + "%");
                        }
                        activity.sleepViewAdapter.notifyItems();
                        activity.sleepViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.autoSleepMode:
                String value = user.getAutoSleepModeStatus();

                if(!isWaitMessage) {
                    if(value.equals("on")) {
                        sendMessage("#;" + user.getId() + ";01;;&"); //자동 수면 유도 off 요청
                    }else {
                        sendMessage("#;" + user.getId() + ";00;;&"); //자동 수면 유도 on 요청

                    }
                    receiveMessage();
                }

                break;
            case R.id.mypageButton:
                Intent intent = new Intent(getApplicationContext(), RegisterUpdateActivity.class);
                startActivity(intent);
                break;
            case R.id.logoutButton:
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

                logoutSuccessed = false;
                sendMessage("#;" + user.getId() + ";55;;&");

                receiveMessage();
                try{
                    worker.join();
                }catch(InterruptedException e) {}


                if(logoutSuccessed) {
                    //자동 로그인을 위한 정보 삭제
                    SharedPreferences.Editor autoLogin = auto.edit();
                    autoLogin.clear();
                    autoLogin.commit();
                    user.logout();

                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    activityMove = true;
                    finish();
                }

                break;
        }


    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new VisualFragment(), "VISUAL");
        adapter.addFragment(new CctvFragment(), "CCTV");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof VisualFragment) {
                return POSITION_NONE;
            }else {
                return super.getItemPosition(object);
            }

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    //Visual fragment listView에 담길 객체 주입
    @Override
    public SleepViewAdapter onReceiveAdapter() {
        //Log.i(this.getClass().getName(), "수면 데이터 왔다 sleepViewAdapter : " + sleepViewAdapter.toString());
        return sleepViewAdapter;
    }

    @Override
    public boolean isWaitMessage() {
        //Log.i(this.getClass().getName(), "수면 데이터 왔다 sleepViewAdapter : " + sleepViewAdapter.toString());
        return isWaitMessage;
    }

    // 서버와 통신하는 IClient 인터페이스 메소드 구현 중 보내는 역할
    @Override
    public void sendMessage(String data) {
        final String msg = data;
        Log.i(this.getClass().getName(), "data 보낸다.");
        new Thread() {
            public void run() {
                byte[] byteArray = msg.getBytes();
                try{
                    outputStream.write(byteArray);
                }catch(IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
        isWaitMessage = true;
        Log.i(this.getClass().getName(), "data 보내졌다.");
    }

    // 서버와 통신하는 IClient 인터페이스 메소드 구현 중 접속하는 역할
    @Override
    public void connectToServer() {
    }

    // 서버와 통신하는 IClient 인터페이스 메소드 구현 중 받는 역할
    @Override
    public void receiveMessage() {
        worker = new Thread() {
            public void run() {
                try {
                    byte[] byteArray = new byte[256];
                    int size = 0;
                    String totalMessage = "";
                    String message = null;
                    totalMessage = message;
                    while(message == null || message.charAt(message.length()-1) != '&') {
                        size = inputStream.read(byteArray);
                        message = new String(byteArray, 0, size, "UTF-8");
                        totalMessage += message;
                    }
                    Log.i(this.getClass().getName(), "받은 문자 : " + totalMessage);
                    String[] command = totalMessage.split(";");

                    switch (command[2]){
                        case "51":  //로그인 성공
                            String[] userDataObject = command[3].split("@");
                            Message messageToQueue = new Message();
                            messageToQueue.what = 51;
                            Bundle data = new Bundle();
                            data.putString("name", userDataObject[0]);
                            data.putString("birth", userDataObject[1]);
                            data.putString("autoSleepModeStatus", userDataObject[2]);

                            messageToQueue.setData(data);
                            mHandler.sendMessage(messageToQueue);
                            break;
                        case "52":  //로그인 실패
                            break;
                        case "53":  // 디바이스를 먼저 안켜서 로그인 실패
                            break;
                        case "21":  // 수면시간 데이터 전송
                            messageToQueue = new Message();
                            messageToQueue.what = 21;
                            data = new Bundle();
                            data.putString("data", command[3]);
                            messageToQueue.setData(data);
                            mHandler.sendMessage(messageToQueue);

                            break;
                        case "02":  //자동수면 유도 ON완료
                            messageToQueue = new Message();
                            messageToQueue.what = 2;
                            Log.i(this.getClass().getName(), "자동수면 유도 ON");
                            mHandler.sendMessage(messageToQueue);
                            break;
                        case "03":  //자동수면 유도 OFF완료
                            messageToQueue = new Message();
                            messageToQueue.what = 3;
                            Log.i(this.getClass().getName(), "자동수면 유도 OFF");
                            mHandler.sendMessage(messageToQueue);
                            break;
                        case "56":  //로그아웃 성공
                            logoutSuccessed = true;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isWaitMessage = false;
            }
        };

        worker.start();
//        try{
//            worker.join();
//        }catch(InterruptedException e) {}
    }
}

