package to.charlie.foodPlanner.domain.extraction.manual.data;

public interface ExtractedItem {

  String getOriginalText();

  boolean isPossibleDuplicate();

  void setPossibleDuplicate(boolean possibleDuplicate);
}
