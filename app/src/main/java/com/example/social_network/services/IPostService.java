package com.example.social_network.services;

import com.example.social_network.dtos.CommentDTO;
import com.example.social_network.dtos.NewCommentDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IPostService {

    @Headers(
            {"User-Agent: Mobile-Android",
                    "Content-Type:application/json"}
    )

    @GET(ServiceUtils.posts + "/{id}/files")
    Call<ResponseBody> downloadFile(@Path("id") Long id);

    @DELETE(ServiceUtils.posts + "/{id}")
    Call<Void> delete(@Path("id") Long id);

    @POST(ServiceUtils.posts + "/{id}/comments")
    Call<CommentDTO> createComment(@Path("id") Long id, @Body NewCommentDTO newCommentDTO);

    @GET(ServiceUtils.posts + "/{id}/comments")
    Call<List<CommentDTO>> getAllComments(@Path("id") Long id);

}
