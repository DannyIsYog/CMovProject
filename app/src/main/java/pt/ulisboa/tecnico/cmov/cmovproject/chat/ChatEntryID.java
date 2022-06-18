package pt.ulisboa.tecnico.cmov.cmovproject.chat;

public class ChatEntryID {

    private String chatGroupID;
    private Integer chatEntryIdx;

    public ChatEntryID(String chatGroupID, Integer chatEntryIdx) {
        this.chatGroupID = chatGroupID;
        this.chatEntryIdx = chatEntryIdx;
    }

    @Override
    public boolean equals(Object otherObj) {
        if ( otherObj.getClass() != ChatEntryID.class ) {
            return false;
        }
        ChatEntryID otherEntry = (ChatEntryID) otherObj;

        return
                this.chatGroupID.equals(otherEntry.chatGroupID)
                &&
                this.chatEntryIdx.equals(otherEntry.chatEntryIdx);
    }
}
