package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="user_chat_tbl")
public class UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String sessionId;
    private String userInput;
    private String gptResponse;

    private LocalDateTime createdAt;

    // Getters and Setters
}
