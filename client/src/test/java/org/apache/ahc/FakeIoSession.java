/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.ahc;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.mina.common.*;

public class FakeIoSession implements IoSession {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public IoService getService() {
        return null;
    }

    public IoServiceConfig getServiceConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IoHandler getHandler() {
        return null;
    }

    public IoSessionConfig getConfig() {
        return null;
    }

    public IoFilterChain getFilterChain() {
        return null;
    }

    public WriteFuture write(Object object) {
        return null;
    }

    public CloseFuture close() {
        return null;
    }

    public Object getAttachment() {
        return null;
    }

    public Object setAttachment(Object object) {
        return null;
    }

    public Object getAttribute(String string) {
        return attributes.get(string);
    }

    public Object getAttribute(String key, Object defaultValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object setAttribute(String string, Object object) {
        return attributes.put(string, object);
    }

    public Object setAttribute(String string) {
        return attributes.put(string, null);
    }

    public Object setAttributeIfAbsent(String key, Object value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object removeAttribute(String string) {
        return attributes.remove(string);
    }

    public boolean removeAttribute(String key, Object value) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean replaceAttribute(String key, Object oldValue, Object newValue) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean containsAttribute(String string) {
        return attributes.containsKey(string);
    }

    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    public TransportType getTransportType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isConnected() {
        return false;
    }

    public boolean isClosing() {
        return false;
    }

    public CloseFuture getCloseFuture() {
        return null;
    }

    public SocketAddress getRemoteAddress() {
        return null;
    }

    public SocketAddress getLocalAddress() {
        return null;
    }

    public SocketAddress getServiceAddress() {
        return null;
    }

    public int getIdleTime(IdleStatus idleStatus) {
        return 0;
    }

    public long getIdleTimeInMillis(IdleStatus idleStatus) {
        return 0;
    }

    public void setIdleTime(IdleStatus idleStatus, int i) {
    }

    public int getWriteTimeout() {
        return 0;
    }

    public long getWriteTimeoutInMillis() {
        return 0;
    }

    public void setWriteTimeout(int i) {
    }

    public TrafficMask getTrafficMask() {
        return null;
    }

    public void setTrafficMask(TrafficMask trafficMask) {
    }

    public void suspendRead() {
    }

    public void suspendWrite() {
    }

    public void resumeRead() {
    }

    public void resumeWrite() {
    }

    public long getReadBytes() {
        return 0;
    }

    public long getWrittenBytes() {
        return 0;
    }

    public long getReadMessages() {
        return 0;
    }

    public long getWrittenMessages() {
        return 0;
    }

    public int getScheduledWriteMessages() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getWrittenWriteRequests() {
        return 0;
    }

    public int getScheduledWriteRequests() {
        return 0;
    }

    public int getScheduledWriteBytes() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getCreationTime() {
        return 0;
    }

    public long getLastIoTime() {
        return 0;
    }

    public long getLastReadTime() {
        return 0;
    }

    public long getLastWriteTime() {
        return 0;
    }

    public boolean isIdle(IdleStatus idleStatus) {
        return false;
    }

    public int getIdleCount(IdleStatus idleStatus) {
        return 0;
    }

    public long getLastIdleTime(IdleStatus idleStatus) {
        return 0;
    }
}
