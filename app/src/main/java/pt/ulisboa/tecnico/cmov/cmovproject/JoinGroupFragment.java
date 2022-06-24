package pt.ulisboa.tecnico.cmov.cmovproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class JoinGroupFragment extends Fragment {

    private Button btnJoinPrivate;
    private Button btnJoinPubGeo;

    private final OkHttpClient client = new OkHttpClient();

    JSONObject respObject;

    double latitude;
    double longitude;
    

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_joingroup, container, false);

        btnJoinPrivate = rootView.findViewById(R.id.btn_joinPrivateGroup);
        btnJoinPubGeo = rootView.findViewById(R.id.btn_joinOtherTypesOfGroups);

        btnJoinPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        btnJoinPubGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        return rootView;
    }





}
