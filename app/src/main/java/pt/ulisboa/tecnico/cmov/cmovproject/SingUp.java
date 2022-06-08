package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SingUp extends AppCompatActivity {

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