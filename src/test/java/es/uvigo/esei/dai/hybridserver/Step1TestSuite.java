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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import es.uvigo.esei.dai.hybridserver.step1.HTTPRequestResponseSuite;
import es.uvigo.esei.dai.hybridserver.step1.WelcomePageTest;

@Suite
@SelectClasses({
  WelcomePageTest.class,
  HTTPRequestResponseSuite.class
})
public class Step1TestSuite {

}
