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

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;
import javax.net.ssl.*;
import javax.net.*;
import javax.net.*;

public class ServerSocketThread
    extends Thread
{

    // ------------------------------------------------------------------------
    // References:
    //   http://tvilda.stilius.net/java/java_ssl.php
    //   http://www.jguru.com/faq/view.jsp?EID=993651

    // ------------------------------------------------------------------------
    // SSL:
    //    keytool -genkey -keystore <mySrvKeystore> -keyalg RSA
    // Required Properties:
    //   -Djavax.net.ssl.keyStore=<mySrvKeystore>
    //   -Djavax.net.ssl.keyStorePassword=<123456>
    // For debug, also add:
    //   -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol 
    //   -Djavax.net.debug=ssl
    // ------------------------------------------------------------------------

    private DatagramSocket datagramSocket = null;
    private ServerSocket serverSocket = null;
    
    private Vector clientThreadPool = null;
    
    private ClientPacketHandler clientPacketHandler = null;
    private Class clientPacketHandlerClass = null;

    private long sessionTimeoutMS   = -1L;
    private long idleTimeoutMS      = -1L;
    private long packetTimeoutMS    = -1L;
    
    private int  lingerTimeoutSec   = 4; // SO_LINGER timeout is in *Seconds*

    private int maxReadLength       = -1;
    private int minReadLength       = -1;

    private boolean terminateOnTimeout = true;
    
    private boolean isTextPackets = true;
    private int lineTerminatorChar[] = new int[] { '\n' };
    private int backspaceChar[] = new int[] { '\b' };
    private int ignoreChar[] = new int[] { '\r' };

    private byte prompt[] = null;
    private int promptIndex = -1;
    private boolean autoPrompt = false;
    
    private Vector actionListeners = null;
    
    // ------------------------------------------------------------------------

    private ServerSocketThread()
    {
        this.clientThreadPool = new Vector();
        this.actionListeners = new Vector();
    }
    
    public ServerSocketThread(DatagramSocket ds) 
    {
        this();
        this.datagramSocket = ds;
    }

    public ServerSocketThread(ServerSocket ss) 
    {
        this();
        this.serverSocket = ss;
    }

    public ServerSocketThread(int port)
        throws IOException 
    {
        this(new ServerSocket(port));
    }

    public ServerSocketThread(int port, boolean useSSL)
        throws IOException 
    {
        this(useSSL?
            SSLServerSocketFactory.getDefault().createServerSocket(port) :
            ServerSocketFactory   .getDefault().createServerSocket(port)
        );
    }
    
    // ------------------------------------------------------------------------

    public void run() 
    {
        while (true) {
            ClientSocket clientSocket = null;

            /* wait for client session */
            try {
                if (this.serverSocket != null) {
                    clientSocket = new ClientSocket(this.serverSocket.accept());
                } else 
                if (this.datagramSocket != null) {
                    byte b[] = new byte[ServerSocketThread.this.getMaximumPacketLength()];
                    ServerSocketThread.this.getMaximumPacketLength();
                    DatagramPacket dp = new DatagramPacket(b, b.length);
                    this.datagramSocket.receive(dp);
                    clientSocket = new ClientSocket(dp);
                } else {
                    Print.logStackTrace("ServerSocketThread has not been properly initialized");
                }
            } catch (IOException ioe) {
                Print.logError("Connection - " + ioe);
                continue; // go back and wait again
            }
            
            /* ip address */
            String ipAddr;
            try {
                InetAddress inetAddr = clientSocket.getInetAddress();
                ipAddr = (inetAddr != null)? inetAddr.getHostAddress() : "?";
            } catch (Throwable t) {
                ipAddr = "?";
            }

            /* find an available client thread */
            boolean foundThread = false;
            for (Iterator i = this.clientThreadPool.iterator(); i.hasNext() && !foundThread;) {
                ServerSessionThread sst = (ServerSessionThread)i.next();
                foundThread = sst.setClientIfAvailable(clientSocket);
            }
            if (!foundThread) { // add new thread to pool
                //Print.logInfo("New thread for ip ["+ipAddr+"] ...");
                ServerSessionThread sst = new ServerSessionThread(clientSocket);
                this.clientThreadPool.add(sst);
            } else {
                //Print.logDebug("Reuse existing thread for ip ["+ipAddr+"] ...");
            }

        }
    }
    
    // ------------------------------------------------------------------------
    
    public boolean hasListeners()
    {
        return (this.actionListeners.size() > 0);
    }
    
    public void addActionListener(ActionListener al)
    {
        // used for simple one way messaging
        if (!this.actionListeners.contains(al)) {
            this.actionListeners.add(al);
        }
    }
    
    public void removeActionListener(ActionListener al)
    {
        this.actionListeners.remove(al);
    }
    
    protected boolean invokeListeners(byte msgBytes[])
        throws Exception
    {
        if (msgBytes != null) {
            String msg = StringTools.toStringValue(msgBytes);
            for (Iterator i = this.actionListeners.iterator(); i.hasNext();) {
                Object alObj = i.next();
                if (alObj instanceof ActionListener) {
                    ActionListener al = (ActionListener)i.next();
                    ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, msg);
                    al.actionPerformed(ae);
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    // ------------------------------------------------------------------------
    
    public void setClientPacketHandler(ClientPacketHandler cph)
    {
        this.clientPacketHandler = cph;
    }
    
    public void setClientPacketHandlerClass(Class cphc)
    {
        if ((cphc == null) || ClientPacketHandler.class.isAssignableFrom(cphc)) {
            this.clientPacketHandlerClass = cphc;
            this.clientPacketHandler = null;
        } else {
            throw new ClassCastException("Invalid ClientPacketHandler class");
        }
    }

    public ClientPacketHandler getClientPacketHandler()
    {
        if (this.clientPacketHandler != null) {
            // single instance
            return this.clientPacketHandler;
        } else
        if (this.clientPacketHandlerClass != null) {
            // new instance
            try {
                return (ClientPacketHandler)this.clientPacketHandlerClass.newInstance();
            } catch (Throwable t) {
                Print.logException("ClientPacketHandler", t);
                return null;
            }
        } else {
            // not defined
            return null;
        }
    }

    // ------------------------------------------------------------------------

    public void setSessionTimeout(long timeoutMS)
    {
        this.sessionTimeoutMS = timeoutMS;
    }

    public long getSessionTimeout()
    {
        return this.sessionTimeoutMS;
    }

    // ------------------------------------------------------------------------

    public void setIdleTimeout(long timeoutMS)
    {
        this.idleTimeoutMS = timeoutMS;
    }
    
    public long getIdleTimeout()
    {
        // the timeout for waiting for something to appear on the socket
        return this.idleTimeoutMS;
    }

    public void setPacketTimeout(long timeoutMS)
    {
        // once a byte is finally read, the timeout for waiting until the 
        // entire packet is finished
        this.packetTimeoutMS = timeoutMS;
    }
    
    public long getPacketTimeout()
    {
        return this.packetTimeoutMS;
    }

    public void setTerminateOnTimeout(boolean timeoutQuit)
    {
        this.terminateOnTimeout = timeoutQuit;
    }
    
    public boolean getTerminateOnTimeout()
    {
        return this.terminateOnTimeout;
    }

    // ------------------------------------------------------------------------

    public void setLingerTimeoutSec(int timeoutSec)
    {
        this.lingerTimeoutSec = timeoutSec;
    }
    
    public int getLingerTimeoutSec()
    {
        return this.lingerTimeoutSec;
    }

    // ------------------------------------------------------------------------

    public void setTextPackets(boolean isText)
    {
        this.isTextPackets = isText;
        if (!this.isTextPackets()) {
            this.setBackspaceChar(null);
            this.setLineTerminatorChar(null);
            this.setIgnoreChar(null);
        }
    }
    
    public boolean isTextPackets()
    {
        return this.isTextPackets;
    }

    // ------------------------------------------------------------------------

    public void setMaximumPacketLength(int len)
    {
        this.maxReadLength = len;
    }
    
    public int getMaximumPacketLength()
    {
        if (this.maxReadLength > 0) {
            return this.maxReadLength;
        } else
        if (this.isTextPackets()) {
            return 2048; // default for text packets
        } else {
            return 1024; // default for binary packets
        }
    }
    
    // ------------------------------------------------------------------------

    public void setMinimumPacketLength(int len)
    {
        this.minReadLength = len;
    }
    
    public int getMinimumPacketLength()
    {
        if (this.minReadLength > 0) {
            return this.minReadLength;
        } else
        if (this.isTextPackets()) {
            return 1; // at least '\r' (however, this isn't used for text packets)
        } else {
            return this.getMaximumPacketLength();
        }
    }

    // ------------------------------------------------------------------------

    public void setLineTerminatorChar(int term)
    {
        this.setLineTerminatorChar(new int[] { term });
    }

    public void setLineTerminatorChar(int term[])
    {
        this.lineTerminatorChar = term;
    }
    
    public int[] getLineTerminatorChar()
    {
        return this.lineTerminatorChar;
    }
    
    public boolean isLineTerminatorChar(int ch)
    {
        if ((this.lineTerminatorChar != null) && (ch >= 0)) {
            for (int i = 0; i < this.lineTerminatorChar.length; i++) {
                if (this.lineTerminatorChar[i] == ch) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // ------------------------------------------------------------------------

    public void setBackspaceChar(int bs)
    {
        this.setBackspaceChar(new int[] { bs });
    }

    public void setBackspaceChar(int bs[])
    {
        this.backspaceChar = bs;
    }
    
    public int[] getBackspaceChar()
    {
        return this.backspaceChar;
    }
    
    public boolean isBackspaceChar(int ch)
    {
        if (this.hasPrompt() && (this.backspaceChar != null) && (ch >= 0)) {
            for (int i = 0; i < this.backspaceChar.length; i++) {
                if (this.backspaceChar[i] == ch) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // ------------------------------------------------------------------------

    public void setIgnoreChar(int bs[])
    {
        this.ignoreChar = bs;
    }
    
    public int[] getIgnoreChar()
    {
        return this.ignoreChar;
    }
    
    public boolean isIgnoreChar(int ch)
    {
        if ((this.ignoreChar != null) && (ch >= 0)) {
            for (int i = 0; i < this.ignoreChar.length; i++) {
                if (this.ignoreChar[i] == ch) {
                    return true;
                }
            }
        }
        return false;
    }
   
    // ------------------------------------------------------------------------
    
    public void setAutoPrompt(boolean auto)
    {
        if (auto) {
            this.prompt = null;
            this.autoPrompt = true;
        } else {
            this.autoPrompt = false;
        }
    }
    
    public void setPrompt(byte prompt[])
    {
        this.prompt = prompt;
        this.autoPrompt = false;
    }
    
    public void setPrompt(String prompt)
    {
        this.setPrompt(StringTools.getBytes(prompt));
    }
    
    protected byte[] getPrompt(int ndx)
    {
        this.promptIndex = ndx;
        if (this.prompt != null) {
            return this.prompt;
        } else
        if (this.autoPrompt && this.isTextPackets()) {
            return StringTools.getBytes("" + (this.promptIndex+1) + "> ");
        } else {
            return null;
        }
    }
    
    public boolean hasPrompt()
    {
        return (this.prompt != null) || (this.autoPrompt && this.isTextPackets());
    }

    protected int getPromptIndex()
    {
        return this.promptIndex;
    }
    
    // ------------------------------------------------------------------------

    private static class ClientSocket
    {
        private Socket tcpClient = null;
        private DatagramPacket udpClient = null;
        private InputStream bais = null;
        public ClientSocket(Socket tcpClient) {
            this.tcpClient = tcpClient;
        }
        public ClientSocket(DatagramPacket udpClient) {
            this.udpClient = udpClient;
        }
        public boolean isTCP() {
            return (this.tcpClient != null);
        }
        public boolean isUDP() {
            return (this.udpClient != null);
        }
        public int available() {
            try {
                return this.getInputStream().available();
            } catch (Throwable t) {
                return 0;
            }
        }
        public InetAddress getInetAddress() {
            if (this.tcpClient != null) {
                return this.tcpClient.getInetAddress();
            } else 
            if (this.udpClient != null) {
                SocketAddress sa = this.udpClient.getSocketAddress();
                if (sa instanceof InetSocketAddress) {
                    return ((InetSocketAddress)sa).getAddress();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        public OutputStream getOutputStream() throws IOException {
            if (this.tcpClient != null) {
                return this.tcpClient.getOutputStream();
            } else {
                return null;
            }
        }
        public InputStream getInputStream() throws IOException {
            if (this.tcpClient != null) {
                return this.tcpClient.getInputStream();
            } else 
            if (this.udpClient != null) {
                if (bais == null) {
                    bais = new ByteArrayInputStream(this.udpClient.getData(), 0, this.udpClient.getLength());
                } 
                return bais;
            } else {
                return null;
            }
        }
        public void setSoTimeout(int timeoutSec) throws SocketException {
            if (this.tcpClient != null) {
                this.tcpClient.setSoTimeout(timeoutSec);
            }
        }
        public void setSoLinger(int timeoutSec) throws SocketException {
            if (this.tcpClient != null) {
                if (timeoutSec <= 0) {
                    this.tcpClient.setSoLinger(false, 0); // no linger
                } else {
                    this.tcpClient.setSoLinger(true, timeoutSec);
                }
            }
        }
        public void setSoLinger(boolean on, int timeoutSec) throws SocketException {
            if (this.tcpClient != null) {
                if (timeoutSec <= 0) { on = false; }
                this.tcpClient.setSoLinger(on, timeoutSec);
            }
        }
        public void close() throws IOException {
            if (this.tcpClient != null) {
                this.tcpClient.close();
            }
        }
    }
        
    // ------------------------------------------------------------------------

    public class ServerSessionThread
        extends Thread
    {
    
        private Object runLock = new Object();
        private ClientSocket client = null;
        private long readByteCount = 0L;
        private long writeByteCount = 0L;

        public ServerSessionThread(Socket client) {
            super("ClientSession");
            this.client = new ClientSocket(client);
            this.start();
        }

        public ServerSessionThread(ClientSocket client) {
            super("ClientSession");
            this.client = client;
            this.start();
        }

        public boolean setClientIfAvailable(ClientSocket clientSocket) {
            boolean rtn = false;
            synchronized (this.runLock) {
                if (this.client != null) {
                    rtn = false; // not available
                } else {
                    this.client = clientSocket;
                    this.runLock.notify();
                    rtn = true;
                }
            }
            return rtn;
        }
        
        public void run() {
    
            /* loop forever */
            while (true) {
    
                /* wait for client (if necessary) */
                synchronized (this.runLock) {
                    while (this.client == null) {
                        try { this.runLock.wait(); } catch (InterruptedException ie) {}
                    }
                }
                
                /* reset byte counts */
                this.readByteCount  = 0L;
                this.writeByteCount = 0L;
                
                /* IP address */
                InetAddress inetAddr = this.client.getInetAddress();

                /* session timeout */
                long sessionStartTime = DateTime.getCurrentTimeMillis();
                long sessionTimeoutMS = ServerSocketThread.this.getSessionTimeout();
                long sessionTimeoutAt = (sessionTimeoutMS > 0L)? (sessionStartTime + sessionTimeoutMS) : -1L;

                /* client session handler */
                ClientPacketHandler clientHandler = ServerSocketThread.this.getClientPacketHandler();
                if (clientHandler != null) {
                    clientHandler.sessionStarted(inetAddr, this.client.isTCP(), ServerSocketThread.this.isTextPackets());
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
                        } else {
                            // Binary: read until packet length or timeout
                            line = this.readPacket(this.client, clientHandler);
                        }
                        
                        /* send packet to listeners */
                        if ((line != null) && ServerSocketThread.this.hasListeners()) {
                            try {
                                ServerSocketThread.this.invokeListeners(line);
                            } catch (Throwable t) {
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
                            } catch (Throwable t) {
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
                    
                } catch (SSSessionTimeoutException ste) {
                    Print.logError(ste.getMessage());
                    termError = ste;
                } catch (SSReadTimeoutException rte) {
                    Print.logError(rte.getMessage());
                    termError = rte;
                } catch (SSEndOfStreamException eos) {
                    if (this.client.isTCP()) { // run
                        Print.logError(eos.getMessage());
                        termError = eos;
                    } else {
                        // We're at the end of the UDP datastream
                    }
                } catch (SocketException se) {
                    Print.logError("Connection closed");
                    termError = se;
                } catch (Throwable t) {
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
                } catch (IOException ioe) {
                    /* unable to close? */
                }
    
                /* clear for next requestor */
                synchronized (this.runLock) {
                    this.client = null;
                }
    
            } // while (true)
    
        } // run()
        
        private void writeBytes(OutputStream output, byte cmd[]) throws IOException {
            if ((output != null) && (cmd != null) && (cmd.length > 0)) {
                try {
                    String c = StringTools.toStringValue(cmd);
                    //Print.logDebug("<-- [" + c.length() + "] " + c);
                    output.write(cmd);
                    output.flush();
                    this.writeByteCount += cmd.length;
                } catch (IOException t) {
                    Print.logError("writeBytes error - " + t);
                    throw t;
                }
            }
        }
        
        private int readByte(ClientSocket client, long timeoutAt) throws IOException {
            // Read until:
            //  - Timeout
            //  - IO error
            //  - Read byte
            int ch;
            InputStream input = client.getInputStream();
            while (true) {
                if (timeoutAt > 0L) {
                    long currentTimeMS = DateTime.getCurrentTimeMillis();
                    if (currentTimeMS >= timeoutAt) {
                        throw new SSReadTimeoutException("Read timeout");
                    }
                    //if (input.available() <= 0) {
                    int timeout = (int)(timeoutAt - currentTimeMS);
                    client.setSoTimeout(timeout);
                    //}
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
                } catch (InterruptedIOException ie) {
                    // timeout
                    continue;
                } catch (SocketException se) {
                    // rethrow IO error
                    throw se;
                } catch (IOException ioe) {
                    // rethrow IO error
                    throw ioe;
                }
            }
        }

        private byte[] readLine(ClientSocket client, ClientPacketHandler clientHandler) throws IOException {
            // Read until:
            //  - EOL
            //  - Timeout
            //  - IO error
            //  - Read 'maxLen' characters
            
            /* timeouts */
            long idleTimeoutMS = ServerSocketThread.this.getIdleTimeout();
            long pcktTimeoutMS = ServerSocketThread.this.getPacketTimeout();
            long pcktTimeoutAt = (idleTimeoutMS > 0L)? (DateTime.getCurrentTimeMillis() + idleTimeoutMS) : -1L;

            /* max read length */
            int maxLen = ServerSocketThread.this.getMaximumPacketLength();
            // no minimum
            
            /* set default socket timeout */
            client.setSoTimeout(10000);

            /* packet */
            byte buff[]  = new byte[maxLen];
            int  buffLen = 0;
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
                    } else
                    if (ServerSocketThread.this.isIgnoreChar(ch)) {
                        // ignore this character (typically '\r')
                        continue;
                    } else
                    if (ServerSocketThread.this.isBackspaceChar(ch)) {
                        if (buffLen > 0) {
                            buffLen--;
                        }
                        continue;
                    } else
                    if (ch < ' ') {
                        // ignore non-printable characters
                        continue;
                    }
                    
                    /* save byte */
                    if (buffLen >= buff.length) { // overflow?
                        byte newBuff[] = new byte[buff.length * 2];
                        System.arraycopy(buff, 0, newBuff, 0, buff.length);
                        buff = newBuff;
                    }
                    buff[buffLen++] = (byte)ch;
                    
                    /* check lengths */
                    if ((maxLen > 0) && (buffLen >= maxLen)) {
                        // we've read all the bytes we can
                        break;
                    }

                }
            } catch (SSReadTimeoutException te) {
                //Print.logError("Timeout: " + StringTools.toStringValue(buff, 0, buffLen));
                if (ServerSocketThread.this.getTerminateOnTimeout()) {
                    throw te;
                }
            } catch (SSEndOfStreamException eos) {
                if (client.isTCP()) { // readLine
                    // This could mean a protocol error
                    Print.logError(eos.getMessage());
                    throw eos;
                } else {
                    // We're at the end of the UDP datastream
                }
            } catch (IOException ioe) {
                Print.logError("ReadLine error - " + ioe);
                throw ioe;
            }
            long readEndTime = DateTime.getCurrentTimeMillis();
            
            /* return packet */
            if (buff.length == buffLen) {
                // highly unlikely
                return buff;
            } else {
                // resize buffer
                byte newBuff[] = new byte[buffLen];
                System.arraycopy(buff, 0, newBuff, 0, buffLen);
                return newBuff;
            }
            
        }

        private byte[] readPacket(ClientSocket client, ClientPacketHandler clientHandler) throws IOException {
            // Read until:
            //  - Timeout
            //  - IO error
            //  - Read 'maxLen' characters
            //  - Read 'actualLen' characters

            /* timeouts */
            long idleTimeoutMS = ServerSocketThread.this.getIdleTimeout();
            long pcktTimeoutMS = ServerSocketThread.this.getPacketTimeout();
            long pcktTimeoutAt = (idleTimeoutMS > 0L)? (DateTime.getCurrentTimeMillis() + idleTimeoutMS) : -1L;

            /* packet/read length */
            int maxLen = ServerSocketThread.this.getMaximumPacketLength();
            int minLen = ServerSocketThread.this.getMinimumPacketLength();
            int actualLen = 0;
            
            /* set default socket timeout */
            client.setSoTimeout(10000);

            /* packet */
            byte packet[] = new byte[maxLen];
            int  packetLen = 0;
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
                        } else
                        if (ServerSocketThread.this.isIgnoreChar(ch)) {
                            // ignore this character (typically '\r')
                            continue;
                        } else {
                            // save byte
                            packet[packetLen++] = (byte)ch;
                        }
                    } else {
                        // save byte
                        packet[packetLen++] = (byte)ch;
                    }
                    
                    /* check lengths */
                    if (packetLen >= maxLen) {
                        // we've read all the bytes we can
                        break;
                    } else
                    if ((actualLen > 0) && (packetLen >= actualLen)) {
                        // we've read the bytes we expected to read
                        break;
                    } else
                    if ((clientHandler != null) && (actualLen <= 0) && (packetLen >= minLen)) {
                        // we've read the minimum number of bytes
                        // get the actual expected packet length
                        actualLen = clientHandler.getActualPacketLength(packet, packetLen);
                        if (actualLen <= 0) {
                            // look for line terminator character
                            actualLen = maxLen;
                            isTextLine = true;
                        } else
                        if (actualLen > maxLen) {
                            Print.logStackTrace("Actual length [" + actualLen + "] > Maximum length [" + maxLen + "]");
                            actualLen = maxLen;
                        } else {
                            //Print.logDebug("New actual packet len: " + actualLen);
                        }
                    }

                }
            } catch (SSReadTimeoutException t) {
                if (ServerSocketThread.this.getTerminateOnTimeout()) {
                    throw t;
                }
            } catch (SSEndOfStreamException eos) {
                // This could mean a protocol error
                // this.client
                if (client.isTCP()) { // readPacket
                    Print.logError(eos.getMessage());
                    throw eos;
                } else {
                    // We're at the end of the UDP datastream
                    throw eos;
                }
            } catch (SocketException se) {
                Print.logError("ReadPacket error - " + se);
                throw se;
            } catch (IOException ioe) {
                Print.logError("ReadPacket error - " + ioe);
                throw ioe;
            }
            
            /* return packet */
            if (packet.length == packetLen) {
                // highly unlikely
                return packet;
            } else {
                // resize buffer
                byte newPacket[] = new byte[packetLen];
                System.arraycopy(packet, 0, newPacket, 0, packetLen);
                return newPacket;
            }
            
        }

    }
    
    // ------------------------------------------------------------------------
    
    public static class SSSessionTimeoutException
        extends IOException
    {
        public SSSessionTimeoutException(String msg) {
            super(msg);
        }
    }

    public static class SSReadTimeoutException
        extends IOException
    {
        public SSReadTimeoutException(String msg) {
            super(msg);
        }
    }
    
    public static class SSEndOfStreamException
        extends IOException
    {
        public SSEndOfStreamException(String msg) {
            super(msg);
        }
    }

    // ------------------------------------------------------------------------
    
}
