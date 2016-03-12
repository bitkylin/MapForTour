package com.lml.baidumaptwo;

import com.baidu.location.BDLocation;

public class MapDeviceMessage {
    float maxZoomLevel;//地图最大缩放等级
    String entityName;//本机entity标识

    //获取地图最大缩放等级
    float getMaxZoomLevel() {
        return maxZoomLevel;
    }

    //设置地图最大缩放等级
    void setMaxZoomLevel(float maxZoomLevel) {
        this.maxZoomLevel = maxZoomLevel;
    }

    //获取本机entity标识
    String getEntityName() {
        return entityName;
    }

    //设置本机entity标识
    void setEntityName() {
        entityName = android.os.Build.MODEL + "(v" + android.os.Build.VERSION.RELEASE + ")_" + android.os.Build.SERIAL;
    }

    String receiveLocationShowMessage(BDLocation location) {
        //Receive Location
        StringBuilder sb = new StringBuilder(256);
        sb.append("经度:");
        sb.append(location.getLongitude());
        sb.append("；纬度:");
        sb.append(location.getLatitude());
        sb.append("；精度:");
        sb.append(location.getRadius());
        sb.append("米");
        sb.append("\n错误代码:");
        sb.append(location.getLocType());
        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append(";GPS定位成功");
            sb.append("\n速度:");
            sb.append(location.getSpeed());// 单位：公里每小时
            sb.append("km/s");
            sb.append("；星数:");
            sb.append(location.getSatelliteNumber());
            sb.append("颗；海拔:");
            sb.append(location.getAltitude());// 单位：米
            sb.append("；角度:");
            sb.append(location.getDirection());// 单位度
            //sb.append("\n地址:");
            //sb.append(location.getAddrStr());
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            sb.append(";网络定位成功");
            sb.append("\n运营商:");
            sb.append(location.getOperators());
            //sb.append("\n地址:");
            //sb.append(location.getAddrStr());
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\n离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            sb.append("\n服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\n网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\n无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        //sb.append("\n位置语义化信息:");
        //sb.append(location.getLocationDescribe());// 位置语义化信息
        return sb.toString();
    }
}
