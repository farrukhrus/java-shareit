package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "start_date")
    LocalDateTime start;

    @Column(name = "end_date")
    LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    Item item;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "booker_id")
    User booker;

    @Column(length = 20)
    @Enumerated(value = EnumType.STRING)
    BookingStatus status;
}