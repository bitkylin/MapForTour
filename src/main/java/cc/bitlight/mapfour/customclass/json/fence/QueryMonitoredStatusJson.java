package cc.bitlight.mapfour.customclass.json.fence;

import java.util.List;

/**
 * 查询监控对象状态回调接口
 */
public class QueryMonitoredStatusJson {

    /**
     * status : 0
     * message : 成功
     * size : 3
     * monitored_person_statuses : [{"monitored_person":"胡思","monitored_status":0},{"monitored_person":"刘少飞","monitored_status":0},{"monitored_person":"黄亚飞","monitored_status":0}]
     */

    private int status;
    private String message;
    private int size;
    /**
     * monitored_person : 胡思
     * monitored_status : 0
     */

    private List<MonitoredPersonStatusesBean> monitored_person_statuses;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<MonitoredPersonStatusesBean> getMonitored_person_statuses() {
        return monitored_person_statuses;
    }

    public void setMonitored_person_statuses(List<MonitoredPersonStatusesBean> monitored_person_statuses) {
        this.monitored_person_statuses = monitored_person_statuses;
    }

    public static class MonitoredPersonStatusesBean {
        private String monitored_person;
        private int monitored_status;

        public String getMonitored_person() {
            return monitored_person;
        }

        public void setMonitored_person(String monitored_person) {
            this.monitored_person = monitored_person;
        }

        public int getMonitored_status() {
            return monitored_status;
        }

        public void setMonitored_status(int monitored_status) {
            this.monitored_status = monitored_status;
        }
    }
}
