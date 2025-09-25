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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Launcher {
  public static void main(String[] args) {
    // TODO Ejecutar el servidor

    Map<String,String> pages = new ConcurrentHashMap<>();

    String plantilla_html =
            """
            <!DOCTYPE html>
            <html>
              <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${uuid}</title>
              </head>
              <body>
                <h1>Estas en la página con uuid: ${uuid}</h1>
              </body>
            </html>
            """;

    for(int i = 0; i < 5; i++){ // Creamos 5 uuid (5 secciones distintas)
        String uuid = UUID.randomUUID().toString();
        String pagina = plantilla_html.replace("${uuid}", uuid); // Sustituimos "${uuid}" por el uuid que se generó
        pages.put(uuid, pagina);
    }
    final HybridServer server = new HybridServer(pages);

    server.start();
  }
}
