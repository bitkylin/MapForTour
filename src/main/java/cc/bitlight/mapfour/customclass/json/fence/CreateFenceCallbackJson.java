package cc.bitlight.mapfour.customclass.json.fence;

/**
 * 创建围栏回调信息
 */
public class CreateFenceCallbackJson {

    /**
     * status : 0
     * message : 成功
     * fence_id : 11
     * fence_name : 李明亮_fence
     */

    private int status;
    private String message;
    private int fence_id;
    private String fence_name;

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

    public int getFence_id() {
        return fence_id;
    }

    public void setFence_id(int fence_id) {
        this.fence_id = fence_id;
    }

    public String getFence_name() {
        return fence_name;
    }

    public void setFence_name(String fence_name) {
        this.fence_name = fence_name;
    }
}
