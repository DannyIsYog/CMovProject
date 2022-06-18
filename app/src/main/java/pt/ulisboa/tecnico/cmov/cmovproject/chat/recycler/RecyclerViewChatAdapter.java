package pt.ulisboa.tecnico.cmov.cmovproject.chat.recycler;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.cmovproject.R;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntry;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntryID;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatGroup;

public class RecyclerViewChatAdapter extends RecyclerView.Adapter<RecyclerViewChatAdapter.CustomViewHolder> {

    private List<ChatEntry> entries;
    private ChatGroup chatGroup;
    private Context myContext;
    private LruCache<ChatEntryID, ChatEntry> chatEntryLruCache;

    public RecyclerViewChatAdapter(Context context, ChatGroup chatGroup) {
        this.entries = chatGroup.getEntries();
        this.chatGroup = chatGroup;
        this.myContext = context;
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
        ChatEntry chatEntry = chatGroup.getEntries().get(i);

        Log.d("RecyclerAdapt: bind", "myString = "+customViewHolder.toString());
        //Setting text view title
        customViewHolder.msgTextView.setText(chatEntry.getMsg().getText());
        customViewHolder.usernameView.setText(chatEntry.getUsername());
    }

    @Override
    public int getItemCount() {
        return (null != entries ? entries.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

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
