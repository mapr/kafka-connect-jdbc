package io.confluent.connect.jdbc.dialect;

import io.confluent.connect.jdbc.source.TimestampIncrementingCriteria;
import io.confluent.connect.jdbc.util.ColumnId;
import io.confluent.connect.jdbc.util.ExpressionBuilder;
import io.confluent.connect.jdbc.util.TableId;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HiveDatabaseDialectTest extends BaseDialectTest<HiveDatabaseDialect> {

  @Override
  protected HiveDatabaseDialect createDialect() {
    return new HiveDatabaseDialect(sourceConfigWithUrl("jdbc:hive2://something"));
  }

  @Test
  public void shouldBuildTimestampIncrementingCriteriaWithoutDbName() {
    String expected = " WHERE \"myTable\".\"myColumn\" > ? ORDER BY \"myTable\".\"myColumn\" ASC";

    ExpressionBuilder builder = dialect.expressionBuilder();
    TableId tableWithDbNameId = new TableId(null, "myDb", "myTable");
    ColumnId columnWithDbNameId = new ColumnId(tableWithDbNameId, "myColumn");
    TimestampIncrementingCriteria criteria = dialect.criteriaFor(columnWithDbNameId, Collections.emptyList());
    criteria.whereClause(builder);

    assertEquals(expected, builder.toString());
  }
}