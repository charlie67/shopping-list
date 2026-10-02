package to.charlie.foodPlanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockConfiguration {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
