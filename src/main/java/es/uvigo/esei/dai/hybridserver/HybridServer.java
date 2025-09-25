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
                                                <h2>Autores:</h2>
                                                <p>Daniel Domínguez Rosales</p>
                                                <p>Ismael Conde Conde</p>
                                                
                                                <h3>Enlaces:</h3>
                                                    <ul>
                                                      <li><a href="/htm">Ver listado de páginas HTML</a></li>
                                                    </ul>
                                              </body>
                                            </html>
                                            """;
                                    int len = html_bienvenida.length();
                                    outStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                                    outStream.write(("Content-Length: " + len + "\r\n").getBytes());
                                    outStream.write("Content-Type: text/html\r\n".getBytes());
                                    outStream.write("\r\n".getBytes());
                                    outStream.write(html_bienvenida.getBytes());
                                    outStream.flush();

                                } else if (path.startsWith("/htm")) {      //Dos opciones, mostrar página concreta o lista

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

                                    if (params.containsKey("uuid")) {   //Página concreta
                                        String id = params.get("uuid");
                                        String page = pages.get(id);
                                        if (page != null) {
                                            outStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                                            outStream.write(("Content-Length: " + page.length() + "\r\n").getBytes());
                                            outStream.write("Content-Type: text/html\r\n".getBytes());
                                            outStream.write("\r\n".getBytes());
                                            outStream.write(page.getBytes());
                                            outStream.flush();
                                        } else {        //Página no encontrada
                                            String body = "<h1>404 Not Found</h1>";
                                            outStream.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                                            outStream.write(("Content-Length: " + body.length() + "\r\n").getBytes());
                                            outStream.write("Content-Type: text/html\r\n".getBytes());
                                            outStream.write("\r\n".getBytes());
                                            outStream.write(body.getBytes());
                                            outStream.flush();
                                        }
                                    } else {        //Listado de todas las páginas
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(
                                                "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Listado</title></head><body>");
                                        sb.append("<h1>Páginas HTML almacenadas</h1><ul>");
                                        for (String id : pages.keySet()) {
                                            sb.append("<li><a href='/htm?uuid=").append(id).append("'>").append(id).append("</a></li>");
                                        }
                                        sb.append("</ul></body></html>");

                                        String body = sb.toString();
                                        outStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                                        outStream.write(("Content-Length: " + body.length() + "\r\n").getBytes());
                                        outStream.write("Content-Type: text/html\r\n".getBytes());
                                        outStream.write("\r\n".getBytes());
                                        outStream.write(body.getBytes());
                                        outStream.flush();
                                    }
                                } else {        //Recurso no encontrado
                                    String body = "<h1>404 Not Found</h1>";
                                    outStream.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                                    outStream.write(("Content-Length: " + body.length() + "\r\n").getBytes());
                                    outStream.write("Content-Type: text/html\r\n".getBytes());
                                    outStream.write("\r\n".getBytes());
                                    outStream.write(body.getBytes());
                                    outStream.flush();
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
}
