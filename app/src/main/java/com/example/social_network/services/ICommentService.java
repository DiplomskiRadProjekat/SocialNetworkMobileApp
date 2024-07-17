package com.example.social_network.services;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ICommentService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @DELETE(ServiceUtils.comments + "/{id}")
    Call<Void> delete(@Path("id") Long id);

}
