package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;


public class MoreFragment extends Fragment implements View.OnClickListener{

    private Button btnNewGroup;
    private Button btnJoinGroup;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more,container,false);

        btnNewGroup = rootView.findViewById(R.id.btn_createGroup);
        btnJoinGroup = rootView.findViewById(R.id.btn_joinGroup);


        btnNewGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addGroup();

            }
        });

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                joinGroup();

            }
        });


        return  rootView;
    }

    public void addGroup(){

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Please fill all the fields");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        final EditText groupName = new EditText(getActivity());
        groupName.setHint("Name:");
        layout.addView(groupName);
        final EditText groupType = new EditText(getActivity());
        groupType.setHint("Type:");
        layout.addView(groupType);

        alert.setView(layout);


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = groupName.getText().toString();
                String type = groupType.getText().toString();

                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    public void joinGroup(){
        //mostrar os grupos para escolher

    }



    @Override
    public void onClick(View view) {


    }

}