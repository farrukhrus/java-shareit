package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemSaveDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}