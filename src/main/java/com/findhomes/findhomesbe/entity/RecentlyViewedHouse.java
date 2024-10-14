package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="recently_viewed_houses_tbl")
public class RecentlyViewedHouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recentlyViewedHouseId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;


    LocalDateTime viewAt;

    public RecentlyViewedHouse(User user, House house) {
        this.user = user;
        this.house = house;
        this.viewAt = LocalDateTime.now();
    }

    public void updateViewedAt() {
        this.viewAt = LocalDateTime.now();  // 현재 시각으로 설정
    }
}
