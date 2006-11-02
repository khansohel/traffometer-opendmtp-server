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
// Description:
//  Template for general server socket support
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Vector;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Socket thread on the server side. Contains datagram socket, server socket, client threads pool.
 * 
 * @author Martin D. Flynn
 * @author Alexey Olkov
 * 
 */

public class ServerSocketThread extends Thread {

  // ------------------------------------------------------------------------
  // References:
  // http://tvilda.stilius.net/java/java_ssl.php
  // http://www.jguru.com/faq/view.jsp?EID=993651

  // ------------------------------------------------------------------------
  // SSL:
  // keytool -genkey -keystore <mySrvKeystore> -keyalg RSA
  // Required Properties:
  // -Djavax.net.ssl.keyStore=<mySrvKeystore>
  // -Djavax.net.ssl.keyStorePassword=<123456>
  // For debug, also add:
  // -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol
  // -Djavax.net.debug=ssl
  // ------------------------------------------------------------------------

  /**
   * Datagram socket.
   */
  private DatagramSocket datagramSocket = null;
  /**
   * Server socket.
   */
  private ServerSocket serverSocket = null;
  /**
   * Pool of clients threads.
   */
  private Vector clientThreadPool = null;
  /**
   * Client packets handler.
   */
  private ClientPacketHandler clientPacketHandler = null;
  /**
   * The class of cliend packet handler.
   */
  private Class clientPacketHandlerClass = null;

  /**
   * Session timeout.
   */
  private long sessionTimeoutMS = -1L;
  /**
   * Idle time-out in milliseconds.
   */
  private long idleTimeoutMS = -1L;
  /**
   * Packets time-out in milliseconds.
   */
  private long packetTimeoutMS = -1L;
  /**
   * Linger time-out in seconds.
   */
  private int lingerTimeoutSec = 4; // SO_LINGER timeout is in *Seconds*
  /**
   * Maximum length of read.
   */
  private int maxReadLength = -1;
  /**
   * Miminum length of read.
   */
  private int minReadLength = -1;

  /**
   * Indicates if the process should be terminated on time-out.
   */
  private boolean terminateOnTimeout = true;
  /**
   * Indicates if the packets are textual.
   */
  private boolean isTextPackets = true;
  /**
   * The termination characters.
   */
  private int lineTerminatorChar[] = new int[] { '\n' };
  /**
   * The backspace characters.
   */

  private int backspaceChar[] = new int[] { '\b' };
  /**
   * The characters to ignore.
   */
  private int ignoreChar[] = new int[] { '\r' };
  /**
   * Prompt.
   */
  private byte prompt[] = null;
  /**
   * Prompt index.
   */
  private int promptIndex = -1;
  /**
   * Autoprompting.
   */
  private boolean autoPrompt = false;
  /**
   * A vector of action listeners.
   */
  private Vector actionListeners = null;

  // ------------------------------------------------------------------------
  /**
   * Creates a new thread.
   */
  private ServerSocketThread() {
    this.clientThreadPool = new Vector();
    this.actionListeners = new Vector();
  }

  /**
   * Creates a new thread with a specified datagram socket.
   * 
   * @param ds Datatgram socket
   */
  public ServerSocketThread(DatagramSocket ds) {
    this();
    this.datagramSocket = ds;
  }

  /**
   * Creates a new thread with a specified server socket.
   * 
   * @param ss Server socket
   */
  public ServerSocketThread(ServerSocket ss) {
    this();
    this.serverSocket = ss;
  }

  /**
   * Creates a new thread and a new server socket bound to a specified port.
   * 
   * @param port a port number
   * @throws IOException if an error occured
   */
  public ServerSocketThread(int port) throws IOException {
    this(new ServerSocket(port));
  }

  /**
   * Creates a new thread and a new SSL server socket bound to a specified port.
   * 
   * @param port a prort number
   * @param useSSL true if creating SSL, false otherwise
   * @throws IOException if an error occured
   */
  public ServerSocketThread(int port, boolean useSSL) throws IOException {
    this(useSSL ? SSLServerSocketFactory.getDefault().createServerSocket(port)
        : ServerSocketFactory.getDefault().createServerSocket(port));
  }

  // ------------------------------------------------------------------------
  /**
   * Implements a run() method of the Thread interface.
   */
  public void run() {
    while (true) {
      ClientSocket clientSocket = null;

      /* wait for client session */
      try {
        if (this.serverSocket != null) {
          clientSocket = new ClientSocket(this.serverSocket.accept());
        }
        else if (this.datagramSocket != null) {
          byte b[] = new byte[ServerSocketThread.this.getMaximumPacketLength()];
          ServerSocketThread.this.getMaximumPacketLength();
          DatagramPacket dp = new DatagramPacket(b, b.length);
          this.datagramSocket.receive(dp);
          clientSocket = new ClientSocket(dp);
        }
        else {
          Print.logStackTrace("ServerSocketThread has not been properly initialized");
        }
      }
      catch (IOException ioe) {
        Print.logError("Connection - " + ioe);
        continue; // go back and wait again
      }

      /* ip address */
      String ipAddr;
      try {
        InetAddress inetAddr = clientSocket.getInetAddress();
        ipAddr = (inetAddr != null) ? inetAddr.getHostAddress() : "?";
      }
      catch (Throwable t) {
        ipAddr = "?";
      }

      /* find an available client thread */
      boolean foundThread = false;
      for (Iterator i = this.clientThreadPool.iterator(); i.hasNext() && !foundThread;) {
        ServerSessionThread sst = (ServerSessionThread) i.next();
        foundThread = sst.setClientIfAvailable(clientSocket);
      }
      if (!foundThread) { // add new thread to pool
        // Print.logInfo("New thread for ip ["+ipAddr+"] ...");
        ServerSessionThread sst = new ServerSessionThread(clientSocket);
        this.clientThreadPool.add(sst);
      }
      else {
        // Print.logDebug("Reuse existing thread for ip ["+ipAddr+"] ...");
      }

    }
  }

  /**
   * Indicates if the thread has listeners.
   * 
   * @return true if there are listners, false otherwise.
   */
  public boolean hasListeners() {
    return (this.actionListeners.size() > 0);
  }

  /**
   * Adds action listener.
   * 
   * @param al Action listener
   */
  public void addActionListener(ActionListener al) {
    // used for simple one way messaging
    if (!this.actionListeners.contains(al)) {
      this.actionListeners.add(al);
    }
  }

  /**
   * Removes action listener.
   * 
   * @param al action listener
   */
  public void removeActionListener(ActionListener al) {
    this.actionListeners.remove(al);
  }

  /**
   * Invokes listeners.
   * 
   * @param msgBytes a byte array that may specify a command (possibly one of several) associated
   *        with the event the listenrs listen to.
   * @return true in case there are listeners invoked, false otherwise
   * @throws Exception in case of error
   */
  protected boolean invokeListeners(byte msgBytes[]) throws Exception {
    if (msgBytes != null) {
      String msg = StringTools.toStringValue(msgBytes);
      for (Iterator i = this.actionListeners.iterator(); i.hasNext();) {
        Object alObj = i.next();
        if (alObj instanceof ActionListener) {
          ActionListener al = (ActionListener) i.next();
          ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, msg);
          al.actionPerformed(ae);
        }
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Assignes a client packet handler.
   * 
   * @param cph ClientPacketHandler
   */
  public void setClientPacketHandler(ClientPacketHandler cph) {
    this.clientPacketHandler = cph;
  }

  /**
   * Assigns a class for the client packet handler.
   * 
   * @param cphc ClientPacketHandlerClass
   */
  public void setClientPacketHandlerClass(Class cphc) {
    if ((cphc == null) || ClientPacketHandler.class.isAssignableFrom(cphc)) {
      this.clientPacketHandlerClass = cphc;
      this.clientPacketHandler = null;
    }
    else {
      throw new ClassCastException("Invalid ClientPacketHandler class");
    }
  }

  /**
   * Returns or creates a ClientPacketHandler of the thread.
   * 
   * @return ClientPacketHandler
   */
  public ClientPacketHandler getClientPacketHandler() {
    if (this.clientPacketHandler != null) {
      // single instance
      return this.clientPacketHandler;
    }
    else if (this.clientPacketHandlerClass != null) {
      // new instance
      try {
        return (ClientPacketHandler) this.clientPacketHandlerClass.newInstance();
      }
      catch (Throwable t) {
        Print.logException("ClientPacketHandler", t);
        return null;
      }
    }
    else {
      // not defined
      return null;
    }
  }

  /**
   * Sets session time-out.
   * 
   * @param timeoutMS time-out in milliseconds
   */
  public void setSessionTimeout(long timeoutMS) {
    this.sessionTimeoutMS = timeoutMS;
  }

  /**
   * Returns session time-out.
   * 
   * @return long time-out in milliseconds
   */
  public long getSessionTimeout() {
    return this.sessionTimeoutMS;
  }

  /**
   * Sets idle time-out.
   * 
   * @param timeoutMS idle time-out in milliseconds.
   */
  public void setIdleTimeout(long timeoutMS) {
    this.idleTimeoutMS = timeoutMS;
  }

  /**
   * Returns idle time-out.
   * 
   * @return long time-out in milliseconds
   */
  public long getIdleTimeout() {
    // the timeout for waiting for something to appear on the socket
    return this.idleTimeoutMS;
  }

  /**
   * Sets packet time-out.
   * 
   * @param timeoutMS packet time-out in milliseconds.
   */
  public void setPacketTimeout(long timeoutMS) {
    // once a byte is finally read, the timeout for waiting until the
    // entire packet is finished
    this.packetTimeoutMS = timeoutMS;
  }

  /**
   * Returns packet time-out.
   * 
   * @return long time-out in milliseconds
   */
  public long getPacketTimeout() {
    return this.packetTimeoutMS;
  }

  /**
   * Sets the requirement to terminate on time-out.
   * 
   * @param timeoutQuit true for termination, false otherwise
   */
  public void setTerminateOnTimeout(boolean timeoutQuit) {
    this.terminateOnTimeout = timeoutQuit;
  }

  /**
   * Returns whether the termination on time-out is required.
   * 
   * @return true if termination on time-out is required, false otherwise
   */
  public boolean getTerminateOnTimeout() {
    return this.terminateOnTimeout;
  }

  /**
   * Sets linger time-out.
   * 
   * @param timeoutSec packet time-out in seconds.
   */

  public void setLingerTimeoutSec(int timeoutSec) {
    this.lingerTimeoutSec = timeoutSec;
  }

  /**
   * Returns linger time-out.
   * 
   * @return long time-out in milliseconds
   */
  public int getLingerTimeoutSec() {
    return this.lingerTimeoutSec;
  }

  /**
   * Set if the packets are textual.
   * 
   * @param isText true if the packets are textual, false otherwise
   */
  public void setTextPackets(boolean isText) {
    this.isTextPackets = isText;
    if (!this.isTextPackets()) {
      this.setBackspaceChar(null);
      this.setLineTerminatorChar(null);
      this.setIgnoreChar(null);
    }
  }

  /**
   * Says if the packets are textual.
   * 
   * @return true of the packets are textual, false otherwise
   */
  public boolean isTextPackets() {
    return this.isTextPackets;
  }

  /**
   * Sets the maximum packet length.
   * 
   * @param len packet length
   */
  public void setMaximumPacketLength(int len) {
    this.maxReadLength = len;
  }

  /**
   * Returns the maximum packet length.
   * 
   * @return packet length
   */
  public int getMaximumPacketLength() {
    if (this.maxReadLength > 0) {
      return this.maxReadLength;
    }
    else if (this.isTextPackets()) {
      return 2048; // default for text packets
    }
    else {
      return 1024; // default for binary packets
    }
  }

  /**
   * Sets the maximum packet length.
   * 
   * @param len packet length
   */

  public void setMinimumPacketLength(int len) {
    this.minReadLength = len;
  }

  /**
   * Returns the minumum packet length.
   * 
   * @return packet length
   */
  public int getMinimumPacketLength() {
    if (this.minReadLength > 0) {
      return this.minReadLength;
    }
    else if (this.isTextPackets()) {
      return 1; // at least '\r' (however, this isn't used for text packets)
    }
    else {
      return this.getMaximumPacketLength();
    }
  }

  /**
   * Sets line terminator character.
   * 
   * @param term integer value
   */
  public void setLineTerminatorChar(int term) {
    this.setLineTerminatorChar(new int[] { term });
  }

  /**
   * Sets line terminator characters.
   * 
   * @param term integer array
   */

  public void setLineTerminatorChar(int term[]) {
    this.lineTerminatorChar = term;
  }

  /**
   * Gets line terminator characters.
   * 
   * @return array of line terminating symbols.
   */
  public int[] getLineTerminatorChar() {
    return this.lineTerminatorChar;
  }

  /**
   * Checks if the given character is among the line terminators.
   * 
   * @param ch character
   * @return true if the character is a line terminator, false otherwise
   */
  public boolean isLineTerminatorChar(int ch) {
    if ((this.lineTerminatorChar != null) && (ch >= 0)) {
      for (int i = 0; i < this.lineTerminatorChar.length; i++) {
        if (this.lineTerminatorChar[i] == ch) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Sets backspace character.
   * 
   * @param bs character
   */
  public void setBackspaceChar(int bs) {
    this.setBackspaceChar(new int[] { bs });
  }

  /**
   * Sets an array of backspace characters.
   * 
   * @param bs array of backspace characters.
   */
  public void setBackspaceChar(int bs[]) {
    this.backspaceChar = bs;
  }

  /**
   * Returns the backspace characters.
   * 
   * @return array of backspace characters
   */
  public int[] getBackspaceChar() {
    return this.backspaceChar;
  }

  /**
   * Checks if the given character is among the backspace terminators.
   * 
   * @param ch character
   * @return true if the character is a backspaqce, false otherwise
   */

  public boolean isBackspaceChar(int ch) {
    if (this.hasPrompt() && (this.backspaceChar != null) && (ch >= 0)) {
      for (int i = 0; i < this.backspaceChar.length; i++) {
        if (this.backspaceChar[i] == ch) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Sets an array of ignore characters.
   * 
   * @param bs array of ignore characters.
   */

  public void setIgnoreChar(int bs[]) {
    this.ignoreChar = bs;
  }

  /**
   * Returns the ignore characters.
   * 
   * @return array of ignore characters
   */

  public int[] getIgnoreChar() {
    return this.ignoreChar;
  }

  /**
   * Checks if the given character is among the ignore characters.
   * 
   * @param ch character
   * @return true if the character is an ignore character, false otherwise
   */

  public boolean isIgnoreChar(int ch) {
    if ((this.ignoreChar != null) && (ch >= 0)) {
      for (int i = 0; i < this.ignoreChar.length; i++) {
        if (this.ignoreChar[i] == ch) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Sets autopromrpting.
   * 
   * @param auto true if autopromting is required, false otherwise
   */
  public void setAutoPrompt(boolean auto) {
    if (auto) {
      this.prompt = null;
      this.autoPrompt = true;
    }
    else {
      this.autoPrompt = false;
    }
  }

  /**
   * Sets the prompt.
   * 
   * @param prompt byte array
   */
  public void setPrompt(byte prompt[]) {
    this.prompt = prompt;
    this.autoPrompt = false;
  }

  /**
   * Sets a string prompt.
   * 
   * @param prompt string
   */
  public void setPrompt(String prompt) {
    this.setPrompt(StringTools.getBytes(prompt));
  }

  /**
   * Returns a ndx'th prompt as a byte array.
   * 
   * @param ndx index
   * @return a found prompt as a byte array, or null if no prompt found at a given index
   */
  protected byte[] getPrompt(int ndx) {
    this.promptIndex = ndx;
    if (this.prompt != null) {
      return this.prompt;
    }
    else if (this.autoPrompt && this.isTextPackets()) {
      return StringTools.getBytes("" + (this.promptIndex + 1) + "> ");
    }
    else {
      return null;
    }
  }

  /**
   * Checks if the prompting is on.
   * 
   * @return true if prompting is on, false otherwise.
   */
  public boolean hasPrompt() {
    return (this.prompt != null) || (this.autoPrompt && this.isTextPackets());
  }

  /**
   * Returns an index of prompt.
   * 
   * @return an index
   */
  protected int getPromptIndex() {
    return this.promptIndex;
  }

  /**
   * Client socket.
   * 
   * @autor Martin D. Flynn
   * @author Alexey Olkov
   * 
   */
  private static class ClientSocket {
    private Socket tcpClient = null;
    private DatagramPacket udpClient = null;
    private InputStream bais = null;

    /**
     * Creates Client socket with a specific tcp client.
     * 
     * @param tcpClient tcp client
     */

    public ClientSocket(Socket tcpClient) {
      this.tcpClient = tcpClient;
    }

    /**
     * Creates a client socket for a specific udp client.
     * 
     * @param udpClient actually this is a udp packet
     */
    public ClientSocket(DatagramPacket udpClient) {
      this.udpClient = udpClient;
    }

    /**
     * Checks if the socket deals with tcp.
     * 
     * @return true if a socket deals with tcp, false otherwise
     */
    public boolean isTCP() {
      return (this.tcpClient != null);
    }

    /**
     * Checks if the udp is not null.
     * 
     * @return true if udp client is not null, false otherwise
     */
    public boolean isUDP() {
      return (this.udpClient != null);
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from the input stream without
     * blocking by the next caller of a method for this input stream.
     * 
     * @return the number of bytes that can be read from this input stream without blocking.
     */
    public int available() {
      try {
        return this.getInputStream().available();
      }
      catch (Throwable t) {
        return 0;
      }
    }

    /**
     * Returns the address to which the socket is connected.
     * 
     * @return Returns the address to which the socket is connected or null if the socket is not
     *         connected to any clients.
     */
    public InetAddress getInetAddress() {
      if (this.tcpClient != null) {
        return this.tcpClient.getInetAddress();
      }
      else if (this.udpClient != null) {
        SocketAddress sa = this.udpClient.getSocketAddress();
        if (sa instanceof InetSocketAddress) {
          return ((InetSocketAddress) sa).getAddress();
        }
        else {
          return null;
        }
      }
      else {
        return null;
      }
    }

    /**
     * Returns an output stream for this socket.
     * 
     * @return an output stream for this socket
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket
     *         is not connected
     */
    public OutputStream getOutputStream() throws IOException {
      if (this.tcpClient != null) {
        return this.tcpClient.getOutputStream();
      }
      else {
        return null;
      }
    }

    /**
     * Returns an input stream for this socket.
     * 
     * @return an input stream for this socket
     * @throws IOException if an I/O error occurs when creating the input stream or if the socket is
     *         not connected
     */

    public InputStream getInputStream() throws IOException {
      if (this.tcpClient != null) {
        return this.tcpClient.getInputStream();
      }
      else if (this.udpClient != null) {
        if (bais == null) {
          bais = new ByteArrayInputStream(this.udpClient.getData(), 0, this.udpClient.getLength());
        }
        return bais;
      }
      else {
        return null;
      }
    }

    /**
     * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
     * 
     * @param timeoutSec the specified timeout, in milliseconds.
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * 
     */
    public void setSoTimeout(int timeoutSec) throws SocketException {
      if (this.tcpClient != null) {
        this.tcpClient.setSoTimeout(timeoutSec);
      }
    }

    /**
     * Enable/disable SO_LINGER with the specified linger time in seconds.
     * 
     * @param timeoutSec how long to linger for, if positive, and no linger otherwise
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * 
     */
    public void setSoLinger(int timeoutSec) throws SocketException {
      if (this.tcpClient != null) {
        if (timeoutSec <= 0) {
          this.tcpClient.setSoLinger(false, 0); // no linger
        }
        else {
          this.tcpClient.setSoLinger(true, timeoutSec);
        }
      }
    }

    /**
     * Enable/disable SO_LINGER with the specified linger time in seconds.
     * 
     * @param on whether or not to linger on
     * @param timeoutSec how long to linger for, if positive, and no linger otherwise
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * 
     */
    public void setSoLinger(boolean on, int timeoutSec) throws SocketException {
      if (this.tcpClient != null) {
        if (timeoutSec <= 0) {
          on = false;
        }
        this.tcpClient.setSoLinger(on, timeoutSec);
      }
    }

    /**
     * Closes the socket.
     * 
     * @throws IOException if any I/O errors occured
     */
    public void close() throws IOException {
      if (this.tcpClient != null) {
        this.tcpClient.close();
      }
    }
  }

  /**
   * Server session thread.
   * 
   * @author Martin D. Flynn
   * @author Alexey Olkov
   * 
   */

  public class ServerSessionThread extends Thread {

    private Object runLock = new Object();
    private ClientSocket client = null;
    private long readByteCount = 0L;
    private long writeByteCount = 0L;

    /**
     * Creates a thread for a specified socket, and starts.
     * 
     * @param client Socket for which the thread is started
     */
    public ServerSessionThread(Socket client) {
      super("ClientSession");
      this.client = new ClientSocket(client);
      this.start();
    }

    /**
     * Creates a thread for a specified client socket, and starts.
     * 
     * @param client client socket for which the thread is started
     */

    public ServerSessionThread(ClientSocket client) {
      super("ClientSession");
      this.client = client;
      this.start();
    }

    /**
     * Sets the client socket if client is avaliable.
     * 
     * @param clientSocket a client socket
     * @return false if the client is already used, true otherwise
     */
    public boolean setClientIfAvailable(ClientSocket clientSocket) {
      boolean rtn = false;
      synchronized (this.runLock) {
        if (this.client != null) {
          rtn = false; // not available
        }
        else {
          this.client = clientSocket;
          this.runLock.notify();
          rtn = true;
        }
      }
      return rtn;
    }

    /**
     * Implements the run() method of Thread interface.
     */
    public void run() {

      /* loop forever */
      while (true) {

        /* wait for client (if necessary) */
        synchronized (this.runLock) {
          while (this.client == null) {
            try {
              this.runLock.wait();
            }
            catch (InterruptedException ie) {
            }
          }
        }

        /* reset byte counts */
        this.readByteCount = 0L;
        this.writeByteCount = 0L;

        /* IP address */
        InetAddress inetAddr = this.client.getInetAddress();

        /* session timeout */
        long sessionStartTime = DateTime.getCurrentTimeMillis();
        long sessionTimeoutMS = ServerSocketThread.this.getSessionTimeout();
        long sessionTimeoutAt = (sessionTimeoutMS > 0L) ? (sessionStartTime + sessionTimeoutMS)
            : -1L;

        /* client session handler */
        ClientPacketHandler clientHandler = ServerSocketThread.this.getClientPacketHandler();
        if (clientHandler != null) {
          clientHandler.sessionStarted(inetAddr, this.client.isTCP(), ServerSocketThread.this
              .isTextPackets());
        }

        /* process client requests */
        Throwable termError = null;
        try {

          /* get output stream */
          OutputStream output = this.client.getOutputStream();

          /* loop until timeout, error, client terminate */
          for (int i = 0;; i++) {

            /* session timeout? */
            if (sessionTimeoutAt > 0L) {
              long currentTimeMS = DateTime.getCurrentTimeMillis();
              if (currentTimeMS >= sessionTimeoutAt) {
                throw new SSSessionTimeoutException("Session timeout");
              }
            }

            /* display prompt */
            byte prompt[] = ServerSocketThread.this.getPrompt(i);
            this.writeBytes(output, prompt);

            /* read packet */
            byte line[] = null;
            if (ServerSocketThread.this.isTextPackets()) {
              // ASCII: read until packet EOL
              line = this.readLine(this.client, clientHandler);
            }
            else {
              // Binary: read until packet length or timeout
              line = this.readPacket(this.client, clientHandler);
            }

            /* send packet to listeners */
            if ((line != null) && ServerSocketThread.this.hasListeners()) {
              try {
                ServerSocketThread.this.invokeListeners(line);
              }
              catch (Throwable t) {
                // a listener can terminate this session
                break;
              }
            }

            /* get response */
            if ((line != null) && (clientHandler != null)) {
              try {
                byte response[] = clientHandler.getHandlePacket(line);
                this.writeBytes(output, response);
                if (clientHandler.terminateSession()) {
                  break;
                }
              }
              catch (Throwable t) {
                // the ClientPacketHandler can terminate this session
                Print.logException("Unexpected exception: ", t);
                break;
              }
            }

            /* terminate now if we're reading a Datagram and we're out of data */
            if (this.client.isUDP() && (this.client.available() <= 0)) {
              // Normal end of UDP connection
              break;
            }

          } // socket read loop

          /* flush output before closing */
          if (output != null) {
            output.flush();
            this.client.setSoLinger(ServerSocketThread.this.getLingerTimeoutSec()); // (seconds)
          }

        }
        catch (SSSessionTimeoutException ste) {
          Print.logError(ste.getMessage());
          termError = ste;
        }
        catch (SSReadTimeoutException rte) {
          Print.logError(rte.getMessage());
          termError = rte;
        }
        catch (SSEndOfStreamException eos) {
          if (this.client.isTCP()) { // run
            Print.logError(eos.getMessage());
            termError = eos;
          }
          else {
            // We're at the end of the UDP datastream
          }
        }
        catch (SocketException se) {
          Print.logError("Connection closed");
          termError = se;
        }
        catch (Throwable t) {
          Print.logException("?", t);
          termError = t;
        }

        /* client session terminated */
        if (clientHandler != null) {
          clientHandler.sessionTerminated(termError, this.readByteCount, this.writeByteCount);
        }

        /* close socket */
        try {
          this.client.close();
        }
        catch (IOException ioe) {
          /* unable to close? */
        }

        /* clear for next requestor */
        synchronized (this.runLock) {
          this.client = null;
        }

      } // while (true)

    } // run()

    /**
     * Writes a byte array into an output stream.
     * 
     * @param output output stream
     * @param cmd byte array
     * @throws IOException if I/O errors occured.
     */
    private void writeBytes(OutputStream output, byte cmd[]) throws IOException {
      if ((output != null) && (cmd != null) && (cmd.length > 0)) {
        try {
          String c = StringTools.toStringValue(cmd);
          // Print.logDebug("<-- [" + c.length() + "] " + c);
          output.write(cmd);
          output.flush();
          this.writeByteCount += cmd.length;
        }
        catch (IOException t) {
          Print.logError("writeBytes error - " + t);
          throw t;
        }
      }
    }

    /**
     * Reads a byte frin a client's imput stream.
     * 
     * @param client client socket
     * @param timeoutAt time-out
     * @return the byte read
     * @throws IOException if any I/O errors occured
     */
    private int readByte(ClientSocket client, long timeoutAt) throws IOException {
      // Read until:
      // - Timeout
      // - IO error
      // - Read byte
      int ch;
      InputStream input = client.getInputStream();
      while (true) {
        if (timeoutAt > 0L) {
          long currentTimeMS = DateTime.getCurrentTimeMillis();
          if (currentTimeMS >= timeoutAt) {
            throw new SSReadTimeoutException("Read timeout");
          }
          // if (input.available() <= 0) {
          int timeout = (int) (timeoutAt - currentTimeMS);
          client.setSoTimeout(timeout);
          // }
        }
        try {
          // this read is expected to time-out if no data is available
          ch = input.read();
          if (ch < 0) {
            // socket likely closed by client
            throw new SSEndOfStreamException("End of stream");
          }
          this.readByteCount++;
          return ch; // <-- valid character returned
        }
        catch (InterruptedIOException ie) {
          // timeout
          continue;
        }
        catch (SocketException se) {
          // rethrow IO error
          throw se;
        }
        catch (IOException ioe) {
          // rethrow IO error
          throw ioe;
        }
      }
    }

    /**
     * Reads line from a client's input stream.
     * 
     * @param client client socket
     * @param clientHandler client packet handler
     * @return array of read bytes
     * @throws IOException if any I/O errors occured
     */
    private byte[] readLine(ClientSocket client, ClientPacketHandler clientHandler)
        throws IOException {
      // Read until:
      // - EOL
      // - Timeout
      // - IO error
      // - Read 'maxLen' characters

      /* timeouts */
      long idleTimeoutMS = ServerSocketThread.this.getIdleTimeout();
      long pcktTimeoutMS = ServerSocketThread.this.getPacketTimeout();
      long pcktTimeoutAt = (idleTimeoutMS > 0L) ? (DateTime.getCurrentTimeMillis() + idleTimeoutMS)
          : -1L;

      /* max read length */
      int maxLen = ServerSocketThread.this.getMaximumPacketLength();
      // no minimum

      /* set default socket timeout */
      client.setSoTimeout(10000);

      /* packet */
      byte buff[] = new byte[maxLen];
      int buffLen = 0;
      boolean isIdle = true;
      long readStartTime = DateTime.getCurrentTimeMillis();
      try {
        while (true) {

          /* read byte */
          int ch = this.readByte(client, pcktTimeoutAt);
          // valid character returned

          /* reset idle timeout */
          if (isIdle) {
            isIdle = false;
            if (pcktTimeoutMS > 0L) {
              // reset timeout
              pcktTimeoutAt = DateTime.getCurrentTimeMillis() + pcktTimeoutMS;
            }
          }

          /* check special characters */
          if (ServerSocketThread.this.isLineTerminatorChar(ch)) {
            // end of line (typically '\n')
            break;
          }
          else if (ServerSocketThread.this.isIgnoreChar(ch)) {
            // ignore this character (typically '\r')
            continue;
          }
          else if (ServerSocketThread.this.isBackspaceChar(ch)) {
            if (buffLen > 0) {
              buffLen--;
            }
            continue;
          }
          else if (ch < ' ') {
            // ignore non-printable characters
            continue;
          }

          /* save byte */
          if (buffLen >= buff.length) { // overflow?
            byte newBuff[] = new byte[buff.length * 2];
            System.arraycopy(buff, 0, newBuff, 0, buff.length);
            buff = newBuff;
          }
          buff[buffLen++] = (byte) ch;

          /* check lengths */
          if ((maxLen > 0) && (buffLen >= maxLen)) {
            // we've read all the bytes we can
            break;
          }

        }
      }
      catch (SSReadTimeoutException te) {
        // Print.logError("Timeout: " + StringTools.toStringValue(buff, 0, buffLen));
        if (ServerSocketThread.this.getTerminateOnTimeout()) {
          throw te;
        }
      }
      catch (SSEndOfStreamException eos) {
        if (client.isTCP()) { // readLine
          // This could mean a protocol error
          Print.logError(eos.getMessage());
          throw eos;
        }
        else {
          // We're at the end of the UDP datastream
        }
      }
      catch (IOException ioe) {
        Print.logError("ReadLine error - " + ioe);
        throw ioe;
      }
      long readEndTime = DateTime.getCurrentTimeMillis();

      /* return packet */
      if (buff.length == buffLen) {
        // highly unlikely
        return buff;
      }
      else {
        // resize buffer
        byte newBuff[] = new byte[buffLen];
        System.arraycopy(buff, 0, newBuff, 0, buffLen);
        return newBuff;
      }

    }

    /**
     * Reads a packet from a client's input stream.
     * 
     * @param client client socket
     * @param clientHandler client packet handler
     * @return the packet read as a byte array
     * @throws IOException if any I/O errors occured
     */
    private byte[] readPacket(ClientSocket client, ClientPacketHandler clientHandler)
        throws IOException {
      // Read until:
      // - Timeout
      // - IO error
      // - Read 'maxLen' characters
      // - Read 'actualLen' characters

      /* timeouts */
      long idleTimeoutMS = ServerSocketThread.this.getIdleTimeout();
      long pcktTimeoutMS = ServerSocketThread.this.getPacketTimeout();
      long pcktTimeoutAt = (idleTimeoutMS > 0L) ? (DateTime.getCurrentTimeMillis() + idleTimeoutMS)
          : -1L;

      /* packet/read length */
      int maxLen = ServerSocketThread.this.getMaximumPacketLength();
      int minLen = ServerSocketThread.this.getMinimumPacketLength();
      int actualLen = 0;

      /* set default socket timeout */
      client.setSoTimeout(10000);

      /* packet */
      byte packet[] = new byte[maxLen];
      int packetLen = 0;
      boolean isIdle = true;
      boolean isTextLine = false;
      try {
        while (true) {

          /* read byte */
          int ch = this.readByte(client, pcktTimeoutAt);
          // valid character returned

          /* reset idle timeout */
          if (isIdle) {
            isIdle = false;
            if (pcktTimeoutMS > 0L) {
              // reset packet timeout
              pcktTimeoutAt = DateTime.getCurrentTimeMillis() + pcktTimeoutMS;
            }
          }

          /* look for line terminator? */
          if (isTextLine) {
            if (ServerSocketThread.this.isLineTerminatorChar(ch)) {
              // end of line (typically '\n')
              break;
            }
            else if (ServerSocketThread.this.isIgnoreChar(ch)) {
              // ignore this character (typically '\r')
              continue;
            }
            else {
              // save byte
              packet[packetLen++] = (byte) ch;
            }
          }
          else {
            // save byte
            packet[packetLen++] = (byte) ch;
          }

          /* check lengths */
          if (packetLen >= maxLen) {
            // we've read all the bytes we can
            break;
          }
          else if ((actualLen > 0) && (packetLen >= actualLen)) {
            // we've read the bytes we expected to read
            break;
          }
          else if ((clientHandler != null) && (actualLen <= 0) && (packetLen >= minLen)) {
            // we've read the minimum number of bytes
            // get the actual expected packet length
            actualLen = clientHandler.getActualPacketLength(packet, packetLen);
            if (actualLen <= 0) {
              // look for line terminator character
              actualLen = maxLen;
              isTextLine = true;
            }
            else if (actualLen > maxLen) {
              Print.logStackTrace("Actual length [" + actualLen + "] > Maximum length [" + maxLen
                  + "]");
              actualLen = maxLen;
            }
            else {
              // Print.logDebug("New actual packet len: " + actualLen);
            }
          }

        }
      }
      catch (SSReadTimeoutException t) {
        if (ServerSocketThread.this.getTerminateOnTimeout()) {
          throw t;
        }
      }
      catch (SSEndOfStreamException eos) {
        // This could mean a protocol error
        // this.client
        if (client.isTCP()) { // readPacket
          Print.logError(eos.getMessage());
          throw eos;
        }
        else {
          // We're at the end of the UDP datastream
          throw eos;
        }
      }
      catch (SocketException se) {
        Print.logError("ReadPacket error - " + se);
        throw se;
      }
      catch (IOException ioe) {
        Print.logError("ReadPacket error - " + ioe);
        throw ioe;
      }

      /* return packet */
      if (packet.length == packetLen) {
        // highly unlikely
        return packet;
      }
      else {
        // resize buffer
        byte newPacket[] = new byte[packetLen];
        System.arraycopy(packet, 0, newPacket, 0, packetLen);
        return newPacket;
      }

    }

  }

  /**
   * 
   * Session timeout exception.
   * 
   * @author Martin D. Flynn
   * @author Alexey Olkov
   * 
   */

  public static class SSSessionTimeoutException extends IOException {
    /**
     * Creates an exception with the given message.
     * 
     * @param msg text message
     */
    public SSSessionTimeoutException(String msg) {
      super(msg);
    }
  }

  /**
   * Read timeout exception.
   * 
   * @author Martin D. Flynn *
   * @author Alexey Olkov
   * 
   */
  public static class SSReadTimeoutException extends IOException {
    /**
     * Creates an exception with the given message.
     * 
     * @param msg text message
     */
    public SSReadTimeoutException(String msg) {
      super(msg);
    }
  }

  /**
   * End of stream exception.
   * 
   * @author Martin D. Flynn
   * @author Alexey Olkov
   * 
   */
  public static class SSEndOfStreamException extends IOException {
    /**
     * Creates an exception with the given message.
     * 
     * @param msg text message
     */
    public SSEndOfStreamException(String msg) {
      super(msg);
    }
  }

  // ------------------------------------------------------------------------

}
