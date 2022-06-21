package pt.ulisboa.tecnico.cmov.cmovproject;

import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntry;
import pt.ulisboa.tecnico.cmov.cmovproject.chat.ChatEntryID;

import android.app.Application;
import android.util.LruCache;

// has a LRU cache with messages, abstracts the access to cache and download from server
public class AppContext extends Application {
    private LruCache<ChatEntryID, ChatEntry> chatEntryLruCache;

    public AppContext() {
        int CACHE_SIZE = 4 * 1024 * 1024; // 4MiB

        this.chatEntryLruCache = new LruCache<>(CACHE_SIZE) {
            protected int sizeOf(ChatEntryID key, ChatEntry value) {
                return value.getByteCount();
            }
        };

    }

    public ChatEntry getChatEntry(ChatEntryID key) {
        ChatEntry cacheRes = chatEntryLruCache.get(key);

        synchronized (cacheRes) {
            if (chatEntryLruCache.get(key) == null) {

                // value not in cache, download it
                

                // store new value in cache
                chatEntryLruCache.put(key, value);
            }
        }
        return cacheRes;
    }
}