package pt.ulisboa.tecnico.cmov.cmovproject;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView view;
    GroupListAdapter.OnNoteListener onNoteListener;

    public RecyclerViewHolder(@NonNull View itemView, GroupListAdapter.OnNoteListener onNoteListener){
        super(itemView);
        view = itemView.findViewById(R.id.chatName);
        this.onNoteListener = onNoteListener;

        itemView.setOnClickListener(this);
    }

    public TextView getView(){
        return view;
    }

    @Override
    public void onClick(View view) {
        onNoteListener.onNoteClick(getAdapterPosition());
    }
}