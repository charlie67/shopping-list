package to.charlie.integrationTests.foodPlanner;

import java.io.IOException;
import java.net.ServerSocket;

public class Ports {

  private static final int SPRING = getUnusedPort();


  public static int getUnusedPort() {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Could not find an unused port", e);
    }

    return port;
  }
}
