package to.charlie.foodPlanner.domain.model.internal.recipeExtraction;

public interface ExtractedItem {

  String getOriginalText();

  boolean isPossibleDuplicate();

  void setPossibleDuplicate(boolean possibleDuplicate);
}
