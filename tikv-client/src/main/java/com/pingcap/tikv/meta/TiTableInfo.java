/*
 * Copyright 2017 PingCAP, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pingcap.tikv.meta;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.pingcap.tidb.tipb.TableInfo;
import com.pingcap.tikv.exception.TiClientInternalException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TiTableInfo implements Serializable {
  private final long id;
  private final String name;
  private final String charset;
  private final String collate;
  private final List<TiColumnInfo> columns;
  private final List<TiIndexInfo> indices;
  private final boolean pkIsHandle;
  private final String comment;
  private final long autoIncId;
  private final long maxColumnId;
  private final long maxIndexId;
  private final long oldSchemaId;

  @JsonCreator
  public TiTableInfo(
      @JsonProperty("id") long id,
      @JsonProperty("name") CIStr name,
      @JsonProperty("charset") String charset,
      @JsonProperty("collate") String collate,
      @JsonProperty("pk_is_handle") boolean pkIsHandle,
      @JsonProperty("cols") List<TiColumnInfo> columns,
      @JsonProperty("index_info") List<TiIndexInfo> indices,
      @JsonProperty("comment") String comment,
      @JsonProperty("auto_inc_id") long autoIncId,
      @JsonProperty("max_col_id") long maxColumnId,
      @JsonProperty("max_idx_id") long maxIndexId,
      @JsonProperty("old_schema_id") long oldSchemaId) {
    this.id = id;
    this.name = name.getL();
    this.charset = charset;
    this.collate = collate;
    this.columns = ImmutableList.copyOf(requireNonNull(columns, "columns is null"));
    this.pkIsHandle = pkIsHandle;
    this.indices = indices != null ? ImmutableList.copyOf(indices) : ImmutableList.of();
    this.comment = comment;
    this.autoIncId = autoIncId;
    this.maxColumnId = maxColumnId;
    this.maxIndexId = maxIndexId;
    this.oldSchemaId = oldSchemaId;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCharset() {
    return charset;
  }

  public String getCollate() {
    return collate;
  }

  public List<TiColumnInfo> getColumns() {
    return columns;
  }

  public TiColumnInfo getColumn(int offset) {
    if (offset < 0 || offset >= columns.size()) {
      throw new TiClientInternalException(String.format("Column offset %d out of bound", offset));
    }
    return columns.get(offset);
  }

  public boolean isPkHandle() {
    return pkIsHandle;
  }

  public List<TiIndexInfo> getIndices() {
    return indices;
  }

  public String getComment() {
    return comment;
  }

  public long getAutoIncId() {
    return autoIncId;
  }

  public long getMaxColumnId() {
    return maxColumnId;
  }

  public long getMaxIndexId() {
    return maxIndexId;
  }

  public long getOldSchemaId() {
    return oldSchemaId;
  }

  public TableInfo toProto() {
    return TableInfo.newBuilder()
        .setTableId(getId())
        .addAllColumns(
            getColumns().stream().map(col -> col.toProto(this)).collect(Collectors.toList()))
        .build();
  }

  // Only Integer Column will be a PK column
  // and there exists only one PK column
  public TiColumnInfo getPrimaryKeyColumn() {
    if (isPkHandle()) {
      for (TiColumnInfo col : getColumns()) {
        if (col.isPrimaryKey()) {
          return col;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return toProto().toString();
  }
}
