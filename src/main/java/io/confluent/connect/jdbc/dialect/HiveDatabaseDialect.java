/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License; you may not use this file
 * except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.connect.jdbc.dialect;

import io.confluent.connect.jdbc.dialect.DatabaseDialectProvider.SubprotocolBasedProvider;
import io.confluent.connect.jdbc.source.TimestampIncrementingCriteria;
import io.confluent.connect.jdbc.util.ColumnDefinition;
import io.confluent.connect.jdbc.util.ColumnId;
import io.confluent.connect.jdbc.util.TableId;
import org.apache.kafka.common.config.AbstractConfig;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link DatabaseDialect} for Hive.
 */
public class HiveDatabaseDialect extends GenericDatabaseDialect {
  /**
   * The provider for {@link HiveDatabaseDialect}.
   */
  public static class Provider extends SubprotocolBasedProvider {
    public Provider() {
      super(HiveDatabaseDialect.class.getSimpleName(), "hive", "hive2");
    }

    @Override
    public DatabaseDialect create(AbstractConfig config) {
      return new HiveDatabaseDialect(config);
    }
  }

  /**
   * Create a new dialect instance with the given connector configuration.
   *
   * @param config the connector configuration; may not be null
   */
  public HiveDatabaseDialect(AbstractConfig config) {
    super(config);
  }

  @Override
  public TableId parseTableIdentifier(String fqn) {
    List<String> parts = identifierRules().parseQualifiedIdentifier(fqn)
            .stream().map(p -> p.trim()).collect(Collectors.toList());
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Invalid fully qualified name: '" + fqn + "'");
    }
    if (parts.size() == 1) {
      return new TableId(null, null, parts.get(0));
    }
    if (parts.size() == 3) {
      return new TableId(parts.get(0), parts.get(1), parts.get(2));
    }
    assert parts.size() >= 2;
    if (useCatalog()) {
      return new TableId(parts.get(0), null, parts.get(1));
    }
    return new TableId(null, parts.get(0), parts.get(1));
  }

  @Override
  protected ColumnDefinition describeColumn(
      ResultSetMetaData rsMetadata,
      int column
  ) throws SQLException {
    // String catalog = rsMetadata.getCatalogName(column);
    // String schema = rsMetadata.getSchemaName(column);
    // String tableName = rsMetadata.getTableName(column);
    String catalog = null;
    String schema = null;
    String tableName = null;
    TableId tableId = new TableId(catalog, schema, tableName);
    String name = rsMetadata.getColumnName(column);
    String alias = rsMetadata.getColumnLabel(column);
    ColumnId id = new ColumnId(tableId, name, alias);
    ColumnDefinition.Nullability nullability;
    switch (rsMetadata.isNullable(column)) {
      case ResultSetMetaData.columnNullable:
        nullability = ColumnDefinition.Nullability.NULL;
        break;
      case ResultSetMetaData.columnNoNulls:
        nullability = ColumnDefinition.Nullability.NOT_NULL;
        break;
      case ResultSetMetaData.columnNullableUnknown:
      default:
        nullability = ColumnDefinition.Nullability.UNKNOWN;
        break;
    }
    ColumnDefinition.Mutability mutability = ColumnDefinition.Mutability.READ_ONLY;
    return new ColumnDefinition(
        id,
        rsMetadata.getColumnType(column),
        rsMetadata.getColumnTypeName(column),
        rsMetadata.getColumnClassName(column),
        nullability,
        mutability,
        rsMetadata.getPrecision(column),
        rsMetadata.getScale(column),
        // rsMetadata.isSigned(column),
        false,
        rsMetadata.getColumnDisplaySize(column),
        rsMetadata.isAutoIncrement(column),
        rsMetadata.isCaseSensitive(column),
        // rsMetadata.isSearchable(column),
        false,
        rsMetadata.isCurrency(column),
        false
    );
  }

  @Override
  public TimestampIncrementingCriteria criteriaFor(
      ColumnId incrementingColumn,
      List<ColumnId> timestampColumns
  ) {
    ColumnId validIncrementingColumn = validateForHive(incrementingColumn);
    List<ColumnId> validTimestampColumns = timestampColumns.stream()
            .map(this::validateForHive).collect(Collectors.toList());
    return new TimestampIncrementingCriteria(validIncrementingColumn,
            validTimestampColumns, timeZone());
  }

  private ColumnId validateForHive(ColumnId columnId) {
    TableId tableId = columnId.tableId();
    if ((tableId == null) || ((tableId.catalogName() == null) && (tableId.schemaName() == null))) {
      return columnId;
    } else {
      TableId validTableId = new TableId(null, null, tableId.tableName());
      return new ColumnId(validTableId, columnId.name());
    }
  }

  /**
   * Determine the name of the field. By default this is the column alias or name.
   *
   * @param columnDefinition the column definition; never null
   * @return the field name; never null
   */
  @Override
  protected String fieldNameFor(ColumnDefinition columnDefinition) {
    String fullFieldName = columnDefinition.id().aliasOrName();
    return fullFieldName.substring(fullFieldName.lastIndexOf('.') + 1);
  }
}
