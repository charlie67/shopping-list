package to.charlie.foodplanner.infrastructure.restful;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import to.charlie.foodplanner.domain.dto.DayPlannerDto;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/planner", produces = "application/json")
public class DayPlannerController {

  public ResponseEntity<String> saveDay(@RequestBody
  DayPlannerDto dayPlannerDto) {
    return ResponseEntity.ok("");
  }
}
