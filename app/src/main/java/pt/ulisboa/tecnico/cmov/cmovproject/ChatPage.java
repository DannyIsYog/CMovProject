package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Activity;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;


public class ChatPage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar topbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        bottomNavigationView=findViewById(R.id.bottomNav);
        bottomNavigationView.setOnItemSelectedListener(bottomNavMethod);

        topbar=findViewById(R.id.topNav);
        setSupportActionBar(topbar);


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


