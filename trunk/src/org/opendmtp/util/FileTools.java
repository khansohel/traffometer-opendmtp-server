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
//  This class provides many File based utilities
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class provides many File based utilities.
 * 
 * @author Martin D. Flynn
 * @author Pavel Senin.
 * 
 */
public class FileTools {

  /**
   * Copies from input stream to output stream.
   * 
   * @param input input stream.
   * @param output output stream.
   * @return number of bytes copied
   * @throws IOException if IO error encountered.
   */
  public static int copyStreams(InputStream input, OutputStream output) throws IOException {
    return FileTools.copyStreams(input, output, -1);
  }

  /**
   * Copies streams byte-by-byte.
   * 
   * @param input input stream.
   * @param output output stream.
   * @param maxLen limits the amount of data to copy.
   * @return return number of bytes copied
   * @throws IOException if IO Exception encountered.
   */
  public static int copyStreams(InputStream input, OutputStream output, int maxLen)
      throws IOException {

    /* copy nothing? */
    if (maxLen == 0) {
      return 0;
    }

    /* copy bytes */
    int length = 0; // count of bytes copied
    byte tmpBuff[] = new byte[10 * 1024]; // 10K blocks
    while (true) {

      /* read length */
      int readLen;
      if (maxLen >= 0) {
        readLen = maxLen - length;
        if (readLen == 0) {
          break; // done reading
        }
        else if (readLen > tmpBuff.length) {
          readLen = tmpBuff.length; // max block size
        }
      }
      else {
        readLen = tmpBuff.length;
      }

      /* read input stream */
      int cnt = input.read(tmpBuff, 0, readLen);

      /* copy to output stream */
      if (cnt < 0) {
        if ((maxLen >= 0) && (length != maxLen)) {
          Print.logError("Copy size mismatch: " + maxLen + " => " + length);
        }
        break;
      }
      else if (cnt > 0) {
        output.write(tmpBuff, 0, cnt);
        length += cnt;
        if ((maxLen >= 0) && (length >= maxLen)) {
          break; // per 'maxLen', done copying
        }
      }
      else {
        // Print.logDebug("Read 0 bytes ... continuing");
      }

    }
    output.flush();

    /* return number of bytes copied */
    return length;
  }

  /**
   * Opens connection to the file as an InputStream.
   * 
   * @param file specifies file name.
   * @return input stream handler or null if unable to open.
   */
  public static InputStream openInputFile(String file) {
    if ((file != null) && !file.equals("")) {
      return FileTools.openInputFile(new File(file));
    }
    else {
      return null;
    }
  }

  /**
   * Opens connection to the file as an InputStream.
   * 
   * @param file specifies file handler.
   * @return input stream handler or null if unable to open.
   */
  public static InputStream openInputFile(File file) {
    try {
      return new FileInputStream(file);
    }
    catch (IOException ioe) {
      Print.logError("Unable to open file: " + file + " [" + ioe + "]");
      return null;
    }
  }

  /**
   * Closes InputStream connection.
   * 
   * @param in stream to close.
   */
  public static void closeStream(InputStream in) {
    if (in != null) {
      try {
        in.close();
      }
      catch (IOException ioe) {
        // Print.logError("Unable to close stream: " + ioe);
      }
    }
  }

  /**
   * Closes OutputStream connection.
   * 
   * @param out stream to close.
   */
  public static void closeStream(OutputStream out) {
    if (out != null) {
      try {
        out.close();
      }
      catch (IOException ioe) {
        // Print.logError("Unable to close stream: " + ioe);
      }
    }
  }

  /**
   * Reads the stream into byte array.
   * 
   * @param input stream to read from.
   * @return readed bytes array.
   * @throws IOException if an IO error encountered.
   */
  public static byte[] readStream(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    copyStreams(input, output);
    return output.toByteArray();
  }

  /**
   * Writes String into OutputStream.
   * 
   * @param output stream to write to.
   * @param dataStr string to write.
   * @throws IOException if an IO error encountered.
   */
  public static void writeStream(OutputStream output, String dataStr) throws IOException {
    byte data[] = dataStr.getBytes();
    output.write(data, 0, data.length);
  }

  /**
   * Reads the File into byte array.
   * 
   * @param file file name.
   * @return readed bytes array or null if file is empty.
   */

  public static byte[] readFile(String file) {
    if ((file != null) && !file.equals("")) {
      return FileTools.readFile(new File(file));
    }
    else {
      return null;
    }
  }

  /**
   * Reads the File into byte array.
   * 
   * @param file file to read from.
   * @return readed bytes array or null if file is empty.
   */
  public static byte[] readFile(File file) {
    if ((file != null) && file.exists()) {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        return readStream(fis);
      }
      catch (IOException ioe) {
        Print.logError("Unable to read file: " + file + " [" + ioe + "]");
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (IOException ioe) {/* ignore */
          }
        }
      }
    }
    return null;
  }

  /**
   * Reads string from the input stream.
   * 
   * @param input stream to read from.
   * @return readed line
   * @throws IOException if IO error encountered.
   */
  public static String readLine(InputStream input) throws IOException {
    StringBuffer sb = new StringBuffer();
    while (true) {
      int ch = input.read();
      if (ch < 0) { // eof
        throw new EOFException("End of InputStream");
      }
      else if ((ch == '\r') || (ch == '\n')) {
        return sb.toString();
      }
      sb.append((char) ch);
    }
  }

  /**
   * Reads line from standard input.
   * 
   * @return readed string.
   * @throws IOException if an IO error encountered.
   */
  public static String readLine_stdin() throws IOException {
    while (System.in.available() > 0) {
      System.in.read();
    }
    return FileTools.readLine(System.in);
  }

  /**
   * Reads line from standard input. Prints friendly prompt and waits for the input. If nothing
   * entered returns default value.
   * 
   * @param msg prompt message.
   * @param dft default value.
   * @return readed string or default value.
   * @throws IOException if an IO error encountered.
   */
  public static String readString_stdin(String msg, String dft) throws IOException {
    if (msg == null) {
      msg = "";
    }
    Print.sysPrintln(msg + "    [String: default='" + dft + "'] ");
    for (;;) {
      Print.sysPrint("?");
      String line = FileTools.readLine_stdin();
      if (line.equals("")) {
        if (dft != null) {
          return dft;
        }
        else {
          // if there is no default, a non-empty String is required
          Print.sysPrint("String required, please re-enter] ");
          continue;
        }
      }
      return line;
    }
  }

  /**
   * Reads boolean value from standard input. Prints friendly prompt and waits for the input. If
   * nothing entered returns default value. If not boolean input provided re-requests for input.
   * Valid values for true: "true", "yes", "on", "1"; for false: "false", "no", "off", "0".
   * 
   * @param msg prompt message.
   * @param dft default value.
   * @return readed string or default value.
   * @throws IOException if an IO error encountered.
   */
  public static boolean readBoolean_stdin(String msg, boolean dft) throws IOException {
    if (msg == null) {
      msg = "";
    }
    Print.sysPrintln(msg + "    [Boolean: default='" + dft + "'] ");
    for (;;) {
      Print.sysPrint("?");
      String line = FileTools.readLine_stdin().trim();
      if (line.equals("")) {
        return dft;
      }
      else if (!StringTools.isBoolean(line)) {
        Print.sysPrint("Boolean required, please re-enter] ");
        continue;
      }
      return StringTools.parseBoolean(line, dft);
    }
  }

  /**
   * Reads long value from standard input. Prints friendly prompt and waits for the input. If
   * nothing entered returns default value. If not numeric value entered re-requests for input.
   * 
   * @param msg prompt message.
   * @param dft default value.
   * @return readed string or default value.
   * @throws IOException if an IO error encountered.
   */
  public static long readLong_stdin(String msg, long dft) throws IOException {
    if (msg == null) {
      msg = "";
    }
    Print.sysPrintln(msg + "    [Long: default='" + dft + "'] ");
    for (;;) {
      Print.sysPrint("?");
      String line = FileTools.readLine_stdin().trim();
      if (line.equals("")) {
        return dft;
      }
      else if (!Character.isDigit(line.charAt(0)) && (line.charAt(0) != '-')) {
        Print.sysPrint("Long required, please re-enter] ");
        continue;
      }
      return StringTools.parseLong(line, dft);
    }
  }

  /**
   * Reads double value from standard input. Uses dot "." as decimal delimeter. Prints promt
   * friendly prompt and waits for the input. If nothing entered returns default value. If not
   * numeric value entered re-requests for input.
   * 
   * @param msg prompt message.
   * @param dft default value.
   * @return readed string or default value.
   * @throws IOException if an IO error encountered.
   */
  public static double readDouble_stdin(String msg, double dft) throws IOException {
    if (msg == null) {
      msg = "";
    }
    Print.sysPrintln(msg + "    [Double: default='" + dft + "'] ");
    for (;;) {
      Print.sysPrint("?");
      String line = FileTools.readLine_stdin().trim();
      if (line.equals("")) {
        return dft;
      }
      else if (!Character.isDigit(line.charAt(0)) && (line.charAt(0) != '-')
          && (line.charAt(0) != '.')) {
        Print.sysPrint("Double required, please re-enter] ");
        continue;
      }
      return StringTools.parseDouble(line, dft);
    }
  }

  /**
   * Writes bytes buffer into file.
   * 
   * @param data buffer to write.
   * @param file file handler.
   * @return true if buffer written.
   * @throws IOException if an IO error encountered.
   */
  public static boolean writeFile(byte data[], File file) throws IOException {
    return FileTools.writeFile(data, file, false);
  }

  /**
   * Writes bytes buffer into file. If file is closed, opens the file.
   * 
   * @param data buffer to write.
   * @param file file handler.
   * @param append specifies the writing method. Data will be added at the and of the file instead
   *        of overwriting.
   * @return true if buffer written.
   * @throws IOException if an IO error encountered.
   */
  public static boolean writeFile(byte data[], File file, boolean append) throws IOException {
    if ((data != null) && (file != null)) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(file, append);
        fos.write(data, 0, data.length);
        return true;
      }
      finally {
        try {
          fos.close();
        }
        catch (Throwable t) {/* ignore */
        }
      }
    }
    return false;
  }

  /**
   * Returns file extension.
   * 
   * @param filePath path to the file.
   * @return file extension.
   */
  public static String getExtension(String filePath) {
    File file = new File(filePath);
    String fileName = file.getName();
    int p = fileName.indexOf(".");
    if ((p >= 0) && (p < (fileName.length() - 1))) {
      return fileName.substring(p + 1);
    }
    return "";
  }

  /**
   * Reports whether a file has extension within specified set.
   * 
   * @param filePath path to the file.
   * @param extn specifies extensions set to check within.
   * @return true if file extension is within set.
   */

  public static boolean hasExtension(String filePath, String extn[]) {
    if (filePath != null) {
      String e = getExtension(filePath);
      for (int i = 0; i < extn.length; i++) {
        if (e.equalsIgnoreCase(extn[i])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Clears the file extension.
   * 
   * @param filePath file path.
   * @return new path to the file without extension.
   */
  public static String removeExtension(String filePath) {
    File file = new File(filePath);
    String fileName = file.getName();
    int p = fileName.indexOf(".");
    if (p > 0) { // '.' in column 0 not allowed
      file = new File(file.getParentFile(), fileName.substring(0, p));
    }
    return file.getPath();
  }

  /**
   * Test class to check correctness of standard input methods.
   * 
   * @param argv parameters string, unused.
   * @throws Throwable if an error encountered.
   */
  public static void main(String argv[]) throws Throwable {
    Print.logInfo("Value: " + readString_stdin("Enter a string:", "test"));
    Print.logInfo("Value: " + readBoolean_stdin("Enter a boolean:", false));
    Print.logInfo("Value: " + readLong_stdin("Enter a long:", -1L));
    Print.logInfo("Value: " + readDouble_stdin("Enter a double:", -1.0));
  }

}
