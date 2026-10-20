package ru.practicum.shareit.booking;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {
    public static Booking toBooking(NewBookingDto dto, Item item, User booker) {
        return Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .bookingStartTime(dto.getStart())
                .bookingFinishTime(dto.getEnd())
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getBookingStartTime())
                .end(booking.getBookingFinishTime())
                .status(booking.getStatus())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .build();
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
            return BookingShortDto.builder()
                    .id(booking.getId())
                    .bookerId(booking.getBooker().getId())
                    .start(booking.getBookingStartTime())
                    .end(booking.getBookingFinishTime())
                    .build();
        }
    }