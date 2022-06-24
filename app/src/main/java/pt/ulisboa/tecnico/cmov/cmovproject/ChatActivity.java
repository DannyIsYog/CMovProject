package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.*;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.Message;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.message.TextMessage;
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



        // new code for weird recycle list

        RecyclerView layoutList = findViewById(R.id.chat_entries);

        // instantiate my custom adapter and assign it to the view
        adapter = new RecyclerViewChatAdapter(ChatActivity.this,
                this.appContext,
                this.groupID,
                this.myUsername,
                this.myPwd);
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
                    //addEntry(getNewEntry()); OLD CODE
                    sendMessage(getNewEntry().getMsg());
                }
                return false;
            }
        });

        // show existing messages
        updateShowMessages();


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
                .add("chatroom", groupID)
                .add("username", myUsername)
                .add("password", myPwd)
                .add("message", msg.getText())

                .build();

        Request req =   new Request.Builder()
                .url(AppContext.SERVER_ADDR+"/message/send")
                .post(reqBody)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                try {
                    respObject[0] = new JSONObject(resp);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
                try {
                    Log.d("ChatActivity - Response", respObject[0].getString("status"));
                    if (!respObject[0].getString("status").equals("success")) {
                        throw new IOException("ERROR SENDING MESSAGE");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d("AppContext", e.getMessage());
            }
        });
        updateShowMessages();
    }
}