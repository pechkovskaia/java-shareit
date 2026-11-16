package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto dto);

    List<ItemRequestDto> findOwn(Long userId);

    List<ItemRequestDto> findAllOthers(Long userId);

    ItemRequestDto findById(Long userId, Long requestId);

}