package to.charlie.foodPlanner.infrastructure.rest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.converter.ShoppingListItemEntityMapper;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.ShoppingListService;
import to.charlie.foodPlanner.errorhandler.ResourceNotFoundException;

@RestController
@RequestMapping("/shoppinglist")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

  private final ShoppingListService shoppingListService;

  @ResponseStatus(code = HttpStatus.CREATED)
  @PostMapping
  public ResponseEntity<ShoppingListItemDto> create(
      @Valid @RequestBody final ShoppingListItemCreateDto createDto) {
    final ShoppingListItemDto shoppingListItemDto = shoppingListService.create(createDto);
    return new ResponseEntity<>(shoppingListItemDto, HttpStatus.CREATED);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/pageable/{pageNumber}", method = RequestMethod.GET)
  public ResponseEntity<Page<ShoppingListItemDto>> readPageable(
      @PathVariable final int pageNumber) {
    final int pageSize = shoppingListService.calculatePageSize();

    final Page<ShoppingListItemEntity> entities = shoppingListService.readAllPageable(pageNumber,
        pageSize);
    final Page<ShoppingListItemDto> dtos = entities.map(ShoppingListItemEntityMapper::entityToDto);
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @PatchMapping(value = "/{id}")
  public ResponseEntity<ShoppingListItemDto> update(
      @PathVariable final UUID id,
      @Valid @RequestBody final ShoppingListItemUpdateDto shoppingListItemUpdateDto) {
    return new ResponseEntity<>(shoppingListService.updateById(id, shoppingListItemUpdateDto),
        HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> delete(@PathVariable final UUID id) {
    try {
      shoppingListService.deleteById(id);
    } catch (final ResourceNotFoundException e) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
  }
}
