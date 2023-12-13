package to.charlie.foodPlanner.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.dal.repository.TodoPagingRepository;
import to.charlie.foodPlanner.domain.dal.repository.TodoRepository;
import to.charlie.foodPlanner.domain.model.dto.CountDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList.ShoppingListItemDeletedDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.model.converter.ShoppingListItemEntityMapper;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;
import to.charlie.foodPlanner.errorhandler.BadRequestException;
import to.charlie.foodPlanner.errorhandler.InvalidPageException;
import to.charlie.foodPlanner.errorhandler.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {

  private final TodoRepository todoRepository;

  private final TodoPagingRepository todoPagingRepository;

  private final WebSocketService webSocketService;

  public ShoppingListItemDto create(final ShoppingListItemCreateDto todoCreateDto) {
    ShoppingListItemEntity shoppingListItem =
            ShoppingListItemEntity.builder().title(todoCreateDto.getTitle()).build();
    shoppingListItem = todoRepository.save(shoppingListItem);

    final ShoppingListItemDto shoppingListItemDto = ShoppingListItemEntityMapper.entityToDto(
            shoppingListItem);
    webSocketService.sendMessageToAllClients(
            WebSocketMessageDto.builder().data(shoppingListItemDto)
                    .messageType(SHOPPING_LIST_ITEM_CREATED).build());
    return shoppingListItemDto;
  }

  public ShoppingListItemEntity readById(final UUID id, final String username) {
    final Optional<ShoppingListItemEntity> shoppingListItem = todoRepository.findById(id);
    if (shoppingListItem.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }
    return shoppingListItem.get();
  }

  public List<ShoppingListItemEntity> readAll(final String username) {
    return todoRepository.findAll();
  }

  public Page<ShoppingListItemEntity> readAllPageable(final int pageNumber, final int pageSize) {

    final Pageable pageable =
            PageRequest.of(
                    pageNumber,
                    pageSize,
                    Sort.by("completed").ascending().and(Sort.by("createdAtTime").descending()));
    return todoPagingRepository.findAll(pageable);
  }

  public List<ShoppingListItemEntity> readAllByIsCompleted(final String username,
                                                           final String isCompleted) {
    final boolean _isCompleted = isCompletedStringToBoolean(isCompleted);
    return todoRepository.findAllByCompleted(_isCompleted);
  }

  public Page<ShoppingListItemEntity> readAllByIsCompletedPageable(
          final Boolean isCompleted, final int pageNumber, final int pageSize) {

    final Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return todoPagingRepository.findAllByCompleted(isCompleted, pageable);
  }

  public void deleteById(final UUID id) {
    final Optional<ShoppingListItemEntity> shoppingListItem = todoRepository.findById(id);
    if (shoppingListItem.isEmpty()) {
      log.error("Shopping List Item not found");
      throw new ResourceNotFoundException("Item not found");
    }
    todoRepository.deleteById(id);

    webSocketService.sendMessageToAllClients(
            WebSocketMessageDto.builder().data(ShoppingListItemDeletedDto.builder().id(id).build())
                    .messageType(SHOPPING_LIST_ITEM_DELETED).build());
  }

  public ShoppingListItemDto updateById(final UUID id,
                                        final ShoppingListItemUpdateDto shoppingListItemUpdateDto) {
    final Optional<ShoppingListItemEntity> optionalShoppingListItem = todoRepository.findById(id);
    if (optionalShoppingListItem.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }

    final ShoppingListItemEntity shoppingListItem = optionalShoppingListItem.get();

    if (shoppingListItemUpdateDto.getTitle() != null
            && shoppingListItemUpdateDto.getTitle().length() > 0) {
      shoppingListItem.setTitle(shoppingListItemUpdateDto.getTitle());
    } else if (shoppingListItemUpdateDto.getComplete() != null) {
      shoppingListItem.setCompleted(shoppingListItemUpdateDto.getComplete());
    } else {
      throw new BadRequestException("Invalid request");
    }

    final ShoppingListItemDto shoppingListItemDto = ShoppingListItemEntityMapper.entityToDto(todoRepository.save(shoppingListItem));

    webSocketService.sendMessageToAllClients(
            WebSocketMessageDto.builder().data(shoppingListItemDto)
                    .messageType(SHOPPING_LIST_ITEM_UPDATED).build());
    return shoppingListItemDto;
  }

  public ShoppingListItemEntity markCompleteById(final UUID id) {
    final Optional<ShoppingListItemEntity> shoppingListItemOptional = todoRepository.findById(id);
    if (shoppingListItemOptional.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }

    final ShoppingListItemEntity shoppingListItem = shoppingListItemOptional.get();

    shoppingListItem.setCompleted(!shoppingListItem.isCompleted());
    return todoRepository.save(shoppingListItem);
  }

  public CountDto countAll() {
    return new CountDto(todoRepository.count());
  }

  public CountDto countAllByIsCompleted(final String username, final String isCompletedString) {
    final boolean isCompleted = isCompletedStringToBoolean(isCompletedString);
    return new CountDto(todoRepository.countByCompleted(isCompleted));
  }

  private boolean isCompletedStringToBoolean(final String isCompleted) {
    try {
      return Boolean.parseBoolean(isCompleted);
    } catch (final Exception e) {
      throw new BadRequestException("Invalid isCompleted");
    }
  }

  private int pageNumberStringToInteger(final String pageNumber) {
    final int _pageNumber;

    try {
      _pageNumber = Integer.parseInt(pageNumber);
    } catch (final Exception e) {
      throw new InvalidPageException("Invalid Page Number");
    }

    if (_pageNumber < 0) {
      throw new InvalidPageException("Invalid page number");
    }

    return _pageNumber;
  }

  public int calculatePageSize() {
    final long amountCompleted = todoRepository.countByCompletedTrue();

    return Math.max((int) (amountCompleted + 10), 100);
  }
}
