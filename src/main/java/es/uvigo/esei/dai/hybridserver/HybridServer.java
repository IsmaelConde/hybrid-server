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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

public class HybridServer implements AutoCloseable {
  private static final int SERVICE_PORT = 8888;
  private Thread serverThread;
  private boolean stop;

  public HybridServer() {
    // TODO Inicializar con los parámetros por defecto
  }

  public HybridServer(Map<String, String> pages) {
    // TODO Inicializar con la base de datos en memoria conteniendo "pages"
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
              String html_bienvenida =
                      """
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
                        </body>
                      </html>
                      """;
              OutputStream outStream = socket.getOutputStream();

              int len = html_bienvenida.length();
              // Parte cabecera
              outStream.write("HTTP/1.1 200 OK\r\n".getBytes()); // Escribimos cabecera. Hay que pasaralo a bytes, ya que es lo que maneja el socket
              outStream.write(("Content-Length: "+len+"\r\n").getBytes());
              outStream.write("Content-Type: text/html\r\n".getBytes());
              outStream.write("\r\n".getBytes());
              // Fin parte cabecera

              outStream.write(html_bienvenida.getBytes());
              outStream.flush();
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
