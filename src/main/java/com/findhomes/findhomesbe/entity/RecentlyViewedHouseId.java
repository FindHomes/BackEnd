package com.findhomes.findhomesbe.entity;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

public class RecentlyViewedHouseId implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentlyViewedHouseId that = (RecentlyViewedHouseId) o;
        return Objects.equals(user, that.user) && Objects.equals(house, that.house);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, house);
    }
}
