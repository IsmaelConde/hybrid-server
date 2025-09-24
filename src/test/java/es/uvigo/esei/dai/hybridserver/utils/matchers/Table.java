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
package es.uvigo.esei.dai.hybridserver.utils.matchers;

import java.util.LinkedList;
import java.util.List;

public class Table {
  private final String schema;
  private final String name;
  private final List<Column> columns;

  Table(String schema, String name) {
    this.schema = schema;
    this.name = name;
    this.columns = new LinkedList<>();
  }

  public Column withColumn(String name) {
    return new Column(this, name);
  }

  void addColumn(Column column) {
    this.columns.add(column);
  }

  String getSchema() {
    return schema;
  }

  String getName() {
    return name;
  }

  String getQualifiedName() {
    return this.schema + "." + this.getName();
  }

  List<Column> getColumns() {
    return columns;
  }
}
