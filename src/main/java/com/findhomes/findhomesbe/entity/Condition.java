package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.print.Book;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="conditions_tbl")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer conditionId;
    private String roomType;
    private String transactionType;
    private Integer deposit;
    private Integer rent;
    private String detailInput;
    //
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //
    @OneToMany(mappedBy = "condition", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarkList;
}
