package cc.bitlight.mapfour.customclass.json.fence;
/**
 * 查询围栏列表回调信息
 */

import java.util.List;

public class QueryFenceListJson {

    /**
     * status : 0
     * message : 成功
     * size : 1
     * fences : [{"fence_id":6,"name":"李明亮_fence","description":"test fence","valid_times":[{"begin_time":"0800","end_time":"2300"}],"valid_cycle":4,"valid_days":[],"shape":1,"center":{"longitude":110.34207270790517,"latitude":25.287036151702843},"radius":100,"vertexes":[],"alarm_condition":3,"creator":"李明亮","observers":["李明亮"],"monitored_persons":["李明亮"],"create_time":"2016-04-10 19:59:44","update_time":"2016-04-10 20:00:20","coord_type":3}]
     */

    private int status;
    private String message;
    private int size;
    /**
     * fence_id : 6
     * name : 李明亮_fence
     * description : test fence
     * valid_times : [{"begin_time":"0800","end_time":"2300"}]
     * valid_cycle : 4
     * valid_days : []
     * shape : 1
     * center : {"longitude":110.34207270790517,"latitude":25.287036151702843}
     * radius : 100
     * vertexes : []
     * alarm_condition : 3
     * creator : 李明亮
     * observers : ["李明亮"]
     * monitored_persons : ["李明亮"]
     * create_time : 2016-04-10 19:59:44
     * update_time : 2016-04-10 20:00:20
     * coord_type : 3
     */

    private List<FencesMessage> fences;

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

    public List<FencesMessage> getFences() {
        return fences;
    }

    public void setFences(List<FencesMessage> fences) {
        this.fences = fences;
    }

    public static class FencesMessage {
        private int fence_id;
        private String name;
        private String description;
        private int valid_cycle;
        private int shape;
        /**
         * longitude : 110.34207270790517
         * latitude : 25.287036151702843
         */

        private LatlngCenterList center;
        private int radius;
        private int alarm_condition;
        private String creator;
        private String create_time;
        private String update_time;
        private int coord_type;
        /**
         * begin_time : 0800
         * end_time : 2300
         */

        private List<ValidTimesBean> valid_times;
        private List<?> valid_days;
        private List<?> vertexes;
        private List<String> observers;
        private List<String> monitored_persons;

        public int getFence_id() {
            return fence_id;
        }

        public void setFence_id(int fence_id) {
            this.fence_id = fence_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getValid_cycle() {
            return valid_cycle;
        }

        public void setValid_cycle(int valid_cycle) {
            this.valid_cycle = valid_cycle;
        }

        public int getShape() {
            return shape;
        }

        public void setShape(int shape) {
            this.shape = shape;
        }

        public LatlngCenterList getCenter() {
            return center;
        }

        public void setCenter(LatlngCenterList center) {
            this.center = center;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getAlarm_condition() {
            return alarm_condition;
        }

        public void setAlarm_condition(int alarm_condition) {
            this.alarm_condition = alarm_condition;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(String update_time) {
            this.update_time = update_time;
        }

        public int getCoord_type() {
            return coord_type;
        }

        public void setCoord_type(int coord_type) {
            this.coord_type = coord_type;
        }

        public List<ValidTimesBean> getValid_times() {
            return valid_times;
        }

        public void setValid_times(List<ValidTimesBean> valid_times) {
            this.valid_times = valid_times;
        }

        public List<?> getValid_days() {
            return valid_days;
        }

        public void setValid_days(List<?> valid_days) {
            this.valid_days = valid_days;
        }

        public List<?> getVertexes() {
            return vertexes;
        }

        public void setVertexes(List<?> vertexes) {
            this.vertexes = vertexes;
        }

        public List<String> getObservers() {
            return observers;
        }

        public void setObservers(List<String> observers) {
            this.observers = observers;
        }

        public List<String> getMonitored_persons() {
            return monitored_persons;
        }

        public void setMonitored_persons(List<String> monitored_persons) {
            this.monitored_persons = monitored_persons;
        }

        public static class LatlngCenterList {
            private double longitude;
            private double latitude;

            public double getLongitude() {
                return longitude;
            }

            public void setLongitude(double longitude) {
                this.longitude = longitude;
            }

            public double getLatitude() {
                return latitude;
            }

            public void setLatitude(double latitude) {
                this.latitude = latitude;
            }
        }

        public static class ValidTimesBean {
            private String begin_time;
            private String end_time;

            public String getBegin_time() {
                return begin_time;
            }

            public void setBegin_time(String begin_time) {
                this.begin_time = begin_time;
            }

            public String getEnd_time() {
                return end_time;
            }

            public void setEnd_time(String end_time) {
                this.end_time = end_time;
            }
        }
    }
}
