package ru.practicum.shareit.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    @NotBlank
    @Size(max = 30)
    private String name;

    @Column(length = 100, unique = true, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String email;
}