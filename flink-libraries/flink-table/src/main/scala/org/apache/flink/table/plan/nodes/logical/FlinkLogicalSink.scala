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

package org.apache.flink.table.plan.nodes.logical

import org.apache.flink.table.plan.metadata.FlinkRelMetadataQuery
import org.apache.flink.table.plan.nodes.FlinkConventions
import org.apache.flink.table.plan.nodes.calcite.{LogicalSink, Sink}
import org.apache.flink.table.sinks.TableSink

import org.apache.calcite.plan.{Convention, RelOptCluster, RelOptRule, RelTraitSet}
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.convert.ConverterRule

import java.util

import scala.collection.JavaConversions._

class FlinkLogicalSink(
    cluster: RelOptCluster,
    traitSet: RelTraitSet,
    input: RelNode,
    sink: TableSink[_],
    sinkName: String)
  extends Sink(cluster, traitSet, input, sink, sinkName)
  with FlinkLogicalRel {

  override def copy(traitSet: RelTraitSet, inputs: util.List[RelNode]): RelNode = {
    new FlinkLogicalSink(cluster, traitSet, inputs.head, sink, sinkName)
  }

  override def isDeterministic: Boolean = true
}

private class FlinkLogicalSinkConverter
  extends ConverterRule(
    classOf[LogicalSink],
    Convention.NONE,
    FlinkConventions.LOGICAL,
    "FlinkLogicalSinkConverter") {

  override def convert(rel: RelNode): RelNode = {
    val sink = rel.asInstanceOf[LogicalSink]
    val newInput = RelOptRule.convert(sink.getInput, FlinkConventions.LOGICAL)
    FlinkLogicalSink.create(
      newInput,
      sink.sink,
      sink.sinkName)
  }
}

object FlinkLogicalSink {
  val CONVERTER: ConverterRule = new FlinkLogicalSinkConverter()

  def create(
      input: RelNode,
      sink: TableSink[_],
      sinkName: String): FlinkLogicalSink = {
    val cluster = input.getCluster
    val traitSet = cluster.traitSetOf(Convention.NONE)
    // FIXME: FlinkRelMdDistribution requires the current RelNode to compute
    // the distribution trait, so we have to create FlinkLogicalSink to
    // calculate the distribution trait
    val sinkNode = new FlinkLogicalSink(
      cluster,
      traitSet,
      input,
      sink,
      sinkName)
    val newTraitSet = FlinkRelMetadataQuery.traitSet(sinkNode)
        .replace(FlinkConventions.LOGICAL).simplify()
    sinkNode.copy(newTraitSet, sinkNode.getInputs).asInstanceOf[FlinkLogicalSink]
  }
}
