package org.b3log.siyuan;

public class OnSiYuanMainRequestEvent {
    private int requestCode;
    private int resultCode;
    private String callback;

    public OnSiYuanMainRequestEvent(int requestCode, int resultCode, String callback) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.callback = callback;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getCallback() {
        return callback;
    }
}

