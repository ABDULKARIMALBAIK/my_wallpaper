package com.abdulkarimalbaik.dev.mywallpapers.Remote;

import com.abdulkarimalbaik.dev.mywallpapers.Model.AzureComputerVision.ComputerVision;
import com.abdulkarimalbaik.dev.mywallpapers.Model.AzureComputerVision.URLUpload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IComputerVision {

	//Azure service Detecting +18 photos

    @Headers({
            "Content-Type:application/json",
            "Ocp-Apim-Subscription-Key:..................................."
    })

    @POST
    Call<ComputerVision> analyzeImage(@Url String apiEndpoint , @Body URLUpload url);
}
