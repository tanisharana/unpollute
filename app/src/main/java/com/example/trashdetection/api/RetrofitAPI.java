package com.example.trashdetection.api;

import com.example.trashdetection.model.UploadResponse;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RetrofitAPI {

    //     .addHeader("Content-Type", "image/jpeg")
//                .addHeader("Authorization", "Bearer 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")
    @POST("Salesforce/blip-image-captioning-large")
    Call<ArrayList<UploadResponse>> uploadBinaryFile(
            @Header("Authorization") String accessToken,
            @Header("Content-Type") String contentType,
            @Body RequestBody body);

}
