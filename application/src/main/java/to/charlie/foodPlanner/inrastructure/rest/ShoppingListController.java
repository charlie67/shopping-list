package to.charlie.foodPlanner.inrastructure.rest;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
  @RequestMapping(value = "/todo", method = RequestMethod.POST)
  public ResponseEntity<ShoppingListItemEntity> create(
      @Valid @RequestBody TodoCreateDto todoCreateDto) {
    return new ResponseEntity<>(todoService.create(todoCreateDto), HttpStatus.CREATED);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/todo", method = RequestMethod.GET)
  public ResponseEntity<List<ShoppingListItemEntity>> readAll(
      Principal principal, @RequestParam(required = false) String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
          todoService.readAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(todoService.readAll(principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/todo/count", method = RequestMethod.GET)
  public ResponseEntity<CountDto> countAll(
      Principal principal, @RequestParam(required = false) String isCompleted) {
    if (isCompleted != null) {
      return new ResponseEntity<>(
          todoService.countAllByIsCompleted(principal.getName(), isCompleted), HttpStatus.OK);
    }
    return new ResponseEntity<>(todoService.countAll(), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/todo/pageable/{pageNumber}", method = RequestMethod.GET)
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
  @RequestMapping(value = "/todo/id/{id}", method = RequestMethod.GET)
  public ResponseEntity<ShoppingListItemEntity> read(@PathVariable long id, Principal principal) {
    return new ResponseEntity<>(todoService.readById(id, principal.getName()), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/todo/{id}/markcomplete", method = RequestMethod.PUT)
  public ResponseEntity<ShoppingListItemEntity> markComplete(@PathVariable long id) {
    return new ResponseEntity<>(todoService.markCompleteById(id), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.OK)
  @RequestMapping(value = "/todo/{id}", method = RequestMethod.PUT)
  public ResponseEntity<ShoppingListItemEntity> update(
      @PathVariable long id, @Valid @RequestBody TodoUpdateDto todoUpdateDto) {
    return new ResponseEntity<>(todoService.updateById(id, todoUpdateDto), HttpStatus.OK);
  }

  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @RequestMapping(value = "/todo/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> delete(@PathVariable long id) {
    todoService.deleteById(id);
    return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
  }
}
