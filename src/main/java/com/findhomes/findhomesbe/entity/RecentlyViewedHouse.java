package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "recently_viewed_houses_tbl", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "house_id"}) // 유저-매물 중복 방지
})
public class RecentlyViewedHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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