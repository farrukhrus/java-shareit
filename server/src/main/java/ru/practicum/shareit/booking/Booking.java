package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    @NotNull
    @FutureOrPresent
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    @NotNull
    @Future
    private LocalDateTime end;

    @ManyToOne
    @ToString.Exclude
    @NotNull
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @ToString.Exclude
    @NotNull
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @Column(length = 20)
    @Enumerated(value = EnumType.STRING)
    private BookingStatus status;
}