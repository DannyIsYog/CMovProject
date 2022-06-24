package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MoreFragment extends Fragment implements View.OnClickListener{

    private Button btnNewGroup;
    private Button btnJoinGroup;

    private final OkHttpClient client = new OkHttpClient();

    JSONObject respObject;


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

        Context context = getContext();

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Please fill all the fields");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        final EditText groupName = new EditText(context);
        groupName.setHint("Name:");
        layout.addView(groupName);
        final EditText groupType = new EditText(context);
        groupType.setHint("Type:");
        layout.addView(groupType);

        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = groupName.getText().toString();
                String type = groupType.getText().toString();

                Log.d("create group",name);
                Log.d("type",type);

                if(name.isEmpty() || type.isEmpty()){
                    Toast.makeText(getActivity(), "Invalid Input", Toast.LENGTH_SHORT).show();

                    return;
                }
                else{
                    ChatPage chatPage = (ChatPage) getActivity();

                    //cria grupo no server
                    RequestBody formBody = new FormBody.Builder()
                            .add("name",name)
                            .add("roomType",type)
                            .build();
                    Request request =   new Request.Builder()
                            .url("http://10.0.2.2:5000/room/create")
                            .post(formBody)
                            .build();

                    RequestBody formBody2 = new FormBody.Builder()
                            .add("room",name)
                            .add("user",chatPage.getUserName())
                            .build();
                    Request request2 =   new Request.Builder()
                            .url("http://10.0.2.2:5000/room/join")
                            .post(formBody2)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String resp = response.body().string();
                            Log.d("Response", resp);
                            try {
                                respObject = new JSONObject(resp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if(respObject.getString("status").equals("success"))
                                {
                                    //go to show chats activity or join the chat that was just created maybe
                                }
                                else
                                {
                                    chatPage.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(chatPage.getApplicationContext(), respObject.getString("message"), Toast.LENGTH_SHORT).show();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    client.newCall(request2).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String resp = response.body().string();
                            Log.d("Response", resp);
                        }
                    });
                }
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