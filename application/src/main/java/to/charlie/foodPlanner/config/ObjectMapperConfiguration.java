package to.charlie.foodPlanner.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;

public class ObjectMapperConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    return objectMapper;
  }
}
