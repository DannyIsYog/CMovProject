package pt.ulisboa.tecnico.cmov.cmovproject;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntry;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntryID;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.nio.file.NoSuchFileException;
import java.util.concurrent.CountDownLatch;

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

    // gets entry from cache, if not in cache then downloads it
    public ChatEntry getChatEntry(ChatEntryID key) {
        ChatEntry res;
        boolean isCached;

        // lock for read
        synchronized (chatEntryLruCache) {
            res = chatEntryLruCache.get(key);
            isCached = !(chatEntryLruCache.get(key) == null);
        }

        if (!isCached) {

            // value not in cache, download it
            try {
                res = downloadChatEntry(key);
                synchronized (chatEntryLruCache) {
                    chatEntryLruCache.put(key, res);
                }
            } catch (IOException de) {
                // had problem downloading chat, show empty for now
                return ChatEntry.getEmptyEntry();
            }
        }

        return res;
    }


    private ChatEntry downloadChatEntry(@NonNull ChatEntryID key)
        throws IOException, NoSuchFileException {
        final Object hasCompletedLock = new Object();

        SharedPreferences sharedPref = getSharedPreferences(AppContext.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "MR. NOBODY");
        String passwd = sharedPref.getString("password", "I HAVE NO PASSWORD");

        final OkHttpClient client = new OkHttpClient();
        final JSONObject[] respObject = new JSONObject[1];


        RequestBody reqBody = new FormBody.Builder()
                .add("username", username) // TODO: CHANGE USER AND PASS
                .add("password", passwd)
                .add("chatroom", key.getGroupID())
                .add("msgID", key.getMsgID())

                .build();

        Request req =   new Request.Builder()
                .url(SERVER_ADDR+"/message/get")
                .post(reqBody)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String resp;
                try {
                    resp = response.body().string();
                } catch (IOException ioe) {
                    Log.e("AppContext", "error on getting msg with ID "+key.getMsgID());
                    Log.e("AppContext", "download entry: response json is bad");
                    Log.e("AppContext", "json received: "+response.body());
                    countDownLatch.countDown();
                    return;
                }
                try {
                    respObject[0] = new JSONObject(resp);
                    Log.d("AppContext - Response", respObject[0].getString("status"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppContext", "will return IOException because json received was bad");
                    Log.d("AppContext", "json that triggered the exception: "+respObject[0]);
                    Log.d("AppContext", "exception msg: "+e.getLocalizedMessage());
                    //throw new IOException(e.getLocalizedMessage());
                } finally {
                    countDownLatch.countDown();
                }

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d("AppContext", "Failed downloading! Msg: "+e.getMessage());
                countDownLatch.countDown();
            }
        });

        // wait for server response
        try {
            countDownLatch.await();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            Log.d("AppContext", "Thread interrupted: "+ie.getLocalizedMessage());
        }
        try {
            String usernameFromMsg = respObject[0].getString("username");
            String message = respObject[0].getString("message");
            return new ChatEntry(usernameFromMsg, message);
        } catch (JSONException | NullPointerException e) {
            Log.d("AppContext", "conclusion download Exception: "+e.getLocalizedMessage());
            Log.d("AppContext", "End of download, received json: "+respObject[0]);
            throw new IOException(e);
        }

    }
}