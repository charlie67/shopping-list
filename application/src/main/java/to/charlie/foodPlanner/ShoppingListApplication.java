package to.charlie.foodPlanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ShoppingListApplication {

  public static void main(final String[] args) {
    SpringApplication.run(ShoppingListApplication.class, args);
  }
}
