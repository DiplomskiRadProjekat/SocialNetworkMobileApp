package com.example.social_network.dtos;

public class FriendRequestDTO {

    private Long fromUserId;

    private Long toUserId;

    private FriendRequestStatus status;

    public FriendRequestDTO() {
    }

    public FriendRequestDTO(Long fromUserId, Long toUserId, FriendRequestStatus status) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = status;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }
}
