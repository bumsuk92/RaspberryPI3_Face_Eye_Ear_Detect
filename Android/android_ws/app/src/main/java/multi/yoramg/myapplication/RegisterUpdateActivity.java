package multi.yoramg.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUpdateActivity extends Activity implements IClient{
    private TextView mainScreen;

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
    private Button buttonUpdate = null;
    private Button buttonDelete = null;

    private UserItem user;

    private boolean isSuccessed;
    private boolean isWaitMessage;

    private String id, password, name, birth, serial;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register_update);

        user = UserItem.getUser();
        isWaitMessage = false;  //메세지 여러번 대기 하지 않도록 하는 flag

        userId = (EditText) findViewById(R.id.userId);
        userPassword = (EditText) findViewById(R.id.userPassword);
        userName = (EditText) findViewById(R.id.userName);
        userBirth = (EditText) findViewById(R.id.userBirth);
        userSerial = (EditText) findViewById(R.id.userSerial);

        userId.setText(user.getId());
        userName.setText(user.getName());
        userBirth.setText(user.getBirth());
        userSerial.setText(user.getSerial());

        userId.setClickable(false);//바보
        userId.setFocusable(false);//한쥬스

        mainScreen = (TextView) findViewById(R.id.link_to_main);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);

        client = Client.getClient();
        socket = client.getSocket();
        if(socket.isClosed()) {
            client.connectToServer();
        }
        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();

        // Listening to main Screen link
        mainScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                finish();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //회원정보 삭제 요청
                if(!isWaitMessage) {
                    sendMessage("#;" + user.getId() + ";90;;&");

                    receiveMessage();
//                try{
//                    worker.join();
//                }catch (InterruptedException e) {}

                    if(isSuccessed == true) {
                        Toast.makeText(getApplicationContext(), "회원삭제 성공..", Toast.LENGTH_LONG).show();

                        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

                        //자동 로그인을 위한 정보 삭제
                        SharedPreferences.Editor autoLogin = auto.edit();
                        autoLogin.clear();
                        autoLogin.commit();
                        user.logout();


                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(), "회원삭제 실패..", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // 회원가입
                id = userId.getText().toString();
                password = userPassword.getText().toString();
                name = userName.getText().toString();
                birth = userBirth.getText().toString();
                serial = userSerial.getText().toString();

                int canRegister = checkRegister(password, name, birth, serial);
                switch(canRegister) {
                    case 0: //validation 성공
                        if(!isWaitMessage) {
                            sendMessage("#;" + user.getId() + ";80;" + name + "@" +  birth + "@" + serial + "@" + password + ";&");

                            receiveMessage();
                            try{
                                worker.join();
                            }catch (InterruptedException e) {}

                            if(isSuccessed == true) {
                                Toast.makeText(getApplicationContext(), "회원수정 성공..", Toast.LENGTH_LONG).show();
                                user.setPassword(password);
                                user.setName(name);
                                user.setBirth(birth);
                                user.setSerial(serial);

                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(), "회원수정 실패..", Toast.LENGTH_LONG).show();
                            }
                        }
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
            }


        });
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
        isWaitMessage = true;
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
                    Log.i(this.getClass().getName(), "받은 문자 : " + message);
                    String[] command = message.split(";");

                    switch (command[2]) {
                        case "81":  //회원 수정 성공
                            isSuccessed = true;
                            break;
                        case "82":  //회원 수정 실패
                            isSuccessed = false;
                            break;
                        case "91":  //회원 삭제 성공
                            isSuccessed = true;
                            break;
                        case "92":  //회원 삭제 실패
                            isSuccessed = false;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isWaitMessage = false;
            }
        };

        worker.start();
    }
}