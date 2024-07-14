package com.example.social_network.services;

import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @PUT(ServiceUtils.friendRequests + "/responses")
    Call<FriendRequestDTO> respondToPendingRequest(
            @Query("fromUserId") Long fromUserId,
            @Query("toUserId") Long toUserId,
            @Query("status") FriendRequestStatus status
    );

}