package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    
    BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long bookerId);
    
    BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId);
    
    BookingResponseDto getBookingById(Long bookingId, Long userId);
    
    List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size);
    
    List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state, int from, int size);
}
