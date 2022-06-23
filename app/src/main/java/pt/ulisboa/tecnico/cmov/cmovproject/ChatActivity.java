package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.*;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.Message;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.recycler.RecyclerViewChatAdapter;

public class ChatActivity extends AppCompatActivity {
    private ChatGroup chatGroup;
    private String myUsername;
    private String myPwd;
    private String groupID;
    private EditText et;
    private RecyclerViewChatAdapter adapter;
    private AppContext appContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d("ChatActivity","ENTERED CHAT_ACTIVITY" );

        this.appContext = (AppContext) getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(AppContext.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        this.chatGroup = new ChatGroup();
        //this.groupID = savedInstanceState.getString("groupID"); // TODO: change to this
        this.groupID = "myGroup";

        this.myUsername = sharedPref.getString("username", "MR. NOBODY");
        this.myPwd = sharedPref.getString("password", "I HAVE NO PASSOWRD");

        if (myPwd == null || myUsername == null) {
            Log.d("ChatActivity - create()",
                "ERROR: USERNAME OR PASSWORD COULDN'T BE RETRIEVED FROM SHARED PREFERENCES");
        }

        // show messages
        //updateShowMessages();

        // new code for weird recycle list

        RecyclerView layoutList = findViewById(R.id.chat_entries);

        // instantiate my custom adapter and assign it to the view
        adapter = new RecyclerViewChatAdapter(ChatActivity.this, this.chatGroup);
        layoutList.setLayoutManager(new LinearLayoutManager(this));
        Log.d("ChatActivity", "LayoutList was set on recyclerView!");
        Log.d("ChatActivity", "Will set adapter to recyclerView!");
        layoutList.setAdapter(adapter);
        Log.d("ChatActivity", "Adapter was set on recyclerView!");

        // Insert new message on chat
        et = findViewById(R.id.new_chat_entry);
        Log.d("ChatActivity", "EditText of new chat entry OK");
        et.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Log.d("ChatActivity : OnKey", "detected key press");

                // filter action to only run method on release
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // enter detected, lets add string to list
                    addEntry(getNewEntry());

                    Log.d("ChatActivity : OnKey","list = "+chatGroup.getEntries().toString());
                }
                return false;
            }
        });


    }

    private void updateShowMessages(){

        Log.d("ChatActivity", "Update() : will notify RecyclerAdapter");
        this.adapter.notifyDataSetChanged();

    }

    public ChatEntry getNewEntry() {
        String textMsg = et.getText().toString();
        // reset text on input
        et.setText("");
        Log.d("ChatActivity: getNE", "textMsg = "+textMsg);
        return new ChatEntry(this.myUsername, textMsg);
    }

    private void addEntry(ChatEntry newEntry){
        this.chatGroup.addEntry(newEntry);
        // update messages on screen
        updateShowMessages();
    }

    private void sendMessage(Message msg) {
        final OkHttpClient client = new OkHttpClient();
        final JSONObject[] respObject = new JSONObject[1];

        RequestBody reqBody = new FormBody.Builder()
                .add("room", groupID)
                .add("user", myUsername)
                .add("password", myPwd)
                .add("message", msg.getText())

                .build();

        Request req =   new Request.Builder()
                .url(AppContext.SERVER_ADDR+"/message/send")
                .post(reqBody)
                .build();
    }
}