package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NoLogin extends AppCompatActivity {

    private Button btnContinue;
    private EditText edtName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_login);

        btnContinue= findViewById(R.id.btn_continue);
        edtName= findViewById(R.id.edt_name);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                {
                    String username = edtName.getText().toString();
                    if(username.isEmpty()){
                        Toast.makeText(NoLogin.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        Intent intent = new Intent(NoLogin.this,ChatPage.class);
                        intent.putExtra("username",username);
                        startActivity(intent);
                    }
                }

            }


        });
    }
}