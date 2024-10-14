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
@Table(name="favorite_houses_tbl")
public class FavoriteHouse {
    public FavoriteHouse(User user, House house) {
        this.user = user;
        this.house = house;
        this.createdAt = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer favoriteHouseId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;

    private LocalDateTime createdAt;
}
