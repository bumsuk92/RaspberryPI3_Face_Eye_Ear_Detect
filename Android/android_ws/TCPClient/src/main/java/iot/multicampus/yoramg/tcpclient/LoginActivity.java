package iot.multicampus.yoramg.tcpclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView registerScreen;
    private Button btnLogin;
    private String ipaddress;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        registerScreen = (TextView) findViewById(R.id.link_to_register);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener( this );
        registerScreen.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        switch ( v.getId() ) {
            case R.id.btnLogin:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.link_to_register:
                i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                break;
        }
    }
}
