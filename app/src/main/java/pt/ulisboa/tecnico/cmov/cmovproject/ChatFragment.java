package pt.ulisboa.tecnico.cmov.cmovproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private CardView groupChatName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat,container,false);

        groupChatName = (CardView) rootView.findViewById(R.id.chatGroup);
        groupChatName.setOnClickListener(this);

        return  rootView;
    }

    @Override
    public void onClick(View view) {

        //mandar para a activity do Miguel
        //Intent i = new Intent(ChatFragment.this,.class)
        //startActivity(i);
    }
}