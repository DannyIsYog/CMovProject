package pt.ulisboa.tecnico.cmov.cmovproject.chat;

import android.util.LruCache;

// has a LRU cache with messages, abstracts the access to cache and download from server
public class ChatCache extends Application{
    private LruCache<ChatEntryID, ChatEntry> chatEntryLruCache;
}
