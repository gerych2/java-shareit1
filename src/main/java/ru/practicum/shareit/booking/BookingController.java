package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    
    private final BookingService bookingService;
    
    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    @PostMapping
    public BookingResponseDto createBooking(@RequestBody BookingCreateDto bookingCreateDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(bookingCreateDto, userId);
    }
    
    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(@PathVariable Long bookingId,
                                                 @RequestParam Boolean approved,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.updateBookingStatus(bookingId, approved, userId);
    }
    
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }
    
    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestParam(defaultValue = "ALL") BookingState state,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getUserBookings(userId, state, from, size);
    }
    
    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") BookingState state,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getOwnerBookings(userId, state, from, size);
    }
}
