package com.lml.baidumaptwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 521;
    MapView mMapView = null;
    TextView mapMessageShow;
    BaiduMap baiduMap;
    ToggleButton toggleButtonLocation;
    ToggleButton toggleButtonLBSTrace;
    Boolean locationOnce = true;
    Button btnLocation;
    CheckBox keyInCoordinateFromBaidu;
    private EditText editTextLatitude;//输入经度
    private EditText editTextLongitude;//输入纬度
    private MapDeviceMessage mapDeviceMessage;//自定义全局信息类
    //定位
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //轨迹追踪
    LBSTraceClient lbsTraceClient;
    //实例化轨迹服务
    Trace trace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        toggleButtonLocation = (ToggleButton) findViewById(R.id.toggleButtonLocation);
        toggleButtonLBSTrace = (ToggleButton) findViewById(R.id.toggleButtonLBSTrace);
        btnLocation = (Button) findViewById(R.id.btnLocation);
        keyInCoordinateFromBaidu = (CheckBox) findViewById(R.id.keyInCoordinateFromBaidu);
        editTextLatitude = (EditText) findViewById(R.id.editTextLatitude);
        editTextLongitude = (EditText) findViewById(R.id.editTextLongitude);
        //程序运行之前首先申请权限
        requestPermissionForLocation();
        //地图的初始化
        mapInitialization();

    }

    private void mapInitialization() {
        //自定义全局信息类
        mapDeviceMessage = new MapDeviceMessage();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
        mapDeviceMessage.setMaxZoomLevel(baiduMap.getMaxZoomLevel());
        Log.d("lml", "最大缩放级别:" + mapDeviceMessage.getMaxZoomLevel());
        mapMessageShow = (TextView) findViewById(R.id.mapMessageShow);
        //定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        /**鹰眼轨迹*/
        //实例化轨迹服务客户端
        lbsTraceClient = new LBSTraceClient(getApplicationContext());
        //位置采集周期
        int gatherInterval = 5;
        //打包周期
        int packInterval = 30;
        //设置位置采集和打包周期
        lbsTraceClient.setInterval(gatherInterval, packInterval);
        //鹰眼服务ID
        long lbsTraceServiceId = 107354;
        //设置本机entity标识
        mapDeviceMessage.setEntityName();
        //轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
        int traceType = 2;
        //实例化轨迹服务
        trace = new Trace(getApplicationContext(), lbsTraceServiceId, mapDeviceMessage.getEntityName(), traceType);
        toggleButtonLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                    if (locationOnce)
                        mLocationClient.start();
                } else
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
            }
        });
        toggleButtonLBSTrace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //实例化开启轨迹服务回调接口
                    OnStartTraceListener startTraceListener = new OnStartTraceListener() {
                        //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
                        @Override
                        public void onTraceCallback(int arg0, String arg1) {
                            Log.d("lml", "消息编码:" + arg0 + ";消息内容:" + arg1);
                        }

                        //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
                        @Override
                        public void onTracePushCallback(byte arg0, String arg1) {
                            Log.d("lml", "接收服务端推送:" + arg0 + ";消息类型:" + arg1);
                        }
                    };
                    //开启轨迹服务
                    lbsTraceClient.startTrace(trace, startTraceListener);
                } else {
                    //实例化停止轨迹服务回调接口
                    OnStopTraceListener stopTraceListener = new OnStopTraceListener() {
                        // 轨迹服务停止成功
                        @Override
                        public void onStopTraceSuccess() {
                            Log.d("lml", "轨迹追踪停止成功！");
                        }

                        // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
                        @Override
                        public void onStopTraceFailed(int arg0, String arg1) {
                            Log.d("lml", "轨迹追踪停止失败；错误编码:" + arg0 + "消息内容:" + arg1);
                        }
                    };
                    //停止轨迹服务
                    lbsTraceClient.stopTrace(trace, stopTraceListener);
                }
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationClient.stop();
                LatLng destinationLatLng;
                locationOnce = true;
                toggleButtonLocation.setChecked(false);
                Double lon = Double.valueOf(editTextLongitude.getText().toString());
                Double lat = Double.valueOf(editTextLatitude.getText().toString());
                LatLng sourceLatLng = new LatLng(lat, lon);
                if (keyInCoordinateFromBaidu.isChecked()) {
                    /**坐标转换*/
                    // 将GPS设备采集的原始GPS坐标转换成百度坐标
                    CoordinateConverter converter = new CoordinateConverter();
                    converter.from(CoordinateConverter.CoordType.GPS);
                    // sourceLatLng待转换坐标
                    converter.coord(sourceLatLng);
                    destinationLatLng = converter.convert();
                    /**坐标转换end*/
                } else destinationLatLng = sourceLatLng;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(destinationLatLng, mapDeviceMessage.getMaxZoomLevel());
                baiduMap.animateMapStatus(u);//动画移动摄像头
                /**地图标注*/
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_geo);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(destinationLatLng)
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                baiduMap.addOverlay(option);
            }
        });
    }

    private void requestPermissionForLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int ownPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (ownPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    public void setTextViewMessage(String str) {
        mapMessageShow.setText(str);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient.isStarted()) {
            //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
            mLocationClient.stop();
            mLocationClient = null;
        }
        /**鹰眼轨迹*/
        //实例化停止轨迹服务回调接口
        OnStopTraceListener stopTraceListener = new OnStopTraceListener() {
            // 轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
                Log.d("lml", "轨迹追踪停止成功！");
            }

            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
                Log.d("lml", "轨迹追踪停止失败；错误编码:" + arg0 + "消息内容:" + arg1);
            }
        };
        //停止轨迹服务
        lbsTraceClient.stopTrace(trace, stopTraceListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("lml", "onRequestPermissionsResult; requestCode:" + requestCode + "permissions" + permissions[0] + "grantResults" + grantResults[0]);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
           Log.d("lml", "定位权限请求回调");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("lml", "定位权限允许");
            }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Log.d("lml", "定位权限拒绝");
            }
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d("lml", "MyLocationListener_onReceiveLocation");
            setTextViewMessage(mapDeviceMessage.receiveLocationShowMessage(location));
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);
            if (locationOnce) {
                locationOnce = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, mapDeviceMessage.getMaxZoomLevel() - 2);
                baiduMap.animateMapStatus(u);
            }
            editTextLongitude.setText(String.valueOf(location.getLongitude()));
            editTextLatitude.setText(String.valueOf(location.getLatitude()));
        }
    }
}
