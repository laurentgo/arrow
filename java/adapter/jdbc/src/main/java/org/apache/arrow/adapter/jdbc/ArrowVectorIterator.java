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
import java.util.Iterator;

import org.apache.arrow.sql.ArrowResultSet;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * VectorSchemaRoot iterator over JDBC data.
 */
public interface ArrowVectorIterator extends Iterator<VectorSchemaRoot>, AutoCloseable {
  @Override
  void close();

  /**
   * Create a ArrowVectorIterator to partially convert data.
   */
  static ArrowVectorIterator create(ResultSet resultSet, JdbcToArrowConfig config)
      throws SQLException {
    if (resultSet.isWrapperFor(ArrowResultSet.class)) {
      return DirectArrowVectorIterator.create(resultSet, config);
    }
    return IndirectArrowVectorIterator.create(resultSet, config);
  }
}
