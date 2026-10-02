package to.charlie.foodPlanner.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import to.charlie.foodPlanner.config.modelMapper.ModelMapperConfiguration;
import to.charlie.foodPlanner.domain.exception.BadRequestException;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebSocketMessageDto;
import to.charlie.foodPlanner.domain.model.dto.websocket.WebsocketUpdateType;
import to.charlie.foodPlanner.domain.model.dto.websocket.shoppingList.ShoppingListItemsCreatedDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.websocket.WebSocketService;
import to.charlie.foodPlanner.infrastructure.dal.repository.TodoPagingRepository;
import to.charlie.foodPlanner.infrastructure.dal.repository.TodoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private TodoPagingRepository todoPagingRepository;

	@Mock
	private WebSocketService webSocketService;

	private ShoppingListService service;

	@BeforeEach
	void setUp() {
		final ModelMapper modelMapper = new ModelMapperConfiguration().modelMapper();
		service = new ShoppingListService(todoRepository, todoPagingRepository, webSocketService, modelMapper);
	}

	private ShoppingListItemEntity buildSavedEntity(final UUID id, final String title, final boolean completed) {
		return ShoppingListItemEntity.builder()
						.id(id)
						.title(title)
						.completed(completed)
						.createdAtTime(LocalDateTime.now())
						.updatedAtTime(LocalDateTime.now())
						.build();
	}

	@Test
	void create_whenValidCreateDto_thenSavesItemAndSendsWebsocketMessage() {
		// given
		final ShoppingListItemCreateDto createDto = new ShoppingListItemCreateDto("Milk");
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity savedEntity = buildSavedEntity(id, "Milk", false);
		when(todoRepository.save(any(ShoppingListItemEntity.class))).thenReturn(savedEntity);

		// when
		final ShoppingListItemDto result = service.create(createDto);

		// then
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getTitle()).isEqualTo("Milk");
		verify(todoRepository).save(any(ShoppingListItemEntity.class));
		final ArgumentCaptor<WebSocketMessageDto> messageCaptor = ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService).sendMessageToAllClients(messageCaptor.capture());
		assertThat(messageCaptor.getValue().getMessageType()).isEqualTo(WebsocketUpdateType.SHOPPING_LIST_ITEM_CREATED);
	}

	@Test
	void createAll_whenSeveralTitles_thenSavesThemAllAndBroadcastsOnce() {
		// given a recipe's ingredients: one request rather than a dozen, and one broadcast rather than
		// a dozen re-sorts on every connected phone
		final List<String> titles = List.of("Onion", "Red pepper", "Beef mince");
		when(todoRepository.saveAllAndFlush(anyList())).thenAnswer(call -> {
			final List<ShoppingListItemEntity> toSave = call.getArgument(0);
			return toSave.stream()
							.map(entity -> buildSavedEntity(UUID.randomUUID(), entity.getTitle(), false))
							.toList();
		});

		// when
		final List<ShoppingListItemDto> created = service.createAll(titles);

		// then
		assertThat(created).extracting(ShoppingListItemDto::getTitle)
						.containsExactly("Onion", "Red pepper", "Beef mince");

		final ArgumentCaptor<WebSocketMessageDto> messageCaptor = ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService, times(1)).sendMessageToAllClients(messageCaptor.capture());
		assertThat(messageCaptor.getValue().getMessageType())
						.isEqualTo(WebsocketUpdateType.SHOPPING_LIST_ITEMS_CREATED);
		assertThat(((ShoppingListItemsCreatedDto) messageCaptor.getValue().getData()).getItems())
						.hasSize(3);
	}

	@Test
	void createAll_whenATitleIsBlank_thenItIsDroppedRatherThanFailingTheBatch() {
		// given the titles come from a scraped ingredient list, so losing the whole recipe over one
		// unreadable line is the worse outcome
		when(todoRepository.saveAllAndFlush(anyList())).thenAnswer(call -> {
			final List<ShoppingListItemEntity> toSave = call.getArgument(0);
			return toSave.stream()
							.map(entity -> buildSavedEntity(UUID.randomUUID(), entity.getTitle(), false))
							.toList();
		});

		// when
		final List<ShoppingListItemDto> created = service.createAll(List.of("Onion", "   ", "Garlic"));

		// then
		assertThat(created).extracting(ShoppingListItemDto::getTitle)
						.containsExactly("Onion", "Garlic");
	}

	@Test
	void createAll_whenNothingUsableIsGiven_thenThrowsBadRequestExceptionAndBroadcastsNothing() {
		// given / when / then
		assertThatThrownBy(() -> service.createAll(List.of(" ")))
						.isInstanceOf(BadRequestException.class);
		verify(webSocketService, times(0)).sendMessageToAllClients(any());
	}

	@Test
	void deleteById_whenItemExists_thenDeletesItemAndSendsWebsocketMessage() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Bread", false);
		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));

		// when
		service.deleteById(id);

		// then
		verify(todoRepository).deleteById(id);
		final ArgumentCaptor<WebSocketMessageDto> messageCaptor = ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService).sendMessageToAllClients(messageCaptor.capture());
		assertThat(messageCaptor.getValue().getMessageType()).isEqualTo(WebsocketUpdateType.SHOPPING_LIST_ITEM_DELETED);
	}

	@Test
	void deleteById_whenItemNotFound_thenThrowsResourceNotFoundException() {
		// given
		final UUID id = UUID.randomUUID();
		when(todoRepository.findById(id)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> service.deleteById(id))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateById_whenTitleProvided_thenUpdatesTitleAndSendsWebsocketMessage() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Old Title", false);
		final ShoppingListItemUpdateDto updateDto = new ShoppingListItemUpdateDto("New Title", null);
		final ShoppingListItemEntity updatedEntity = buildSavedEntity(id, "New Title", false);

		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));
		when(todoRepository.save(entity)).thenReturn(updatedEntity);

		// when
		final ShoppingListItemDto result = service.updateById(id, updateDto);

		// then
		assertThat(result.getTitle()).isEqualTo("New Title");
		final ArgumentCaptor<WebSocketMessageDto> messageCaptor = ArgumentCaptor.forClass(WebSocketMessageDto.class);
		verify(webSocketService).sendMessageToAllClients(messageCaptor.capture());
		assertThat(messageCaptor.getValue().getMessageType()).isEqualTo(WebsocketUpdateType.SHOPPING_LIST_ITEM_UPDATED);
	}

	@Test
	void updateById_whenCompleteProvided_thenUpdatesCompleteStatusAndSendsWebsocketMessage() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Butter", false);
		final ShoppingListItemUpdateDto updateDto = new ShoppingListItemUpdateDto(null, true);
		final ShoppingListItemEntity updatedEntity = buildSavedEntity(id, "Butter", true);

		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));
		when(todoRepository.save(entity)).thenReturn(updatedEntity);

		// when
		final ShoppingListItemDto result = service.updateById(id, updateDto);

		// then
		assertThat(result.isCompleted()).isTrue();
	}

	@Test
	void updateById_whenNeitherTitleNorCompleteProvided_thenThrowsBadRequestException() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Cheese", false);
		final ShoppingListItemUpdateDto updateDto = new ShoppingListItemUpdateDto(null, null);

		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));

		// when / then
		assertThatThrownBy(() -> service.updateById(id, updateDto))
						.isInstanceOf(BadRequestException.class);
	}

	@Test
	void updateById_whenItemNotFound_thenThrowsResourceNotFoundException() {
		// given
		final UUID id = UUID.randomUUID();
		when(todoRepository.findById(id)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> service.updateById(id, new ShoppingListItemUpdateDto("title", null)))
						.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void markCompleteById_whenItemIsNotCompleted_thenTogglesCompletedToTrue() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Yogurt", false);
		final ShoppingListItemEntity savedEntity = buildSavedEntity(id, "Yogurt", true);

		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));
		when(todoRepository.save(entity)).thenReturn(savedEntity);

		// when
		final ShoppingListItemEntity result = service.markCompleteById(id);

		// then
		assertThat(result.isCompleted()).isTrue();
	}

	@Test
	void markCompleteById_whenItemIsCompleted_thenTogglesCompletedToFalse() {
		// given
		final UUID id = UUID.randomUUID();
		final ShoppingListItemEntity entity = buildSavedEntity(id, "Juice", true);
		final ShoppingListItemEntity savedEntity = buildSavedEntity(id, "Juice", false);

		when(todoRepository.findById(id)).thenReturn(Optional.of(entity));
		when(todoRepository.save(entity)).thenReturn(savedEntity);

		// when
		final ShoppingListItemEntity result = service.markCompleteById(id);

		// then
		assertThat(result.isCompleted()).isFalse();
	}

	@Test
	void markCompleteById_whenItemNotFound_thenThrowsResourceNotFoundException() {
		// given
		final UUID id = UUID.randomUUID();
		when(todoRepository.findById(id)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> service.markCompleteById(id))
						.isInstanceOf(ResourceNotFoundException.class);
	}
}
