package multi.yoramg.myapplication;

import java.util.HashMap;
import java.util.LinkedList;

public class SleepAmountList {
    private static SleepAmountList sleepAmountList;

    private HashMap<String, HashMap<String, String>> daySleepList;
    private String lastDate;
    private String lastTime;

    private SleepAmountList() {
        daySleepList = new HashMap<>();
    }

    public static SleepAmountList getSleepAmountList() {
        if (sleepAmountList == null) {
            sleepAmountList = new SleepAmountList();
        }
        return sleepAmountList;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public HashMap<String, HashMap<String, String>> getDaySleepList() {
        return daySleepList;
    }

    public void setDaySleepList(HashMap<String, HashMap<String, String>> daySleepList) {
        this.daySleepList = daySleepList;
    }
}
