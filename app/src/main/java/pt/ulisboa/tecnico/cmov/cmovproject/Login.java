package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword ;
    private Button btnLogin;
    private Button btnSignUp;
    private TextView btnNoLogIn;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signup);
        btnNoLogIn= findViewById(R.id.btn_no_login);

        btnNoLogIn.setPaintFlags(btnNoLogIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(Login.this,SingUp.class);
                startActivity(intent);

            }
        });

        btnNoLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(Login.this,ChatPage.class);
                startActivity(intent1);

            }
        });



    }
}