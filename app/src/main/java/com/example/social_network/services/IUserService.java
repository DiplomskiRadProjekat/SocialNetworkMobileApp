package com.example.social_network.services;

import com.example.social_network.dtos.PasswordChangeDTO;
import com.example.social_network.dtos.UpdateUserDTO;
import com.example.social_network.dtos.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IUserService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @GET(ServiceUtils.user + "/{id}")
    Call<UserDTO> get(@Path("id") Long id);

    @PUT(ServiceUtils.user + "/{id}/password-change")
    Call<Void> changePassword(@Path("id") Long id, @Body PasswordChangeDTO passwordChangeDTO);

    @PUT(ServiceUtils.user + "/{id}")
    Call<UserDTO> update(@Path("id") Long id, @Body UpdateUserDTO updateUserDTO);

}
