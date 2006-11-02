// ----------------------------------------------------------------------------
// Copyright 2006, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql;

import org.opendmtp.dbtools.DBAdmin;
import org.opendmtp.server.base.DMTPServer;
import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;
import org.opendmtp.util.RTKey;

/**
 * Configure the server using MySQL to store data.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 */
public class Main {

  /** Version number of the OpenDMTP server. */
  public static final String DMTP_VERS = "1.1.4";

  private static final String DMTP_NAME = "OpenDMTP";
  private static final String DMTP_TYPE = "Server(MySQL)";

  /** Full version name of the OpenDMTP server. */
  public static final String DMTP_VERSION = DMTP_NAME + "_" + DMTP_TYPE + "." + DMTP_VERS;

  /** Copyright information about this implementation. */
  public static final String COPYRIGHT = "Copyright 2006, Martin D. Flynn";

  /** RTConfig start property. */
  public static final String ARG_START = "start";

  /** RTConfig port property. */
  public static final String ARG_PORT = "port";

  private static final int DEFAULT_DATA_PORT = 31000;

  /**
   * Get the port number from RTConfig. If that property is not initialized, search the property
   * stack for the DMTP port. Finally, if that cannot be found, use DEFAULT_DATA_PORT.
   * 
   * @return The port number to be used.
   */
  private static int _serverPort() {
    int port = RTConfig.getInt(ARG_PORT, -1);
    if (port <= 0) {
      int p = RTConfig.getInt(RTKey.DMTP_PORT);
      port = (p > 0) ? p : DEFAULT_DATA_PORT;
    }
    return port;
  }

  /**
   * Displays usage information about the class.
   */
  private static void usage() {
    Print.logInfo("");
    Print.logInfo("Usage:");
    Print.logInfo("  java ... " + Main.class.getName() + " {options}");
    Print.logInfo("Options:");
    Print.logInfo("  [-port]  Server port to listen for TCP/UDP connections [default="
        + _serverPort() + "]");
    Print.logInfo("  -start   Start server on specified port");
    Print.logInfo("");
    System.exit(1);
  }

  /**
   * Configures the server using MySQL data store with options from the command line. Starts the
   * server using the port from the _serverPort() method.
   * <p>
   * System exits with an error if the MySQL database has not been properly initialized.
   * 
   * @param argv Command line options to pass to the configuration.
   */
  public static void main(String argv[]) {

    /* configure server for MySQL data store */
    DBConfig.init(argv, false);

    /* header */
    Print.logInfo("OpenDMTP Java Server Reference Implementation.");
    Print.logInfo("Version: " + DMTP_VERSION);
    Print.logInfo(COPYRIGHT);

    /* make sure the DB is properly initialized */
    if (!DBAdmin.verifyTablesExist()) {
      Print.logFatal("MySQL database has not yet been properly initialized");
      System.exit(1);
    }

    /* start server */
    if (RTConfig.getBoolean(ARG_START, false)) {
      try {
        DMTPServer.createTrackSocketHandler(_serverPort());
      }
      catch (Throwable t) { // trap any server exception
        Print.logError("Error: " + t);
      }
      /* wait here forever while the server is running in a thread */
      while (true) {
        try {
          Thread.sleep(60L * 60L * 1000L);
        }
        catch (Throwable t) {
        }
      }
    }

    /* display usage */
    usage();

  }

}
