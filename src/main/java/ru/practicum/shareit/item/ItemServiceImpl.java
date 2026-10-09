package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        checkUserExists(ownerId);
        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        checkUserExists(ownerId);
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = getItemOrThrow(itemId);
        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper :: toCommentDto)
                .toList();
        return ItemMapper.toItemDto(item, comments);
    }

    @Override
    public List<ItemDto> findAllByOwner(Long ownerId) {
        checkUserExists(ownerId);
        LocalDateTime now = LocalDateTime.now();
        List<ItemDto> result = new ArrayList<>();

        for (Item item : itemRepository.findAllByOwnerId(ownerId)) {
            List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                    .map(CommentMapper::toCommentDto)
                    .toList();
            ItemDto dto = ItemMapper.toItemDto(item, comments);
            dto.setLastBooking(bookingRepository
                    .findFirstByItemIdAndStatusAndBookingStartTimeBeforeOrderByBookingStartTimeDesc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null));
            dto.setNextBooking(bookingRepository.findFirstByItemIdAndStatusAndBookingStartTimeAfterOrderByBookingStartTimeAsc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        boolean hasRented = bookingRepository.existsByBookerIdAndItemIdAndStatusAndBookingFinishTimeBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasRented) {
            throw new ValidationException("Отзыв может оставить только пользователь, бравший вещь в аренду");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private void checkUserExists(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}