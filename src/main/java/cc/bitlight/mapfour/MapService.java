package cc.bitlight.mapfour;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyInfo;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarNearbySearchSortType;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.radar.RadarUploadInfoCallback;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnGeoFenceListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.bitlight.mapfour.customclass.DeviceMessageApplication;
import cc.bitlight.mapfour.customclass.MyRadarNearbyInfo;
import cc.bitlight.mapfour.customclass.json.GsonService;
import cc.bitlight.mapfour.customclass.json.HistoryTrackData;
import cc.bitlight.mapfour.customclass.json.fence.CreateFenceCallbackJson;
import cc.bitlight.mapfour.customclass.json.fence.QueryFenceListJson;
import cc.bitlight.mapfour.customclass.json.fence.QueryMonitoredStatusJson;
import cc.bitlight.mapfour.customclass.json.fence.TracePushFenceCallbackJson;

/**
 * RadarUploadInfoCallback:周边雷达上传信息回调接口（实现方法：onUploadInfoCallback）
 */
public class MapService extends Service implements RadarUploadInfoCallback, RadarSearchListener, OnStartTraceListener, OnStopTraceListener {
  DeviceMessageApplication application;
  //定位
  public LocationClient mLocationClient = null;
  public BDLocationListener locationListener;
  //周边雷达
  RadarSearchManager radarSearchManager = null;    //查询周边管理
  RadarNearbySearchOption radarNearbyOption = null;//查找周边参数设置
  MyOnTrackListener myOnTrackListener;             //OnTrackListener接口的实现类，实现轨迹监听器回调方法
  //鹰眼轨迹
  LBSTraceClient lbsTraceClient;
  long lbsTraceServiceId = 107354; //鹰眼服务ID
  Trace trace;
  int traceType;
  //地理围栏
  int preFenceId = -50;//地理围栏的Id,其中-50是随意设置的不会取到的数
  boolean queryFenceEntityStatus = false;
  int delayTime = 1000 * 60 * 60 * 24 * 7;
  List<QueryMonitoredStatusJson.MonitoredPersonStatusesBean> monitoredPersonStatusesBeanList = null;//监控对象列表
  //轨迹服务中地理围栏监听器
  MyOnGeoFenceListener myOnGeoFenceListener;
  MyOnEntityListener myOnEntityListener;
  /**
   * 广播
   */
  //广播接收器
  BroadcastReceiver myServiceReceiver;
  //本地广播管理器
  LocalBroadcastManager mLocalBroadcastManager;
  //常量
  //时间常量
  final int TIME_DELAY_NORMAL = 1000 * 60 * 60 * 24 * 2;
  final int TIME_DELAY_SHORT = 1000 * 60 * 10;

  public MapService() {
  }


  @Override
  public void onCreate() {
    Log.d("lml", "MapService:onCreate");
    super.onCreate();
    SDKInitializer.initialize(getApplicationContext());
    application = (DeviceMessageApplication) getApplication();
    initializeService();
    mLocationClient.start();
  }

  void initializeService() {

    /**-------------定位---------------*/
    locationListener = new MyLocationListener();
    mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
    mLocationClient.registerLocationListener(locationListener);    //注册监听函数
    LocationClientOption option = new LocationClientOption();
    option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
    int span = 5000;
    option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
    option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
    option.setOpenGps(true);//可选，默认false,设置是否使用gps
    option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
    option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
    option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
    option.setCoorType("bd09ll");
    mLocationClient.setLocOption(option);
    /**-------------周边雷达---------------*/
    //实例化
    radarSearchManager = RadarSearchManager.getInstance();
    radarNearbyOption = new RadarNearbySearchOption();
    radarNearbyOption.pageCapacity(50);
    radarNearbyOption.sortType(RadarNearbySearchSortType.distance_from_far_to_near);
    radarNearbyOption.pageNum(0);
    radarNearbyOption.radius(5000);

    /**-------------鹰眼轨迹--------------*/
    //实例化轨迹服务客户端
    lbsTraceClient = new LBSTraceClient(getApplicationContext());
    // 设置定位模式
    lbsTraceClient.setLocationMode(LocationMode.High_Accuracy);
    //位置采集周期
    int gatherInterval = 5;
    //打包周期
    int packInterval = 30;
    //设置位置采集和打包周期
    lbsTraceClient.setInterval(gatherInterval, packInterval);
    //轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
    traceType = 2;
    //轨迹监听器
    myOnTrackListener = new MyOnTrackListener();
    lbsTraceClient.setOnTrackListener(myOnTrackListener);
    /**-------------地理围栏---------------*/
    //OnTrackListener接口的实现类，实现轨迹监听器回调方法

    //轨迹服务中地理围栏监听器
    myOnGeoFenceListener = new MyOnGeoFenceListener();
    //Entity监听器
    myOnEntityListener = new MyOnEntityListener();
    lbsTraceClient.setOnGeoFenceListener(myOnGeoFenceListener);
    lbsTraceClient.setOnEntityListener(myOnEntityListener);

    /**--------------广播------------------*/
    mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    myServiceReceiver = new myServiceReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("cc.bitlight.broadcast.service.start");
    intentFilter.addAction("cc.bitlight.broadcast.service.stop");
    intentFilter.addAction("cc.bitlight.broadcast.track.queryhistorytrack");
    intentFilter.addAction("cc.bitlight.broadcast.geofence.query");
    intentFilter.addAction("cc.bitlight.broadcast.mytemp");
    mLocalBroadcastManager.registerReceiver(myServiceReceiver, intentFilter);
  }

  //轨迹查询
  //历史轨迹查询
  void queryHistoryTrack(String entityName) {
    //是否返回精简的结果（0 : 否，1 : 是）
    int simpleReturn = 0;
    // 是否返回纠偏后轨迹（0 : 否，1 : 是）
    int isProcessed = 1;
    // 分页大小
    int pageSize = 1000;
    // 分页索引
    int pageIndex = 1;
    //开始时间
    int startTime = (int) (application.getMillisecondStartTime() / 1000);
    //开始时间
    int endTime = (int) (application.getMillisecondEndTime() / 1000);
    lbsTraceClient.queryProcessedHistoryTrack(lbsTraceServiceId, entityName, simpleReturn, isProcessed, startTime, endTime, pageSize, pageIndex, myOnTrackListener);
  }

  /**
   * 显示历史轨迹
   */
  protected void handleHistoryTrackString(String historyTrack) {

    HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack, HistoryTrackData.class);
    if (historyTrackData != null && historyTrackData.getStatus() == 0 & historyTrackData.getListPoints() != null) {
      List<LatLng> latLngList = historyTrackData.getListPoints();
      // 发送广播，使Mainactivity绘制历史轨迹
      Intent intent = new Intent("cc.bitlight.broadcast.track.historytrack.draw");
      application.latLngHistoryList = latLngList;
      intent.putExtra("distance", historyTrackData.distance);
      mLocalBroadcastManager.sendBroadcast(intent);
    }
  }

  //创建一个圆形围栏
  void createGroFence(double longitude, double latitude, int radius, String userId) {
    //创建者（entity标识）
    String creator = application.getEntityName();
    //围栏名称
    String fenceName = application.getEntityName() + "_fence";
    //围栏描述
    String fenceDesc = "myFence";
    //监控对象列表（多个entityName，以英文逗号"," 分割）
    String monitoredPersons;
    if (application.entityNameList.size() == 0)
      monitoredPersons = application.getEntityName();
    else {
      StringBuilder entityNameBuilder = new StringBuilder();
      boolean judge = false;
      for (String entityName : application.entityNameList) {
        if (judge)
          entityNameBuilder.append(",");
        judge = true;
        entityNameBuilder.append(entityName);
      }
      monitoredPersons = entityNameBuilder.toString();
      Log.d("lml", monitoredPersons);
    }
    //观察者列表（多个entityName，以英文逗号"," 分割）
    String observers = application.getEntityName();
    //生效时间列表
    String validTimes = "0001,2359";
    //生效周期
    int validCycle = 4;
    //围栏生效日期
    String validDate = "";
    //生效日期列表
    String validDays = "";
    //坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
    int coordType = 3;
    //围栏圆心（圆心位置, 格式 : "经度,纬度"）
    String center = longitude + "," + latitude;// "116.838463,40.263548";
    //报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
    int alarmCondition = 3;
    //创建圆形地理围栏
    Log.d("lml", "准备发送地理围栏请求");
    lbsTraceClient.createCircularFence(lbsTraceServiceId, creator, fenceName, fenceDesc
        , monitoredPersons, observers, validTimes, validCycle, validDate, validDays
        , coordType, center, radius, alarmCondition, myOnGeoFenceListener);
  }

  void updateGroFence(double longitude, double latitude, int radius, String userId) {
    Log.d("lml", "Fence:" + "开始更新围栏");
    // 围栏名称
    String fenceName = application.getEntityName() + "_fence";
    // 围栏ID
    int fenceId2 = preFenceId;
    // 围栏描述
    String fenceDesc = "myFence";
    // 监控对象列表（多个entityName，以英文逗号"," 分割）
    String monitoredPersons;
    if (application.entityNameList.size() == 0)
      monitoredPersons = application.getEntityName();
    else {
      StringBuilder entityNameBuilder = new StringBuilder();
      boolean judge = false;
      for (String entityName : application.entityNameList) {
        if (judge)
          entityNameBuilder.append(",");
        judge = true;
        entityNameBuilder.append(entityName);
      }
      monitoredPersons = entityNameBuilder.toString();
      Log.d("lml", monitoredPersons);
    }
    // 观察者列表（多个entityName，以英文逗号"," 分割）
    String observers = application.getEntityName();
    // 生效时间列表
    String validTimes = "0001,2359";
    // 生效周期
    int validCycle = 4;
    // 围栏生效日期
    String validDate = "";
    // 生效日期列表
    String validDays = "";
    // 坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
    int coordType = 3;
    // 围栏圆心（圆心位置, 格式 : "经度,纬度"）
    String center = longitude + "," + latitude;
    // 报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
    int alarmCondition = 3;
    lbsTraceClient.updateCircularFence(lbsTraceServiceId, fenceName, preFenceId, fenceDesc,
        monitoredPersons, observers, validTimes, validCycle, validDate, validDays
        , coordType, center, radius, alarmCondition, myOnGeoFenceListener);
  }

  //删除围栏
  void deleteGeoFence() {
    lbsTraceClient.deleteFence(lbsTraceServiceId, preFenceId, myOnGeoFenceListener);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    //定位服务停止
    mLocationClient.stop();
    if (application.isLoginSucceed()) {
      //停止自动上传当前位置
      radarSearchManager.stopUploadAuto();
      //周边雷达移除监听
      radarSearchManager.removeNearbyInfoListener(MapService.this);
      //停止轨迹服务
      lbsTraceClient.stopTrace(trace, MapService.this);
    }
    //释放资源
    radarSearchManager.destroy();
    radarSearchManager = null;
    lbsTraceClient.onDestroy();
    application.setLoginSucceed(false);
  }

  @Override
  public IBinder onBind(Intent intent) {
    //throw new UnsupportedOperationException("Not yet implemented");
    return new ServiceBinder();
  }

  /**
   * 周边雷达主要操作回调接口RadarUploadInfoCallback
   */
  //周边雷达连续自动上传位置信息回调接口
  @Override
  public RadarUploadInfo onUploadInfoCallback() {
    //上传位置
    RadarUploadInfo info = new RadarUploadInfo();
    info.comments = String.valueOf(application.getGender());
    info.pt = application.getLatLng();
    radarNearbyOption.centerPt(application.getLatLng());
    Date dateBefore = new Date(System.currentTimeMillis() - delayTime);
    Date dateCurrent = new Date(System.currentTimeMillis());
    radarNearbyOption.timeRange(dateBefore, dateCurrent);
    if (queryFenceEntityStatus)
      lbsTraceClient.queryMonitoredStatus(lbsTraceServiceId, preFenceId, "", myOnGeoFenceListener);
    else
      monitoredPersonStatusesBeanList = null;
    radarSearchManager.nearbyInfoRequest(radarNearbyOption);
    Log.d("lml", "MapService:进行操作：上传位置信息 & 查询周边");
    return info;
  }

  //查询周边监听回调接口RadarSearchListener
  //查询周边的人监听
  @Override
  public void onGetNearbyInfoList(RadarNearbyResult radarNearbyResult, RadarSearchError radarSearchError) {
    if (radarSearchError == RadarSearchError.RADAR_NO_ERROR) {
      Log.d("lml", "MapService:收到查询周边回调");
      application.setRadarNearbyResult(radarNearbyResult);
      List<MyRadarNearbyInfo> myRadarNearbyInfoList = new ArrayList<>();
      if (monitoredPersonStatusesBeanList == null) {
        for (RadarNearbyInfo radarNearbyInfo : radarNearbyResult.infoList) {
          MyRadarNearbyInfo myRadarNearbyInfo = new MyRadarNearbyInfo(radarNearbyInfo.userID, radarNearbyInfo.pt, radarNearbyInfo.distance, radarNearbyInfo.timeStamp, radarNearbyInfo.comments, 0);
          myRadarNearbyInfoList.add(myRadarNearbyInfo);
        }
      } else {
        int fenceStatus = 0;
        for (RadarNearbyInfo radarNearbyInfo : radarNearbyResult.infoList) {
          for (QueryMonitoredStatusJson.MonitoredPersonStatusesBean monitoredPersonStatusesBean : monitoredPersonStatusesBeanList) {
            if (radarNearbyInfo.userID.equals(monitoredPersonStatusesBean.getMonitored_person())) {
              fenceStatus = monitoredPersonStatusesBean.getMonitored_status();
              break;
            } else
              fenceStatus = 0;
          }
          MyRadarNearbyInfo myRadarNearbyInfo = new MyRadarNearbyInfo(radarNearbyInfo.userID, radarNearbyInfo.pt, radarNearbyInfo.distance, radarNearbyInfo.timeStamp, radarNearbyInfo.comments, fenceStatus);
          myRadarNearbyInfoList.add(myRadarNearbyInfo);
        }
      }
      application.setMyRadarNearbyInfoList(myRadarNearbyInfoList);
      //上传成功
      //Log.d("lml", "onGetNearbyInfoList:查询周边成功:" + "radarNearbyResult.totalNum:" + radarNearbyResult.totalNum + "radarNearbyResult.pageNum:" + radarNearbyResult.pageNum);
      Log.d("lml", "onGetNearbyInfoList:开始发送显示周边信息广播:" + "myRadarNearbyResult.totalNum:" + radarNearbyResult.totalNum + "radarNearbyResult.pageNum:" + radarNearbyResult.pageNum);
      Intent intent = new Intent("cc.bitlight.broadcast.nearbyinfo.data");
      mLocalBroadcastManager.sendBroadcast(intent);
    } else {
      //上传失败
      Log.d("lml", "onGetNearbyInfoList: 查询周边失败" + radarSearchError);
    }
  }

  //上传位置监听
  @Override
  public void onGetUploadState(RadarSearchError radarSearchError) {
    if (radarSearchError == RadarSearchError.RADAR_NO_ERROR) {
      //上传成功
      Log.d("lml", "onGetUploadState: 单次上传位置成功");
    } else {
      //上传失败
      Log.d("lml", "onGetUploadState: 单次上传位置失败" + radarSearchError);
    }
  }

  //清除位置信息监听
  @Override
  public void onGetClearInfoState(RadarSearchError radarSearchError) {
    if (radarSearchError == RadarSearchError.RADAR_NO_ERROR) {
      //移除监听
      radarSearchManager.removeNearbyInfoListener(this);
      //上传成功
      Log.d("lml", "onGetClearInfoState: 清除位置成功");
      Intent intent = new Intent("cc.bitlight.broadcast.nearbyinfo.clearinfosucceed");
      mLocalBroadcastManager.sendBroadcast(intent);
    } else {
      //上传失败
      Log.d("lml", "onGetClearInfoState: 清除位置失败" + radarSearchError);
    }
  }

  /**
   * 轨迹追踪监听回调接口
   */
  //实现OnStartTraceListener开启轨迹服务回调接口
  // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
  @Override
  public void onTraceCallback(int i, String s) {
    if (BuildConfig.DEBUG)
      Log.d("lml", "MapService:开启轨迹服务回调接口消息 [消息编码 : " + i + "，消息内容 : " + s + "]");
  }

  // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
  @Override
  public void onTracePushCallback(byte b, String s) {
    Log.d("lml", "MapService:轨迹服务推送接口消息 [消息类型 : " + b + "，消息内容 : " + s + "]");
    switch (b) {
      case 3:
        Log.d("lml", "围栏内entity状态改变");
        queryFenceEntityStatus = true;
        TracePushFenceCallbackJson tracePushFenceCallbackJson = GsonService.parseJson(s, TracePushFenceCallbackJson.class);
        if (tracePushFenceCallbackJson != null) {
          Intent intent = new Intent("cc.bitlight.broadcast.geofence.notification");
          intent.putExtra("userID", tracePushFenceCallbackJson.getMonitored_person());
          intent.putExtra("fenceStatus", tracePushFenceCallbackJson.getAction());
          mLocalBroadcastManager.sendBroadcast(intent);
          break;
        }
    }
  }

  //实现OnStopTraceListener停止轨迹服务回调接口
  // 轨迹服务停止成功
  @Override
  public void onStopTraceSuccess() {
    Log.d("lml", "MapService:停止轨迹服务成功");

  }

  // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
  @Override
  public void onStopTraceFailed(int i, String s) {
    Log.d("lml", "MapService:停止轨迹服务接口消息 [错误编码 : " + i + "，消息内容 : " + s + "]");

  }

  //OnTrackListener接口的实现类，实现轨迹监听器回调方法
  class MyOnTrackListener extends OnTrackListener {
    //查询历史轨迹回调接口
    @Override
    public void onQueryHistoryTrackCallback(String s) {
      //message - 返回消息 (格式参考鹰眼Web服务API v2.0中track/gethistory接口返回值格式http://lbsyun.baidu.com/index.php?title=yingyan/api/track)
      super.onQueryHistoryTrackCallback(s);
      Log.d("lml", "MyOnTrackListener:查询历史轨迹回调");
      Log.d("lml", s);
      handleHistoryTrackString(s);
    }

//        //轨迹属性回调接口
//        @Override
//        public Map onTrackAttrCallback() {
//            Log.d("lml", "MyOnTrackListener:轨迹属性回调接口");
//            return super.onTrackAttrCallback();
//        }

    //请求失败回调接口
    @Override
    public void onRequestFailedCallback(String s) {
      Log.d("lml", "MyOnTrackListener:轨迹监听器回调接口，轨迹查询失败：");
      Log.d("lml", s);
    }
  }


  //定位服务监听回调接口实现类
  public class MyLocationListener implements BDLocationListener {

    @Override
    public void onReceiveLocation(BDLocation location) {
      Log.d("lml", "MyLocationListener:成功获取定位信息" + application.receiveLocationShowMessage(location));
      if (application.isLocateSuccess()) {
        Intent intent = new Intent("cc.bitlight.broadcast.location.data");
        mLocalBroadcastManager.sendBroadcast(intent);
        //设置系统时间

      }
    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }
  }

  //轨迹服务中Entity监听器
  public class MyOnEntityListener extends OnEntityListener {
    public MyOnEntityListener() {
      super();
    }

    //添加Entity回调接口
    @Override
    public void onAddEntityCallback(String s) {
      super.onAddEntityCallback(s);
      Log.d("lml", "MyOnEntityListener:添加Entity回调接口:" + s);
    }

    //查询Entity列表回调接口
    @Override
    public void onQueryEntityListCallback(String s) {
      super.onQueryEntityListCallback(s);
      Log.d("lml", "MyOnEntityListener:查询Entity列表回调接口:" + s);
    }

    //Entity实时定位回调接口
    @Override
    public void onReceiveLocation(TraceLocation traceLocation) {
      super.onReceiveLocation(traceLocation);
      Log.d("lml", "MyOnEntityListener:Entity实时定位回调接口");
    }

    //请求失败回调接口
    @Override
    public void onRequestFailedCallback(String s) {
      Log.d("lml", "MyOnEntityListener:请求失败回调接口:" + s);
    }

    //更新Entity回调接口
    @Override
    public void onUpdateEntityCallback(String s) {
      super.onUpdateEntityCallback(s);
      Log.d("lml", "MyOnEntityListener:更新Entity回调接口:" + s);
    }
  }

  //轨迹服务中地理围栏监听器
  class MyOnGeoFenceListener extends OnGeoFenceListener {

    //创建圆形围栏回调接口
    @Override
    public void onCreateCircularFenceCallback(String s) {
      super.onCreateCircularFenceCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:创建圆形围栏回调接口" + s);
      CreateFenceCallbackJson createFenceCallback = GsonService.parseJson(s, CreateFenceCallbackJson.class);
      if (createFenceCallback != null && createFenceCallback.getStatus() == 0) {
        preFenceId = createFenceCallback.getFence_id();

      }

    }

    //创建多边形围栏回调接口
    @Override
    public void onCreateVertexesFenceCallback(String s) {
      super.onCreateVertexesFenceCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:创建多边形围栏回调接口" + s);
    }

    // 延迟报警回调接口
    @Override
    public void onDelayAlarmCallback(String s) {
      super.onDelayAlarmCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:延迟报警回调接口" + s);
    }

    //删除围栏回调接口
    @Override
    public void onDeleteFenceCallback(String s) {
      super.onDeleteFenceCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:删除围栏回调接口" + s);
    }

    //查询围栏列表回调接口
    @Override
    public void onQueryFenceListCallback(String s) {
      super.onQueryFenceListCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:查询围栏列表回调接口" + s);
      QueryFenceListJson queryFenceListJson = GsonService.parseJson(s, QueryFenceListJson.class);
      if (queryFenceListJson != null && queryFenceListJson.getStatus() == 0 & queryFenceListJson.getSize() != 0) {
        Log.d("lml", "MyOnGeoFenceListener:有围栏");
        List<QueryFenceListJson.FencesMessage> fencesMessageList = queryFenceListJson.getFences();
        QueryFenceListJson.FencesMessage fencesMessage = fencesMessageList.get(0);

        preFenceId = fencesMessage.getFence_id();
        Log.d("lml", "MyOnGeoFenceListener:获取围栏Id:" + preFenceId);
        QueryFenceListJson.FencesMessage.LatlngCenterList latlngCenterList = fencesMessage.getCenter();
        int radius = fencesMessage.getRadius();
        Intent intent = new Intent("cc.bitlight.broadcast.geofence.reload");
        intent.putExtra("latitude", latlngCenterList.getLatitude());
        intent.putExtra("longitude", latlngCenterList.getLongitude());
        intent.putExtra("radius", radius);

        mLocalBroadcastManager.sendBroadcast(intent);
      }
    }

    //查询围栏历史报警信息回调接口
    @Override
    public void onQueryHistoryAlarmCallback(String s) {
      super.onQueryHistoryAlarmCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:查询围栏历史报警信息回调接口" + s);
    }

    //查询监控对象状态回调接口
    @Override
    public void onQueryMonitoredStatusCallback(String s) {
      super.onQueryMonitoredStatusCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:查询监控对象状态回调接口" + s);
      QueryMonitoredStatusJson queryMonitoredStatusJson = GsonService.parseJson(s, QueryMonitoredStatusJson.class);
      if (queryMonitoredStatusJson != null && queryMonitoredStatusJson.getStatus() == 0 & queryMonitoredStatusJson.getSize() != 0) {
        monitoredPersonStatusesBeanList = queryMonitoredStatusJson.getMonitored_person_statuses();

      }
    }

    //请求失败回调接口
    @Override
    public void onRequestFailedCallback(String s) {

      Log.d("lml", "MyOnGeoFenceListener:请求失败回调接口" + s);
    }

    //更新圆形围栏回调接口
    @Override
    public void onUpdateCircularFenceCallback(String s) {
      super.onUpdateCircularFenceCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:更新圆形围栏回调接口" + s);
    }

    //更新多边形围栏回调接口
    @Override
    public void onUpdateVertexesFenceCallback(String s) {
      super.onUpdateVertexesFenceCallback(s);
      Log.d("lml", "MyOnGeoFenceListener:更新多边形围栏回调接口" + s);
    }

  }

  class myServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case "cc.bitlight.broadcast.service.start":
          //周边雷达EntityName设置
          radarSearchManager.setUserID(application.getEntityName());
          //周边雷达设置监听
          radarSearchManager.addNearbyInfoListener(MapService.this);
          //设置自动上传位置
          radarSearchManager.startUploadAuto(MapService.this, 5000);
          //实例化轨迹服务
          trace = new Trace(getApplicationContext(), lbsTraceServiceId, application.getEntityName(), traceType);
          //开启轨迹服务
          lbsTraceClient.startTrace(trace, MapService.this);
          //添加Entity
          lbsTraceClient.addEntity(lbsTraceServiceId, application.getEntityName(), "", myOnEntityListener);
          //查询围栏列表
          lbsTraceClient.queryFenceList(lbsTraceServiceId, application.getEntityName(), "", myOnGeoFenceListener);
          //查询Entity列表
          lbsTraceClient.queryEntityList(lbsTraceServiceId, application.getEntityName() + ",黄亚飞" + ",刘少飞" + ",马超", "", 0, 0, 1000, 1, myOnEntityListener);

          application.setLoginSucceed(true);

          break;
        case "cc.bitlight.broadcast.service.stop":
          //停止自动上传当前位置
          radarSearchManager.stopUploadAuto();
          //周边雷达移除监听
          radarSearchManager.removeNearbyInfoListener(MapService.this);
          //停止轨迹服务
          lbsTraceClient.stopTrace(trace, MapService.this);
          application.setLoginSucceed(false);
          break;

        //查询历史轨迹
        case "cc.bitlight.broadcast.track.queryhistorytrack":
          queryHistoryTrack(intent.getStringExtra("entityName"));
          break;

        //设置地理围栏
        case "cc.bitlight.broadcast.geofence.query":
          Log.d("lml", "已收到地理围栏广播");
          switch (intent.getStringExtra("type")) {
            case "create":
              double longitude = intent.getDoubleExtra("longitude", 0);
              double latitude = intent.getDoubleExtra("latitude", 0);
              int radius = intent.getIntExtra("radius", 0);
              String userId = intent.getStringExtra("userId");
              if (preFenceId == -50)
                createGroFence(longitude, latitude, radius, userId);
              else
                updateGroFence(longitude, latitude, radius, userId);
              break;
            case "delete":
              queryFenceEntityStatus = false;
              deleteGeoFence();
              preFenceId = -50;
              break;
          }
          break;
        case "cc.bitlight.broadcast.mytemp":
          Log.d("lml", "myServiceReceiver:收到测试广播");
          queryFenceEntityStatus = true;
          lbsTraceClient.queryMonitoredStatus(lbsTraceServiceId, preFenceId, "", myOnGeoFenceListener);
          break;
      }
    }
  }

  class ServiceBinder extends Binder {

  }
}


