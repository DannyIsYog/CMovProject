package pt.ulisboa.tecnico.cmov.cmovproject.chat.recycler;

import android.content.Context;
import android.util.Log;
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
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatGroup;

public class RecyclerViewChatAdapter extends RecyclerView.Adapter<RecyclerViewChatAdapter.CustomViewHolder> {

    private List<ChatEntry> entries;
    private ChatGroup chatGroup;
    private Context myContext;

    // TODO: maybe just chatGroup? see how I will recover things from cache...
    public RecyclerViewChatAdapter(Context context, ChatGroup chatGroup) {
        this.entries = chatGroup.getEntries();
        this.chatGroup = chatGroup;
        this.myContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_chat, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        Log.d("RecyclerAdapt: onCreate", "onCreateViewHolder was called!");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        ChatEntry chatEntry = chatGroup.getEntries().get(i);

        // TODO : PUT DATA INTO CUSTOM VIEW HOLDER?
        customViewHolder.myString = chatEntry.getUsername() + " : " + chatEntry.getMsg();

        Log.d("RecyclerAdapt: bind", "myString = "+customViewHolder.myString);
        //Setting text view title
        //customViewHolder.textView.setText("blabla");
    }

    @Override
    public int getItemCount() {
        return (null != entries ? entries.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        //protected TextView textView;
        protected String myString = "asd";

        public CustomViewHolder(View view) {
            super(view);
            this.myString = "ola ola ola debug debug";
            //this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            //this.textView = (TextView) view.findViewById(R.id.title);
        }
    }
}
