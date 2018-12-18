package iot.multicampus.yoramg.tcpclient;
import android.graphics.drawable.Drawable;

public class MyItem {
    private Drawable icon;
    private String startSleep;
    private String endSleep;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getStartSleep() {
        return startSleep;
    }

    public void setStartSleep(String startSleep) {
        this.startSleep = startSleep;
    }

    public String getEndSleep() {
        return endSleep;
    }

    public void setEndSleep(String endSleep) {
        this.endSleep = endSleep;
    }
}
