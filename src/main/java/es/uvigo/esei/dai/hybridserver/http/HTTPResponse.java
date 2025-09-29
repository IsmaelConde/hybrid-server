package es.uvigo.esei.dai.hybridserver.http;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HTTPResponse {
    private HTTPResponseStatus status;
    private String version;
    private String content;
    private Map<String, String> parameters;

    public HTTPResponse() {

        this.status = HTTPResponseStatus.S200;
        this.version = "HTTP/1.1";
        this.content = "";
        this.parameters = new LinkedHashMap<>();

    }

    public HTTPResponseStatus getStatus() {
        return this.status;
    }

    public void setStatus(HTTPResponseStatus status) {
        this.status = status;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String putParameter(String name, String value) {
        return this.parameters.put(name, value);
    }

    public boolean containsParameter(String name) {
        return this.parameters.containsKey(name);
    }

    public String removeParameter(String name) {
        return this.parameters.remove(name);
    }

    public void clearParameters() {
        this.parameters.clear();
    }

    public List<String> listParameters() {
        return new ArrayList<>(this.parameters.keySet());
    }

    public void print(Writer writer) throws IOException {
        // Primera línea
        writer.write(this.version + " " + this.status.getCode() + " " + this.status.getStatus() + "\r\n");

// Añadir Content-Length solo si hay contenido
        if (!this.content.isEmpty()) {
            byte[] bodyBytes = this.content.getBytes(StandardCharsets.UTF_8);
            this.parameters.put("Content-Length", String.valueOf(bodyBytes.length));
        }

// Escribir cabeceras
        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

// Línea en blanco
        writer.write("\r\n");

// Cuerpo
        if (!this.content.isEmpty()) {
            writer.write(this.content);
        }


        writer.flush();
    }

    @Override
    public String toString() {
        try (final StringWriter writer = new StringWriter()) {
            this.print(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected I/O exception", e);
        }
    }
}
