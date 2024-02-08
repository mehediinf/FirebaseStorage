package com.example.firebasestorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button chooseButton,saveButton,displayButton;
    private ImageView imageView;
    private EditText imageNameEditText;
    private ProgressBar progressBar;
    private Uri imageUri;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    StorageTask uploadTask;

    private static final int IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        storageReference = FirebaseStorage.getInstance().getReference("Upload");


        chooseButton = findViewById(R.id.chooseImageButton_Id);
        saveButton = findViewById(R.id.saveImageButton_Id);
        displayButton = findViewById(R.id.displayImageButton_Id);

        imageView = findViewById(R.id.imageView_Id);
        imageNameEditText = findViewById(R.id.imageNameEditText_Id);


        saveButton.setOnClickListener(this);
        displayButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id==R.id.chooseImageButton_Id)
        {
            openFileChooser();

        }
        else if (id==R.id.saveImageButton_Id)
        {
            if (uploadTask!=null && uploadTask.isInProgress())
            {
                Toast.makeText(getApplicationContext(),"Uploading in progress",Toast.LENGTH_SHORT).show();
            }
            else {

                saveData();
            }

        }
        else if (id==R.id.displayImageButton_Id)
        {
            Intent intent = new Intent(MainActivity.this,ImageActivity.class);
            startActivity(intent);

        }



    }


    private void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }


    }

//getting the image Extension
    public String getFileExtension(Uri imageUri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

    }


    private void saveData()
    {
        String imageName = imageNameEditText.getText().toString().trim();

        if (imageName.isEmpty())
        {
            imageNameEditText.setError("Error the image name");
            imageNameEditText.requestFocus();
            return;
        }

        StorageReference ref = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(getApplicationContext(),"Image is stored successfully",Toast.LENGTH_LONG).show();

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();

                Upload upload = new Upload(imageName,downloadUrl.toString());

                String uploadId = databaseReference.push().getKey();
                databaseReference.child(uploadId).setValue(upload);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(),"Image is not stored successfully",Toast.LENGTH_LONG).show();

            }
        });

    }
}
















