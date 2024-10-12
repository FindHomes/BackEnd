package com.findhomes.findhomesbe.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users_tbl")
public class User {
    @Id
    private String userId;
    @Column(unique = true, nullable = false)
    private String kakaoId;  // 카카오 고유 ID
    private String userNickname;
    private String loginApi;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String kakaoId, String userNickname, String loginApi, String status, LocalDateTime createdAt) {
        this.userId = UUID.randomUUID().toString();  // UUID를 사용하여 고유 ID 생성
        this.kakaoId = kakaoId;
        this.userNickname = userNickname;
        this.loginApi = loginApi;
        this.status = status;
        this.createdAt = createdAt;
    }

    //
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarkList;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Condition> conditionList;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SavedRoom> savedRoomList;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RecentlyViewedHouse> viewedRoomList = new ArrayList<>();
}
