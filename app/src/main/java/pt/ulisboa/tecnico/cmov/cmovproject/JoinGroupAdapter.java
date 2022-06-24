package pt.ulisboa.tecnico.cmov.cmovproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JoinGroupAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private List<Room> _groupChats;

    public JoinGroupAdapter() {
    }

    @Override
    public int getItemViewType(final int position){
        return R.layout.frame_groupview;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(viewType,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {

        String currentGroup = _groupChats.get(position).getName();

        holder.getView().setText(String.valueOf(currentGroup));

    }

    @Override
    public int getItemCount() {
        return _groupChats.size();
    }
}
