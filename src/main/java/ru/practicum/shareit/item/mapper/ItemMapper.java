package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    
    public static ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }
    
    public static Item toItem(ItemDto itemDto, Long ownerId) {
        if (itemDto == null) {
            return null;
        }
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId
        );
    }
    
    public static List<ItemDto> toDto(List<Item> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}
