package co.drop.dropper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NextUserActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mAddressField;
    private Spinner mStateSpinnerField;
    private Button nextSUbutton;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String userID;
    private DatabaseReference mCarrierDatabase;
    private String mName;
    private String mPhone;
    private String mSpinner;
    private String mAddress;
    private String mSpinnerV;
    private ImageView userProfile;
    private String mProfileImageUrl;
    private Uri resultUri;
    private Spinner vehicleSpinnerField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_user);

        userProfile = findViewById(R.id.userProfile);

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        vehicleSpinnerField = findViewById(R.id.vehicleSpinner);

        mStateSpinnerField = findViewById(R.id.stateSpinner);

        mAddressField = findViewById(R.id.nextSUaddress);

        mNameField = findViewById(R.id.nextSUfname);

        mPhoneField = findViewById(R.id.nextSUPhone);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        nextSUbutton = findViewById(R.id.nextSUbutton);

        userID = firebaseAuth.getInstance().getCurrentUser().getUid();

        mCarrierDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userID);

        getUserInfo();

        nextSUbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserInfotoDB();

                userProfileRequest();

            }
        });
    }

    private void userProfileRequest(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mNameField.getText().toString())
                .setPhotoUri(resultUri)
                .build();

    }

    private void getUserInfo() {

        mCarrierDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("Name") != null) {

                        mName = map.get("Name").toString();
                        mNameField.setText(mName);
                    }

                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(userProfile);
                    }

                    if (map.get("Phone") != null) {

                        mPhone = map.get("Phone").toString();
                        mPhoneField.setText(mPhone);
                    }

                    if (map.get("Address") != null) {

                        mAddress = map.get("Address").toString();
                        mAddressField.setText(mAddress);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInfotoDB() {

        mName = mNameField.getText().toString();

        mPhone = mPhoneField.getText().toString();

        mSpinner = mStateSpinnerField.getSelectedItem().toString();

        mAddress = mAddressField.getText().toString();

        mSpinnerV = vehicleSpinnerField.getSelectedItem().toString();

        if (TextUtils.isEmpty(mName)) {
            Toast.makeText(getApplicationContext(), "Please enter your Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mPhone)) {
            Toast.makeText(getApplicationContext(), "Please enter your Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mPhone.length()<10){
            Toast.makeText(getApplicationContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mAddress)) {
            Toast.makeText(getApplicationContext(), "Please enter your Address", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving Information");
        progressDialog.show();

        Map userInfo = new HashMap();

        userInfo.put("Name", mName);
        userInfo.put("Phone", mPhone);
        userInfo.put("State", mSpinner);
        userInfo.put("Vehicle", mSpinnerV);
        userInfo.put("Address", mAddress);

        mCarrierDatabase.updateChildren(userInfo)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), AccountDetailsActivity.class));
                    finish();
                }else{
                    progressDialog.dismiss();
                    finish();
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mCarrierDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            userProfile.setImageURI(resultUri);
        }

    }
}