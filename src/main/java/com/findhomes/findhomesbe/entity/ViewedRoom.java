package com.findhomes.findhomesbe.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="viewed_rooms_tbl")
public class ViewedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer viewedRoomId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;
}
