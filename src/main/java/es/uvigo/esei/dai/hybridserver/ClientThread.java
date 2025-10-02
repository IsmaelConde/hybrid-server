package es.uvigo.esei.dai.hybridserver;

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static es.uvigo.esei.dai.hybridserver.HybridServer.SERVICE_PORT;
import static es.uvigo.esei.dai.hybridserver.HybridServer.pages;

public class ClientThread implements Runnable {
    private final Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream))) {

            try {
                // ---- VALIDACIÓN DE REQUEST LINE ----
                String requestLine = reader.readLine();
                if (requestLine == null || requestLine.isEmpty()) {
                    throw new HTTPParseException("Petición vacía"); // Va aparecer un error en la consola si el cliente se conecta, no envía datos y cierra la conexión
                }

                String[] parts = requestLine.split(" ");
                if (parts.length < 3) {
                    throw new HTTPParseException("Línea de petición incompleta: " + requestLine);
                }

                String method = parts[0];
                String path = parts[1];
                String version = parts[2];

                if (!version.startsWith("HTTP/")) {
                    throw new HTTPParseException("Versión de protocolo inválida: " + version);
                }
                // ------------------------------------

                if (path.equals("/")) {     //Página de Bienvenida
                    String html_bienvenida = """
                                            <!DOCTYPE html>
                                            <html>
                                              <head>
                                                <meta charset="UTF-8">
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <title>Bienvenida</title>
                                              </head>
                                              <body>
                                                <h1>Hybrid Server</h1>
                                                <hr>
                                                <h3>Autores:</h3>
                                                <p>Daniel Domínguez Rosales</p>
                                                <p>Ismael Conde Conde</p>
                                                <hr>
                                                <h3>Crear página (POST)</h3>
                                                <form action="/html" method="POST">
                                                   <textarea name="html"></textarea>
                                                   <button type="submit">Submit</button>
                                                </form>
                                                <hr>
                                                <h3>Enlaces:</h3>
                                                    <ul>
                                                      <li><a href="/html">Ver listado de páginas HTML</a></li>
                                                    </ul>
                                              </body>
                                            </html>
                                            """;

                    showHtml(outStream, 200, html_bienvenida);

                } else if (path.contains("/html")) {      // POST, DELETE o GET
                    if ("POST".equalsIgnoreCase(method)) {
                        int contentLength = 0;
                        String headerLine;
                        while (!(headerLine = reader.readLine()).isEmpty()) {
                            if (headerLine.toLowerCase().startsWith("content-length:")) {
                                contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                            }
                        }

                        char[] bodyChars = new char[contentLength];
                        int totalRead = 0;
                        while (totalRead < contentLength) {
                            int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                            if (read == -1) break;
                            totalRead += read;
                        }
                        if (totalRead != contentLength) {
                            showHtml(outStream, 400, "Body incompleto");
                            return;
                        }

                        String body = new String(bodyChars); // contenido tal cual llega

                        String decodedBody = java.net.URLDecoder.decode(body, StandardCharsets.UTF_8);

                        Map<String, String> formParams = new HashMap<>();
                        for (String pair : decodedBody.split("&")) {
                            String[] kv = pair.split("=", 2);
                            if (kv.length == 2) {
                                formParams.put(kv[0], kv[1]); // ya está decodificado
                            }
                        }

                        String htmlContent = formParams.get("html");
                        if (htmlContent == null || htmlContent.isEmpty()) {
                            showHtml(outStream, 400, "Missing 'html' parameter.");
                        } else {
                            String uuid = java.util.UUID.randomUUID().toString();
                            pages.put(uuid, htmlContent);

                            String response = String.format(
                                    "<!DOCTYPE html><html><body>Página creada. <a href='/html?uuid=%s'>%s</a>"
                                            + insertFooter() + "</body></html>",
                                    uuid, uuid
                            );

                            showHtml(outStream, 200, response);
                        }
                    } else if ("GET".equalsIgnoreCase(method)) {
                        String query = "";
                        int qIndex = path.indexOf("?");
                        if (qIndex != -1)
                            query = path.substring(qIndex + 1);

                        Map<String, String> params = new HashMap<>();
                        if (!query.isEmpty()) {
                            for (String pair : query.split("&")) {
                                String[] kv = pair.split("=");
                                if (kv.length == 2)
                                    params.put(kv[0], kv[1]);
                            }
                        }

                        if (params.containsKey("uuid")) {
                            String id = params.get("uuid");
                            String page = pages.get(id);
                            if (page != null) {
                                showHtml(outStream, 200, page);
                            } else {
                                showHtml(outStream, 404, "Página no encontrada");
                            }
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append(
                                    "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Listado</title></head><body>");
                            sb.append("<h1>Páginas HTML almacenadas</h1><ul>");
                            for (String id : pages.keySet()) {
                                sb.append("<li><a href='/html?uuid=").append(id).append("'>").append(id).append("</a></li>");
                            }
                            sb.append("</ul>");
                            sb.append(insertFooter());
                            sb.append("</body></html>");

                            String body = sb.toString();
                            showHtml(outStream, 200, body);
                        }
                    } else if("DELETE".equalsIgnoreCase(method)){
                        // Extraer query string (?uuid=...)
                        String query = "";
                        int qIndex = path.indexOf("?");
                        if (qIndex != -1) {
                            query = path.substring(qIndex + 1);
                        }

                        // Parsear parámetros
                        Map<String, String> params = new HashMap<>();
                        if (!query.isEmpty()) {
                            for (String pair : query.split("&")) {
                                String[] kv = pair.split("=", 2);
                                if (kv.length == 2) {
                                    String key = java.net.URLDecoder.decode(kv[0], "UTF-8");
                                    String value = java.net.URLDecoder.decode(kv[1], "UTF-8");
                                    params.put(key, value);
                                }
                            }
                        }

                        String uuid = params.get("uuid");
                        String response;
                        if (uuid != null && pages.containsKey(uuid)) {
                            pages.remove(uuid);
                            response = "<html><body><h1>Página con UUID " + uuid + " eliminada correctamente.</h1></body></html>";
                        } else {
                            response = "<html><body><h1>No se encontró ninguna página con UUID " + uuid + ".</h1></body></html>";
                        }

                        //  Responder
                        showHtml(outStream, 200, response);
                    }else {
                        showHtml(outStream, 400,  "Operation not permitted");
                    }

                } else {  // Recurso no encontrado
                    showHtml(outStream, 400,  "Parametros incorrectos");
                }

            } catch (HTTPParseException e) {
                e.printStackTrace();
                showHtml(outStream, 400, "Bad Request: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showHtml(outStream, 500, "Error interno del servidor");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showHtml(OutputStream outStream, int code, String message) throws IOException {
        int statusCode;
        String reasonPhrase;
        switch (code) {
            case 200 -> { statusCode = 200; reasonPhrase = "OK"; }
            case 400 -> { statusCode = 400; reasonPhrase = "Bad Request"; }
            case 404 -> { statusCode = 404; reasonPhrase = "Not Found"; }
            case 500 -> { statusCode = 500; reasonPhrase = "Internal Server Error"; }
            default -> { statusCode = code; reasonPhrase = "Error"; }
        }

        String body = (code == 200) ? message : "<html><body><h1>" + statusCode + " " + reasonPhrase + "</h1><p>" + message + "</p>" + insertFooter() + "</body></html>";

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        // Línea de estado
        outStream.write(("HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n").getBytes(StandardCharsets.UTF_8));

        // Cabeceras
        outStream.write(("Content-Length: " + bodyBytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        outStream.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.UTF_8));
        outStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Cuerpo
        outStream.write(bodyBytes);
        outStream.flush();
    }


    private String insertFooter(){
        String footer =
                new StringBuilder().append("<hr><footer><a href=http://${ip_server}:").append(SERVICE_PORT).append(">Volver al inicio</a></footer>").toString();

        return footer.replace("${ip_server}", "localhost");
    }
}
