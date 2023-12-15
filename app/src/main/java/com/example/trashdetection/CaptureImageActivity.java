package com.example.trashdetection;

import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.trashdetection.api.APIClient;
import com.example.trashdetection.api.RetrofitAPI;
import com.example.trashdetection.model.CaptionInfo;
import com.example.trashdetection.model.UploadResponse;
import com.example.trashdetection.utils.FileUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;


import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureImageActivity extends AppCompatActivity {

    DatabaseReference reference;
    ProgressBar progressShow;
    AppCompatTextView imageCaption, captureRecord, capturePhoto;
    private double longitude, latitude;
    private int type = 0;
    private String filePath = null;
    private int CAMERA_REQUEST = 1998;
    private int GALLERY_REQUEST = 1887;
    RetrofitAPI apiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        latitude = getIntent().getDoubleExtra("latitude", 0.0);

        apiClient = APIClient.getClient().create(RetrofitAPI.class);


        System.out.println("CI latitude: " + latitude);
        System.out.println("CI longitude: " + longitude);
        initUI();
        AppCompatTextView count = findViewById(R.id.count);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            initCount();
            count.setVisibility(View.VISIBLE);
        } else {
            count.setVisibility(View.GONE);
        }
    }

    private void initCount() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("caption_image");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                System.out.println("Success data change: " + dataSnapshot);
                ArrayList listCaption = new ArrayList();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    CaptionInfo captionInfo = snap.getValue(CaptionInfo.class);

                    if(captionInfo.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        listCaption.add(captionInfo);
                }

                AppCompatTextView count = findViewById(R.id.count);
                count.setVisibility(View.VISIBLE);
                count.setText("Total garbage count:" + listCaption.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error data change: " + error.getMessage());
            }
        });
    }

    private void openDialogBottomSheet() {

        final BottomSheetDialog bottomSheet = new BottomSheetDialog(CaptureImageActivity.this);
        bottomSheet.setContentView(R.layout.modal_bottom_sheet);
        AppCompatTextView camera = bottomSheet.findViewById(R.id.cameraAppCompatTextView);
        AppCompatTextView gallery = bottomSheet.findViewById(R.id.galleryAppCompatTextView);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(camera_intent, CAMERA_REQUEST);
                PictureSelector.create(CaptureImageActivity.this)
                        .openCamera(SelectMimeType.ofImage())
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                type = 1;
                                System.out.println("result.get(0).getPath(): " + result.get(0).getPath());
                                filePath = getRealPathFromURI(Uri.parse(result.get(0).getPath()), CaptureImageActivity.this);
                                System.out.println("File path: " + filePath);
                                AppCompatImageView capture = findViewById(R.id.captureImageAppCompatImageView);
                                capture.setImageURI(Uri.parse(filePath));
                            }

                            @Override
                            public void onCancel() {

                            }
                        });

                bottomSheet.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
                bottomSheet.dismiss();

                PictureSelector.create(CaptureImageActivity.this)
                        .openSystemGallery(SelectMimeType.ofImage())
                        .forSystemResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                System.out.println("Gallery.get(0).getPath(): " + result.get(0).getPath());
                                type = 1;
                                filePath = getRealPathFromURI(Uri.parse(result.get(0).getPath()), CaptureImageActivity.this);
                                System.out.println("File path: " + filePath);
                                AppCompatImageView capture = findViewById(R.id.captureImageAppCompatImageView);
                                capture.setImageURI(Uri.parse(filePath));
                            }

                            @Override
                            public void onCancel() {

                            }
                        });


//                PictureSelector.create(CaptureImageActivity.this)
//                        .openSystemGallery(SelectMimeType.ofImage())
//                        .forSystemResult(new OnResultCallbackListener<LocalMedia>() {
//                            @Override
//                            public void onResult(ArrayList<LocalMedia> result) {
//
//                            }
//
//                            @Override
//                            public void onCancel() {
//
//                            }
//                        });


            }
        });
        bottomSheet.show();
    }
    private void initUI() {
        progressShow = findViewById(R.id.progressShow);
        imageCaption = findViewById(R.id.imageCaptionAppCompatTextView);
        captureRecord = findViewById(R.id.capture);
        capturePhoto = findViewById(R.id.capturePhoto);
        captureRecord.setVisibility(View.GONE);
        capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("type: " + type);

                if(type == 0) {

                    openDialogBottomSheet();

//                    ImagePicker.with(CaptureImageActivity.this)
//                            .start();
                    capturePhoto.setText("Get image caption");

                } else if(type == 1) {
                    progressShow.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            retrofitAPICall(filePath);
//                            try {
//                                apiCall(filePath);
//                            } catch (IOException e) {
//                                System.out.println("IO message : " + e.getMessage());
//                                Toast.makeText(CaptureImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            } catch (JSONException e) {
//                                System.out.println("JSON message : " + e.getMessage());
//                                Toast.makeText(CaptureImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
                        }
                    }, 50);

                }
            }
        });

        captureRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressShow.setVisibility(View.VISIBLE);

                if(imageCaption.getText().toString().contains("trash") || imageCaption.getText().toString().contains("garbage") || imageCaption.getText().toString().contains("junk") || imageCaption.getText().toString().contains("scrape") || imageCaption.getText().toString().contains("rubbish")) {
                    reference = FirebaseDatabase.getInstance().getReference("caption_image").child(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)));
                    Random rand = new Random();
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("longitude", String.valueOf(longitude));
                    hashMap.put("latitude", String.valueOf(latitude));
                    hashMap.put("caption", imageCaption.getText().toString());
                    hashMap.put("density", String.valueOf(rand.nextInt(1000)));
                    hashMap.put("userId", currentFirebaseUser.getUid());

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressShow.setVisibility(View.GONE);

                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(CaptureImageActivity.this, "No Garbage available", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void retrofitAPICall(String filePath) {

        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
//        MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
//        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());


        Call<ArrayList<UploadResponse>> call1 = apiClient.uploadBinaryFile("Bearer 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", "image/jpeg", requestBody);
        call1.enqueue(new Callback<ArrayList<UploadResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<UploadResponse>> call, Response<ArrayList<UploadResponse>> response) {

                ArrayList<UploadResponse> listOfUpload = response.body();

                for(int i=0; i< listOfUpload.size(); i++){

                    if(i == 0) {
                        UploadResponse jj = listOfUpload.get(i);

                        if(jj != null) {
                            String textInfo = jj.getGenerated_text();

                            type = 0;
                            imageCaption.setText(textInfo);
                            progressShow.setVisibility(View.GONE);
                            captureRecord.setVisibility(View.VISIBLE);
                            capturePhoto.setVisibility(View.GONE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<UploadResponse>> call, Throwable t) {
                Toast.makeText(CaptureImageActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {
            type = 1;

            AppCompatImageView capture = findViewById(R.id.captureImageAppCompatImageView);

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            capture.setImageBitmap(photo);
            Uri uri  = data.getData();
            filePath = FileUtils.getRealPath(CaptureImageActivity.this, uri);
            capture.setImageURI(Uri.parse(filePath));
        } else if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST) {
            type = 1;

            Uri uri  = data.getData();
            filePath = getRealPathFromURI(uri, CaptureImageActivity.this);
            AppCompatImageView capture = findViewById(R.id.captureImageAppCompatImageView);
            capture.setImageURI(Uri.parse(filePath));
        }
//        if (resultCode == Activity.RESULT_OK) {
//            type = 1;
//            Uri uri  = data.getData();
////            progressShow.setVisibility(View.VISIBLE);
//            System.out.println("Uri: " + uri.toString());
//            capturePhoto.setText("Get image caption");
//
////            filePath = new FileUtils(this).getPath(uri);
//
//            if(uri.toString().startsWith("content")) {
//                filePath = getRealPathFromURI(uri, CaptureImageActivity.this);
//            } else {
//                filePath = FileUtils.getRealPath(CaptureImageActivity.this, uri);
//            }
//            AppCompatImageView capture = findViewById(R.id.captureImageAppCompatImageView);
//            capture.setImageURI(Uri.parse(filePath));
////            progressShow.setVisibility(View.GONE);
//
//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
//        }
    }

    private void apiCall(String filePath) throws IOException, JSONException {

//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("image/jpeg");
//        RequestBody body = RequestBody.create(mediaType, new File(filePath));
//        Request request = new Request.Builder()
//                .url("https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-large")
//                .method("POST", body)
//                .addHeader("Content-Type", "image/jpeg")
//                .addHeader("Authorization", "Bearer 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")
//                .build();
//        Response response = client.newCall(request).execute();
//
//
//        String res = response.body().string();
//        JSONArray jsonArray = new JSONArray(res);
//
//        if(jsonArray != null) {
//            for(int i=0; i< jsonArray.length(); i++){
//
//                if(i == 0) {
//                    JSONObject jj = jsonArray.optJSONObject(i);
//
//                    if(jj != null) {
//                        String textInfo = jj.getString("generated_text");
//
//                        type = 0;
////                        imageCaption.setText(textInfo);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                imageCaption.setText(textInfo);
//                                progressShow.setVisibility(View.GONE);
//                                captureRecord.setVisibility(View.VISIBLE);
//                                capturePhoto.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//
//                }
//            }
//        }
    }


    private static String getRealPathFromURI(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

}