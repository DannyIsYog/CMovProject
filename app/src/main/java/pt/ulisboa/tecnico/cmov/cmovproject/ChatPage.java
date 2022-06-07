package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Activity;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ChatPage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        bottomNavigationView=findViewById(R.id.bottomNav);

        bottomNavigationView.setOnItemSelectedListener(bottomNavMethod);

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

                case R.id.settingsFragment:
                    fragment=new SettingsFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment).commit();
            return true;
        }
    };

}


