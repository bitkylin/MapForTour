package cc.bitlight.mapfour.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.radar.RadarNearbyInfo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cc.bitlight.mapfour.R;
import cc.bitlight.mapfour.customclass.DeviceMessageApplication;
import cc.bitlight.mapfour.customclass.Main2Activity;
import cc.bitlight.mapfour.customclass.MyRadarNearbyInfo;
import swipemenulistview.SwipeMenu;
import swipemenulistview.SwipeMenuCreator;
import swipemenulistview.SwipeMenuItem;
import swipemenulistview.SwipeMenuListView;

public class NearbyFragment extends Fragment implements View.OnClickListener {
    DeviceMessageApplication application;

    SwipeMenuListView listView;
    View view;
    //位置查询功能
    List<MyRadarNearbyInfo> radarNearbyInfoList = null;
    RadarResultListAdapter radarResultListAdapter;
    SimpleDateFormat simpleDateFormat;
    CoordinatorLayout coordinatorLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("lml", "NearbyPersonFragment_onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("lml", "NearbyPersonFragment_onCreate");
        application = (DeviceMessageApplication) getActivity().getApplication();
        simpleDateFormat = new SimpleDateFormat("yyyy-M-d H:mm:ss", Locale.CHINA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nearby, container, false);
        listView = (SwipeMenuListView) view.findViewById(R.id.ListViewNearbyInfoShow);
        radarResultListAdapter = new RadarResultListAdapter();
        listView.setAdapter(radarResultListAdapter);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayoutContainer);


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getActivity());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xf3, 0x8d, 0xe5)));
                // set item width
                openItem.setWidth(300);
                // set item title
                openItem.setTitle("导航");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xf3, 0x8d, 0xe5)));
                // set item width
                deleteItem.setWidth(300);
                // set item title
                deleteItem.setTitle("聊天");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        listView.setMenuCreator(creator);
        // step 2. listener item click event
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                MyRadarNearbyInfo radarNearbyInfo = radarNearbyInfoList.get(position);
                switch (index) {
                    case 0:
                        Snackbar.make(coordinatorLayout, "已向用户\"" + radarNearbyInfo.userID + "\"发送导航信息", Snackbar.LENGTH_LONG).show();
                        break;
                    case 1:
                        Snackbar.make(coordinatorLayout, "正在与用户\"" + radarNearbyInfo.userID + "\"建立即时通信链接", Snackbar.LENGTH_LONG).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                    Intent intent = new Intent(getContext(), Main2Activity.class);
                                    startActivity(intent);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        listView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("lml", "NearbyPersonFragment_onDetach");
    }

    public void setRadarNearbyInfoList(List<MyRadarNearbyInfo> radarNearbyInfoList, int infoListSize) {
        this.radarNearbyInfoList = radarNearbyInfoList;
        radarResultListAdapter.notifyDataSetChanged();


    }


    public void clearList() {
        radarNearbyInfoList.clear();
        radarResultListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 结果列表listview适配器
     */
    private class RadarResultListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (radarNearbyInfoList == null) {
                return 0;
            } else {
                return radarNearbyInfoList.size();
            }

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyRadarNearbyInfo radarNearbyInfo = radarNearbyInfoList.get(position);
            ViewHolder viewHolder;
            //判断条目是否有缓存
            if (convertView == null) {
                viewHolder = new ViewHolder();
                //把布局文件填充成一个View对象
                convertView = View.inflate(getContext(), R.layout.item_radarnearby_info, null);
                viewHolder.imageViewItem = (ImageView) convertView.findViewById(R.id.imageViewItem);
                viewHolder.itemUserID = (TextView) convertView.findViewById(R.id.itemUserID);
                viewHolder.itemDistance = (TextView) convertView.findViewById(R.id.itemDistance);
                viewHolder.itemTimeStamp = (TextView) convertView.findViewById(R.id.itemTimeStamp);
                viewHolder.itemLayout = (LinearLayout) convertView.findViewById(R.id.itemLayout);
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.imageViewItem.setImageResource(radarNearbyInfo.comments.equals("m") ? R.mipmap.map_portrait_man : R.mipmap.map_portrait_woman);
            viewHolder.itemUserID.setText(radarNearbyInfo.userID);
            switch (radarNearbyInfo.fenceStatus) {
                case 0:
                    viewHolder.itemLayout.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case 1:
                    viewHolder.itemLayout.setBackgroundColor(getResources().getColor(R.color.green));
                    break;
                case 2:
                    viewHolder.itemLayout.setBackgroundColor(getResources().getColor(R.color.pink));
                    break;
            }

            viewHolder.itemDistance.setText("距离" + String.valueOf(radarNearbyInfo.distance) + "米");
            viewHolder.itemTimeStamp.setText(simpleDateFormat.format(radarNearbyInfo.timeStamp));
            return convertView;
        }

        class ViewHolder {
            ImageView imageViewItem;
            TextView itemUserID;
            TextView itemDistance;
            TextView itemTimeStamp;
            LinearLayout itemLayout;
        }

        @Override
        public Object getItem(int index) {
            if (radarNearbyInfoList == null) {
                return new RadarNearbyInfo();
            } else {
                return radarNearbyInfoList.get(index);
            }

        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }
}
