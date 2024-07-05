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
    private String userNickname;
    private String userPassword;
    private String loginApi;
    private String userEmail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
