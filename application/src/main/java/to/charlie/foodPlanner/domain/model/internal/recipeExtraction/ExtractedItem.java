package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

public interface ExtractedItem {

  String getOriginalText();

  void setPossibleDuplicate(boolean possibleDuplicate);

  boolean isPossibleDuplicate();
}
