package to.charlie.foodPlanner.inrastructure.rest;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import to.charlie.foodPlanner.domain.model.dto.CountDto;
import to.charlie.foodPlanner.domain.model.dto.TodoCreateDto;
import to.charlie.foodPlanner.domain.model.dto.TodoUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.TodoService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ShoppingListController {

  private final TodoService todoService;

  @ResponseStatus(code = HttpStatus.CREATED)
  @PostMapping(value = "/shoppinglist")
  public ResponseEntity<ShoppingListItemEntity> create(
      @Valid @RequestBody TodoCreateDto todoCreateDto) {
    return new ResponseEntity<>(todoService.create(todoCreateDto), HttpStatus.CREATED);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @GetMapping(value = "/shoppinglist")
  public ResponseEntity<List<ShoppingListItemEntity>> readAll(
      Principal principal, @RequestParam(required = false) String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
          todoService.readAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(todoService.readAll(principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/count", method = RequestMethod.GET)
  public ResponseEntity<CountDto> countAll(
      Principal principal, @RequestParam(required = false) String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
          todoService.countAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(todoService.countAll(), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/pageable/{pageNumber}", method = RequestMethod.GET)
  public ResponseEntity<Page<ShoppingListItemEntity>> readPageable(
      @PathVariable String pageNumber, @RequestParam(required = false) String isCompleted) {
    int pageSize = todoService.calculatePageSize();

    if (isCompleted != null) {
      return new ResponseEntity<>(
          todoService.readAllByIsCompletedPageable(isCompleted, pageNumber, pageSize),
          HttpStatus.OK);
    }

    return new ResponseEntity<>(todoService.readAllPageable(pageNumber, pageSize), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/shoppinglist/id/{id}", method = RequestMethod.GET)
  public ResponseEntity<ShoppingListItemEntity> read(@PathVariable UUID id, Principal principal) {
    return new ResponseEntity<>(todoService.readById(id, principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @PatchMapping(value = "/shoppinglist/{id}")
  public ResponseEntity<ShoppingListItemEntity> update(
          @PathVariable UUID id, @Valid @RequestBody TodoUpdateDto todoUpdateDto) {
    return new ResponseEntity<>(todoService.updateById(id, todoUpdateDto), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @RequestMapping(value = "/shoppinglist/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> delete(@PathVariable UUID id) {
    todoService.deleteById(id);
    return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
  }
}
