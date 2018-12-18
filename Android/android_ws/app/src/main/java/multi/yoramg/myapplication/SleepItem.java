package multi.yoramg.myapplication;

/**
 * 20181122 / 작성자 : 배한주
 * VisualFragment에서 일자별 수면 성공 정보에 대한 객체 정보
 */

import android.graphics.drawable.Drawable;

public class SleepItem {
    private Drawable icon;
    private String date;
    private String successRate;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }
}
