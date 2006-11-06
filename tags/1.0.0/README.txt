-----------------------------------------------------------------------------------
Project: OpenDMTP Reference Implementation - Java server
URL    : http://www.opendmtp.org
File   : README.txt
-----------------------------------------------------------------------------------

This is version v1.1.4 update to the OpenDMTP Java server protocol reference 
implementation.  This release contains the following major changes:
- Now includes a build target for creating a Servlet WAR file for delivering CSV, 
  or KML(XML) formatted records over the web.  KML formatted records can be 
  imported directly into Google Earth (which also automatically retrieve periodic 
  updates to the data) so that as new records come in to the MySQL database, they 
  will automatically be updated in Google Earth.  The 'Events' Servlet needs to
  run in a Servlet container, such as Tomcat, or JBoss.
- An OpenDMTP server is now available on the 'net for selected development efforts.
  Let us know if you are interested in using this server as a test-bed, and what
  type of project you are working on.  We may set up an account for you.  Let us
  help you with your GPS tracking project.

README Contents:
   1) Introduction
   2) Supported Platforms
   3) QuickStart Build/Installation
   4) Installing/Running the 'events.war' Servlet
   5) Source Package Layout
   6) Future OpenDMTP Server Reference Implementation Features
   7) Contact Info
  
-----------------------------------------------------------------------------------
1) Introduction:

The "Open Device Monitoring and Tracking Protocol", otherwise known as OpenDMTP(tm), 
is a protocol and framework that allows bi-directional data communications between 
servers and client devices over the Internet and similar networks.  OpenDMTP is 
particularly geared towards location-based information, such as GPS, as well as 
other types of data collected in remote-monitoring devices. OpenDMTP is small, and 
is especially suited for micro-devices such as PDA's, mobile phones, and custom OEM
devices.

This server reference implementation is intended to be used as a developer kit for 
creating customized feature-rich back-end services for the OpenDMTP device clients.
This OpenDMTP server reference implementation is divided into 2 parts: the OpenDMTP
protocol support, and the data store.  

The OpenDMTP protocol support is a full featured reference implementation and 
includes the following features:
  - Supports both TCP (Duplex) and UDP (Simplex) connections from the client.
  - Full support of client custom event packet negotiation.
  - Supports multiple accounts and devices.
  - Supports connection accounting.
  - Support for both Flat-file and MySQL data store.

This reference implementaion supports two types of data storage schemes:
 1) A very simple flat-file, CSV format data store, which is very simple to set up,
    but does not take advantage of all the features the protocol can provide.  If a 
    client device attaches to this server to send data, the data store simply accepts
    the events and stores them in a file named after the incoming account and device
    (received events will be appended to a file named after the owning account and 
    device in the format "<Account>_<Device>.csv").  This simplistic flat-file data 
    store imposes the following restrictions:
      - Limited support of client custom event packet negotiation (all devices will
        utilize the same PacketTemplate cache, and the cache is reset each time the
        server is restarted).
      - Does not provide connection accounting information.
      - Does not provide a scalable solution and should not be used to collect data 
        for more than just a few devices.
 2) A MySQL data store which takes full advantage of all the features of the 
    protocol. (Future releases may include a more generic DB data storage system, 
    such as "Hibernate").

Once the data is stored in the chosen datastore, it is then up to the application
developer to create a means of presenting the data to a user, such as through a web
interface with various types of reports, and a map.

Documentation is included with this release for setting up a very simple static web 
page using Google Maps to provide web-based display and mapping of received data.  
For more information see the file "webserve/WEBSERVE.txt".

-----------------------------------------------------------------------------------
2) Supported Platforms:

This reference implementation is completely implemented in Java and should run on 
any system that fully supports the Java Runtime Environment.  However, this 
implementation does support MySQL, and is therefore limited to systems on which MySQL
runs (see the MySQL website for supported systems at "http://www.mysql.org").  The 
server, with a MySQL datastore, has been tested on both Linux and Windows-XP systems.

-----------------------------------------------------------------------------------
3) QuickStart Build/Installation:

Depending on your chosen type of datastore, refer to section "A" below for building 
and configuring OpenDMTP with the Flat-file datastore, and refer to section "B" below 
for building and configuring OpenDMTP with the MySQL datastore.

Compiling Prerequisites:
In addition to the tools needed for the specific datastore in use, compiling the
OpenDMTP Java server reference implementation requires that the following packages
or application be installed, configured, and running of the local system:
  - Java SDK v1.4+  [http://java.sun.com/j2se]
  - Ant v1.6.5+ [http://ant.apache.org/]

[Note: OpenDMTP server commands referenced below must be executed from the OpenDMTP
installation directory.  Each server command is provided in a Linux version (ending 
with '.sh'), and a Windows-XP version (ending with '.bat'). The Linux version of
the server commands are referenced below.  Window-XP commands must have arguments 
enclosed in quotes (eg.> bin\initdb.bat "-rootUser=root"), or the options may be
specified with ':', instead of '=' (eg.> bin\initdb.bat -rootUser:root).]

A) Compiling/Running the OpenDMTP server with the Flat-file datastore:
  1) Unzip the OpenDMTP Java server package in a convenient directory.
  2) Compile the Java application using the supplied Ant 'build.xml' script:
      % ant filestore
  3) Start the server:
      % bin/server_file.sh
     The server will initialize and start listening on port 31000 (default) for
     TCP & UDP connections. Received data will be stored in the directory "./data".
     The default service port number can be overridden in the runtime config file 
     "default.conf" if necessary. The default file store directory can be overidden
     on the command line (eg. "-storedir=<directory>").

B) Compiling/Running the OpenDMTP server with the MySQL datastore:
  1) In addition to the above specified tools, download and install version 5.0 of
     the MySQL server for your platform [from http://www.mysql.org].
  2) Also download and install the MySQL Connector/J v3.1.X JDBC driver.  Place the
     Connector/J jar file "mysql-connector-java-3.1.X-bin.jar" into the Java JRE
     directory "$JAVA_HOME/jre/lib/ext/.".
  3) Unzip the OpenDMTP Java server package in a convenient directory.
  4) Compile the Java application using the supplied Ant 'build.xml' script:
      % ant sqlstore
  5) Create/Initialize the "dmtp" database for the OpenDMTP server:
      % bin/initdb.sh [-rootUser=<rootUser> [-rootPass=<rootPass>]]
     Where <rootUser> is the user with root access to MySQL, and <rootPass> is the
     root user password (may be optional).
     This command performs the following steps:
        a) Creates a database called 'dmtp'.
        b) Creates/Grants user 'dmtp' with password 'opendmtp' with access to the
           'dmtp' database.
        c) Creates the following tables in the 'dmtp' database:
           - Account        - Account owner table
           - Device         - Device information table
           - EventTemplate  - Custom event packet templates
           - EventData      - Received Event data
        d) Creates a sample default account called "opendmtp" with a single device
           called "mobile" (with default field values). (The commands 'adminAccount'
           and 'adminDevice' can be used to edit specific field values within these
           records).
  6) Start the server:
      % bin/server_mysql.sh
     The server will initialize and start listening on port 31000 (default) for
     TCP & UDP connections. The default service port number, as well as the default
     MySQL database name and user (specified in 'DBConfig.java'), can be overridden 
     in the the runtime config file "default.conf" if necessary.
  7) Events can be extracted from the MySQL database with the 'bin/adminDevice'
     command.  To extract the latest 10 events from the database, issue the
     following command (if not specified, the default 'limit' is 30):
        % bin/adminDevice -account=opendmtp -device=mobile -events=10 -format=csv -output=data.csv
     The events will be printed to file "data.csv" in CSV format at follows:
         Date,Time,Code,Latitude,Longitude,Speed,Heading,Altitude
         2006/04/03,03:59:15,Stopped,26.17161,-102.13527,0.0,0.0,551.0
         ...
         2006/04/03,03:59:16,Waymark,26.17161,-102.13527,0.0,0.0,552.0
     This file can be loaded and viewed in Microsoft MapPoints.
  8) Events can also be extracted from the MySQL "Device" table in Google Earth's
     "kml" (XML) format by specifying the output format as "-format=kml".  The 
     resulting file can then be loaded into Google Earth and viewed on the map.

Note on the use of the JavaMail API:
This reference implementation also includes support for using the JavaMail api to
send email based on received events.  For instance, the JavaMail api can be used to
send email notification when a particular remote device has sent an "Arrival" event
to the server.  In this release, the "org/opendmtp/util/SendMail.java" has been
temporarily renamed to "SendMail.java.save" to allow the compile to continue without
the JavaMail support installed.  Rename the source file back to "SendMail.java" 
after the JavaMail api has been installed to be able to implement your own set of
device event rules and email notification.  

The JavaMail api can be downloaded from the Sun at the following location:
   http://java.sun.com/products/javamail/index.jsp
The "mail.jar" and "activation.jar" files from this package will need to be
installed in your Java library directory:  $JAVA_HOME/jre/lib/ext/

-----------------------------------------------------------------------------------
4) Installing/Running the 'events.war' Servlet

The 'events.war' runs in a Java Servlet container and works with the MySQL DB 
datastore to allow downloading selected portions of a sequence of events over the 
web.  This can be used with web-based mapping applications to provide near real-time 
tracking of a vehicle or person.  The 'events.war' servlet currently supports data 
retrieval in KML or CSV file formats and can be used in mapping programs such as 
Google Earth, or MS MapPoints.

  1) In addition to the tools needed for the MySQL DB datastore server, download
     and install the Apache Tomcat servlet container.  Following the installation
     instructions on the Apache Tomcat website for configuration on your particular
     system.
  2) The MySQL Connector/J v3.1.X JDBC driver downloaded previously must also be 
     copied to the Tomcat directory "$CATALINA_HOME/common/lib/.". 
  3) To compile the 'events.war' servlet, this Ant build script (build.xml)
     expects to find the symbolic link 'Tomcat' in the OpenDMTP server installation
     directory which points to the Tomcat installation directory (typically the
     same as what $CATALINA_HOME will be set to).  
  4) Compile the 'events.war' Servlet using the supplied Ant 'build.xml' script:
      % ant events.war
     This will build the Servlet war file "./build/events.war".
  5) Install the 'events.war' server per the Tomcat installation/configuration
     instructions.
  6) Access the data stored in the MySQL DB via the web with the following URL:
       http[s]://<mydomainname>/events/<file>.[kml|csv]
            ?a=<account> - the account nmae
            &p=<passwd>  - the account password
            &d=<device>  - the device nmae
            [&r=<range>] - optional date/time range specification.  The range
                           is specified in the format "<fromTime>/<toTime>/<limit>"
                           where <fromTime> and <toTime> are specified in POSIX
                           'Epoch' time (number of seconds since midnight Jan 1
                           1970).  If not specified, the last 30 events will be
                           returned.
     Configuration and use of 'https' (ie. SSL) is highly recommended as the URL
     includes the account password and will be encrypted via 'https', but will be
     sent in the clear if plain 'http' is used.
     Some examples:
       1) https://example.com/events/data.csv?a=opendmtp&p=mypass&d=mobile
          Return a CSV formatted data file ('data.csv') containing the last 30
          event record for the device 'opendmtp'/'mobile'.  The data is returned
          via a http SSL connection.
       2) http://example.com/events/data.csv?a=dmtp&d=dev&r=1145776000/1145777000/10
          Return the virst 10 events within the specified range for the device
          "dmtp/dev".
          
Google Earth has the capability of automatically polling data from this URL at
specified intervals.  To configure Google Earth to read out event data points from
our server, click on "Add" on the main menu bar, then select "Network Link".  Add
the URL to the server (as specified above) and click "Refresh Parameters" to be able
to enter periodic refresh times.

-----------------------------------------------------------------------------------
5) Source Package Layout:

org.opendmtp.util
    Miscellaneous utilities
    
org.opendmtp.dbtools
    MySQL database tools/utilities

org.opendmtp.server
    Server protocol support specific implementation
    
org.opendmtp.server.base
    Basic DMTP protocol support
    
org.opendmtp.server.codes
    DMTP protocol constants/codes

org.opendmtp.server.db
    Interface definitions for use by backend data store implementation.
    
org.opendmtp.server_file
    Main entry point and implementation of flat-file event data store.
    
org.opendmtp.server_mysql
org.opendmtp.server_mysql.db
org.opendmtp.server_mysql.dbtypes
    Main entry point and implementation of MySQL event data store.

org.opendmtp.war
org.opendmtp.war.events
org.opendmtp.war.tools
    'events.war' Java Servlet.

-------------------------------------------------------------------------------
6) Future OpenDMTP Server Reference Implementation Features:

The following items are also in the works for future OpenDMTP server support:
  - Support for "Hibernate" backend DB data store.
  - Web service for displaying GPS information.
  - Etc!

-------------------------------------------------------------------------------
7) Contact Info:

Please feel free to contact us regarding questions on this package.  Or if you
would like to be be kept up to date on the progress of this project, please let
us know.

Thanks,
Martin D. Flynn
devstaff@opendmtp.org
