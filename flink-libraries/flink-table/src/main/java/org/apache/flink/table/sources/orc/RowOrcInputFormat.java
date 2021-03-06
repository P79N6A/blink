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

package org.apache.flink.table.sources.orc;

import org.apache.flink.core.fs.FileInputSplit;
import org.apache.flink.core.fs.Path;
import org.apache.flink.table.api.types.InternalType;
import org.apache.flink.types.Row;

import org.apache.orc.mapred.OrcStruct;

import java.io.IOException;

/**
 * A subclass of {@link OrcInputFormat} to read from Parquet files and convert to {@link Row}.
 */
@Deprecated
public class RowOrcInputFormat extends OrcInputFormat<Row, OrcStruct> {

	private static final long serialVersionUID = -6522628444326366947L;

	private OrcDeserializer orcDeserializer;
	private transient Row reuse;

	public RowOrcInputFormat(Path filePath, InternalType[] fieldTypes, String[] fieldNames) {
		super(filePath, fieldTypes, fieldNames);

	}

	@Override
	public void open(FileInputSplit fileSplit) throws IOException {
		super.open(fileSplit);
		orcDeserializer = new OrcDeserializer(fieldTypes, fieldNames, columnIds);
	}

	@Override
	public Row convert(OrcStruct current) {
		if (reuse == null) {
			return orcDeserializer.deserialize(current, new Row(fieldNames.length));
		}
		return orcDeserializer.deserialize(current, reuse);
	}
}
