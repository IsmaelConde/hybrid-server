package es.uvigo.esei.dai.hybridserver.step1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
public class HTTPResponseNoContentTest {
  private HTTPResponse response;

  @BeforeEach
  public void setUp() throws Exception {
    this.response = new HTTPResponse();

    this.response.setStatus(HTTPResponseStatus.S200);
    this.response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());
  }

  @Test
  public final void testPrint() throws IOException {
    try (final StringWriter writer = new StringWriter()) {
      this.response.print(writer);

      assertThat(writer.toString(), is(equalTo("HTTP/1.1 200 OK\r\n\r\n")));
    }
  }
}
