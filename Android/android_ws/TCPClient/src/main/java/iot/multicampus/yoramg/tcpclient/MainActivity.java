package iot.multicampus.yoramg.tcpclient;

import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    public static final int PORT = 6001;

    private Button buttonTest;

    private Handler handler;
    private Socket socket;
    private BufferedReader networkReader = null;
    private BufferedWriter networkWriter = null;
    private TCPClient client = null;

    private RadioGroup rGroup1;
    private RadioButton remoteCtl, visualCtl, cctvCtl;
    private WebView webView;
    private TableLayout remoteWindow, visualWindow, cctvWindow;
    private Button[] bedButtons = new Button[2];
//    private Integer[] bedBtnIDs = {R.id.bedBtn1, R.id.bedBtn2};
    private ArrayList<HashMap<String,String>> Data = new ArrayList<HashMap<String, String>>();
    private ListView listView;
    private String[] tabs = { "VISUAL", "CCTV"};
    private String ipaddress;
    private int port;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //handler = new Handler();

//        rGroup1 = (RadioGroup) findViewById(R.id.Rgroup1);
//        remoteCtl = (RadioButton) findViewById(R.id.remoteCtl);
//        visualCtl = (RadioButton) findViewById(R.id.visualCtl);
//        cctvCtl = (RadioButton) findViewById(R.id.cctvCtl);
//
//        remoteWindow = (TableLayout) findViewById(R.id.remoteWindow);
//        visualWindow = (TableLayout) findViewById(R.id.visualWindow);
//        cctvWindow = (TableLayout) findViewById(R.id.cctvWindow);
//        webView = (WebView)findViewById(R.id.webView);
//        listView = (ListView)findViewById(R.id.listview);
        //btnConnect

//        rGroup1.setVisibility(View.INVISIBLE);

        //서버 접속 시작
        ipaddress = "70.12.113.141";
        port = 6001;

//        if ( client == null ) {
//            try {
//                client = new TCPClient( ipaddress, port );
//                client.start();
//            } catch ( RuntimeException e ) {
//                Toast.makeText( getApplicationContext(), "IP 주소나 포트 번호가 잘못되었습니다.", Toast.LENGTH_LONG ).show();
//            }
//        }
        //서버 접속 끝
        // Adding Toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

//        buttonTest = (Button) findViewById(R.id.buttonTest);
//        buttonTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });
//
//        rGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if(checkedId == R.id.remoteCtl) {
//                    remoteWindow.setVisibility(View.VISIBLE);
//                    visualWindow.setVisibility(View.GONE);
//                    cctvWindow.setVisibility(View.GONE);
//                }else if(checkedId == R.id.visualCtl) {
//                    remoteWindow.setVisibility(View.GONE);
//                    visualWindow.setVisibility(View.VISIBLE);
//                    cctvWindow.setVisibility(View.GONE);
//
//                    new Thread() {
//                        public void run() {
//                            try {
//                                networkWriter.write("5"); // DB 받아오기
//                                networkWriter.newLine();
//                                networkWriter.flush();
//                            } catch (IOException e) {
//                            }
//                        }
//                    }.start();
//
//                }else if(checkedId == R.id.cctvCtl) {
//                    remoteWindow.setVisibility(View.GONE);
//                    visualWindow.setVisibility(View.GONE);
//                    cctvWindow.setVisibility(View.VISIBLE);
//
//                    webView.setWebViewClient(new WebViewClient());
//                    webView.setBackgroundColor(255);
//                    //영상을 폭에 꽉 차게 할려고 했지만 먹히지 않음???
//                    webView.getSettings().setLoadWithOverviewMode(true);
//                    webView.getSettings().setUseWideViewPort(true);
//                    //이건 최신 버전에서는 사용하지 않게됨
//                    //webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
//                    WebSettings webSettings = webView.getSettings();
//                    webSettings.setJavaScriptEnabled(true);
//
//                    //영상을 폭을 꽉 차게 하기 위해 직접 html태그로 작성함.
//                    webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;} div{overflow: hidden;} </style></head><body><div><img src='http://192.168.0.10:8080/stream/video.mjpeg'/></div></body></html>" ,"text/html",  "UTF-8");
//                }
//            }
//        });
//
//        int i = 0;
//        for(i = 0; i < bedButtons.length; ++i) {
//            bedButtons[i] = (Button) findViewById(bedBtnIDs[i]);
//            final int index;
//            index = i+1;
//            bedButtons[i].setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    new Thread() {
//                        public void run() {
//                            try {
//                                networkWriter.write( Integer.toString(index));
//                                networkWriter.newLine();
//                                networkWriter.flush();
//                            }catch(IOException e) {}
//
//                        }
//                    }.start();
//                }
//            });
//        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new VisualFragment(), "VISUAL");
        adapter.addFrag(new CctvFragment(), "CCTV");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onClick(View v) {
//        switch ( v.getId() ) {
//            case R.id.btnFinish:
//                if ( client != null ) {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            client.quit();
//                            client = null;
//                        }
//                    }.start();
//                    Toast.makeText( getApplicationContext(), "접속이 종료되었습니다.", Toast.LENGTH_LONG ).show();
//
//                    btnConnect.setEnabled( true );
//                    btnSend.setEnabled( false );
//                    btnFinish.setEnabled( false );
//                    diconnectedView();
//                }
//                break;
//        }
    }



    public void diconnectedView() {
        rGroup1.setVisibility(View.INVISIBLE);
        remoteWindow.setVisibility(View.GONE);
        visualWindow.setVisibility(View.GONE);
        cctvWindow.setVisibility(View.GONE);
    }

    public class TCPClient extends Thread {
        private final int connection_timeout = 3000;

        boolean loop;
        SocketAddress socketAddress;
        String line;

        private String ip;
        private int port;

        public TCPClient( String ip, int port ) {
            this.ip = ip;
            this.port = port;

            socketAddress = new InetSocketAddress( ip, port );
        }

        @Override
        public void run() {
            try {
                socket = new Socket();
                //socket.setSoTimeout( connection_timeout );
                //socket.setSoLinger( true, connection_timeout );
                socket.connect( socketAddress, connection_timeout );

                networkWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
                InputStreamReader i = new InputStreamReader( socket.getInputStream() );
                networkReader = new BufferedReader( i );

                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( getApplicationContext(), "정상적으로 서버에 접속하였습니다.", Toast.LENGTH_LONG ).show();
                        rGroup1.setVisibility(View.VISIBLE);
                    }
                });

                loop = true;
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            new Thread() {
                public void run() {
                    boolean run = true;
                    try {
                        while (run) {
                            Log.i(this.getClass().getName(), "아무것도 없음 ");
                            line = networkReader.readLine();
                            Log.i(this.getClass().getName(), "realdLIne : " + line);
                            if (line != null) {

                                Log.i(this.getClass().getName(), "아무것도 없음2222 ");
                                if (line.equals("quit"))
                                    run = false;
                                Log.i(this.getClass().getName(), "뭔가 있음 : " + line);

                                runOnUiThread(new Runnable(){
                                    MyAdapter mMyAdapter = new MyAdapter();
                                    @Override
                                    public void run() {

                                        for(int i = 0; i < 10; i++) {
                                            mMyAdapter.addItem(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon), "name_" + i, "contents_" + i);
                                        }
                                        listView.setAdapter(mMyAdapter);
                                    }
                                });
                            }
                        }

                    } catch (IOException e) {
                        Log.i(this.getClass().getName(), "아무것도 없음 errrrrr" + e.toString());
                    }

                }
            }.start();
        }

        public void quit() {
            loop = false;

            try {
                if ( socket != null ) {
                    socket.close();
                    socket = null;
                }
                Thread.sleep( connection_timeout );
            } catch ( InterruptedIOException e ) {

            } catch ( IOException e ) {

            } catch ( Exception e ) {

            }
        }
    }
}
