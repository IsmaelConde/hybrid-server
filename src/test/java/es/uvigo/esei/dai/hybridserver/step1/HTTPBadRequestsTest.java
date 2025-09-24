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
package es.uvigo.esei.dai.hybridserver.step1;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.StringReader;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;

@Tag("request")
public class HTTPBadRequestsTest {
  public static Stream<Arguments> badRequests() {
    return Stream
      .of(
        arguments(
          named(
            "Missing method",
            "/hello HTTP/1.1\r\n"
            + "Host: localhost\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        ),
        arguments(
          named(
            "Missing resource",
            "GET HTTP/1.1\r\n"
            + "Host: localhost\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        ),
        arguments(
          named(
            "Missing version",
            "GET /hello\r\n"
            + "Host: localhost\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        ),
        arguments(
          named(
            "Missing first line",
            "Host: localhost\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        ),
        arguments(
          named(
            "Invalid header",
            "GET /hello/world.html?country=Spain&province=Ourense&city=Ourense HTTP/1.1\r\n"
            + "Host\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        ),
        arguments(
          named(
            "Missing new line after header",
            "GET /hello/world.html?country=Spain&province=Ourense&city=Ourense HTTP/1.1"
            + "Host: localhost\r\n"
            + "Accept: text/html\r\n"
            + "Accept-Encoding: gzip,deflate\r\n"
          )
        )
      );
  }

  @ParameterizedTest
  @MethodSource("badRequests")
  public void testThatThrowsHTTPParseException(final String requestText) {
    assertThrows(HTTPParseException.class, () -> new HTTPRequest(new StringReader(requestText)));
  }
}
