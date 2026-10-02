package to.charlie.foodPlanner.infrastructure.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodPlanner.domain.model.dto.options.OptionDto;
import to.charlie.foodPlanner.domain.model.dto.options.OptionUpdateDto;
import to.charlie.foodPlanner.domain.model.internal.options.Option;
import to.charlie.foodPlanner.domain.service.OptionService;

@RestController
@RequestMapping("/options")
@RequiredArgsConstructor
public class OptionController {

	private final OptionService optionService;

	// An unknown option name fails enum conversion, which Spring answers with a 400.
	@GetMapping("/{option}")
	public ResponseEntity<OptionDto> getOption(@PathVariable final Option option) {
		return ResponseEntity.ok(optionService.getOption(option));
	}

	@PutMapping("/{option}")
	public ResponseEntity<OptionDto> updateOption(
					@PathVariable final Option option,
					@RequestBody final OptionUpdateDto optionUpdate) {
		return ResponseEntity.ok(optionService.updateOption(option, optionUpdate.getValue()));
	}
}
