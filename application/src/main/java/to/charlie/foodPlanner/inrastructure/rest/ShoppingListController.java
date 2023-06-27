package to.charlie.foodPlanner.inrastructure.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import to.charlie.foodPlanner.domain.model.dto.CountDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.ShoppingListService;
import to.charlie.foodPlanner.errorhandler.ResourceNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

  private final ShoppingListService shoppingListService;

  @ResponseStatus(code = HttpStatus.CREATED)
  @PostMapping(value = "/shoppinglist")
  public ResponseEntity<ShoppingListItemDto> create(
          @Valid @RequestBody final ShoppingListItemCreateDto todoCreateDto) {
    final ShoppingListItemDto shoppingListItemDto = shoppingListService.create(todoCreateDto);
    return new ResponseEntity<>(shoppingListItemDto, HttpStatus.CREATED);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @GetMapping(value = "/shoppinglist")
  public ResponseEntity<List<ShoppingListItemEntity>> readAll(
          final Principal principal, @RequestParam(required = false) final String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
              shoppingListService.readAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(shoppingListService.readAll(principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/count", method = RequestMethod.GET)
  public ResponseEntity<CountDto> countAll(
          final Principal principal, @RequestParam(required = false) final String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
              shoppingListService.countAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(shoppingListService.countAll(), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/pageable/{pageNumber}", method = RequestMethod.GET)
  public ResponseEntity<Page<ShoppingListItemEntity>> readPageable(
          @PathVariable final int pageNumber,
          @RequestParam(required = false) final Boolean isCompleted) {
    final int pageSize = shoppingListService.calculatePageSize();

    if (isCompleted != null) {
      return new ResponseEntity<>(
              shoppingListService.readAllByIsCompletedPageable(isCompleted, pageNumber, pageSize),
              HttpStatus.OK);
    }

    return new ResponseEntity<>(shoppingListService.readAllPageable(pageNumber, pageSize), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/id/{id}", method = RequestMethod.GET)
  public ResponseEntity<ShoppingListItemEntity> read(@PathVariable final UUID id,
                                                     final Principal principal) {
    return new ResponseEntity<>(shoppingListService.readById(id, principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @PatchMapping(value = "/shoppinglist/{id}")
  public ResponseEntity<ShoppingListItemDto> update(
          @PathVariable final UUID id,
          @Valid @RequestBody final ShoppingListItemUpdateDto shoppingListItemUpdateDto) {
    return new ResponseEntity<>(shoppingListService.updateById(id, shoppingListItemUpdateDto),
            HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @RequestMapping(value = "/shoppinglist/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> delete(@PathVariable final UUID id) {
    try {
      shoppingListService.deleteById(id);
    } catch (final ResourceNotFoundException e) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
  }
}
