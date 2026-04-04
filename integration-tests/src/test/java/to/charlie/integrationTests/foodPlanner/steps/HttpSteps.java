package to.charlie.integrationTests.foodPlanner.steps;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.integrationTests.foodPlanner.utilities.Context;
import to.charlie.integrationTests.foodPlanner.utilities.DataLoader;
import to.charlie.integrationTests.foodPlanner.utilities.Ports;

public class HttpSteps {

  @Autowired
  public DataLoader loader;

  @Autowired
  public Context context;

  private final CloseableHttpClient client = HttpClientBuilder
      .create()
      .setDefaultHeaders(getDefaultHeaders())
      .build();

  private List<BasicHeader> getDefaultHeaders() {
    final List<BasicHeader> headers = new ArrayList<>();
    headers.add(new BasicHeader("Content-Type", "application/json"));

    return headers;
  }

  @SneakyThrows
  @When("I send an HTTP {} request to {string} with the body from file: {string}")

  public void sendHttpRequest(final String method, final String url, final String bodyFile) {
    final String content = loader.loadData(bodyFile);
    final String builtUrl = "http://localhost:" + Ports.SPRING + url;

    sendRequest(method, builtUrl, content);
  }

  private void sendRequest(final String method, final String url, final String body) {
    if (method.equalsIgnoreCase("POST")) {
      sendHttpPostRequest(url, body);
    } else {
      throw new UnsupportedOperationException("HTTP method not supported: " + method);
    }

  }

  @SneakyThrows
  private void sendHttpPostRequest(final String url, final String body) {
    final HttpPost post = new HttpPost(url);

    post.setEntity(new StringEntity(body));
    final CloseableHttpResponse response = client.execute(post);

    context.set("RESPONSE_STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
    context.set("RESPONSE_BODY", EntityUtils.toString(response.getEntity()));
  }

  @Then("the response body should contain the following fields:")
  public void theResponseBodyShouldContainTheFollowingFields(final DataTable table) {
    final String body = context.get("RESPONSE_BODY");

    final DocumentContext context = JsonPath.parse(body);

    for (final List<String> row : table.asLists(String.class)) {
      final String jsonPath = row.get(0);
      final String expectedValue = row.get(1);

      final String actualValue = context.read(jsonPath, String.class);
      if (actualValue == null) {
        throw new AssertionError(
            "Expected value for JSON path '" + jsonPath + "' not found in response body.");
      }

      if (expectedValue.equals("<valid_uuid>")) {
        try {
          UUID.fromString(actualValue);
        } catch (final IllegalArgumentException e) {
          throw new AssertionError("Expected a valid UUID for JSON path '" + jsonPath
              + "', but got '" + actualValue + "'.");
        }
      } else if (!actualValue.equals(expectedValue)) {
        throw new AssertionError("Expected value '" + expectedValue + "' for JSON path '" + jsonPath
            + "', but got '" + actualValue + "'.");
      }
    }
  }
}
