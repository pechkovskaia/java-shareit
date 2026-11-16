package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    @Test
    void createShouldReturnCreatedRequest() {
        UserDto dasha = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());

        ItemRequestDto created = itemRequestService.create(dasha.getId(),
                ItemRequestDto.builder().description("Нужна палатка").build());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна палатка");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        ItemRequestDto request = ItemRequestDto.builder().description("Нужна палатка").build();
        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(999L, request));
    }

    @Test
    void findOwnShouldReturnUserRequests() {
        UserDto dasha = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        itemRequestService.create(dasha.getId(),
                ItemRequestDto.builder().description("Нужна палатка").build());
        itemRequestService.create(dasha.getId(),
                ItemRequestDto.builder().description("Нужен велосипед").build());

        List<ItemRequestDto> result = itemRequestService.findOwn(dasha.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void findOwnShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.findOwn(999L));

    }

    @Test
    void findAllOthersShouldReturnOtherUsersRequests() {
        UserDto dasha = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        UserDto masha = userService.create(
                UserDto.builder().name("Маша").email("masha@mail.ru").build());
        itemRequestService.create(dasha.getId(),
                ItemRequestDto.builder().description("Нужна гитара").build());

        List<ItemRequestDto> result = itemRequestService.findAllOthers(masha.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Нужна гитара");
    }

    @Test
    void findAllOthersShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.findAllOthers(999L));
    }


    @Test
    void findByIdShouldReturnRequestWithItems() {
        UserDto dasha = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        UserDto masha = userService.create(
                UserDto.builder().name("Маша").email("masha@mail.ru").build());
        ItemRequestDto request = itemRequestService.create(dasha.getId(),
                ItemRequestDto.builder().description("Нужна палатка").build());
        itemService.create(masha.getId(), ItemDto.builder()
                .name("Палатка").description("Двухместная").available(true)
                .requestId(request.getId()).build());

        ItemRequestDto result = itemRequestService.findById(dasha.getId(), request.getId());

        assertThat(result.getDescription()).isEqualTo("Нужна палатка");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(masha.getId());
    }

    @Test
    void findByIdShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(999L, 777L));
    }

    @Test
    void findByIdShouldThrowWhenRequestNotFound() {
        UserDto dasha = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(dasha.getId(), 777L));

    }
}