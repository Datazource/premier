package valet.digikom.com.valetparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.UUID;

import valet.digikom.com.valetparking.dao.AuthResDao;
import valet.digikom.com.valetparking.dao.CarDao;
import valet.digikom.com.valetparking.dao.ColorDao;
import valet.digikom.com.valetparking.dao.DefectDao;
import valet.digikom.com.valetparking.dao.DropDao;
import valet.digikom.com.valetparking.dao.FineFeeDao;
import valet.digikom.com.valetparking.dao.ItemsDao;
import valet.digikom.com.valetparking.dao.TokenDao;
import valet.digikom.com.valetparking.service.ApiClient;
import valet.digikom.com.valetparking.util.PrefManager;
import valet.digikom.com.valetparking.util.ValetDbHelper;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener, AuthResDao.OnAuthListener {

    LinearLayout linearLayout;
    EditText inputEmail;
    EditText inputPassword;
    Button btnLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (PrefManager.getInstance(this).getAuthResponse() != null) {
            goToMain();
            return;
        }

        saveDeviceAndAppId();

        linearLayout = (LinearLayout) findViewById(R.id.container_input);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    private void saveDeviceAndAppId() {
        TelephonyManager telephonyManager = (android.telephony.TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        String appId = UUID.randomUUID().toString();

        if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(appId)) {
            PrefManager.getInstance(this).saveDeviceAndAppId(deviceId, appId);
        }

    }


    @Override
    public void onClick(View view) {
        progressBar.setVisibility(View.VISIBLE);

        final String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(SplashActivity.this, R.string.login_empty,Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                AuthResDao authResDao = AuthResDao.getInstance(SplashActivity.this);
                authResDao.login(email, password, SplashActivity.this);
            }
        }).run();

    }

    @Override
    public void loginFailed() {
        progressBar.setVisibility(View.GONE);
        inputEmail.setText("");
        inputPassword.setText("");
    }

    @Override
    public void loginSuccess() {
        ApiClient.downloadData(this);
        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
