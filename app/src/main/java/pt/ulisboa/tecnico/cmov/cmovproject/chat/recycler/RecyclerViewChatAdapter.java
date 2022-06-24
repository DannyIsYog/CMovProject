package pt.ulisboa.tecnico.cmov.cmovproject.chat.recycler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.ulisboa.tecnico.cmov.cmovproject.AppContext;
import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntry;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntryID;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatGroup;

public class RecyclerViewChatAdapter extends RecyclerView.Adapter<RecyclerViewChatAdapter.CustomViewHolder> {

    //private List<ChatEntry> entries;
    //private ChatGroup chatGroup;
    private String myUsername;
    private String myPasswd;
    private String groupID;
    private Context myContext;
    private AppContext appContext;
    private Integer lastKnownIdx;

    private final Object lockLastIdx = new Object();

    public RecyclerViewChatAdapter(Context context, AppContext appContext, String groupID, String user, String pwd) {
        /*this.entries = chatGroup.getEntries();
        this.chatGroup = chatGroup;*/

        this.appContext = appContext;
        this.groupID = groupID;
        this.myContext = context;
        this.lastKnownIdx = 0;
        this.myUsername = user;
        this.myPasswd = pwd;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_entry_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        Log.d("RecyclerAdapt: onCreate", "onCreateViewHolder was called!");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        //ChatEntry chatEntry = chatGroup.getEntries().get(i);
        ChatEntry chatEntry = this.appContext.getChatEntry(new ChatEntryID(groupID, i));

        Log.d("RecyclerAdapt: bind", "myString = "+customViewHolder.toString());

        //Setting text views on viewHolder
        customViewHolder.msgTextView.setText(chatEntry.getMsg().getText());
        customViewHolder.usernameView.setText(chatEntry.getUsername());
    }

    public void setLastKnownIdx(int newLastIdx) {
        synchronized ( this.lockLastIdx ) {
            if (this.lastKnownIdx < newLastIdx) {
                this.lastKnownIdx = newLastIdx;
                Log.d("RecyclerChat", "setLastKnownID() - hashCode of this instance: "+this.hashCode());
                Log.d("RecyclerChat", "setLastKnownID() - now it is "+this.lastKnownIdx);
            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d("RecyclerViewChat", "Entered getItemCount(), will probably return "+this.lastKnownIdx);
        Log.d("RecyclerViewChat", "Entered getItemCount(), hashCode of this instance: "+this.hashCode());
        final OkHttpClient client = new OkHttpClient();
        final JSONObject[] respObject = new JSONObject[1];


        RequestBody reqBody = new FormBody.Builder()
                .add("username",this.myUsername )
                .add("password", this.myPasswd)
                .add("chatroom", this.groupID)

                .build();

        Request req =   new Request.Builder()
                .url(AppContext.SERVER_ADDR+"/message/getLastID")
                .post(reqBody)
                .build();

        final RecyclerViewChatAdapter myAdapter = this;

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)  {
                String resp;
                try {
                    resp = response.body().string();
                } catch (IOException ioe) {
                    Log.d("RecyclerChat", "getItemCount() - response from server had problem");
                    Log.d("RecyclerChat", "getItemCount() - exception msg: "+ioe.getLocalizedMessage());
                    return;
                }
                try {
                    respObject[0] = new JSONObject(resp);
                    Log.d("RecyclerView - Response", "ItemCount() status: "+respObject[0].getString("status"));
                    if (!respObject[0].getString("status").equals("success")) {
                        Log.d("RecyclerViewChat", "Error getting last msg ID: response was not success, instead it was: "+respObject[0].toString());
                        //throw new IOException("ERROR GETTING LAST MSG ID");
                        return;
                    }

                    Log.d("RecyclerViewChat", "getItemCount(), received pkt with ID = "
                            + respObject[0].getString("message"));

                    myAdapter.setLastKnownIdx( Integer.parseInt(respObject[0].getString("message")) );

                } catch (JSONException e) {
                    Log.d("RecyclerViewChat",
                    "ERROR: Response had json problem, exception msg: "+e.getLocalizedMessage());
                    return;
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d("RecyclerViewChatAdapter", "error downloading last msg ID: "+e.getMessage());
            }
        });
        synchronized (this.lockLastIdx) {
            Log.d("RecyclerChat", "ending getItemCount(), will return "+this.lastKnownIdx);
            return this.lastKnownIdx + 1;
        }
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {

        protected TextView usernameView;
        protected TextView msgTextView;

        public CustomViewHolder(View view) {
            super(view);
            Log.d("RecyclerAdapt: custom", "someone called my constructor!");
            this.usernameView = (TextView) view.findViewById(R.id.chat_entry_row_username);
            this.msgTextView = (TextView) view.findViewById(R.id.chat_entry_row_text);
        }

        @Override
        public String toString() {
            return "(RecyclerAdapter.CustomViewHolder) - username: "+usernameView.getText() +
                    " msg: " + msgTextView.getText();
        }
    }
}
