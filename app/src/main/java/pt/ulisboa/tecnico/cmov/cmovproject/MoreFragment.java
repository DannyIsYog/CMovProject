package pt.ulisboa.tecnico.cmov.cmovproject;

import static com.squareup.moshi.Types.arrayOf;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.CancellationSignal;
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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MoreFragment extends Fragment implements View.OnClickListener, LocationListener {

    private Button btnNewGroup;
    private Button btnJoinGroup;

    private final OkHttpClient client = new OkHttpClient();

    JSONObject respObject;

    double latitude;
    double longitude;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more, container, false);

        btnNewGroup = rootView.findViewById(R.id.btn_createGroup);
        btnJoinGroup = rootView.findViewById(R.id.btn_joinGroup);


        btnNewGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addGroup();

            }
        });

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new ListGroupsFragment();
                FragmentManager fm = getChildFragmentManager();
                fm.beginTransaction().replace(R.id.frag_more, fragment).commit();

            }
        });


        return rootView;
    }

    public void addGroup() {

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
        final EditText groupRadius = new EditText(context);
        groupRadius.setHint("Radius");
        layout.addView(groupRadius);

        alert.setView(layout);
        ChatPage chatPage = (ChatPage) getActivity();
        LocationManager locationManager = (LocationManager) chatPage.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(chatPage, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(chatPage, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(chatPage, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10000, this);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = groupName.getText().toString();
                String type = groupType.getText().toString();
                String radius = groupRadius.getText().toString();
                Log.d("create group",name);
                Log.d("type",type);

                if(name.isEmpty() || type.isEmpty()){
                    Toast.makeText(getActivity(), "Invalid Input", Toast.LENGTH_SHORT).show();

                    return;
                }
                else{
                    //cria grupo no server
                    RequestBody formBody = new FormBody.Builder()
                            .add("name",name)
                            .add("roomType",type)
                            .add("latitude", String.valueOf(latitude))
                            .add("longitude", String.valueOf(longitude))
                            .add("radius", radius)
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

    @Override
    public void onClick(View view) {


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("Location", location.toString());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
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