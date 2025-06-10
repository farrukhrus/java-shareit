package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"items", "requester"})
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"items", "requester"})
    List<ItemRequest> findAllByRequesterIdNotInOrderByCreatedDesc(List<Long> userIds);

}