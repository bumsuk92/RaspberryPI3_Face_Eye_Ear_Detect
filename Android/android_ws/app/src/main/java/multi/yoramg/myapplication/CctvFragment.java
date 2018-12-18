package multi.yoramg.myapplication;

/**
 * 20181122 / 작성자 : 배한주
 * 핵심 UI(MainActivity)의 Tab layout을 통해 ViewPager에 보여줄 Fragment를 변경하는데 그 중 CCTV UI를 구현하는 Fragment
 */


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CctvFragment extends Fragment {
    private View view;

    public CctvFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //View화면이 처음 생성될 때 호출되는 메소드
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.cctv_fragment, container, false);

        WebView webView = (WebView)view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(255);
        //영상을 폭에 꽉 차게 할려고 했지만 먹히지 않음???
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        //이건 최신 버전에서는 사용하지 않게됨
        //webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //영상을 폭을 꽉 차게 하기 위해 직접 html태그로 작성함.
        webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;} div{overflow: hidden;} </style></head><body><div><img src='http://192.168.0.10:8080/stream/video.mjpeg'/></div></body></html>" ,"text/html",  "UTF-8");
        //webView.loadUrl("http://raspberrypi-ip:8080/stream/video.mjpeg");

        return view;
    }
}
