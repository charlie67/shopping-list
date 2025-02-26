package to.charlie.integrationTests.foodPlanner.steps;

import io.cucumber.java.en.When;

public class HttpSteps {

  @When("I send an HTTP {} request to {string} with the following body: {}")
  public void sendHttpRequest(final String method, final String url, final String body) {
    System.out.println("!!!!THIS WORKED");
  }

  @When("I add the folowing items to the shopping list {}")
  public void iSendAnHTTPPOSTRequestToWithTheFollowingBody(String url, String body) {
    System.out.println("!!!!THIS WORKED");

  }
}
