package to.charlie.foodPlanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class ShoppingListApplication {

  public static void main(final String[] args) {
    SpringApplication.run(ShoppingListApplication.class, args);
  }
}
