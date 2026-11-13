package ru.practicum.shareit.request;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с таким user id не существует");
        }

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, userId);

        ItemRequest saved = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> findOwn(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с таким user id не существует");
        }
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        List<ItemRequestDto> result = new ArrayList<>();

        for (ItemRequest request : requests) {
            List<Item> items = itemRepository.findAllByRequestId(request.getId());

            List<ItemShortDto> shortItems = items.stream()
                    .map(ItemRequestMapper::toItemShortDto)
                    .toList();

            result.add(ItemRequestMapper.toItemRequestDto(request, shortItems));
        }
        return result;
    }

    @Override
    public List<ItemRequestDto> findAllOthers(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с таким user id не существует");
        }
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        List<ItemRequestDto> result = new ArrayList<>();

        for (ItemRequest request : requests) {
            List<Item> items = itemRepository.findAllByRequestId(request.getId());

            List<ItemShortDto> shortItems = items.stream()
                    .map(ItemRequestMapper::toItemShortDto)
                    .toList();

            result.add(ItemRequestMapper.toItemRequestDto(request, shortItems));
        }
        return result;
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с таким user id не существует");
        }

        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(()
                -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);

        List<ItemShortDto> shortItems = items.stream()
                .map(ItemRequestMapper::toItemShortDto)
                .toList();

        return ItemRequestMapper.toItemRequestDto(request, shortItems);
    }
}



