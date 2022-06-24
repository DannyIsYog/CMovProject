package pt.ulisboa.tecnico.cmov.cmovproject;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ListGroupsFragment extends Fragment implements View.OnClickListener, LocationListener, GroupListAdapter.OnNoteListener {

    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();

    private final Type type = Types.newParameterizedType(List.class, Room.class);
    private final JsonAdapter<List<Room>> adapter = moshi.adapter(type);
    private RecyclerView recyclerView;

    double latitude;
    double longitude;
    Location currentLoc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ChatPage chatPage = (ChatPage) getActivity();
        RequestBody formBody = new FormBody.Builder()
                .add("user",chatPage.getUserName())
                .build();
        Request request =   new Request.Builder()
                .url("http://10.0.2.2:5000/room/get/join")
                .post(formBody)
                .build();
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
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
                List<Room> filteredRooms = new ArrayList<>();
                Location loc;
                float distance;
                for(Room room: rooms)
                {
                    if(room.getRoomType()==3) {
                        loc = new Location("");
                        loc.setLatitude(room.getLatitude());
                        loc.setLongitude(room.getLongitude());
                        while(currentLoc == null) {}
                        distance = loc.distanceTo(currentLoc);
                        if (distance < room.getRadius()) filteredRooms.add(room);
                    }
                    else
                    {
                        filteredRooms.add(room);
                    }
                }

                //create recycleview
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView = rootView.findViewById(R.id.recycler);
                        recyclerView .setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
                        recyclerView.setAdapter(new JoinGroupAdapter(ListGroupsFragment.this));

                    }
                });
            }
        });
        return rootView;
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public void onNoteClick(int position) {}

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("Location", location.toString());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        currentLoc = new Location(location);
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}
