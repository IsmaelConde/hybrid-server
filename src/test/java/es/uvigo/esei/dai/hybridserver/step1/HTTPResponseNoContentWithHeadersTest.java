package es.uvigo.esei.dai.hybridserver.step1;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;

@Tag("response")
public class HTTPResponseNoContentWithHeadersTest {
  private HTTPResponse response;

  @BeforeEach
  public void setUp() throws Exception {
    this.response = new HTTPResponse();

    this.response.setStatus(HTTPResponseStatus.S200);
    this.response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());
    this.response.putParameter("Content-Type", "text/html");
    this.response.putParameter("Content-Encoding", "deflate");
    this.response.putParameter("Content-Language", "en");
  }

  @Test
  public final void testPrint() throws IOException {
    try (final StringWriter writer = new StringWriter()) {
      this.response.print(writer);

      final String responseText = writer.toString();
      assertThat(responseText, startsWith("HTTP/1.1 200 OK"));
      assertThat(responseText, containsString("Content-Type: text/html"));
      assertThat(responseText, containsString("Content-Encoding: deflate"));
      assertThat(responseText, containsString("Content-Language: en"));
      assertThat(responseText, endsWith("\r\n\r\n"));
    }
  }
}
