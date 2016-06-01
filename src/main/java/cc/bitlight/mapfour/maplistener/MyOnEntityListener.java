package cc.bitlight.mapfour.maplistener;

import android.util.Log;

import com.baidu.trace.OnEntityListener;
import com.baidu.trace.TraceLocation;

public class MyOnEntityListener extends OnEntityListener {
    public MyOnEntityListener() {
        super();
    }

    //查询Entity列表回调接口
    @Override
    public void onQueryEntityListCallback(String s) {
        super.onQueryEntityListCallback(s);
        Log.d("lml", "MyOnEntityListener:查询Entity列表回调接口:" + s);
    }

    //添加Entity回调接口
    @Override
    public void onAddEntityCallback(String s) {
        super.onAddEntityCallback(s);
        Log.d("lml", "MyOnEntityListener:添加Entity回调接口:" + s);
    }

    //更新Entity回调接口
    @Override
    public void onUpdateEntityCallback(String s) {
        super.onUpdateEntityCallback(s);
        Log.d("lml", "MyOnEntityListener:更新Entity回调接口:" + s);
    }

    //Entity实时定位回调接口
    @Override
    public void onReceiveLocation(TraceLocation traceLocation) {
        super.onReceiveLocation(traceLocation);
        Log.d("lml", "MyOnEntityListener:Entity实时定位回调接口");
   //     Log.d("lml", MapDeviceMessage.receiveLocationMessageTrace(traceLocation));
    }

    //请求失败回调接口
    @Override
    public void onRequestFailedCallback(String s) {
        Log.d("lml", "MyOnEntityListener:请求失败回调接口:" + s);
    }
}
