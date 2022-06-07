package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.*;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.recycler.RecyclerViewChatAdapter;

public class ChatActivity extends AppCompatActivity {
    private ChatGroup chatGroup;
    private String myUsername;
    private EditText et;
    private RecyclerViewChatAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d("ChatActivity","ENTERED CHAT_ACTIVITY" );
        // TODO: get group from the other activity, for now use hard coded one
        // TODO: same but for username
        this.chatGroup = new ChatGroup();
        this.myUsername = "Broccoli";

        // show messages
        //updateShowMessages();

        // new code for weird recycle list

        RecyclerView layoutList = findViewById(R.id.chat_entries);

        // instantiate my custom adapter and assign it to the view
        adapter = new RecyclerViewChatAdapter(ChatActivity.this, this.chatGroup);
        Log.d("ChatActivity", "Will set adapter to recyclerView!");
        layoutList.setAdapter(adapter);
        Log.d("ChatActivity", "Adapter was set on recyclerView!");

        layoutList.setLayoutManager(new LinearLayoutManager(this));
        Log.d("ChatActivity", "LayoutList was set on recyclerView!");
        
        // Insert new message on chat
        et = findViewById(R.id.new_chat_entry);
        Log.d("ChatActivity", "EditText of new chat entry OK");
        et.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d("ChatActivity : OnKey", "detected key press");
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
        ArrayList<ChatEntry> list = chatGroup.getEntries();

        // OLD CODE FOR NORMAL LIST
        /*
        String[] msgArray = new String[list.size()];
        int i = 0;
        for (ChatEntry entry : list) {
            msgArray[i++] = entry.getUsername() + " said: " + entry.getMsg().getText();
        }

        Log.d("ChatActivity", "update: msgArray = "+String.valueOf(msgArray.toString()));

        // put array adapter on R.id.todo_list
        ArrayAdapter<String> arr =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgArray);

        ListView layoutList = findViewById(R.id.chat_entries);
        layoutList.setAdapter(arr);
        // end of old code
        */

        Log.d("ChatActivity","Update() : will notify RecyclerAdapter");
        this.adapter.notifyDataSetChanged();
        Log.d("ChatActivity","Update() : notified RecyclerAdapter");


        //

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
}