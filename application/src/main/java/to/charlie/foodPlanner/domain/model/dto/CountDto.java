package to.charlie.foodPlanner.domain.model.dto;

public class CountDto {

  private long count;

  protected CountDto() {
  }

  public CountDto(final long count) {
    super();
    this.count = count;
  }

  public long getCount() {
    return count;
  }

  public void setCount(final long count) {
    this.count = count;
  }
}
