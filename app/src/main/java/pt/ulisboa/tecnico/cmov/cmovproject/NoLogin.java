package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoLogin extends AppCompatActivity {

    private Button btnContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_login);

        btnContinue= findViewById(R.id.btn_continue);


        btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(NoLogin.this, ChatPage.class);
                startActivity(intent);
            }


        });
    }
}