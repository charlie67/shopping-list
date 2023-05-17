package to.charlie.foodPlanner.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.CountDto;
import to.charlie.foodPlanner.domain.model.dto.TodoCreateDto;
import to.charlie.foodPlanner.domain.model.dto.TodoUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.dal.repository.TodoPagingRepository;
import to.charlie.foodPlanner.domain.dal.repository.TodoRepository;
import to.charlie.foodPlanner.errorhandler.BadRequestException;
import to.charlie.foodPlanner.errorhandler.InvalidPageException;
import to.charlie.foodPlanner.errorhandler.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class TodoService {

  private final TodoRepository todoRepository;

  private final TodoPagingRepository todoPagingRepository;

  public ShoppingListItemEntity create(TodoCreateDto todoCreateDto) {
    ShoppingListItemEntity shoppingListItem =
        ShoppingListItemEntity.builder().title(todoCreateDto.getTitle()).build();
    return todoRepository.save(shoppingListItem);
  }

  public ShoppingListItemEntity readById(UUID id, String username) {
    Optional<ShoppingListItemEntity> shoppingListItem = todoRepository.findById(id);
    if (shoppingListItem.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }
    return shoppingListItem.get();
  }

  public List<ShoppingListItemEntity> readAll(String username) {
    return todoRepository.findAll();
  }

  public Page<ShoppingListItemEntity> readAllPageable(String pageNumber, int pageSize) {
    int _pageNumber = pageNumberStringToInteger(pageNumber);

    Pageable pageable =
        PageRequest.of(
            _pageNumber,
            pageSize,
            Sort.by("completed").ascending().and(Sort.by("updatedAtTime").descending()));
    return todoPagingRepository.findAll(pageable);
  }

  public List<ShoppingListItemEntity> readAllByIsCompleted(String username, String isCompleted) {
    boolean _isCompleted = isCompletedStringToBoolean(isCompleted);
    return todoRepository.findAllByCompleted(_isCompleted);
  }

  public Page<ShoppingListItemEntity> readAllByIsCompletedPageable(
      String isCompleted, String pageNumber, int pageSize) {
    boolean _isCompleted = isCompletedStringToBoolean(isCompleted);
    int _pageNumber = pageNumberStringToInteger(pageNumber);

    Pageable pageable = PageRequest.of(_pageNumber, pageSize);
    return todoPagingRepository.findAllByCompleted(_isCompleted, pageable);
  }

  public void deleteById(UUID id) {
    Optional<ShoppingListItemEntity> shoppingListItem = todoRepository.findById(id);
    if (shoppingListItem.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }
    todoRepository.deleteById(id);
  }

  public ShoppingListItemEntity updateById(UUID id, TodoUpdateDto todoUpdateDto) {
    Optional<ShoppingListItemEntity> optionalShoppingListItem = todoRepository.findById(id);
    if (optionalShoppingListItem.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }

    ShoppingListItemEntity shoppingListItem = optionalShoppingListItem.get();

    if (todoUpdateDto.getTitle() != null && todoUpdateDto.getTitle().length() > 0) {
      shoppingListItem.setTitle(todoUpdateDto.getTitle());
    } else if (todoUpdateDto.getComplete() != null) {
      shoppingListItem.setCompleted(todoUpdateDto.getComplete());
    } else {
      throw new BadRequestException("Invalid request");
    }

    return todoRepository.save(shoppingListItem);
  }

  public ShoppingListItemEntity markCompleteById(UUID id) {
    Optional<ShoppingListItemEntity> shoppingListItemOptional = todoRepository.findById(id);
    if (shoppingListItemOptional.isEmpty()) {
      throw new ResourceNotFoundException("Todo not found");
    }

    ShoppingListItemEntity shoppingListItem = shoppingListItemOptional.get();

    shoppingListItem.setCompleted(!shoppingListItem.isCompleted());
    return todoRepository.save(shoppingListItem);
  }

  public CountDto countAll() {
    return new CountDto(todoRepository.count());
  }

  public CountDto countAllByIsCompleted(String username, String isCompletedString) {
    boolean isCompleted = isCompletedStringToBoolean(isCompletedString);
    return new CountDto(todoRepository.countByCompleted(isCompleted));
  }

  private boolean isCompletedStringToBoolean(String isCompleted) {
    try {
      return Boolean.parseBoolean(isCompleted);
    } catch (Exception e) {
      throw new BadRequestException("Invalid isCompleted");
    }
  }

  private int pageNumberStringToInteger(String pageNumber) {
    int _pageNumber;

    try {
      _pageNumber = Integer.parseInt(pageNumber);
    } catch (Exception e) {
      throw new InvalidPageException("Invalid Page Number");
    }

    if (_pageNumber < 0) {
      throw new InvalidPageException("Invalid page number");
    }

    return _pageNumber;
  }

  public int calculatePageSize() {
    long amountCompleted = todoRepository.countByCompletedTrue();

    return Math.max((int) (amountCompleted + 10), 100);
  }
}
