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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.factories

import java.util

import org.apache.flink.table.sinks.BatchCompatibleStreamTableSink

/**
  * A factory to create configured stream compatible table sink instances in a
  * batch environment based on string-based properties.
  * See also [[BatchCompatibleStreamTableSink]] for more information.
  *
  * <p>Notes: This is almost a hack for reusing stream retract sink, should be removed.
  *
  * @tparam T type of records that the factory consumes
  */
trait BatchCompatibleTableSinkFactory[T] extends TableFactory {

  /**
    * Creates and configures a [[BatchCompatibleStreamTableSink]]
    * using the given properties.
    *
    * @param properties normalized properties describing a table sink.
    * @return the configured table sink.
    */
  def createBatchCompatibleTableSink(
    properties: util.Map[String, String]): BatchCompatibleStreamTableSink[T]
}
