package to.charlie.foodPlanner.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import to.charlie.foodPlanner.domain.model.dto.options.OptionDto;
import to.charlie.foodPlanner.domain.model.entity.OptionEntity;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.infrastructure.dal.repository.OptionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionService {

	private final OptionRepository optionRepository;

	/**
	 * Whatever each feature wants done when its own setting changes. This service deliberately knows
	 * nothing about what any option means - see {@link OptionUpdateHandler}.
	 */
	private final List<OptionUpdateHandler> updateHandlers;

	public OptionDto getOption(final Option option) {
		final String value = optionRepository.findById(option)
						.map(OptionEntity::getValue)
						.orElse(option.getDefaultValue());
		return OptionDto.builder().name(option).value(value).build();
	}

	public OptionDto updateOption(final Option option, final String value) {
		final OptionEntity entity = optionRepository.findById(option)
						.orElseGet(() -> OptionEntity.builder().name(option).build());
		entity.setValue(value);
		final OptionEntity saved = optionRepository.save(entity);

		// After the save, so a handler that reads the setting back sees the new value.
		updateHandlers.stream()
						.filter(handler -> handler.option() == saved.getName())
						.forEach(handler -> handler.onOptionUpdated(saved.getValue()));

		return OptionDto.builder().name(saved.getName()).value(saved.getValue()).build();
	}
}
