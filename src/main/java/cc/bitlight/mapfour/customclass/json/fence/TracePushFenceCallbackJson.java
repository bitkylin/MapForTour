package cc.bitlight.mapfour.customclass.json.fence;

/**
 * 轨迹服务推送接口消息
 */
public class TracePushFenceCallbackJson {

    /**
     * fence_id : 17
     * fence : 冯帅_fence
     * monitored_person : 李明亮
     * action : 1
     * time : 1460685485
     */

    private int fence_id;
    private String fence;
    private String monitored_person;
    private int action;
    private int time;

    public int getFence_id() {
        return fence_id;
    }

    public void setFence_id(int fence_id) {
        this.fence_id = fence_id;
    }

    public String getFence() {
        return fence;
    }

    public void setFence(String fence) {
        this.fence = fence;
    }

    public String getMonitored_person() {
        return monitored_person;
    }

    public void setMonitored_person(String monitored_person) {
        this.monitored_person = monitored_person;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
