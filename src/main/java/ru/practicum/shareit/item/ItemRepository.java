package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long id);

    @Query("select i from Item i where i.available is true " +
            "and (upper(i.name) like upper(concat('%',:text,'%')) " +
            "or upper(i.description) like upper(concat('%',:text,'%')))")
    List<Item> searchAvailableItemsByText(String text);
}
