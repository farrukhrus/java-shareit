package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemResponseToRequestDto {
    private Long id;
    private Long ownerId;
    private String name;
}