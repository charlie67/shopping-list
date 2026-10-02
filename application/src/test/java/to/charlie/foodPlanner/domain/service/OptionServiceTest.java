package to.charlie.foodPlanner.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import to.charlie.foodPlanner.domain.model.dto.options.OptionDto;
import to.charlie.foodPlanner.domain.model.entity.OptionEntity;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.infrastructure.dal.repository.OptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

	@Mock
	private OptionRepository optionRepository;

	/** A handler that records what it was told, standing in for whatever feature owns the setting. */
	private static final class RecordingHandler implements OptionUpdateHandler {

		private final Option option;
		private final List<String> values = new ArrayList<>();

		private RecordingHandler(final Option option) {
			this.option = option;
		}

		@Override
		public Option option() {
			return option;
		}

		@Override
		public void onOptionUpdated(final String value) {
			values.add(value);
		}
	}

	private OptionService serviceWith(final OptionUpdateHandler... handlers) {
		return new OptionService(optionRepository, List.of(handlers));
	}

	private void givenSaveEchoesTheEntity() {
		when(optionRepository.save(any(OptionEntity.class)))
						.thenAnswer(call -> call.getArgument(0));
	}

	@Test
	void getOption_whenNeverSet_thenTheEnumDefault() {
		// given
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());

		// when
		final OptionDto result = serviceWith().getOption(Option.PLAN_PORTION_SIZE);

		// then
		assertThat(result.getValue()).isEqualTo(Option.PLAN_PORTION_SIZE.getDefaultValue());
	}

	@Test
	void getOption_whenStored_thenTheStoredValue() {
		// given
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.of(
						OptionEntity.builder().name(Option.PLAN_PORTION_SIZE).value("5").build()));

		// when
		final OptionDto result = serviceWith().getOption(Option.PLAN_PORTION_SIZE);

		// then
		assertThat(result.getValue()).isEqualTo("5");
	}

	@Test
	void updateOption_whenAHandlerWantsThatOption_thenItIsCalledWithTheStoredValue() {
		// given
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());
		givenSaveEchoesTheEntity();
		final RecordingHandler handler = new RecordingHandler(Option.PLAN_PORTION_SIZE);

		// when
		final OptionDto result = serviceWith(handler).updateOption(Option.PLAN_PORTION_SIZE, "4");

		// then
		assertThat(result.getValue()).isEqualTo("4");
		assertThat(handler.values).containsExactly("4");
	}

	@Test
	void updateOption_whenTheValueIsNotMeaningful_thenTheHandlerStillDecidesRatherThanThisService() {
		// given the option is free text, so what to make of it is the handler's business
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());
		givenSaveEchoesTheEntity();
		final RecordingHandler handler = new RecordingHandler(Option.PLAN_PORTION_SIZE);

		// when
		final OptionDto result = serviceWith(handler).updateOption(Option.PLAN_PORTION_SIZE, "lots");

		// then
		assertThat(result.getValue()).isEqualTo("lots");
		assertThat(handler.values).containsExactly("lots");
	}

	@Test
	void updateOption_whenEveryHandlerWantsAnotherOption_thenNoneIsCalled() {
		// given the only handler is registered against a different option
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());
		givenSaveEchoesTheEntity();
		final RecordingHandler other = new RecordingHandler(null);

		// when
		serviceWith(other).updateOption(Option.PLAN_PORTION_SIZE, "4");

		// then
		assertThat(other.values).isEmpty();
	}

	@Test
	void updateOption_whenNothingIsRegistered_thenTheValueIsSimplyStored() {
		// given
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());
		givenSaveEchoesTheEntity();

		// when
		final OptionDto result = serviceWith().updateOption(Option.PLAN_PORTION_SIZE, "3");

		// then
		assertThat(result.getValue()).isEqualTo("3");
		assertThat(result.getName()).isEqualTo(Option.PLAN_PORTION_SIZE);
	}

	@Test
	void updateOption_whenAlreadySet_thenTheExistingRowIsOverwritten() {
		// given
		final OptionEntity existing = OptionEntity.builder()
						.name(Option.PLAN_PORTION_SIZE).value("2").build();
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.of(existing));
		givenSaveEchoesTheEntity();

		// when
		final OptionDto result = serviceWith().updateOption(Option.PLAN_PORTION_SIZE, "6");

		// then
		assertThat(existing.getValue()).isEqualTo("6");
		assertThat(result.getValue()).isEqualTo("6");
	}

	@Test
	void updateOption_whenSeveralHandlersWantTheSameOption_thenAllOfThemRun() {
		// given nothing says a setting may only interest one feature
		when(optionRepository.findById(Option.PLAN_PORTION_SIZE)).thenReturn(Optional.empty());
		givenSaveEchoesTheEntity();
		final RecordingHandler first = new RecordingHandler(Option.PLAN_PORTION_SIZE);
		final RecordingHandler second = new RecordingHandler(Option.PLAN_PORTION_SIZE);

		// when
		serviceWith(first, second).updateOption(Option.PLAN_PORTION_SIZE, "4");

		// then
		assertThat(first.values).containsExactly("4");
		assertThat(second.values).containsExactly("4");
	}
}
