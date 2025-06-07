package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequestDto toRequestDto(ItemRequest request);

    ItemRequest toRequest(ItemRequestSaveDto requestSaveDto);

    List<ItemRequestDto> toRequestDto(List<ItemRequest> requests);
}