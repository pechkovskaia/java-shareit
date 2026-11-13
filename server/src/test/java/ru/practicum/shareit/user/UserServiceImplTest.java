package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void createAndFindById() {
        UserDto created = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());

        UserDto found = userService.findById(created.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Даша");
        assertThat(found.getEmail()).isEqualTo("dasha@mail.ru");
    }

    @Test
    void createWithDuplicateEmailThrows() {
        userService.create(UserDto.builder().name("Даша").email("dasha@mail.ru").build());

        assertThrows(DuplicatedEmailException.class, () -> userService.create(
                UserDto.builder().name("Маша").email("dasha@mail.ru").build()));
    }

    @Test
    void updateOnlyName() {
        UserDto created = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());

        UserDto onlyName = UserDto.builder().name("Вера").build();
        UserDto updated = userService.update(created.getId(), onlyName);

        assertThat(updated.getEmail()).isEqualTo("dasha@mail.ru");
        assertThat(updated.getName()).isEqualTo("Вера");
    }

    @Test
    void updateOnlyEmail() {
        UserDto created = userService.create(
                UserDto.builder().name("Даша").email("dasha@mail.ru").build());

        UserDto onlyEmail = UserDto.builder().email("new@mail.ru").build();
        UserDto updated = userService.update(created.getId(), onlyEmail);

        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
        assertThat(updated.getName()).isEqualTo("Даша");
    }

    @Test
    void updateNotExistingUserThrows() {
        UserDto newName = UserDto.builder().name("Маша").build();

        assertThrows(NotFoundException.class, () -> userService.update(
                777L, newName));
    }


    @Test
    void updateDuplicatedEmailThrows() {
        userService.create(UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        UserDto masha = userService.create(
                UserDto.builder().name("Маша").email("masha@mail.ru").build());

        UserDto dashasEmail = UserDto.builder().email("dasha@mail.ru").build();

        assertThrows(DuplicatedEmailException.class,
                () -> userService.update(masha.getId(), dashasEmail));
    }

    @Test
    void findByIdThrows() {
        assertThrows(NotFoundException.class,
                () -> userService.findById(1111L));
    }


    @Test
    void findAllReturnsAllUsers() {
        userService.create(UserDto.builder().name("Даша").email("dasha@mail.ru").build());
        userService.create(UserDto.builder().name("Маша").email("masha@mail.ru").build());

        List<UserDto> all = userService.findAll();
        assertThat(all).hasSize(2);
    }


    @Test
    void deleteRemovesUser() {
        UserDto dasha = userService.create(UserDto.builder()
                .name("Даша").email("dasha@mail.ru").build());

        userService.delete(dasha.getId());

        assertThrows(NotFoundException.class,
                () -> userService.findById(dasha.getId()));
    }

}