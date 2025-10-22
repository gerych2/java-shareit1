package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    
    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }
    
    @Override
    public BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + bookerId + " не найден"));
        
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + bookingCreateDto.getItemId() + " не найдена"));
        
        if (item.getOwnerId().equals(bookerId)) {
            throw new IllegalArgumentException("Владелец не может бронировать свою вещь");
        }
        
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        
        if (bookingCreateDto.getStart().isAfter(bookingCreateDto.getEnd()) || 
            bookingCreateDto.getStart().isEqual(bookingCreateDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }
        
        Booking booking = BookingMapper.toBooking(bookingCreateDto, bookerId);
        booking.setItem(item);
        booking.setBooker(booker);
        
        booking = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(booking);
    }
    
    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + bookingId + " не найдено"));
        
        if (!booking.getItem().getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Только владелец вещи может подтверждать бронирование");
        }
        
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже было обработано");
        }
        
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);
        
        return BookingMapper.toResponseDto(booking);
    }
    
    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + bookingId + " не найдено"));
        
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Только автор бронирования или владелец вещи могут просматривать бронирование");
        }
        
        return BookingMapper.toResponseDto(booking);
    }
    
    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        
        switch (state) {
            case ALL:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable));
            case CURRENT:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, pageable));
            case PAST:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable));
            case FUTURE:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable));
            case WAITING:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable));
            case REJECTED:
                return BookingMapper.toResponseDto(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable));
            default:
                throw new IllegalArgumentException("Неизвестный статус: " + state);
        }
    }
    
    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        
        switch (state) {
            case ALL:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable));
            case CURRENT:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdAndCurrentOrderByStartDesc(userId, now, pageable));
            case PAST:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdAndPastOrderByStartDesc(userId, now, pageable));
            case FUTURE:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdAndFutureOrderByStartDesc(userId, now, pageable));
            case WAITING:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable));
            case REJECTED:
                return BookingMapper.toResponseDto(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable));
            default:
                throw new IllegalArgumentException("Неизвестный статус: " + state);
        }
    }
}
