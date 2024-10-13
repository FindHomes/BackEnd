package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "recently_viewed_houses_tbl")
@IdClass(RecentlyViewedHouseId.class)
public class RecentlyViewedHouse {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    // 생성자
    public RecentlyViewedHouse() {}

    public RecentlyViewedHouse(User user, House house) {
        this.user = user;
        this.house = house;
        this.viewedAt = LocalDateTime.now();
    }

    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }
}