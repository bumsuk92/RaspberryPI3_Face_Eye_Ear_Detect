package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * 회원가입을 담당하는 액티비티
 */
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends Activity implements IClient{
    private TextView loginScreen;

    private Client client;
    private Socket socket;
    private InputStream inputStream; // input Stream
    private OutputStream outputStream; //output Stream

    private Thread worker = null;

    private EditText userId = null;
    private EditText userPassword = null;
    private EditText userName = null;
    private EditText userBirth = null;
    private EditText userSerial = null;
    private Button btnRegister = null;
    private Button idCheck = null;

    private boolean isSuccessed;
    private boolean isIdConfirm;


    private String id, password, name, birth, serial;
    private String prevId;

    private final MyHandler mHandler = new MyHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);

        userId = (EditText) findViewById(R.id.userId);
        userPassword = (EditText) findViewById(R.id.userPassword);
        userName = (EditText) findViewById(R.id.userName);
        userBirth = (EditText) findViewById(R.id.userBirth);
        userSerial = (EditText) findViewById(R.id.userSerial);

        loginScreen = (TextView) findViewById(R.id.link_to_login);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        idCheck = (Button) findViewById(R.id.idCheck);
        isIdConfirm = false;

        client = Client.getClient();
        socket = client.getSocket();
        if(socket.isClosed()) {
            client.connectToServer();
        }
        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();

        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                finish();
            }
        });

        idCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 회원가입
                id = userId.getText().toString();
                boolean canIdCheck = checkId(id);

                if(canIdCheck) {
                    Log.i(this.getClass().getName(), "아이디 중복체크 canIdCheck : " + canIdCheck);

                    sendMessage("#;;60;" + id + ";&");
                    Log.i(this.getClass().getName(), "아이디 중복체크 canIdCheck : " + canIdCheck);
//                    Toast.makeText(getApplicationContext(), "아이디 중복체크 버튼 눌렀다!!", Toast.LENGTH_LONG).show();
                    Log.i(this.getClass().getName(), "아이디 중복체크 canIdCheck : " + canIdCheck);
                    receiveMessage();
                    //Toast.makeText(getApplicationContext(), "아이디 중복체크 버튼 눌렀다!!", Toast.LENGTH_LONG).show();
                    try{
                        worker.join();
                    }catch (InterruptedException e) {}
                }else {
                    Log.i(this.getClass().getName(), "아이디 중복체크 canIdCheck : " + canIdCheck);
                    Toast.makeText(getApplicationContext(), "아이디는 6~10자 영소문자 또는 숫자이어야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // 회원가입
                id = userId.getText().toString();
                password = userPassword.getText().toString();
                name = userName.getText().toString();
                birth = userBirth.getText().toString();
                serial = userSerial.getText().toString();

                if(prevId == null || !prevId.equals(id)) {
                    isIdConfirm = false;    //ID중복체크를 성공한 뒤 아이디를 바꾼 경우
                }

                if(isIdConfirm) {
                    int canRegister = checkRegister(password, name, birth, serial);
                    switch(canRegister) {
                        case 0: //validation 성공
                            sendMessage("#;;70;" + id + "@" + name + "@" +  birth + "@" + serial + "@" + password + ";&");

                            receiveMessage();
                            try{
                                worker.join();
                            }catch (InterruptedException e) {}

//                                if(isSuccessed == true) {
////                                    Toast.makeText(getApplicationContext(), "회원가입 성공..", Toast.LENGTH_LONG).show();
////
////                                    finish();
////                                }else {
////                                    Toast.makeText(getApplicationContext(), "회원가입 실패..", Toast.LENGTH_LONG).show();
////                                }
                            break;
                        case 1: //password validation 실패
                            Toast.makeText(getApplicationContext(), "비밀번호가 형식에 안맞습니다..(숫자, 3~12자리)", Toast.LENGTH_LONG).show();
                            break;
                        case 2: //name validation 실패
                            Toast.makeText(getApplicationContext(), "이름이 형식에 안맞습니다..(대소문자, 한글, 2~20자리)", Toast.LENGTH_LONG).show();
                            break;
                        case 3: //birth validation 실패
                            Toast.makeText(getApplicationContext(), "생일이 형식에 안맞습니다..(숫자, 8자리)", Toast.LENGTH_LONG).show();
                            break;
                        case 4: //serial validation 실패
                            Toast.makeText(getApplicationContext(), "시리얼이 형식에 안맞습니다..(숫자, 4자리)", Toast.LENGTH_LONG).show();
                            break;
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "ID 중복 Check를 하시오..", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RegisterActivity> mActivity;

        public MyHandler(RegisterActivity activity) {
            mActivity = new WeakReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            RegisterActivity activity = mActivity.get();
            if (activity != null) {
                if(message.what == 61) { //ID 중복 체크 사용가능
                    activity.idCheck.setText("You can Use");
                    //Toast.makeText(activity.getApplicationContext(), "ID 사용가능!!", Toast.LENGTH_LONG).show();
                }else if(message.what == 62) { //ID 중복 체크 사용 불가능
                    activity.idCheck.setText("You can't Use");
                    //Toast.makeText(activity.getApplicationContext(), "ID 사용 불가능..", Toast.LENGTH_LONG).show();
                }else if(message.what == 71) { //ID 중복 체크 사용가능
                    //Toast.makeText(activity.getApplicationContext(), "회원가입 성공..", Toast.LENGTH_LONG).show();
                    activity.finish();
                }else if(message.what == 72) { //ID 중복 체크 사용 불가능
                    Toast.makeText(activity.getApplicationContext(), "회원가입 실패..", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private int checkRegister(String password, String name, String birth, String serial) {
        String passwordPattern = "^[0-9]{3,12}$";
        Matcher passwordMatcher = Pattern.compile(passwordPattern).matcher(password);
        String namePattern = "^[a-zA-Z가-힣]{2,20}$";
        Matcher nameMatcher = Pattern.compile(namePattern).matcher(name);
        String birthPattern = "^[0-9]{8,8}$";
        Matcher birthMatcher = Pattern.compile(birthPattern).matcher(birth);
        String serialPattern = "^[0-9]{4,4}$";
        Matcher serialMatcher = Pattern.compile(serialPattern).matcher(serial);
        if(!passwordMatcher.matches()) {
            return 1;
        }else if(!nameMatcher.matches()) {
            return 2;
        }else if(!birthMatcher.matches()) {
            return 3;
        }else if(!serialMatcher.matches()) {
            return 4;
        }
        return 0;
    }

    private boolean checkId(String id) {
        String idPattern = "^[a-z0-9_]{6,10}$";
        Matcher matcher = Pattern.compile(idPattern).matcher(id);

        if(!matcher.matches()) {
            return false;
        }
        return true;
    }

    @Override
    public void connectToServer() {

    }

    @Override
    public void sendMessage(String data) {
        final String message = data;
        Log.i(this.getClass().getName(), "data 보낸다.");
        new Thread() {
            public void run() {
                byte[] byteArray = message.getBytes();
                try{
                    outputStream.write(byteArray);
                }catch(IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
        Log.i(this.getClass().getName(), "data 보내졌다.");
    }

    @Override
    public void receiveMessage() {
        Log.i(this.getClass().getName(), "받 쓰레드 시작 전 " );
        worker = new Thread() {
            public void run() {
                try {
                    Log.i(this.getClass().getName(), "받 쓰레드 시작 전 " );
//                    for(int i = 0; i < 3000; ++i) {
//                        ;
//                    }
                    byte[] byteArray = new byte[256];
                    int size = inputStream.read(byteArray);
                    String message = new String(byteArray, 0, size, "UTF-8");
                    Log.i(this.getClass().getName(), "받은 문자 : " + message);
                    String[] command = message.split(";");

                    Log.i(this.getClass().getName(), "받아지는 data는 뭘까 ? : " + command[2]);
                    Message messageToQueue = new Message();
                    switch (command[2]) {
                        case "71":  //회원 정보 등록 성공
                            messageToQueue = new Message();
                            messageToQueue.what = 71;
                            mHandler.sendMessage(messageToQueue);
                            isSuccessed = true;
                            break;
                        case "72":  //회원 정보 등록 실패
                            messageToQueue = new Message();
                            messageToQueue.what = 72;
                            mHandler.sendMessage(messageToQueue);
                            isSuccessed = false;
                            break;
                        case "61":  //아이디 사용 가능
                            messageToQueue = new Message();
                            messageToQueue.what = 61;
                            mHandler.sendMessage(messageToQueue);
                            prevId = id;
                            isIdConfirm = true;
                            break;
                        case "62":  //아이디 중복으로 사용 불가능
                            messageToQueue = new Message();
                            messageToQueue.what = 62;
                            mHandler.sendMessage(messageToQueue);
                            isIdConfirm = false;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.start();
    }
}
