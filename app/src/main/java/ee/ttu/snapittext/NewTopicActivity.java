package ee.ttu.snapittext;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class NewTopicActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private final String MY_SHARED_PREFS = "SnapItTextPrefs";
    Context context;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private Button chooseTopicPicture, createNewTopic;
    private EditText newTopic;
    private ProgressBar progressBarNewTopic;
    private ImageView newTopicPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_new_topic);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.findViewById(R.id.newTopicView).performClick();

        context = this;
        SharedPreferences prefs = context.getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE);

        chooseTopicPicture = (Button) findViewById(R.id.chooseTopicPicture);
        createNewTopic = (Button) findViewById(R.id.createNewTopic);
        newTopic = (EditText) findViewById(R.id.newTopic);
        progressBarNewTopic = (ProgressBar) findViewById(R.id.progressBarNewTopic);
        newTopicPicture = (ImageView) findViewById(R.id.newTopicPicture);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseStorage storage = FirebaseStorage.getInstance();

        chooseTopicPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);

            }
        });

        createNewTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarNewTopic.setVisibility(View.VISIBLE);

                String topicName = newTopic.getText().toString().trim();

                if (user != null && !topicName.equals("")) {
                    if (topicName.length() < 3 || topicName.length() > 20) {
                        newTopic.setError("Topic must be between 3 to 20 characters!");
                    } else {

                        String username = "";
                        if (user.getDisplayName() == null && user.getEmail() != null) {
                            username = user.getEmail().substring(0, user.getEmail().indexOf("@"));

                        } else {
                            username = user.getDisplayName();
                        }

                        StorageReference storageReference = storage.getReference().child(topicName);

                        newTopicPicture.setDrawingCacheEnabled(true);
                        newTopicPicture.buildDrawingCache();
                        Bitmap bitmap = newTopicPicture.getDrawingCache();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            }
                        });

                        FirebaseDatabase.getInstance()
                                .getReference("topic")
                                .child(topicName)
                                .setValue(new Topic(topicName,
                                        username));
                        newTopic.setText("");
                        Toast.makeText(NewTopicActivity.this, "Topic created! It is active for one hour", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(NewTopicActivity.this, HomeActivity.class));
                        finish();
                    }
                } else {
                    newTopic.setError("Enter topic name!");
                }
                progressBarNewTopic.setVisibility(View.GONE);
            }
        });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE).edit();
                if (item.getItemId() == R.id.homeView) {
                    editor.putString("lastOpenActivity", "HomeActivity");
                    editor.commit();
                    startActivity(new Intent(NewTopicActivity.this, HomeActivity.class));
                    finish();
                } else if (item.getItemId() == R.id.newTopicView) {
                    return true;
                } else if (item.getItemId() == R.id.settingsView) {
                    editor.putString("lastOpenActivity", "SettingsActivity");
                    editor.commit();
                    startActivity(new Intent(NewTopicActivity.this, SettingsActivity.class));
                    finish();
                }
                return true;
            }
        });
    }

    public void chooseImage(View view) {
        final String[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (items[which]) {
                    case "Take Photo":
                        if (ContextCompat.checkSelfPermission(NewTopicActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NewTopicActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            dialog.dismiss();
                        } else {
                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
                        }
                        break;
                    case "Choose from Library":
                        if (ContextCompat.checkSelfPermission(NewTopicActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(NewTopicActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE_PERMISSION);
                            dialog.dismiss();
                        } else {
                            openPhotoSelect();
                        }
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openPhotoSelect();
            }
        }
    }

    private void openPhotoSelect() {
        Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        choosePhotoIntent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(choosePhotoIntent, "Select file"),
                SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                newTopicPicture.setImageBitmap(image);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String imagePath = getRealPathFromUri(selectedImageUri);
                Bitmap image = BitmapFactory.decodeFile(imagePath);
                newTopicPicture.setImageBitmap(image);
            }
        }
    }

    private String getRealPathFromUri(Uri contentUri) {
        String selectedImagePath = null;
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            selectedImagePath = cursor.getString(column_index);
        }
        cursor.close();
        return selectedImagePath;
    }
}
