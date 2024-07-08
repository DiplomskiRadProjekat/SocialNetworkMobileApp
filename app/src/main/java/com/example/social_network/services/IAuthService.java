package com.example.social_network.services;

import com.example.social_network.dtos.AuthenticationRequestDTO;
import com.example.social_network.dtos.AuthenticationResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IAuthService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @POST(ServiceUtils.auth + "/login")
    Call<AuthenticationResponseDTO> login(@Body AuthenticationRequestDTO authenticationRequestDTO);

}
