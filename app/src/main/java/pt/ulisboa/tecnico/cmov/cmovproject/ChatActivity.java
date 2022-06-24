package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
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

    private Boolean isOutOfRoom = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d("ChatActivity","ENTERED CHAT_ACTIVITY" );

        this.appContext = (AppContext) getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(AppContext.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        this.chatGroup = new ChatGroup();
       
        this.groupID = getIntent().getExtras().getString("groupID");
        Log.d("ChatActivity", "Create() - groupID from Intent: "+groupID);
        //this.groupID = "myGroup";

        this.myUsername = sharedPref.getString("username", "MR. NOBODY");
        this.myPwd = sharedPref.getString("password", "I HAVE NO PASSOWRD");

        if (myPwd == null || myUsername == null) {
            Log.d("ChatActivity - create()",
                "ERROR: USERNAME OR PASSWORD COULDN'T BE RETRIEVED FROM SHARED PREFERENCES");
        }

        Log.d("ChatActivity", "Create():");
        Log.d("ChatActivity", "Create() - username: "+myUsername);
        Log.d("ChatActivity", "Create() - password: "+myPwd);
        Log.d("ChatActivity", "Create() - groupID: "+groupID);


        // new code for weird recycle list

        RecyclerView layoutList = findViewById(R.id.chat_entries);

        // instantiate my custom adapter and assign it to the view
        adapter = new RecyclerViewChatAdapter(ChatActivity.this,
                this.appContext,
                this.groupID,
                this.myUsername,
                this.myPwd);
        layoutList.setLayoutManager(new LinearLayoutManager(this));
        layoutList.setAdapter(adapter);

        // Insert new message on chat
        et = findViewById(R.id.new_chat_entry);
        et.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Log.d("ChatActivity : OnKey", "detected key press");

                // filter action to only run method on release
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    sendMessage(getNewEntry().getMsg());
                    adapter.updateItemCount();
                }
                return false;
            }
        });

        // ** GPS STUFF **
        // check if room is geoFenced
        Integer roomType = getIntent().getExtras().getInt("roomType", 1);
        Log.d("ChatActivity", "roomType: "+roomType);

        if(roomType==3) {
            Double roomLatitude = getIntent().getExtras().getDouble("roomLatitude");
            Double roomLongitude = getIntent().getExtras().getDouble("roomLongitude");
            Double roomRadius = getIntent().getExtras().getDouble("roomRadius");
            if (roomLongitude == 0 || roomLatitude == 0 || roomRadius == 0 ) {
                Log.e("ChatActivity", "Initialize GPS listener: one of the parms was zero:");
                Log.e("ChatActivity", "roomLatitude = "+roomLatitude);
                Log.e("ChatActivity", "roomLongitude = "+roomLongitude);
                Log.e("ChatActivity", "roomRadius = "+roomRadius);
            }
            ChatGPSListener gpsListener = new ChatGPSListener(this, roomLatitude, roomLongitude, roomRadius);

        }

        // TODO: create a thread to update the item count each x seconds
        Runnable threadItemCount = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                    adapter.updateItemCount();
                } catch (InterruptedException ie) {
                    return;
                }
            }
        };
        Thread thrItemCount = new Thread(threadItemCount, "Thread update item count");
        thrItemCount.start();

        // TODO: create a thread to search for deleted messages a notify adapter/clear cache position

        // TODO: thread to see if user is still in room, otherwise leave


        // show existing messages
        // erase this since it's very heavy
        updateShowMessages();


    }

    private void updateShowMessages(){
        this.adapter.updateItemCount();
        Log.d("ChatActivity", "Update() : will notify RecyclerAdapter");
        this.adapter.notifyDataSetChanged();

        Log.d("ChatActivity", "Update() : notified");

    }

    public ChatEntry getNewEntry() {
        String textMsg = et.getText().toString();
        // reset text on input
        et.setText("");
        Log.d("ChatActivity: getNE", "textMsg = "+textMsg);
        return new ChatEntry(this.myUsername, textMsg, "never");
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

    public void setIsOutOfRoom(Boolean newVal) {
        synchronized (this.isOutOfRoom) {
            this.isOutOfRoom = newVal;
        }
    }

    public Boolean getIsOutOfRoom(){
        synchronized (this.isOutOfRoom) {
            return this.isOutOfRoom;
        }
    }
}