package cc.bitlight.mapfour.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.List;

import cc.bitlight.mapfour.R;
import cc.bitlight.mapfour.customclass.DeviceMessageApplication;
import cc.bitlight.mapfour.customclass.MyRadarNearbyInfo;

public class MapFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, BaiduMap.OnMarkerClickListener, OnGetGeoCoderResultListener {
    DeviceMessageApplication application;
    //碎片view控件
    boolean isFirstLocate = true;
    TextureMapView mMapView = null;
    BaiduMap baiduMap;
    Switch switchLocation;
    public Switch switchLBSTrace;
    //内部接口定义
    OnclickSearchNearbyListener mlistener;
    //周边雷达获取用户
    List<MyRadarNearbyInfo> radarNearbyInfoList = null;
    int infoListSize;

    //地图覆盖物
    BitmapDescriptor bitmapDescriptorMan;
    BitmapDescriptor bitmapDescriptorWoman;
    BitmapDescriptor bitmapDescriptorCurrent;
    // 历史轨迹起点图标
    BitmapDescriptor bmStart;
    // 历史轨迹终点图标
    BitmapDescriptor bmEnd;
    // 起点图标覆盖物
    MarkerOptions startMarker = null;
    // 终点图标覆盖物
    MarkerOptions endMarker = null;
    MarkerOptions option;
    GeoCoder geoCoder;
    LatLng latLngshow;
    ReverseGeoCodeOption reverseGeoCodeOption;
    //Overlay地图覆盖物控件
    LinearLayout layoutAddOverlayRadarNearbyItem;
    Bundle bundle = null;  //地图覆盖物marker的额外信息
    boolean generalIsMale = true;//地图覆盖物marker的性别
    public PolylineOptions polylineMyTrace = null; //创建折线覆盖物选项类 for系统记录的轨迹
    public PolylineOptions polylineHistoryTrace = null;//创建折线覆盖物选项类 for历史轨迹查询获取的轨迹
    public CircleOptions fenceCircleOverlay = null;//地图覆盖物选项类，创建圆形围栏覆盖物
    //用户信息弹窗
    View viewOverlayItem;
    ImageView imageViewAddOverlayItem;
    TextView tvAddOverlayItemUserID;
    TextView tvAddOverlayItemDistance;
    TextView tvAddOverlayGeoCoder;
    TextView tvAddOverlayItemLatlng;
    Button btnAddOverlayItemTrackQuery;
    Button btnAddOverlayItemGeoFencePlace;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 这是为了保证Activity容器实现了用以回调的接口。如果没有，它会抛出一个异常。
        try {
            mlistener = (OnclickSearchNearbyListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (DeviceMessageApplication) getActivity().getApplication();
        //地图覆盖物初始化
        bitmapDescriptorMan = BitmapDescriptorFactory.fromResource(R.mipmap.map_portrait_mark_man_circle);
        bitmapDescriptorWoman = BitmapDescriptorFactory.fromResource(R.mipmap.map_portrait_mark_woman_circle);
        bmStart = BitmapDescriptorFactory.fromResource(R.mipmap.icon_start);
        bmEnd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_end);
        option = new MarkerOptions();
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        reverseGeoCodeOption = new ReverseGeoCodeOption();

        //管理员权限用户信息弹窗初始化
        viewOverlayItem = View.inflate(getContext(), R.layout.item_map_addoverlay_radarnearby_admin, null);
        tvAddOverlayItemUserID = (TextView) viewOverlayItem.findViewById(R.id.tvAddOverlayItemUserID);
        imageViewAddOverlayItem = (ImageView) viewOverlayItem.findViewById(R.id.imageViewAddOverlayItem);
        tvAddOverlayGeoCoder = (TextView) viewOverlayItem.findViewById(R.id.tvAddOverlayGeoCoder);
        tvAddOverlayItemDistance = (TextView) viewOverlayItem.findViewById(R.id.tvAddOverlayItemDistance);
        tvAddOverlayItemLatlng = (TextView) viewOverlayItem.findViewById(R.id.tvAddOverlayItemLatlng);
        layoutAddOverlayRadarNearbyItem = (LinearLayout) viewOverlayItem.findViewById(R.id.layoutAddOverlayRadarNearbyItem);
        btnAddOverlayItemTrackQuery = (Button) viewOverlayItem.findViewById(R.id.btnAddOverlayItemTrackQuery);
        btnAddOverlayItemGeoFencePlace = (Button) viewOverlayItem.findViewById(R.id.btnAddOverlayItemGeoFencePlace);
        btnAddOverlayItemTrackQuery.setOnClickListener(this);
        btnAddOverlayItemGeoFencePlace.setOnClickListener(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // Inflate the layout for this fragment
        switchLocation = (Switch) view.findViewById(R.id.switchLocation);
        switchLBSTrace = (Switch) view.findViewById(R.id.switchLBSTrace);
        switchLocation.setOnCheckedChangeListener(this);
        switchLBSTrace.setOnCheckedChangeListener(this);
        mMapView = (TextureMapView) view.findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
        application.setMaxZoomLevel(baiduMap.getMaxZoomLevel());
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMarkerClickListener(this);
        //再次进入地图fragment时界面刷新
        if (application.latLng != null) {
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(application.getLatLng(), application.getMaxZoomLevel() - 2);
            baiduMap.animateMapStatus(u);//动画移动摄像头
            if (radarNearbyInfoList != null) {
                refreshMapUI();
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    public void setRadarNearbyInfoList(List<MyRadarNearbyInfo> radarNearbyInfoList, int infoListSize) {
        this.radarNearbyInfoList = radarNearbyInfoList;
        this.infoListSize = infoListSize;
        refreshMapUI();

    }

    //刷新地图中的覆盖物
    public void refreshMapUI() {
        baiduMap.clear();
        List<String> entityNameListTemp = new ArrayList<>();
        if (radarNearbyInfoList != null)
            for (RadarNearbyInfo radarNearbyInfo : radarNearbyInfoList) {
                bitmapDescriptorCurrent = radarNearbyInfo.comments.equals("m") ? bitmapDescriptorMan : bitmapDescriptorWoman;
                option.icon(bitmapDescriptorCurrent);
                option.position(radarNearbyInfo.pt);
                Bundle bundle = new Bundle();

                entityNameListTemp.add(radarNearbyInfo.userID);
                bundle.putString("userID", radarNearbyInfo.userID);
                bundle.putString("general", radarNearbyInfo.comments);
                bundle.putInt("distance", radarNearbyInfo.distance);
                option.extraInfo(bundle);
                baiduMap.addOverlay(option);
            }
        if (polylineMyTrace != null) {
            polylineMyTrace.color(getResources().getColor(R.color.deepBlue));
            polylineMyTrace.points(application.getLatLngList());
            baiduMap.addOverlay(polylineMyTrace);
        }
        if (polylineHistoryTrace != null) {
            polylineHistoryTrace.color(getResources().getColor(R.color.deepBlue));
            if (bundle != null)
                if (!bundle.getString("userID").equals(application.getEntityName()))
                    polylineHistoryTrace.color(getResources().getColor(bundle.getString("general").equals("m") ? R.color.deepBlue : R.color.pink));
            polylineHistoryTrace.points(application.latLngHistoryList);
            baiduMap.addOverlay(polylineHistoryTrace);
            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(application.latLngHistoryList.get(application.latLngHistoryList.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加终点图标
            endMarker = new MarkerOptions().position(application.latLngHistoryList.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);
            baiduMap.addOverlay(startMarker);
            baiduMap.addOverlay(endMarker);
        }
        if (fenceCircleOverlay != null) {
            baiduMap.addOverlay(fenceCircleOverlay);
        }
        application.entityNameList = entityNameListTemp;
    }

    //地图中marker监听器回调接口
    //BaiduMap.OnMarkerClickListener接口的方法
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("lml", "MapFragment:覆盖物被点击");
        baiduMap.hideInfoWindow();
        if (marker != null) {
            latLngshow = marker.getPosition();
            reverseGeoCodeOption.location(latLngshow);
            geoCoder.reverseGeoCode(reverseGeoCodeOption);
            tvAddOverlayGeoCoder.setText("正在获取详细位置");
            bundle = marker.getExtraInfo();

            generalIsMale = bundle.getString("general").equals("m");
            layoutAddOverlayRadarNearbyItem.setBackgroundColor(getResources().getColor(generalIsMale ? R.color.blue : R.color.pink));
            imageViewAddOverlayItem.setImageResource(generalIsMale ? R.mipmap.map_portrait_man : R.mipmap.map_portrait_woman);
            tvAddOverlayItemUserID.setText(bundle.getString("userID"));
            tvAddOverlayItemDistance.setText("距离" + bundle.getInt("distance") + "米        ");
            tvAddOverlayItemLatlng.setText("坐标：   " + latLngshow.latitude + "  ,  " + latLngshow.longitude + "           ");
            Log.d("lml", "MapFragment显示的信息:" + bundle.getString("userID"));
            Log.d("lml", bundle.getString("general") + ";" + generalIsMale);
            baiduMap.showInfoWindow(new InfoWindow(viewOverlayItem, marker.getPosition(), -70));
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(marker.getPosition());
            baiduMap.animateMapStatus(update);
            return true;
        } else
            return false;
    }

    //创建地理编码检索监听者
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        Log.d("lml", "获取地址反编码信息成功");
        tvAddOverlayGeoCoder.setText(reverseGeoCodeResult.getAddress());
    }

    //在地图上绘制或更新地理围栏
    public void createOrUpdateFenceShow(int geoFenceRadius, LatLng latLng) {
        CircleOptions fenceCircleOverlayTemp = new CircleOptions();//实例化地理围栏圆形覆盖物
        fenceCircleOverlayTemp.fillColor(0x16ff00bb); //园内填充颜色
        fenceCircleOverlayTemp.center(latLng);  //圆的原点坐标
        fenceCircleOverlayTemp.stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x7b)));//圆的边框
        fenceCircleOverlayTemp.radius(geoFenceRadius);  //圆的半径
        fenceCircleOverlay = fenceCircleOverlayTemp;
        refreshMapUI();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchLBSTrace:
                if (isChecked) {
                    polylineMyTrace = new PolylineOptions();
                    refreshMapUI();
                    mlistener.toolbarOperateCloseTrackOrOpenColor(true);
                } else {
                    mlistener.toolbarOperateCloseTrackOrOpenColor(false);
                }
                break;
            case R.id.switchLocation:
                if (isChecked) {
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, false, null));
                    baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(baiduMap.getMaxZoomLevel() - 2));
                } else
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
                break;
        }
    }

    //按钮点击监听
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //用户信息弹窗点击按钮事件：轨迹追踪
            case R.id.btnAddOverlayItemTrackQuery:
                mlistener.userQueryHistoryTrack(bundle.getString("userID"));
                baiduMap.hideInfoWindow();
                break;
            //用户信息弹窗点击按钮事件：设置围栏
            case R.id.btnAddOverlayItemGeoFencePlace:
                mlistener.geoFenceRadiusSettingDialogShow(bundle.getString("userID"), latLngshow);
                baiduMap.hideInfoWindow();
                break;
        }
    }


    public interface OnclickSearchNearbyListener {
        //改变toolbar中,轨迹清除图标的颜色,也可用于关闭轨迹
        //true:点亮轨迹清除图标
        //false:清除所有轨迹并熄灭图标
        void toolbarOperateCloseTrackOrOpenColor(boolean b);

        //查询其他用户历史轨迹
        void userQueryHistoryTrack(String userId);

        //显示地理围栏设置弹窗
        void geoFenceRadiusSettingDialogShow(String userId, LatLng latLng);
    }

    public void setBaiduMapLocationData(MyLocationData locData) {
        baiduMap.setMyLocationData(locData);
        if (isFirstLocate) {
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(application.getLatLng(), application.getMaxZoomLevel() - 2);
            baiduMap.animateMapStatus(u);//动画移动摄像头
            isFirstLocate = false;
        }
        Log.d("lml", "MapFragment - 设置一次位置");
    }

}
