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
package es.uvigo.esei.dai.hybridserver;

import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class HybridServer implements AutoCloseable {
    private static final int SERVICE_PORT = 8888;
    private Thread serverThread;
    private boolean stop;

    Map<String,String> pages = new ConcurrentHashMap<>();

    public HybridServer() {
        // Iniciar Vacío

    }

    public HybridServer(Map<String, String> pages) {
        this.pages.putAll(pages);
    }

    public HybridServer(Properties properties) {
        // TODO Inicializar con los parámetros recibidos
    }

    public int getPort() {
        return SERVICE_PORT;
    }

    public void start() {
        this.serverThread = new Thread() {
            @Override
            public void run() {
                try (final ServerSocket serverSocket = new ServerSocket(SERVICE_PORT)) {
                    while (true) {
                        try (Socket socket = serverSocket.accept()) {
                            if (stop)
                                break;

                            // TODO Responder al cliente

                            try (
                                    InputStream inStream = socket.getInputStream();
                                    OutputStream outStream = socket.getOutputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream))) {

                                try{ // Try para controlar el manejo de errores
                                    String requestLine = reader.readLine();
                                    if (requestLine == null || requestLine.isEmpty()) {
                                        return; //Petición vacía
                                    }

                                    String[] parts = requestLine.split(" ");
                                    String method = parts[0];
                                    String path = parts[1];

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

                                    } else if (path.contains("/html")) {      //Tres opciones, mostrar página creada, página concreta o lista

                                        // Hay que mirar si recibe un POST
                                        if("POST".equalsIgnoreCase(method)) {
                                            // Leer las cabeceras para obtener Content-Length
                                            int contentLength = 0;
                                            String headerLine;
                                            while (!(headerLine = reader.readLine()).isEmpty()) {
                                                if (headerLine.toLowerCase().startsWith("content-length:")) {
                                                    contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                                                }
                                            }

                                            // Leer body según Content-Length
                                            char[] bodyChars = new char[contentLength];
                                            reader.read(bodyChars, 0, contentLength);
                                            String body = new String(bodyChars);

                                            // Parsear body form-urlencoded
                                            Map<String, String> formParams = new HashMap<>();
                                            for (String pair : body.split("&")) {
                                                String[] kv = pair.split("=", 2);
                                                if (kv.length == 2) {
                                                    String key = java.net.URLDecoder.decode(kv[0], "UTF-8");
                                                    String value = java.net.URLDecoder.decode(kv[1], "UTF-8");
                                                    formParams.put(key, value);
                                                }
                                            }

                                            String htmlContent = formParams.get("html");
                                            if (htmlContent == null || htmlContent.isEmpty()) {
                                                showHtml(outStream, 400, "Missing 'html' parameter.");
                                            } else {
                                                // Generar UUID y guardar en pages
                                                String uuid = java.util.UUID.randomUUID().toString();
                                                pages.put(uuid, htmlContent);

                                                // Responder con enlace a la página creada
                                                String response = String.format(
                                                        "<!DOCTYPE html><html><body>Página creada. <a href='/html?uuid=%s'>%s</a>" + insertFooter() + "</body></html>",
                                                        uuid, uuid);

                                                showHtml(outStream, 200, response);
                                            }

                                        } else if("DELETE".equalsIgnoreCase(method)){ // En caso de recibir un delete
                                            // Extraer query String (?uuid=codigo)
                                            String query = "";
                                            int qIndex = path.indexOf("?");
                                            if(qIndex != -1){
                                                query = path.substring(qIndex+1);
                                            }

                                            // Parsear parámetros
                                            Map<String, String> formParams = new HashMap<>();
                                            if(!query.isEmpty()){
                                                for(String pair : query.split("&")){
                                                    String[] kv = pair.split("=", 2);
                                                    if(kv.length == 2){
                                                        String key = java.net.URLDecoder.decode(kv[0], "UTF-8");
                                                        String value = java.net.URLDecoder.decode(kv[1], "UTF-8");
                                                        formParams.put(key, value);
                                                    }
                                                }
                                            }

                                            String uuid = formParams.get("uuid");
                                            String response;

                                            if(uuid != null && pages.containsKey(uuid)){
                                                pages.remove(uuid);
                                                response = "<html><body><h1>Página con UUID " + uuid + " eliminada correctamente.</h1>" + insertFooter() + "</body></html>";
                                            }else{
                                                response = "<html><body><h1>No se encontró ninguna página con UUID " + uuid + ".</h1>" + insertFooter() + "</body></html>";
                                            }

                                            // Responder
                                            showHtml(outStream, 200, response);

                                        } else if ("GET".equalsIgnoreCase(method)) {
                                            // Sacar parámetros si existen
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

                                            if (params.containsKey("uuid")) {   // Página concreta
                                                String id = params.get("uuid");
                                                String page = pages.get(id);
                                                if (page != null) {
                                                    showHtml(outStream, 200, page);
                                                } else { // Página no encontrada
                                                    showHtml(outStream, 404, "Página no encontrada");
                                                }
                                            } else { //Listado de todas las páginas
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
                                        }else { // En caso de que no sea ni POST ni DELETE ni GET
                                            showHtml(outStream, 400,  "Operation not permited");
                                        }

                                    } else {  // Recurso no encontrado
                                       showHtml(outStream, 400,  "Parametros incorrectos");
                                    }
                                }catch(Exception e){ // Cualquier error en el servidor, salta esto
                                    e.printStackTrace();
                                    showHtml(outStream, 500, "Error interno del servidor");
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        this.stop = false;
        this.serverThread.start();
    }

    @Override
    public void close() {
        // TODO Si es necesario, añadir el código para liberar otros recursos.
        this.stop = true;

        try (Socket socket = new Socket("localhost", SERVICE_PORT)) {
            // Esta conexión se hace, simplemente, para "despertar" el hilo servidor
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.serverThread = null;
    }

    private void showHtml(OutputStream outStream, int code, String message) throws IOException {
        String status;
        String body;
        switch (code) {
            case 200 -> status = "OK";
            case 400 -> status = "400 Bad Request";
            case 404 -> status = "404 Not Found";
            case 500 -> status = "500 Internal Server Error";
            default -> status = code + " Error";
        }
        if(code == 200){
            body = message;
        }else{
            body = "<html><body><h1>" + status + "</h1><p>" + message + "</p>" + insertFooter() + "</body></html>";
        }

        outStream.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        outStream.write(("Content-Length: " + body.getBytes().length + "\r\n").getBytes());
        outStream.write("Content-Type: text/html; charset=UTF-8\r\n".getBytes());
        outStream.write("\r\n".getBytes());
        outStream.write(body.getBytes());
        outStream.flush();
    }

    private String insertFooter(){
        String footer = // Para reutilizar en la lista de secciones, a la hora de generar el id y a la hora de mostrar una sección
                new StringBuilder().append("<hr><footer><a href=http://${ip_server}:").append(SERVICE_PORT).append(">Volver al inicio</a></footer>").toString();

        return footer.replace("${ip_server}", "localhost");
    }
}
