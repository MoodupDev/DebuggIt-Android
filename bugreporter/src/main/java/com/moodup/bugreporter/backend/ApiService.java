package com.moodup.bugreporter.backend;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("https://bitbucket.org/site/oauth2/authorize?client_id={key}&response_type=token")
    Call<ResponseBody> getAccessToken(@Path("{key}") String clientId);
}
