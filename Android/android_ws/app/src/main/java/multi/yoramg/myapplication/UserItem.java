package multi.yoramg.myapplication;

import java.io.Serializable;

/**
 * 20181122 / 작성자 : 배한주
 * 사용자 정보를 담는 객체
 */

public class UserItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String password;
    private String name;
    private String birth;
    private String autoSleepModeStatus;
    private String serial;
    private boolean isDanger;

    private static UserItem user = null;

    private UserItem() {
        id = null;
        password = null;
        name = null;
        birth = null;
        autoSleepModeStatus = null;
        isDanger = false;
        serial = null;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public static UserItem getUser() {
        if (user == null) {
            user = new UserItem();
        }
        return user;
    }

    public void logout() {
        if (user != null) {
            id = null;
            password = null;
            name = null;
            birth = null;
            autoSleepModeStatus = null;
            isDanger = false;
        }
    }

    public boolean isDanger() {
        return isDanger;
    }

    public void setDanger(boolean danger) {
        isDanger = danger;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAutoSleepModeStatus() {
        return autoSleepModeStatus;
    }

    public void setAutoSleepModeStatus(String autoSleepModeStatus) {
        this.autoSleepModeStatus = autoSleepModeStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
