package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Login extends AppCompatActivity {

    private EditText edtName;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView btnSignUp;
    private TextView btnNoLogIn;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtName = findViewById(R.id.edt_name);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signup);
        btnNoLogIn= findViewById(R.id.btn_no_login);

        //underlines the text
        btnNoLogIn.setPaintFlags(btnNoLogIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtName.getText().toString();
                String pass = edtPassword.getText().toString();
                if(username.isEmpty() || pass.isEmpty()){
                    Toast.makeText(Login.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    Intent intent = new Intent(Login.this,ChatPage.class);
                    intent.putExtra("username",username);
                    intent.putExtra("password",pass);
                    startActivity(intent);
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,SingUp.class);
                startActivity(intent);
            }
        });

        btnNoLogIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,NoLogin.class);
                startActivity(intent);
            }
        });

        Request request =   new Request.Builder()
                .url("http://10.0.2.2:5000/room/get/all")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                Log.d("Login", "onResponse " + responseBody.string());
            }
        });

    }
}