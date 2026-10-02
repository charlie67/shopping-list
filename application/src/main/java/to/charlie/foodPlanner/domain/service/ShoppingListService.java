package to.charlie.foodPlanner.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList.ShoppingListItemDeletedDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList.ShoppingListItemsCreatedDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;
import to.charlie.foodPlanner.infrastructure.dal.repository.TodoPagingRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.TodoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.SHOPPING_LIST_ITEMS_CREATED;
import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.SHOPPING_LIST_ITEM_CREATED;
import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.SHOPPING_LIST_ITEM_DELETED;
import static to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType.SHOPPING_LIST_ITEM_UPDATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListService {

	private final TodoRepository todoRepository;

	private final TodoPagingRepository todoPagingRepository;

	private final WebSocketService webSocketService;

	private final ModelMapper modelMapper;

	public ShoppingListItemDto create(final ShoppingListItemCreateDto todoCreateDto) {
		ShoppingListItemEntity shoppingListItem = ShoppingListItemEntity.builder()
						.title(todoCreateDto.getTitle()).build();
		shoppingListItem = todoRepository.save(shoppingListItem);

		final ShoppingListItemDto shoppingListItemDto = modelMapper.map(
						shoppingListItem, ShoppingListItemDto.class);
		webSocketService.sendMessageToAllClients(WebSocketMessageDto
						.builder()
						.data(shoppingListItemDto)
						.messageType(SHOPPING_LIST_ITEM_CREATED)
						.build());
		return shoppingListItemDto;
	}

	@Transactional
	public List<ShoppingListItemDto> createAll(final List<String> titles) {
		if (titles == null) {
			throw new BadRequestException("No items to add");
		}

		final List<ShoppingListItemEntity> toSave = titles.stream()
						.filter(title -> title != null && !title.isBlank())
						.map(title -> ShoppingListItemEntity.builder().title(title.trim()).build())
						.toList();

		if (toSave.isEmpty()) {
			throw new BadRequestException("No items to add");
		}

		final List<ShoppingListItemDto> created = todoRepository.saveAllAndFlush(toSave).stream()
						.map(saved -> modelMapper.map(saved, ShoppingListItemDto.class))
						.toList();

		log.info("Added {} items to the shopping list in one batch", created.size());

		webSocketService.sendMessageToAllClients(WebSocketMessageDto
						.builder()
						.data(ShoppingListItemsCreatedDto.builder().items(created).build())
						.messageType(SHOPPING_LIST_ITEMS_CREATED)
						.build());

		return created;
	}

	public Page<ShoppingListItemEntity> readAllPageable(final int pageNumber, final int pageSize) {

		final Pageable pageable = PageRequest.of(pageNumber, pageSize,
						Sort.by("completed").ascending().and(Sort.by("createdAtTime").descending()));
		return todoPagingRepository.findAll(pageable);
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
						&& !shoppingListItemUpdateDto.getTitle().isEmpty()) {
			shoppingListItem.setTitle(shoppingListItemUpdateDto.getTitle());
		}
		if (shoppingListItemUpdateDto.getComplete() != null) {
			shoppingListItem.setCompleted(shoppingListItemUpdateDto.getComplete());
		}

		if ((shoppingListItemUpdateDto.getTitle() == null
						|| shoppingListItemUpdateDto.getTitle().isEmpty())
						&& shoppingListItemUpdateDto.getComplete() == null) {
			throw new BadRequestException("Invalid request");
		}

		final ShoppingListItemDto shoppingListItemDto = modelMapper.map(
						todoRepository.save(shoppingListItem), ShoppingListItemDto.class);

		webSocketService.sendMessageToAllClients(WebSocketMessageDto.builder().data(shoppingListItemDto)
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
}
