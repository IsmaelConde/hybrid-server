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
package es.uvigo.esei.dai.hybridserver.step1;

import static es.uvigo.esei.dai.hybridserver.utils.TestUtils.getContentWithType;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import es.uvigo.esei.dai.hybridserver.utils.HybridServerTestCase;

public class WelcomePageTest extends HybridServerTestCase {

  @Test
  public void testWelcome() throws IOException {
    for (int i = 0; i < 10; i++) {
      assertThat(getContentWithType(url, "text/html"), containsString("Hybrid Server"));
    }
  }
}
