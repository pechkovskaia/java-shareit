package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemShortDto {
    private Long id;
    private String name;
    private Long ownerId;
}
