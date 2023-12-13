package to.charlie.foodPlanner.domain.model.exception;

public class DuplicateRecipeException extends Exception{

  public DuplicateRecipeException(String message) {
    super(message);
  }
}
