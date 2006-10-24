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
// ----------------------------------------------------------------------------
package org.opendmtp.server.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import org.opendmtp.util.*;

public class Payload
{

    // ------------------------------------------------------------------------

    public static final int     MAX_PAYLOAD_LENGTH = 255;

    // ------------------------------------------------------------------------

    private byte        payload[] = null;
    private int         size = 0;
    private int         index = 0;

    public Payload()
    {
        // configure for creating a new packet (data destination)
        this.payload = new byte[MAX_PAYLOAD_LENGTH];
        this.size    = 0; // no 'size' yet
        this.index   = 0; // start at index '0' for writing
    }
    
    public Payload(byte b[])
    {
        this(b, 0, ((b != null)? b.length : 0));
    }
    
    public Payload(byte b[], int ofs, int len)
    {
        // (data source)
        if ((b == null) || (ofs >= b.length)) {
            this.payload = new byte[0];
            this.size    = 0;
            this.index   = 0;
        } else
        if ((ofs == 0) && (b.length == len)) {
            this.payload = b;
            this.size    = b.length;
            this.index   = 0;
        } else {
            if (len > (b.length - ofs)) { len = b.length - ofs; }
            this.payload = new byte[len];
            System.arraycopy(b, ofs, this.payload, 0, len);
            this.size    = len;
            this.index   = 0;
        }
    }
    
    // ------------------------------------------------------------------------

    public int getSize()
    {
        return this.size;
    }
    
    // ------------------------------------------------------------------------

    private byte[] _getBytes()
    {
        return this.payload;
    }
    
    public byte[] getBytes()
    {
        // return the full payload (regardless of the state of 'this.index')
        byte b[] = this._getBytes();
        if (this.size == b.length) {
            return b;
        } else {
            byte n[] = new byte[this.size];
            System.arraycopy(b, 0, n, 0, this.size);
            return n;
        }
    }
    
    // ------------------------------------------------------------------------

    public void resetIndex()
    {
        // this makes Payload a data source
        this.resetIndex(0);
    }

    public void resetIndex(int ndx)
    {
        this.index = (ndx <= 0)? 0 : ndx;
    }
    
    public boolean isValidLength(int length)
    {
        return ((this.index + length) <= this.size);
    }
    // ------------------------------------------------------------------------
    
    private static long _decodeLong(byte data[], int ofs, int len, boolean signed, long dft)
    {
        // Big-Endian order
        // { 0x01, 0x02, 0x03 } -> 0x010203
        if ((data != null) && (data.length >= (ofs + len))) {
            long n = (signed && ((data[ofs] & 0x80) != 0))? -1L : 0L;
            for (int i = ofs; i < ofs + len; i++) {
                n = (n << 8) | ((long)data[i] & 0xFF); 
            }
            return n;
        } else {
            return dft;
        }
    }

    public long readLong(int length, long dft)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // nothing to read
            return dft;
        } else {
            byte b[] = this._getBytes();
            long val = _decodeLong(b, this.index, maxLen, true, dft);
            this.index += maxLen;
            return val;
        }
    }

    public long readLong(int length)
    {
        return this.readLong(length, 0L);
    }
    
    // ------------------------------------------------------------------------

    public long readULong(int length, long dft)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // nothing to read
            return dft;
        } else {
            byte b[] = this._getBytes();
            long val = _decodeLong(b, this.index, maxLen, false, dft);
            this.index += maxLen;
            return val;
        }
    }

    public long readULong(int length)
    {
        return this.readULong(length, 0L);
    }
    
    // ------------------------------------------------------------------------

    public byte[] readBytes(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // no room left
            return new byte[0];
        } else {
            byte n[] = new byte[maxLen];
            System.arraycopy(this._getBytes(), this.index, n, 0, maxLen);
            this.index += maxLen;
            return n;
        }
    }
    
    // ------------------------------------------------------------------------

    public String readString(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen <= 0) {
            // no room left
            return "";
        } else {
            int m;
            byte b[] = this._getBytes();
            for (m = 0; (m < maxLen) && ((this.index + m) < this.size) && (b[this.index + m] != 0); m++);
            String s = StringTools.toStringValue(b, this.index, m);
            this.index += m;
            if (m < maxLen) { this.index++; }
            return s;
        }
    }
    
    // ------------------------------------------------------------------------

    public GeoPoint readGPS(int length)
    {
        int maxLen = ((this.index + length) <= this.size)? length : (this.size - this.index);
        if (maxLen < 6) {
            // not enough bytes to decode GeoPoint
            GeoPoint gp = new GeoPoint();
            if (maxLen > 0) { this.index += maxLen; }
            return gp;
        } else
        if (length < 8) {
            // 6 <= len < 8
            GeoPoint gp = GeoPoint.decodeGeoPoint(this._getBytes(), this.index, length);
            this.index += maxLen; // 6
            return gp;
        } else {
            // 8 <= len
            GeoPoint gp = GeoPoint.decodeGeoPoint(this._getBytes(), this.index, length);
            this.index += maxLen; // 8
            return gp;
        }
    }

    // ------------------------------------------------------------------------
    
    private static int _encodeLong(byte data[], int ofs, int len, long val)
    {
        // Big-Endian order
        if ((data != null) && (data.length >= (ofs + len))) {
            long n = val;
            for (int i = (ofs + len - 1); i >= ofs; i--) {
                data[i] = (byte)(n & 0xFF);
                n >>>= 8;
            }
            return len;
        } else {
            return 0;
        }
    }

    public int writeLong(long val, int length)
    {
        byte b[] = this._getBytes();
        if (length <= 0) {
            // nothing to write
            return length;
        } else
        if ((this.index + length) > b.length) {
            // no room left
            return 0;
        } else {
            _encodeLong(b, this.index, length, val);
            this.index += length;
            if (this.size < this.index) { this.size = this.index; }
            return length;
        }
    }

    public int writeULong(long val, int length)
    {
        return this.writeLong(val, length);
    }
    
    // ------------------------------------------------------------------------

    public int writeBytes(byte n[], int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if ((n == null) || (n.length == 0)) {
            // nothing to write
            return 0;
        } else
        if (maxLen <= 0) {
            // no room left
            return 0;
        } else {
            int m = (n.length < maxLen)? n.length : maxLen;
            System.arraycopy(n, 0, b, this.index, m);
            for (;m < maxLen; m++) { b[m] = 0; }
            this.index += m;
            if (this.size < this.index) { this.size = this.index; }
            return m;
        }
    }

    // ------------------------------------------------------------------------

    public int writeString(String s, int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if (s == null) {
            // nothing to write
            return 0;
        } else
        if (maxLen <= 0) {
            // no room left
            return 0;
        } else {
            byte n[] = StringTools.getBytes(s);
            int m = (n.length < maxLen)? n.length : maxLen;
            System.arraycopy(n, 0, b, this.index, m);
            this.index += m;
            if (m < maxLen) { 
                b[this.index++] = (byte)0; // terminate string
                m++;
            }
            if (this.size < this.index) { this.size = this.index; }
            return m;
        }
    }

    // ------------------------------------------------------------------------

    public int writeGPS(GeoPoint gp, int length)
    {
        byte b[] = this._getBytes();
        int maxLen = ((this.index + length) <= b.length)? length : (b.length - this.index);
        if (maxLen < 6) {
            // not enough bytes to encode GeoPoint
            return 0;
        } else
        if (length < 8) {
            // 6 <= len < 8
            GeoPoint.encodeGeoPoint(gp, b, this.index, length);
            this.index += 6;
            if (this.size < this.index) { this.size = this.index; }
            return 6;
        } else {
            // 8 <= len
            GeoPoint.encodeGeoPoint(gp, b, this.index, length);
            this.index += 8;
            if (this.size < this.index) { this.size = this.index; }
            return 8;
        }
    }

    // ------------------------------------------------------------------------

    public String toString()
    {
        return StringTools.toHexString(this.payload, 0, this.size);
    }
    
    // ------------------------------------------------------------------------
    
}

