/**
 *  HybridServer
 *  Copyright (C) 2025 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver.step3;

import static es.uvigo.esei.dai.hybridserver.utils.TestUtils.getContentWithType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uvigo.esei.dai.hybridserver.HybridServer;

@Timeout(5L)
public class CustomPortTest {
  public HybridServer startServer() {
    final HybridServer server = new HybridServer();
    server.start();

    return server;
  }

  public HybridServer startServer(int port) {
    final Properties properties = new Properties();
    properties.setProperty("port", Integer.toString(port));
    properties.setProperty("numClients", "50");
    properties.setProperty("db.url", "jdbc:mysql://localhost/hstestdb");
    properties.setProperty("db.user", "dai");
    properties.setProperty("db.password", "daipassword");

    final HybridServer server = new HybridServer(properties);
    server.start();

    return server;
  }

  @Test
  public void testWelcome() throws IOException {
    try (HybridServer server = startServer()) {
      final String url = getUrlForHome(server);

      for (int i = 0; i < 10; i++) {
        assertThat(getContentWithType(url, "text/html"), containsString("Hybrid Server"));
      }
    }
  }

  @ParameterizedTest
  @ValueSource(ints = { 1234, 4242, 7315, 8833, 10201, 21386, 33217, 45450, 55881, 60000 })
  public void testMultipleWelcome(int port) throws IOException {
    try (HybridServer server = startServer(port)) {
      final String url = getUrlForHome(server);

      for (int i = 0; i < 10; i++) {
        assertThat(getContentWithType(url, "text/html"), containsString("Hybrid Server"));
      }
    }
  }

  private String getUrlForHome(HybridServer server) {
    return String.format("http://localhost:%d/", server.getPort());
  }
}
