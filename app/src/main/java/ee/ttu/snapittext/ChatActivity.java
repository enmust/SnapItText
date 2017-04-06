package ee.ttu.snapittext;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    final private long TIME = 60000;

    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final FirebaseUser currentlyLoggedInUser = FirebaseAuth.getInstance().getCurrentUser();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Bundle bundle = getIntent().getExtras();
        final String topicName = bundle.getString("name");

        TextView chatHead = (TextView) findViewById(R.id.chatHead);
        Button backButton = (Button) findViewById(R.id.backButton);

        if (currentlyLoggedInUser == null) {
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            finish();
        } else {
            chatHead.setText(topicName);

            String username = "";
            if (currentlyLoggedInUser.getDisplayName() == null && currentlyLoggedInUser.getEmail() != null) {
                username = currentlyLoggedInUser.getEmail().substring(0, currentlyLoggedInUser.getEmail().indexOf("@"));
            } else {
               username = currentlyLoggedInUser.getDisplayName();
            }
            displayChatMessages(username);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input);

                String username = "";
                if (currentlyLoggedInUser.getDisplayName() == null && currentlyLoggedInUser.getEmail() != null) {
                    username = currentlyLoggedInUser.getEmail().substring(0, currentlyLoggedInUser.getEmail().indexOf("@"));
                } else {
                    username = currentlyLoggedInUser.getDisplayName();
                }

                if (!input.getText().toString().equals("")) {
                    FirebaseDatabase.getInstance()
                            .getReference("topic/" + topicName + "/chatMessage")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    username));
                    input.setText("");
                } else {
                    input.setError("You should write something!");
                }

            }
        });

    }

    private void displayChatMessages(final String username) {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);

        Bundle bundle = getIntent().getExtras();
        final String topicName = bundle.getString("name");

        adapter = new FirebaseListAdapter<ChatMessage>(this,
                ChatMessage.class,
                R.layout.message,
                FirebaseDatabase.getInstance().getReference("topic/" + topicName + "/chatMessage")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView otherText = (TextView)v.findViewById(R.id.other_text);
                TextView otherUser = (TextView)v.findViewById(R.id.other_user);
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);

                messageText.setVisibility(View.VISIBLE);
                messageUser.setVisibility(View.VISIBLE);
                otherText.setVisibility(View.VISIBLE);
                otherUser.setVisibility(View.VISIBLE);

                if ((new Date().getTime() - model.getMessageTime()) > TIME) {
                    // if time is over
                    messageText.setVisibility(View.GONE);
                    messageUser.setVisibility(View.GONE);
                    otherText.setVisibility(View.GONE);
                    otherUser.setVisibility(View.GONE);
                } else {

                    if (model.getMessageUser().trim().equals(username)) {
                        // my text on the right
                        messageText.setText(model.getMessageText());
                        messageUser.setText(model.getMessageUser());
                        otherText.setVisibility(View.GONE);
                        otherUser.setVisibility(View.GONE);
                    } else {
                        // other's on the left
                        otherText.setText(model.getMessageText());
                        otherUser.setText(model.getMessageUser());
                        messageText.setVisibility(View.GONE);
                        messageUser.setVisibility(View.GONE);
                    }

                }
            }
        };
        listOfMessages.setAdapter(adapter);
    }

}
