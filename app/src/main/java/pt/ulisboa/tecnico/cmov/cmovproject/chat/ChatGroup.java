package pt.ulisboa.tecnico.cmov.cmovproject.chat;

import android.util.LruCache;

import java.io.Serializable;
import java.util.ArrayList;

// encapsulates all the info about a Group chat
public class ChatGroup implements Serializable {
    private ArrayList<ChatEntry> entries;
    private LruCache<Integer, ChatEntry> chatEntryLruCache;

    public ChatGroup() {
        // TODO: create cache and load from there
        // TODO: CACHE FOR EVERYTHING SEE APP CONTEXT
        // initialize cache
        int cacheSize = 5 * 1024; // 5Kib
        // TODO: probably change key of LRU to class that encapsulates GroupID + msgID
        chatEntryLruCache = new LruCache<Integer, ChatEntry>(cacheSize) {
            protected int sizeOf(Integer key, ChatEntry value) {
                return value.getByteCount();
            }
        };
        /*
        synchronized (chatEntryLruCache) {
            if (chatEntryLruCache.get(key) == null) {
                chatEntryLruCache.put(key, value);
            }
        } */

        entries = new ArrayList<>();
    }

    public ArrayList<ChatEntry> getEntries() {
        return this.entries;
    }

    public void addEntry(ChatEntry entry) {
        entries.add(entry);
    }
}
