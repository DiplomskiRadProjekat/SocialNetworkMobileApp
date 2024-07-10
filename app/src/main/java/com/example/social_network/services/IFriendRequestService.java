package com.example.social_network.services;

import com.example.social_network.dtos.FriendRequestDTO;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IFriendRequestService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @POST(ServiceUtils.friendRequests)
    Call<FriendRequestDTO> create(
            @Query("fromUserId") Long fromUserId,
            @Query("toUserId") Long toUserId
    );

    @DELETE(ServiceUtils.friendRequests)
    Call<Void> delete(
            @Query("fromUserId") Long fromUserId,
            @Query("toUserId") Long toUserId
    );

    @GET(ServiceUtils.friendRequests)
    Call<FriendRequestDTO> get(
            @Query("fromUserId") Long fromUserId,
            @Query("toUserId") Long toUserId
    );

}