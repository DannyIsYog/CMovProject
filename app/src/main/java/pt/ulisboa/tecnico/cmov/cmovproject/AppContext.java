package pt.ulisboa.tecnico.cmov.cmovproject;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntry;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntryID;

import android.app.Application;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

// has a LRU cache with messages, abstracts the access to cache and download from server
public class AppContext extends Application {
    public static final String SERVER_ADDR = "http://10.0.2.2:5000";
    public static final String SHARED_PREFERENCES = "shared_preferences_cmu";
    private LruCache<ChatEntryID, ChatEntry> chatEntryLruCache;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AppContext", "create()");
        int CACHE_SIZE = 4 * 1024 * 1024; // 4MiB

        this.chatEntryLruCache = new LruCache<ChatEntryID, ChatEntry>(CACHE_SIZE) {
            protected int sizeOf(ChatEntryID key, ChatEntry value) {
                return value.getByteCount();
            }
        };
    }

    @Override
    // Receiving the request, we'll clean the cache
    public void onLowMemory() {
        super.onLowMemory();
        chatEntryLruCache.evictAll();
    }

    public ChatEntry getChatEntry(ChatEntryID key) {
        ChatEntry res = chatEntryLruCache.get(key);

        synchronized (chatEntryLruCache) {
            if (chatEntryLruCache.get(key) == null) {

                // value not in cache, download it
                try {
                    res = downloadChatEntry(key);
                } catch (IOException de) {
                    // had problem downloading chat, show empty for now
                    return ChatEntry.getEmptyEntry();
                }
                // store new value in cache
                chatEntryLruCache.put(key, res);
            }
        }
        return res;
    }

    // TODO: SEND REQUEST TO SERVER
    private ChatEntry downloadChatEntry(@NonNull ChatEntryID key)
        throws IOException {

        final OkHttpClient client = new OkHttpClient();
        final JSONObject[] respObject = new JSONObject[1];

        RequestBody reqBody = new FormBody.Builder()
                .add("user", "aaa") // TODO: CHANGE USER AND PASS
                .add("password", "aaa")
                .add("room", key.getGroupID())
                .add("msgID", key.getMsgID())

                .build();

        Request req =   new Request.Builder()
                .url(SERVER_ADDR+"/message/get")
                .post(reqBody)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                try {
                    respObject[0] = new JSONObject(resp);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
                try {
                    Log.d("AppContext - Response", respObject[0].getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d("AppContext", e.getMessage());
            }
        });
        try {
            String username = respObject[0].getString("user");
            String message = respObject[0].getString("content");
            return new ChatEntry(username, message);
        } catch (JSONException je) {
            throw new IOException(je);
        }

    }
}