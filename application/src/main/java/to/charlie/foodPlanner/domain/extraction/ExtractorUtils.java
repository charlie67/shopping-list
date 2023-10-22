package to.charlie.foodPlanner.domain.extraction;

import java.util.List;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import to.charlie.foodPlanner.domain.extraction.manual.data.ExtractedItem;

public class ExtractorUtils {

  public static void removeDuplicates(List<ExtractedItem> extractedItems, double threshold) {
    JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    for (int i = 0; i < extractedItems.size(); i++) {
      ExtractedItem extractedItemI = extractedItems.get(i);
      for (int j = 0; j < extractedItems.size(); j++) {
        ExtractedItem extractedItemJ = extractedItems.get(j);

        if (i == j
            || extractedItemI.isPossibleDuplicate()
            || extractedItemJ.isPossibleDuplicate()) {
          continue;
        }

        String textI = extractedItems.get(i).getOriginalText();
        String textJ = extractedItems.get(j).getOriginalText();

        if (textI.equals(textJ)) {
          extractedItems.get(j).setPossibleDuplicate(true);
        } else if (jaroWinkler.apply(textI, textJ) >= threshold) {
          extractedItems.get(j).setPossibleDuplicate(true);
        }
      }
    }
  }

  public static void removeIngredientDuplicates(List<ExtractedItem> extractedItems) {
    removeDuplicates(extractedItems, 0.75);
  }

  public static void removeStepDuplicates(List<ExtractedItem> extractedItems) {
    removeDuplicates(extractedItems, 0.85);
  }
}
