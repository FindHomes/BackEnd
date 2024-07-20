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
@Table(name="saved_rooms_tbl")
public class SavedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer savedroomId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;
}
