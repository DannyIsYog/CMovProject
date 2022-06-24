package pt.ulisboa.tecnico.cmov.cmovproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListGroupsFragment extends Fragment implements View.OnClickListener, GroupListAdapter.OnNoteListener {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);



        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView .setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(new JoinGroupAdapter(ListGroupsFragment.this));
        return rootView;

    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public void onNoteClick(int position) {

    }
}
