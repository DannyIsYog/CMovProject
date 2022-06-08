package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class ChatPage extends AppCompatActivity {


    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();

    private final Type type = Types.newParameterizedType(List.class, Room.class);
    private final JsonAdapter<List<Room>> adapter = moshi.adapter(type);

    private BottomNavigationView bottomNavigationView;
    private Toolbar topbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);



        bottomNavigationView=findViewById(R.id.bottomNav);
        topbar=findViewById(R.id.topNav);


        Intent i = getIntent();
        String userName = i.getStringExtra("username");
        String pass = i.getStringExtra("password");

        bottomNavigationView.setOnItemSelectedListener(bottomNavMethod);
        setSupportActionBar(topbar);

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
                Log.d("Response", response.body().string());
                List rooms = adapter.fromJson(response.toString());
                Log.d("Response", String.valueOf(rooms));
            }
        });

    }

    private BottomNavigationView.OnItemSelectedListener bottomNavMethod=new BottomNavigationView.OnItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){

            Fragment fragment=null;
            switch (menuItem.getItemId()){
                case R.id.chatsFragment:
                    fragment=new ChatFragment();
                    break;

                case R.id.addFragment:
                    fragment=new MoreFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment).commit();
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        return true;

    }


}


