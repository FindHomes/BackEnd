package com.findhomes.findhomesbe.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users_tbl")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    @Column(unique = true, nullable = false)
    private String kakaoId;  // 카카오 고유 ID
    private String userNickname;
    private String loginApi;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String kakaoId, String userNickname, String loginApi, String status, LocalDateTime createdAt) {
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
    private List<ViewedRoom> viewedRoomList;
}
