/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.arrow.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Wrapper;

import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.types.pojo.Schema;

/**
 * An interface to unwrap {@link ResultSet} into a sequence of Arrow vectors.
 *
 * The interface is intended for JDBC drivers using Arrow format internally, as a way
 * to bypass deserialization and directly consume arrow vectors.
 *
 */
public interface ArrowResultSet extends Wrapper, AutoCloseable {

  /**
   * Moves the cursor to the next vector.
   *
   * The cursor is initially positioned before the first vector.
   *
   * Any call to <code>next</code> method invalidates the current root vector
   * before attempting to fetch the next one.
   *
   * When the method returns <code>false</code>, any attempt at accessing
   * the root vector will throw <code>SQLException</code>.
   * <code>false</code> if there are no more vectors
   *
   * @return <code>true</code> if the new current vector is valid.
   * @throws SQLException if a database exception occurs
   */
  boolean next() throws SQLException;

  /**
   * Closes the result set and all resources associated with it (including
   * the source {@code} ResultSet).
   *
   * @throws SQLException if a database exception occurs
   */
  @Override
  void close() throws SQLException;

  /**
   * Retrieves if the result set has been closed, or not.
   *
   * @return <code>true</code> if the result is closed,
   * <code>false</code> otherwise.
   * @throws SQLException if a database exception occurs
   */
  boolean isClosed() throws SQLException;

  /**
   * Get the current vector data from the stream.
   *
   * <p>The data in the root may change at any time. Clients should NOT modify the root, but instead unload the data
   * into their own root.
   *
   * @throws SQLException if there was an error reading the schema from the stream.
   */
  VectorSchemaRoot getRoot() throws SQLException;

  /**
   * Get the schema for this stream.
   */
  Schema getSchema() throws SQLException;

  /**
   * Get the provider for dictionaries in this stream.
   *
   * <p>Does NOT retain a reference to the underlying dictionaries. Dictionaries may be updated as the stream is read.
   * This method is intended for stream processing, where the application code will not retain references to values
   * after the stream is closed.
   */
  DictionaryProvider getDictionaryProvider() throws SQLException;
}
