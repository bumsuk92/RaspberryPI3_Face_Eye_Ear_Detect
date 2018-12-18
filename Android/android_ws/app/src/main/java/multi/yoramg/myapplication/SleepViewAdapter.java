package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * VisualFragment에서 일자별 수면 성공율을 나타내는 List View에 SleepItem을 담는 객체
 */
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SleepViewAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<SleepItem> mItems = new ArrayList<>();
    private HashMap<String, SleepItem> hashItems = new HashMap<>();

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public SleepItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_custom, parent, false);
        }

        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_img) ;
        TextView date = (TextView) convertView.findViewById(R.id.date) ;
        TextView successRate = (TextView) convertView.findViewById(R.id.successRate) ;

        /* 각 리스트에 뿌려줄 아이템을 받아오는데 mMyItem 재활용 */
        SleepItem myItem = getItem(position);

        /* 각 위젯에 세팅된 아이템을 뿌려준다 */
        iv_img.setImageDrawable(myItem.getIcon());
        date.setText(myItem.getDate());
        successRate.setText(myItem.getSuccessRate());

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */


        return convertView;
    }

    /* 아이템 데이터 추가를 위한 함수. 자신이 원하는대로 작성 */
    public void addItem(Drawable img, String date, String successRate) {
        SleepItem mItem = new SleepItem();

        /* MyItem에 아이템을 setting한다. */
        mItem.setIcon(img);
        mItem.setDate(date);
        mItem.setSuccessRate(successRate);

        hashItems.put(date, mItem);
    }

    public void notifyItems() {
        Iterator<String> keys = hashItems.keySet().iterator();
        mItems = new ArrayList<>();
        while(keys.hasNext()) {
            String key = keys.next();
            mItems.add(hashItems.get(key));
        }
    }
}
