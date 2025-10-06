package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;
    
    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        validateItem(itemDto);
        
        Item item = ItemMapper.toItem(itemDto, ownerId);
        item.setId(nextId++);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }
    
    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена");
        }
        
        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец может редактировать вещь");
        }
        
        if (itemDto.getName() != null) {
            if (itemDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Название не может быть пустым");
            }
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Описание не может быть пустым");
            }
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        
        return ItemMapper.toItemDto(existingItem);
    }
    
    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }
    
    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                               (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
    
    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available обязательно");
        }
    }
}
