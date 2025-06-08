package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {
    @NotBlank(message =  "Comment can not be blank")
    private String text;
}