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
        Item item = ItemMapper.toItem(itemDto, ownerId);
        item.setId(nextId++);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
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
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        
        return ItemMapper.toDto(existingItem);
    }
    
    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена");
        }
        return ItemMapper.toDto(item);
    }
    
    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return ItemMapper.toDto(items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .collect(Collectors.toList()));
    }
    
    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchText = text.toLowerCase();
        return ItemMapper.toDto(items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                               (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList()));
    }
}
