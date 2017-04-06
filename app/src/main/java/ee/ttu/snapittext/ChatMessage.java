package ee.ttu.snapittext;

import java.util.Date;

public class ChatMessage {

    public String messageUser;
    public String messageText;
    public long messageTime;

    public ChatMessage(String messageText, String messageUser) {
        this.messageUser = messageUser;
        this.messageText = messageText;

        messageTime = new Date().getTime();
    }

    public ChatMessage() {

    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
