package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, CommentRepository commentRepository, 
                          BookingRepository bookingRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        Item item = ItemMapper.toItem(itemDto, ownerId);
        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }
    
    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
        
        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец может редактировать вещь");
        }
        
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        
        existingItem = itemRepository.save(existingItem);
        return ItemMapper.toDto(existingItem);
    }
    
    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
        
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentDtos = ItemMapper.toCommentDto(comments);
        
        ItemWithBookingsDto result = new ItemWithBookingsDto();
        result.setId(item.getId());
        result.setName(item.getName());
        result.setDescription(item.getDescription());
        result.setAvailable(item.getAvailable());
        result.setOwnerId(item.getOwnerId());
        result.setComments(commentDtos);
        
        // Добавляем информацию о бронировании только для владельца
        if (item.getOwnerId().equals(userId)) {
            List<ru.practicum.shareit.booking.Booking> bookings = bookingRepository.findApprovedBookingsByItemId(itemId);
            
            LocalDateTime now = LocalDateTime.now();
            
            // Последнее бронирование
            Optional<ru.practicum.shareit.booking.Booking> lastBooking = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(ru.practicum.shareit.booking.Booking::getEnd));
            
            if (lastBooking.isPresent()) {
                ItemWithBookingsDto.BookingInfo lastBookingInfo = new ItemWithBookingsDto.BookingInfo();
                lastBookingInfo.setId(lastBooking.get().getId());
                lastBookingInfo.setBookerId(lastBooking.get().getBooker().getId());
                lastBookingInfo.setStart(lastBooking.get().getStart());
                lastBookingInfo.setEnd(lastBooking.get().getEnd());
                result.setLastBooking(lastBookingInfo);
            }
            
            // Ближайшее следующее бронирование
            Optional<ru.practicum.shareit.booking.Booking> nextBooking = bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(ru.practicum.shareit.booking.Booking::getStart));
            
            if (nextBooking.isPresent()) {
                ItemWithBookingsDto.BookingInfo nextBookingInfo = new ItemWithBookingsDto.BookingInfo();
                nextBookingInfo.setId(nextBooking.get().getId());
                nextBookingInfo.setBookerId(nextBooking.get().getBooker().getId());
                nextBookingInfo.setStart(nextBooking.get().getStart());
                nextBookingInfo.setEnd(nextBooking.get().getEnd());
                result.setNextBooking(nextBookingInfo);
            }
        }
        
        return result;
    }
    
    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> getItemById(item.getId(), ownerId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchText = text.toLowerCase();
        return ItemMapper.toDto(itemRepository.findAll().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                               (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList()));
    }
    
    @Override
    public CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
        
        // Проверяем, что пользователь действительно брал вещь в аренду
        List<ru.practicum.shareit.booking.Booking> pastBookings = bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(itemId, userId, LocalDateTime.now());
        
        if (pastBookings.isEmpty()) {
            throw new IllegalArgumentException("Пользователь не может оставить комментарий к вещи, которую не брал в аренду");
        }
        
        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        
        comment = commentRepository.save(comment);
        return ItemMapper.toCommentDto(comment);
    }
}
