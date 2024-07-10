package com.example.social_network.services;

import com.example.social_network.dtos.UserDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface IUserService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @GET(ServiceUtils.user + "/{id}")
    Call<UserDTO> get(@Path("id") Long id);

}
