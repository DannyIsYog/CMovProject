package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.moshi.Moshi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Login extends AppCompatActivity {

    private EditText edtName;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView btnSignUp;
    private TextView btnNoLogIn;

    private final OkHttpClient client = new OkHttpClient();
    private JSONObject respObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // GO TO CHAT TO DEBUG
        // TODO: CHANGE THIS IF NEEDED (debug chat activity)
        boolean GO_TO_CHAT = true;
        if (GO_TO_CHAT) {
            Intent intent = new Intent(Login.this, ChatActivity.class);
            startActivity(intent);
        }

        edtName = findViewById(R.id.edt_name);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signup);
        btnNoLogIn= findViewById(R.id.btn_no_login);

        //underlines the text
        btnNoLogIn.setPaintFlags(btnNoLogIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtName.getText().toString();
                String pass = edtPassword.getText().toString();
                if(username.isEmpty() || pass.isEmpty()){
                    Toast.makeText(Login.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    RequestBody formBody = new FormBody.Builder()
                            .add("username",username)
                            .add("password", pass)
                            .build();
                    Request request =   new Request.Builder()
                            .url("http://10.0.2.2:5000/user/login")
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
                            try {
                                respObject = new JSONObject(resp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                Log.d("Response", respObject.getString("status"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if(respObject.getString("status").equals("success"))
                                {
                                    // put username and passwd in shared preferences
                                    SharedPreferences sharedPref = getSharedPreferences(AppContext.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("username", username);
                                    editor.putString("password", pass);
                                    editor.apply();
                                    Intent intent = new Intent(Login.this,ChatPage.class);
                                    intent.putExtra("username",username);
                                    intent.putExtra("password",pass);
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
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,SingUp.class);
                startActivity(intent);
            }
        });

        btnNoLogIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,NoLogin.class);
                startActivity(intent);
            }
        });
    }
}