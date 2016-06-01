package cc.bitlight.mapfour.customclass;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyInfo;

import java.util.Date;

/**
 * RadarNearbyInfo周边雷达回调周边用户信息结构类
 */
public class MyRadarNearbyInfo extends RadarNearbyInfo {
    public int fenceStatus;

    public MyRadarNearbyInfo() {
        super();
    }

    public MyRadarNearbyInfo(String userID) {
        this();
        this.userID = userID;
        fenceStatus = 0;
    }

    public MyRadarNearbyInfo(String userID, LatLng pt, int distance, Date timeStamp, String comments, int fenceStatus) {
        this();
        this.userID = userID;
        this.pt = pt;
        this.distance = distance;
        this.timeStamp = timeStamp;
        this.comments = comments;
        this.fenceStatus = fenceStatus;

    }
}
