package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingUp extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    private Button btnSignUp;
    private EditText edtName;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        btnSignUp= findViewById(R.id.btn_signup);
        edtName = findViewById(R.id.edt_name);
        edtPassword = findViewById(R.id.edt_password);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    String username = edtName.getText().toString();
                    String pass = edtPassword.getText().toString();
                    if(username.isEmpty() || pass.isEmpty()){
                        Toast.makeText(SingUp.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        Request request =   new Request.Builder()
                                .url("http://10.0.2.2:5000/user/create")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            }
                        });
                        Intent intent = new Intent(SingUp.this,Login.class);
                        intent.putExtra("username",username);
                        intent.putExtra("password",pass);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}