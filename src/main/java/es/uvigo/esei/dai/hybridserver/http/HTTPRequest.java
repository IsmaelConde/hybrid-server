package es.uvigo.esei.dai.hybridserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {
    private HTTPRequestMethod method;
    private String resourceChain;
    private String[] resourcePath;
    private String resourceName;
    private Map<String, String> resourceParameters = new HashMap<>();
    private String httpVersion;
    private Map<String, String> headerParameters = new LinkedHashMap<>();
    private String content = null;
    private int contentLength = 0;

    public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
        BufferedReader br = new BufferedReader(reader);

        // Leer request line
        String requestLine = br.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new HTTPParseException("Línea de petición vacía");
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new HTTPParseException("Línea de petición inválida: " + requestLine);
        }

        try {
            this.method = HTTPRequestMethod.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new HTTPParseException("Método HTTP no soportado: " + parts[0]);
        }

        this.resourceChain = parts[1];
        parseResource(parts[1]);

        this.httpVersion = parts[2];
        if (!httpVersion.startsWith("HTTP/")) {
            throw new HTTPParseException("Versión HTTP inválida: " + httpVersion);
        }

        // Leer cabeceras
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            int sep = line.indexOf(":");
            if (sep == -1) {
                throw new HTTPParseException("Cabecera inválida: " + line);
            }
            String key = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();
            headerParameters.put(key, value);

            if (key.equalsIgnoreCase("Content-Length")) {
                try {
                    this.contentLength = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new HTTPParseException("Content-Length inválido: " + value);
                }
            }
        }

        if (this.contentLength > 0) {
            char[] buf = new char[this.contentLength];
            int totalRead = 0;
            while (totalRead < this.contentLength) {
                int read = br.read(buf, totalRead, this.contentLength - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            if (totalRead != this.contentLength) {
                throw new HTTPParseException("Body incompleto, esperado " + this.contentLength + " pero leído " + totalRead);
            }

            String rawBody = new String(buf);

            // Decodificar todos los caracteres de URL
            this.content = URLDecoder.decode(rawBody, StandardCharsets.UTF_8);

            // Llenar resourceParameters desde body
            for (String pair : this.content.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    resourceParameters.put(kv[0], kv[1]);
                }
            }
        }

    }

    public HTTPRequestMethod getMethod() {

        return method;
    }
    public String getResourceChain() {

        return resourceChain;
    }
    public String[] getResourcePath() {

        return resourcePath;
    }
    public String getResourceName() {

        return resourceName;
    }
    public Map<String, String> getResourceParameters() {

        return resourceParameters;
    }
    public String getHttpVersion() {
        return httpVersion;
    }
    public Map<String, String> getHeaderParameters() {
        return headerParameters;
    }
    public String getContent() {
        return content;
    }
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append(this.getMethod().name()).append(' ')
                .append(this.getResourceChain()).append(' ')
                .append(this.getHttpVersion()).append("\r\n");

        for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
            sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
        }

        if (this.getContentLength() > 0 && this.getContent() != null) {
            sb.append("\r\n").append(this.getContent());
        }

        return sb.toString();
    }

    private void parseResource(String chain) throws HTTPParseException {
        String path = chain;
        String query = null;
        int qIndex = path.indexOf("?");
        if (qIndex != -1) {
            query = path.substring(qIndex + 1);
            path = path.substring(0, qIndex);
        }

        if (path.startsWith("/")) path = path.substring(1);

        this.resourcePath = path.isEmpty() ? new String[0] : path.split("/");
        this.resourceName = String.join("/", this.resourcePath);

        // Llenar resourceParameters desde query string
        if (query != null && !query.isEmpty()) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    this.resourceParameters.put(key, value);
                }
            }
        }
    }
}
