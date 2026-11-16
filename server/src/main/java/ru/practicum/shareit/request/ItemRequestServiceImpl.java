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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
        return withItems(requests);
    }

    @Override
    public List<ItemRequestDto> findAllOthers(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с таким user id не существует");
        }
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        return withItems(requests);
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

    private List<ItemRequestDto> withItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemShortDto>> itemsByRequest = itemRepository
                .findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemRequestMapper::toItemShortDto, Collectors.toList())));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request, itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }
}



