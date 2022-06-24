package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GO TO CHAT ACTIVITY, ONLY DEBUG FOR NOW! DELETE THIS IF YOU NEED TO
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }
}