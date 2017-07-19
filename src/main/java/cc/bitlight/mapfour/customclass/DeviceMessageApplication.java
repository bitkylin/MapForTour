package cc.bitlight.mapfour.customclass;


import android.app.Application;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyInfo;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.trace.model.TraceLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.bitlight.mapfour.BuildConfig;

public class DeviceMessageApplication extends Application {
    //用户信息初始化
    String entityName = null;       //本机entity标识
    String entitySignature = null;  //本人个性签名
    String gender = "m";         //本人性别:ture:男，false:女
    String usersIdentity = "管理员";        //用户身份
    boolean loginSucceed = false;   //登陆成功判断
    //地图信息管理
    RadarNearbyResult radarNearbyResult = null;
    List<RadarNearbyInfo> radarNearbyInfoList = null; //周边雷达获取用户列表
    List<MyRadarNearbyInfo> myRadarNearbyInfoList=null;
    public List<RadarNearbyInfo> fenceEntityInfoList = new ArrayList<>(); //地理围栏报警用户列表
    public List<String> entityNameList = new ArrayList<>();  //地理围栏监控用户名列表
    float maxZoomLevel = -1;    //地图最大缩放等级
    TraceLocation traceLocation = null;//轨迹追踪客户端
    //实时定位信息整理
    BDLocation location = null;     //百度实时位置信息
    public LatLng latLng = null;    //实时经纬度信息
    String locateMode;              //定位方式
    double longitude;               //经度
    double latitude;                //纬度
    float radius;                   //精度
    String address = "";            //地址

    //系统时间管理
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second = 59;
    public Date dateStartTime;
    public Date dateEndTime;
    //轨迹
    List<LatLng> latLngList = new ArrayList<>();//系统记录轨迹
    public List<LatLng> latLngHistoryList;   //历史轨迹泛型引用

    boolean isGPSLocation = false;  //实时定位结果来自于GPS
    boolean isLocateSuccess = false;//定位成功

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void clear() {
        maxZoomLevel = 21;    //地图最大缩放等级
        entityName = null;          //本机entity标识
        entitySignature = null;     //本人个性签名
        loginSucceed = false;
        location = null;        //百度实时位置信息
        traceLocation = null;//轨迹追踪客户端
        latLng = null;       //实时经纬度信息
    }

    public Date getdateStartTime() {
        dateStartTime = new Date(year - 1900, month, day, hour, minute, second);
        if (BuildConfig.DEBUG)
            Log.d("lml", "dateStartTime.getYear():" + dateStartTime.getYear());
        return dateStartTime;
    }

    public long getMillisecondStartTime() {
        return dateStartTime.getTime();
    }

    public long getMillisecondEndTime() {
        return dateEndTime.getTime();
    }

    public Date getdateEndTime() {
        dateEndTime = new Date(year - 1900, month, day, hour, minute, second);
        Log.d("lml", "dateStartTime.getYear():" + dateStartTime.getYear());
        return dateEndTime;
    }

    public String getUsersIdentity() {
        return usersIdentity;
    }

    //获得坐标信息列表
    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    //获取周边雷达结果

    public List<RadarNearbyInfo> getRadarNearbyInfoList() {
        return radarNearbyInfoList;
    }

    //设置周边雷达结果
    public void setRadarNearbyResult(RadarNearbyResult radarNearbyResult) {
        this.radarNearbyResult = radarNearbyResult;
        radarNearbyInfoList = radarNearbyResult.infoList;
    }

    public void setMyRadarNearbyInfoList(List<MyRadarNearbyInfo> myRadarNearbyInfoList) {
        this.myRadarNearbyInfoList = myRadarNearbyInfoList;
    }

    public List<MyRadarNearbyInfo> getMyRadarNearbyInfoList() {
        return myRadarNearbyInfoList;
    }

    //获取地图最大缩放等级
    public float getMaxZoomLevel() {
        return maxZoomLevel;
    }

    //设置地图最大缩放等级
    public void setMaxZoomLevel(float maxZoomLevel) {
        this.maxZoomLevel = maxZoomLevel;
    }

    //获取本机entity标识
    public String getEntityName() {
        return entityName;
    }

    //设置个性签名
    public void setEntitySignature(String entitySignature) {
        this.entitySignature = entitySignature;
    }

    //获取个性签名
    public String getEntitySignature() {
        return entitySignature;
    }

    //获取性别
    public String getGender() {
        return gender;
    }

    //设置性别
    public void setGender(String gender) {
        this.gender = gender;
    }

    //设置本机entity标识
    public void setEntityName(String str) {
        entityName = str;
    }

    //是否登陆成功
    public boolean isLoginSucceed() {
        return loginSucceed;
    }

    //设置是否登陆成功
    public void setLoginSucceed(boolean loginSucceed) {
        this.loginSucceed = loginSucceed;
    }

    /**
     * 实时定位为信息获取
     */
    //设置实时位置信息
    public void setLocation(BDLocation location) {
        this.location = location;
    }

    //获取实时经纬度信息
    public LatLng getLatLng() {
        return latLng;
    }

    public boolean isLocateSuccess() {
        return isLocateSuccess;
    }

    public String getLocateMode() {
        return locateMode;
    }

    public float getRadius() {
        return radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    /**
     * 参数：location
     * 输出：location中的详细的位置信息
     */
    public String receiveLocationShowMessage(BDLocation location) {
        setLocation(location);
        longitude = location.getLongitude();           //经度
        latitude = location.getLatitude();             //纬度
        radius = location.getRadius();          //精度
        address = location.getAddrStr();
        this.latLng = new LatLng(latitude, longitude);
        //StringBuilder sb = new StringBuilder(256);
//        sb.append("经度:");
//        sb.append(longitude);
//        sb.append("；纬度:");
//        sb.append(latitude);
//        sb.append("；精度:");
//        sb.append(radius);
//        sb.append("米");
//        sb.append("\n错误代码:");
//        sb.append(location.getLocType());
        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            isLocateSuccess = true;
            isGPSLocation = true;
            latLngList.add(latLng);
            locateMode = "GPS定位";
//            sb.append(";GPS定位成功");
//            sb.append("\n速度:");
//            sb.append(location.getSpeed());// 单位：公里每小时
//            sb.append("km/s");
//            sb.append("；星数:");
//            sb.append(location.getSatelliteNumber());
//            sb.append("颗；海拔:");
//            sb.append(location.getAltitude());// 单位：米
//            sb.append("；角度:");
//            sb.append(location.getDirection());// 单位度
//            sb.append("\n地址:");
//            sb.append(location.getAddrStr());
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            isLocateSuccess = true;
            isGPSLocation = false;
            locateMode = "网络定位";
//            sb.append(";网络定位成功");
//            sb.append("\n运营商:");
//            sb.append(location.getOperators());
//            sb.append("\n地址:");
//            sb.append(location.getAddrStr());
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            isLocateSuccess = true;
            isGPSLocation = false;
            //  sb.append("\n离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            isLocateSuccess = false;
            isGPSLocation = false;
            //  sb.append("\n服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            isLocateSuccess = false;
            isGPSLocation = false;
            //  sb.append("\n网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            isLocateSuccess = false;
            isGPSLocation = false;
            //  sb.append("\n无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        // sb.toString();
        return "success";
    }

    /**
     * 参数：traceLocation
     * 输出：traceLocation中的较为详细的位置信息
     */
    public String receiveLocationMessageTrace(TraceLocation traceLocation) {
        this.traceLocation = traceLocation;
        return "经度:" + traceLocation.getLongitude()
                + "；纬度:" + traceLocation.getLatitude()
                + "；精度:" + traceLocation.getRadius() + "米"
                + "\n 建筑物信息:" + traceLocation.getBuilding()
                + "; 坐标类型:" + traceLocation.getCoordType();
    }
}
