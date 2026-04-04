package to.charlie.integrationTests.foodPlanner.utilities;

import java.io.IOException;
import java.net.ServerSocket;

public class Ports {

  public static final int SPRING = 28080;

  public static final int DATRABASE = getUnusedPort();


  public static int getUnusedPort() {
    final int port;
    try (final ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    } catch (final IOException e) {
      throw new RuntimeException("Could not find an unused port", e);
    }

    return port;
  }
}
