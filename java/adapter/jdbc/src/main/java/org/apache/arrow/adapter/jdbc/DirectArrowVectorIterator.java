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

package org.apache.arrow.adapter.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.arrow.sql.ArrowResultSet;
import org.apache.arrow.util.Preconditions;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * VectorSchemaRoot iterator directly getting data from {@code ArrowResultSet}.
 */
class DirectArrowVectorIterator implements ArrowVectorIterator {

  private final ArrowResultSet resultSet;

  private VectorSchemaRoot nextBatch;

  /**
   * Construct an instance.
   */
  private DirectArrowVectorIterator(ArrowResultSet resultSet) throws SQLException {
    this.resultSet = resultSet;
  }

  private void load() throws SQLException {
    nextBatch = resultSet.next() ? resultSet.getRoot() : null;
  }

  /**
   * Create a ArrowVectorIterator to partially convert data.
   */
  static DirectArrowVectorIterator create(
      ResultSet resultSet,
      JdbcToArrowConfig config)
      throws SQLException {
    Preconditions.checkArgument(resultSet.isWrapperFor(ArrowResultSet.class));

    DirectArrowVectorIterator iterator = new DirectArrowVectorIterator(resultSet.unwrap(ArrowResultSet.class));
    try {
      iterator.load();
      return iterator;
    } catch (SQLException e) {
      try {
        iterator.close();
      } catch (Exception closeException) {
        e.addSuppressed(closeException);
      }
      throw e;
    }
  }

  @Override
  public boolean hasNext() {
    return nextBatch != null;
  }

  /**
   * Gets the next vector. The user is responsible for freeing its resources.
   */
  @Override
  public VectorSchemaRoot next() {
    Preconditions.checkArgument(hasNext());
    VectorSchemaRoot returned = nextBatch;
    try {
      load();
    } catch (SQLException e) {
      close();
      throw new RuntimeException("Error occurred while getting next schema root.", e);
    }
    return returned;
  }

  /**
   * Clean up resources.
   */
  @Override
  public void close() {
  }
}
