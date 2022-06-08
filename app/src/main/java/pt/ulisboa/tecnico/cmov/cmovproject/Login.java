package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private EditText edtName;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView btnSignUp;
    private TextView btnNoLogIn;


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



    }
}