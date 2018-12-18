package multi.yoramg.myapplication;

/**
 * 20181122 / 작성자 : 배한주
 * 핵심 UI(MainActivity)의 Tab layout을 통해 ViewPager에 보여줄 Fragment를 변경하는데 그 중 VISUAL UI를 구현하는 Fragment
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class VisualFragment extends Fragment implements AdapterView.OnItemClickListener{
    private View view;
//    private Button buttonTest1, buttonTest2;
    private IClient client;
    private ListView listView;
    private SleepViewAdapter sleepViewAdapter = null;
    private UserItem user;

    private ISleepViewListener sleepViewListener;
    private SleepAmountList sleepAmountList;

    private boolean isWaitMessage = false;

    public VisualFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(getActivity() != null) {
            if(getActivity() instanceof IClient) {
                client = (IClient)getActivity();
            }else if(getActivity() instanceof ISleepViewListener) {
                sleepViewListener = (ISleepViewListener)getActivity();
                Log.i(this.getClass().getName(), "인터페이스 받아짐");
            }
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.visual_fragment, container, false);

        sleepAmountList = SleepAmountList.getSleepAmountList();
        user = UserItem.getUser();
        listView = (ListView)view.findViewById(R.id.listView);



        if(!isWaitMessage) {
            if(sleepViewAdapter == null) { //가지고 있는 수면 정보가 없는 경우 데이터 다가져오기 요청
                client.sendMessage("#;" + user.getId() + ";20;00000000@00:00:00;&");
            }else {
                int adpaterSize = sleepViewAdapter.getCount();
                client.sendMessage("#;" + user.getId() + ";20;" + sleepAmountList.getLastDate() + "@" + sleepAmountList.getLastTime() +";&"); //가장 최근에 받은 시간 이후의 정보 데이터 가져오기 요청
            }

            client.receiveMessage();
        }

        sleepViewListener = ((ISleepViewListener)getActivity());
        sleepViewAdapter = sleepViewListener.onReceiveAdapter();
        isWaitMessage = sleepViewListener.isWaitMessage();
        listView.setAdapter(sleepViewAdapter);

        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //SleepItem item = (SleepItem)parent.getItemAtPosition(position);
        //Toast.makeText(getActivity(), sleepViewAdapter.getItem(position).getDate(),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity().getApplicationContext(), StackedBarActivity.class);
        intent.putExtra("today", sleepViewAdapter.getItem(position).getDate());
        startActivity(intent);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
        {
//            sleepViewAdapter = sleepViewListener.onReceiveAdapter();
//            listView.setAdapter(sleepViewAdapter);
            //화면에 실제로 보일때
        }
        else
        {
            //preload 될때(전페이지에 있을때)
        }
    }

    public interface ISleepViewListener {
        boolean isWaitMessage();
        SleepViewAdapter onReceiveAdapter();
    }
}
