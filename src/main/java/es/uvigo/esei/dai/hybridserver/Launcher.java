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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Launcher {
  public static void main(String[] args) {
    // TODO Ejecutar el servidor
      String name_file_config = "config.conf"; // Se usa el por defecto
      Properties prop = new Properties();

      if(args.length == 0) { // No se le pasan parametros
          System.out.println("Usando por defecto: \"" + name_file_config + "\".");
      } else if(args.length > 1){
          System.out.println("Formato incorrecto. Se debe ejecutar así:");
          System.out.println("java es.uvigo.esei.dai.hybridserver.Launch <nombre_configuracion>");
          System.exit(0); // Finalizamos la ejecución
      }else{ // Se le pasa un parametro
          name_file_config = args[0];
      }

      try(FileInputStream file = new FileInputStream(name_file_config)){ // Cargamos el fichero de las propiedades
            prop.load(file); // Lo cargamos como propiedades
      } catch (Exception e) {
          e.printStackTrace();
      }

      HybridServer server = null;
      try{
          server = new HybridServer(prop);
          server.start();
      }catch (Exception e){
          e.printStackTrace();
      }
  }
}
