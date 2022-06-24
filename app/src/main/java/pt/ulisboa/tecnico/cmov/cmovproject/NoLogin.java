package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class NoLogin extends AppCompatActivity {

    private Button btnContinue;
    private EditText edtName;

    private final OkHttpClient client = new OkHttpClient();

    JSONObject respObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_login);

        btnContinue= findViewById(R.id.btn_continue);
        edtName= findViewById(R.id.edt_name);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                {
                    String username = edtName.getText().toString();
                    if(username.isEmpty()){
                        Toast.makeText(NoLogin.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        RequestBody formBody = new FormBody.Builder()
                                .add("username",username)
                                .add("password", "")
                                .build();
                        Request request =   new Request.Builder()
                                .url("http://10.0.2.2:5000/user/create")
                                .post(formBody)
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
                                        Intent intent = new Intent(NoLogin.this,ChatPage.class);
                                        intent.putExtra("username",username);
                                        intent.putExtra("password","");
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Toast.makeText(getApplicationContext(), respObject.getString("message"), Toast.LENGTH_SHORT).show();
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

            }


        });
    }
}