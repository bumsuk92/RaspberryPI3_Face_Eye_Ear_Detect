package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * 앱을 실행하면 처음 나타나는 로그인 UI를 담당하는 Activity
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity implements IClient{
    private TextView registerScreen = null;
    private Button btnLogin = null;
    private Client client;
    private Socket socket;
    private InputStream inputStream; // input Stream
    private OutputStream outputStream; //output Stream

    private EditText idTextBox = null;
    private EditText passwordTextBox = null;
    private String id, password, autoId, autoPassword, autoToken;
    private UserItem user = null;
    private Thread worker = null;

    private int failCause;
    private EditText status;
    private boolean activityMove;

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(!activityMove) {
//            client.stop();
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);

        registerScreen = (TextView) findViewById(R.id.link_to_register);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        status = (EditText) findViewById(R.id.status);
        status.setClickable(false);
        status.setFocusable(false);
        activityMove = false;

        client = Client.getClient();
        socket = client.getSocket();
        if(socket.isClosed()) {
            client.connectToServer();
        }
        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();
        user = UserItem.getUser();

        Bundle temp;
        if((temp = getIntent().getExtras()) != null) { //질식 상황일경우
            if (temp.get("danger") != null) {
                Log.i("....", "질식상황으로 셋팅 완료");
                user.setDanger(true);
            }else {
                Log.i("....", "질식상황으로 셋팅 실패" +
                        "");
                user.setDanger(false);
            }
        }else {
            user.setDanger(false);
        }

        //자동로그인을 위해 어플에 저장되는 파일
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        autoId = auto.getString("id",null);
        autoPassword = auto.getString("password", null);
        autoToken = auto.getString("token", null);

        if(autoId !=null && autoPassword != null) {   //자동 로그인!
            sendMessage("#;;50;" + autoId + "@" + autoPassword + "@" + autoToken + ";&");

            receiveMessage();

            try{
                worker.join();
            }catch (InterruptedException e) {}

            if(user.getName() != null) {
                user.setId(autoId);
                user.setPassword(password);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                activityMove = true;
                finish();
            }

        }



        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                //로그인 성공 시 MainActivity로 화면 전환
                idTextBox = (EditText) findViewById(R.id.id);
                passwordTextBox = (EditText) findViewById(R.id.password);

                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

                id = idTextBox.getText().toString();
                password = passwordTextBox.getText().toString();
                autoToken = auto.getString("token", null);

                id = id.replace(" ", "");
                password = password.replace(" ", "");
                boolean canLogin = checkLogin(id, password);

                if(canLogin) {  //로그인 가능!
                    //로그인 시도
                    sendMessage("#;;50;" + id + "@" + password + "@" + autoToken + ";&");

                    receiveMessage();
                    try{
                        worker.join();
                    }catch (InterruptedException e) {}

                    if(user.getName() != null) {
                        user.setId(id);
                        user.setPassword(password);

                        SharedPreferences.Editor autoLogin = auto.edit();
                        autoLogin.putString("id", id);
                        autoLogin.putString("password", password);
                        autoLogin.commit();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        activityMove = true;
                        finish();
                    }else {
                        if(failCause == 0) {
                           status.setText("로그인 실패..");
                            //Toast.makeText(getApplicationContext(), "로그인 실패..", Toast.LENGTH_LONG).show();
                        }else {
                            status.setText("침대를 먼저 켜시오..");
                            //Toast.makeText(getApplicationContext(), "침대를 먼저 켜시오..", Toast.LENGTH_LONG).show();
                        }
                    }
                }else {
                    status.setText("id나 password를 형식에 맞게 기입하십시오..");
                    //Toast.makeText(getApplicationContext(), "id나 password를 형식에 맞게 기입하십시오..", Toast.LENGTH_LONG).show();
                }


            }


        });

        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                //회원가입을 위해 RegisterActivity로 화면 전환
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    public boolean checkLogin(String id, String password) {
        String idPattern = "^[a-z0-9_]{6,10}$";
        Matcher idMatcher = Pattern.compile(idPattern).matcher(id);
        String passwordPattern = "^[0-9]{3,12}$";
        Matcher passwordMatcher = Pattern.compile(passwordPattern).matcher(password);

        if(!idMatcher.matches()) {
            return false;
        }
        if(!passwordMatcher.matches()) {
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
        worker = new Thread() {
            public void run() {
                try {
                    byte[] byteArray = new byte[256];
                    int size = inputStream.read(byteArray);
                    String message = new String(byteArray, 0, size, "UTF-8");
                    Log.i(this.getClass().getName(), "받은 answkanswk문자 : " + message);
                    String[] command = message.split(";");

                    Log.i(this.getClass().getName(), "받아지는 data는 뭘까 ? : " + command[2]);
                    switch (command[2]){
                        case "51":  //로그인 성공
                            String[] userDataObject = command[3].split("@");
                            user.setName(userDataObject[0]);
                            user.setBirth(userDataObject[1]);
                            user.setAutoSleepModeStatus(userDataObject[2]);
                            user.setSerial(userDataObject[3]);

                            break;
                        case "52":  //로그인 실패
                            failCause = 0;
                            break;
                        case "53":  //디바이스 안켜서 로그인 실패
                            failCause = 1;
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
