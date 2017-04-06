package ee.ttu.snapittext;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;


public class HomeActivity extends AppCompatActivity {

    private final String MY_SHARED_PREFS = "SnapItTextPrefs";
    final private long TOPIC_TIME = 3600000;
    final long ONE_MEGABYTE = 1024 * 1024;

    Context context;
    final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.findViewById(R.id.homeView).performClick();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        } else {
            displayTopics();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE).edit();
                if (item.getItemId() == R.id.homeView) {
                    return true;
                } else if (item.getItemId() == R.id.newTopicView) {
                    editor.putString("lastOpenActivity", "NewTopicActivity");
                    editor.apply();
                    startActivity(new Intent(HomeActivity.this, NewTopicActivity.class));
                    finish();
                } else if (item.getItemId() == R.id.settingsView) {
                    editor.putString("lastOpenActivity", "SettingsActivty");
                    editor.apply();
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    finish();
                }
                return true;
            }
        });
    }

    private void displayTopics() {
        FirebaseListAdapter<Topic> topicAdapter;
        ListView listOfTopics = (ListView) findViewById(R.id.topicListView);

        topicAdapter = new FirebaseListAdapter<Topic>(this,
                Topic.class,
                R.layout.topic_list,
                FirebaseDatabase.getInstance().getReference("topic")) {
            @Override
            protected void populateView(View v, Topic model, int position) {
                final ImageView topicImage = (ImageView)v.findViewById(R.id.topicImage);
                TextView topicText = (TextView)v.findViewById(R.id.topicText);

                final StorageReference storageReference = storage.getReference().child(model.getTopicName().trim());

                storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        topicImage.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                topicText.setText(model.getTopicName());

                topicImage.setVisibility(View.VISIBLE);
                topicText.setVisibility(View.VISIBLE);
                if ((new Date().getTime() - model.getTopicTime()) > TOPIC_TIME) {
                    topicImage.setVisibility(View.GONE);
                    topicText.setVisibility(View.GONE);
                }

            }
        };

        listOfTopics.setAdapter(topicAdapter);

        listOfTopics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Topic clickedTopic = (Topic) parent.getItemAtPosition(position);
                String name = clickedTopic.getTopicName();
                Toast.makeText(HomeActivity.this, "Messages last for 60 seconds!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", name);
                context.startActivity(intent);
            }
        });
    }

}
