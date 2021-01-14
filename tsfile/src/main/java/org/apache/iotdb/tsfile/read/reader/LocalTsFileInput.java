/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.tsfile.read.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;
import org.apache.iotdb.tsfile.exception.QueryTimeoutRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTsFileInput implements TsFileInput {

  private static final Logger logger = LoggerFactory.getLogger(LocalTsFileInput.class);

  private final FileChannel channel;
  private final String filePath;

  public LocalTsFileInput(Path file) throws IOException {
    channel = FileChannel.open(file, StandardOpenOption.READ);
    filePath = file.toString();
  }

  @Override
  public long size() throws IOException {
    try {
      return channel.size();
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while getting {} size", filePath);
      throw e;
    }
  }

  @Override
  public long position() throws IOException {
    try {
      return channel.position();
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while getting {} current position", filePath);
      throw e;
    }
  }

  @Override
  public TsFileInput position(long newPosition) throws IOException {
    try {
      channel.position(newPosition);
      return this;
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while changing {} position to {}", filePath, newPosition);
      throw e;
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    try {
      return channel.read(dst);
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while reading {} from current position", filePath);
      throw e;
    }
  }

  @Override
  public int read(ByteBuffer dst, long position) throws IOException {
    try {
      return channel.read(dst, position);
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while reading {} from position {}", filePath, position);
      throw e;
    }
  }

  @Override
  public int read() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(byte[] b, int off, int len) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileChannel wrapAsFileChannel() {
    return channel;
  }

  @Override
  public InputStream wrapAsInputStream() {
    return Channels.newInputStream(channel);
  }

  @Override
  public void close() throws IOException {
    try {
      channel.close();
    } catch (ClosedByInterruptException e) {
      throw new QueryTimeoutRuntimeException(
          QueryTimeoutRuntimeException.TIMEOUT_EXCEPTION_MESSAGE);
    } catch (IOException e) {
      logger.error("Error happened while closing {}", filePath);
      throw e;
    }
  }

  @Override
  public int readInt() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String readVarIntString(long offset) throws IOException {
    long position = channel.position();
    channel.position(offset);
    String res = ReadWriteIOUtils.readVarIntString(wrapAsInputStream());
    channel.position(position);
    return res;
  }
}
