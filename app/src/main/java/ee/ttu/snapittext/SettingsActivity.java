package ee.ttu.snapittext;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class SettingsActivity extends AppCompatActivity {

    private final String MY_SHARED_PREFS = "SnapItTextPrefs";
    Context context;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    //DatabaseReference databaseReference = firebaseDatabase.getReference("users");

    private Button changeProfilePic, showChangeEmail, showChangePassword, sendPasswordResetEmail, removeUserButton,
            changeEmail, changePassword, sendEmail, removeUser, signOut;

    private EditText newEmail, repeatNewEmail, newPassword, repeatNewPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private TextView username;
    private ImageView profilePicture;
    ProfilePictureView fbProfilePicture;

    // USER INFO
    String uid;

    final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.findViewById(R.id.settingsView).performClick();

        fbProfilePicture = (ProfilePictureView) findViewById(R.id.fbProfilePicture);
        profilePicture = (ImageView) findViewById(R.id.profilePicture);
        username = (TextView) findViewById(R.id.username);

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        context = this;

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String nameForUser = "";
                if (user == null) {
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                } else {
                    for (UserInfo profile : user.getProviderData()) {
                        if (profile.getDisplayName() == null) {
                            fbProfilePicture.setVisibility(View.GONE);
                            String str = auth.getCurrentUser().getEmail();
                            String firstHalf = str.substring(0, str.indexOf("@"));
                            username.setText(firstHalf);
                            nameForUser = firstHalf;
                        } else {
                            profilePicture.setVisibility(View.GONE);
                            username.setText(profile.getDisplayName());
                            nameForUser = user.getDisplayName();
                        }
                        uid = profile.getUid();
                        fbProfilePicture.setProfileId(uid);

                        final StorageReference storageReference = storage.getReference().child(nameForUser);

                        profilePicture.setDrawingCacheEnabled(true);
                        profilePicture.buildDrawingCache();
                        Bitmap bitmap = profilePicture.getDrawingCache();
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
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL
                            }
                        });

                    }
                }
            }
        };

        changeProfilePic = (Button) findViewById(R.id.changeProfilePicture);
        showChangeEmail = (Button) findViewById(R.id.showChangeEmailButton);
        showChangePassword = (Button) findViewById(R.id.showChangePasswordButton);
        sendPasswordResetEmail = (Button) findViewById(R.id.passwordResetEmailButton);
        removeUserButton = (Button) findViewById(R.id.removeUserButton);
        changeEmail = (Button) findViewById(R.id.changeEmail);
        changePassword = (Button) findViewById(R.id.changePass);
        sendEmail = (Button) findViewById(R.id.send);
        removeUser = (Button) findViewById(R.id.removeUser);
        signOut = (Button) findViewById(R.id.signOut);

        newEmail = (EditText) findViewById(R.id.newEmail);
        repeatNewEmail = (EditText) findViewById(R.id.repeatNewEmail);
        newPassword = (EditText) findViewById(R.id.newPassword);
        repeatNewPassword = (EditText) findViewById(R.id.repeatNewPassword);

        newEmail.setVisibility(View.GONE);
        repeatNewEmail.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        repeatNewPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        removeUser.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);
            }
        });

        showChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEmail.setVisibility(View.VISIBLE);
                repeatNewEmail.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.GONE);
                repeatNewPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                removeUser.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String firstTimeEmail = newEmail.getText().toString().trim();
                String secondTimeEmail = repeatNewEmail.getText().toString().trim();

                if (user != null && !firstTimeEmail.equals("") && !secondTimeEmail.equals("")) {
                    if (firstTimeEmail.equals(secondTimeEmail)) {
                        user.updateEmail(firstTimeEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Email address is updated. Please sign in with new email!", Toast.LENGTH_LONG).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(SettingsActivity.this, "Failed to update email!: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });

                    } else {
                        repeatNewEmail.setError("Emails do not match");
                        progressBar.setVisibility(View.GONE);
                    }
                } else if (secondTimeEmail.equals("") || firstTimeEmail.equals("")) {
                    if (secondTimeEmail.equals("") && firstTimeEmail.equals("")) {
                        repeatNewEmail.setError("Enter email");
                        newEmail.setError("Enter email");
                    } else if (newEmail.getText().toString().trim().equals("")){
                        newEmail.setError("Enter email");
                    } else {
                        repeatNewEmail.setError("Enter email");
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        showChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEmail.setVisibility(View.GONE);
                repeatNewEmail.setVisibility(View.GONE);
                newPassword.setVisibility(View.VISIBLE);
                repeatNewPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
                removeUser.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String firstTimePassword = newPassword.getText().toString().trim();
                String secondTimePassword = repeatNewPassword.getText().toString().trim();
                if (user != null && !secondTimePassword.equals("") && !firstTimePassword.equals("")) {
                    if (firstTimePassword.length() < 6 || secondTimePassword.length() < 6) {
                        if (firstTimePassword.length() < 6) {
                            newPassword.setError("Password too short, enter minimum 6 characters");
                        } else {
                            repeatNewPassword.setError("Password too short, enter minimum 6 characters");
                        }
                        progressBar.setVisibility(View.GONE);
                    } else {
                        if (firstTimePassword.equals(secondTimePassword)) {
                            user.updatePassword(firstTimePassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, "Password is updated. Please sign in with new Password!", Toast.LENGTH_SHORT).show();
                                                signOut();
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(SettingsActivity.this, "Failed to update newPassword!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        } else {
                            repeatNewPassword.setError("Passwords do not match");
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                } else if (secondTimePassword.equals("") || firstTimePassword.equals("")) {
                    if (secondTimePassword.equals("") && firstTimePassword.equals("")) {
                        repeatNewPassword.setError("Enter password");
                        newPassword.setError("Enter password");
                    } else if (firstTimePassword.equals("")) {
                        newPassword.setError("Enter password");
                    } else {
                        repeatNewPassword.setError("Enter newPassword");
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        sendPasswordResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEmail.setVisibility(View.VISIBLE);
                repeatNewEmail.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                repeatNewPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                removeUser.setVisibility(View.GONE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = newEmail.getText().toString().trim();
                if (!email.equals("")) {
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Reset New Password email is sent!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Failed to send reset email!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        removeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEmail.setVisibility(View.GONE);
                repeatNewEmail.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                repeatNewPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                removeUser.setVisibility(View.VISIBLE);
            }
        });

        removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        signOut();
                                        Toast.makeText(SettingsActivity.this, "Your profile is deleted!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingsActivity.this, SignupActivity.class));
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Failed to delete your account!: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE).edit();
                if (item.getItemId() == R.id.homeView) {
                    editor.putString("lastOpenActivity", "HomeActivity");
                    editor.commit();
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                    finish();
                } else if (item.getItemId() == R.id.newTopicView) {
                    editor.putString("lastOpenActivity", "NewTopicActivity");
                    editor.commit();
                    startActivity(new Intent(SettingsActivity.this, NewTopicActivity.class));
                    finish();
                } else if (item.getItemId() == R.id.settingsView) {
                    return true;
                }
                return true;
            }
        });

    }

    public void signOut() {
        auth.signOut();
        LoginManager.getInstance().logOut();

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
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
                        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        } else {
                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
                        }
                        break;
                    case "Choose from Library":
                        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                profilePicture.setImageBitmap(image);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String imagePath = getRealPathFromUri(selectedImageUri);
                Bitmap image = BitmapFactory.decodeFile(imagePath);
                profilePicture.setImageBitmap(image);
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
