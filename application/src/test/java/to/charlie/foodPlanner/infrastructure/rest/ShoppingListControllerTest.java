package to.charlie.foodPlanner.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import to.charlie.foodPlanner.domain.exception.ResourceNotFoundException;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemCreateDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemDto;
import to.charlie.foodPlanner.domain.model.dto.shoppingList.ShoppingListItemUpdateDto;
import to.charlie.foodPlanner.domain.model.entity.ShoppingListItemEntity;
import to.charlie.foodPlanner.domain.service.ShoppingListService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest {

    @Mock
    private ShoppingListService shoppingListService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ShoppingListController(shoppingListService)).build();
        objectMapper = new ObjectMapper();
    }

    private ShoppingListItemDto buildItemDto(final UUID id, final String title, final boolean completed) {
        return ShoppingListItemDto.builder()
                .id(id)
                .title(title)
                .completed(completed)
                .createdAtTime(1000L)
                .updatedAtTime(2000L)
                .build();
    }

    private ShoppingListItemEntity buildItemEntity(final UUID id, final String title) {
        return ShoppingListItemEntity.builder()
                .id(id)
                .title(title)
                .completed(false)
                .createdAtTime(LocalDateTime.now())
                .updatedAtTime(LocalDateTime.now())
                .build();
    }

    @Test
    void create_whenValidBody_thenReturnsCreatedWithDto() throws Exception {
        // given
        final UUID id = UUID.randomUUID();
        final ShoppingListItemCreateDto createDto = new ShoppingListItemCreateDto("Milk");
        final ShoppingListItemDto responseDto = buildItemDto(id, "Milk", false);
        when(shoppingListService.create(any(ShoppingListItemCreateDto.class))).thenReturn(responseDto);

        // when / then
        mockMvc.perform(post("/shoppinglist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Milk"))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void readPageable_whenPageExists_thenReturnsOkWithPage() throws Exception {
        // given
        final UUID id = UUID.randomUUID();
        final ShoppingListItemEntity entity = buildItemEntity(id, "Eggs");
        final Page<ShoppingListItemEntity> entityPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 100), 1);
        when(shoppingListService.calculatePageSize()).thenReturn(100);
        when(shoppingListService.readAllPageable(0, 100)).thenReturn(entityPage);

        // when / then
        mockMvc.perform(get("/shoppinglist/pageable/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Eggs"));
    }

    @Test
    void readPageable_whenPageIsEmpty_thenReturnsOkWithEmptyPage() throws Exception {
        // given
        final Page<ShoppingListItemEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 100), 0);
        when(shoppingListService.calculatePageSize()).thenReturn(100);
        when(shoppingListService.readAllPageable(1, 100)).thenReturn(emptyPage);

        // when / then
        mockMvc.perform(get("/shoppinglist/pageable/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void update_whenValidBody_thenReturnsOkWithUpdatedDto() throws Exception {
        // given
        final UUID id = UUID.randomUUID();
        final ShoppingListItemUpdateDto updateDto = new ShoppingListItemUpdateDto("Updated Title", null);
        final ShoppingListItemDto responseDto = buildItemDto(id, "Updated Title", false);
        when(shoppingListService.updateById(eq(id), any(ShoppingListItemUpdateDto.class))).thenReturn(responseDto);

        // when / then
        mockMvc.perform(patch("/shoppinglist/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void update_whenCompletedFlagProvided_thenReturnsOkWithUpdatedCompletedStatus() throws Exception {
        // given
        final UUID id = UUID.randomUUID();
        final ShoppingListItemUpdateDto updateDto = new ShoppingListItemUpdateDto(null, true);
        final ShoppingListItemDto responseDto = buildItemDto(id, "Bread", true);
        when(shoppingListService.updateById(eq(id), any(ShoppingListItemUpdateDto.class))).thenReturn(responseDto);

        // when / then
        mockMvc.perform(patch("/shoppinglist/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void delete_whenItemExists_thenReturnsNoContent() throws Exception {
        // given
        final UUID id = UUID.randomUUID();

        // when / then
        mockMvc.perform(delete("/shoppinglist/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenItemNotFound_thenReturnsNotFound() throws Exception {
        // given
        final UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("not found")).when(shoppingListService).deleteById(id);

        // when / then
        mockMvc.perform(delete("/shoppinglist/{id}", id))
                .andExpect(status().isNotFound());
    }
}
