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

public interface DeviceDB {

  public String getAccountName();

  public String getDeviceName();

  public String getDescription();

  public boolean isActive();

  public int getMaxAllowedEvents();

  public long getEventCount(long timeStart, long timeEnd);

  public int getLimitTimeInterval();

  public int getMaxTotalConnections();

  public int getMaxTotalConnectionsPerMinute();

  public byte[] getTotalConnectionProfile();

  public void setTotalConnectionProfile(byte[] profile);

  public long getLastTotalConnectionTime();

  public void setLastTotalConnectionTime(long connectTime);

  public int getMaxDuplexConnections();

  public int getMaxDuplexConnectionsPerMinute();

  public byte[] getDuplexConnectionProfile();

  public void setDuplexConnectionProfile(byte[] profile);

  public long getLastDuplexConnectionTime();

  public void setLastDuplexConnectionTime(long connectTime);

  public boolean supportsEncoding(int encoding);

  public void removeEncoding(int encoding);

  public boolean addClientPayloadTemplate(PayloadTemplate template);

  public PayloadTemplate getClientPayloadTemplate(int custType);

  public int insertEvent(GeoEvent event);

}
