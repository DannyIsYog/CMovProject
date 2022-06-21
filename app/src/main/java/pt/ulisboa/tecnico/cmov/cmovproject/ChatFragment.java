package pt.ulisboa.tecnico.cmov.cmovproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();

    private final Type type = Types.newParameterizedType(List.class, Room.class);
    private final JsonAdapter<List<Room>> adapter = moshi.adapter(type);

    //private CardView groupChatName;

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RequestBody formBody = new FormBody.Builder()
                .add("user","testUser")
                .build();
        Request request =   new Request.Builder()
                .url("http://10.0.2.2:5000/room/get/user")
                .post(formBody)
                .build();


        View rootView = inflater.inflate(R.layout.fragment_chat,container,false);


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                Log.d("Response", resp);
                List<Room> rooms = adapter.fromJson(resp);
                Log.d("Response", String.valueOf(rooms));

                //create recycleview
                recyclerView = rootView.findViewById(R.id.recycler);
                recyclerView .setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
                recyclerView.setAdapter(new GroupListAdapter(rooms));
            }
        });

        //groupChatName = (CardView) rootView.findViewById(R.id.chatGroup);
        //groupChatName.setOnClickListener(this);


        return  rootView;
    }

    @Override
    public void onClick(View view) {

        //mandar para a activity do Miguel
        //Intent i = new Intent(ChatFragment.this,.class)
        //startActivity(i);
    }
}