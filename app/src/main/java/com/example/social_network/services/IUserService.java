package com.example.social_network.services;

import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;
import com.example.social_network.dtos.NewPostDTO;
import com.example.social_network.dtos.PasswordChangeDTO;
import com.example.social_network.dtos.PostDTO;
import com.example.social_network.dtos.UpdateUserDTO;
import com.example.social_network.dtos.UserDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @DELETE(ServiceUtils.user + "/{id}")
    Call<Void> delete(@Path("id") Long id);

    @GET(ServiceUtils.user + "/{id}/friends")
    Call<List<UserDTO>> getAllFriends(@Path("id") Long id);

    @GET(ServiceUtils.user + "/{id}/friend-requests")
    Call<List<FriendRequestDTO>> getAllFriendRequests(@Path("id") Long id, @Query("status") FriendRequestStatus status);

    @GET(ServiceUtils.user + "/{id}/friends-posts")
    Call<List<PostDTO>> getAllFriendsPosts(@Path("id") Long id);

    @GET(ServiceUtils.user + "/{id}/posts")
    Call<List<PostDTO>> getAllPosts(@Path("id") Long id);

    @Multipart
    @POST(ServiceUtils.user + "/{id}/posts")
    Call<PostDTO> createPost(
            @Path("id") Long id,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );

    @GET(ServiceUtils.user)
    Call<List<UserDTO>> searchUsers(@Query("search") String search);

    @PUT(ServiceUtils.user + "/{id}/uid")
    Call<Void> setUid(@Path("id") Long id, @Query("uid") String uid);

}
