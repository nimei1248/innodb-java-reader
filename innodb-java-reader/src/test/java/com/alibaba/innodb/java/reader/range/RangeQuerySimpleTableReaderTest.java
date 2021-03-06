package com.alibaba.innodb.java.reader.range;

import com.alibaba.innodb.java.reader.AbstractTest;
import com.alibaba.innodb.java.reader.TableReader;
import com.alibaba.innodb.java.reader.page.index.GenericRecord;
import com.alibaba.innodb.java.reader.schema.Column;
import com.alibaba.innodb.java.reader.schema.Schema;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author xu.zx
 */
public class RangeQuerySimpleTableReaderTest extends AbstractTest {

  public Schema getSchema() {
    return new Schema()
        .addColumn(new Column().setName("id").setType("int(11)").setNullable(false).setPrimaryKey(true))
        .addColumn(new Column().setName("a").setType("bigint(20)").setNullable(false))
        .addColumn(new Column().setName("b").setType("varchar(64)").setNullable(false))
        .addColumn(new Column().setName("c").setType("varchar(1024)").setNullable(true));
  }

  @Test
  public void testSimpleTableRangeQueryAllMysql56() {
    testSimpleTableRangeQueryAll(IBD_FILE_BASE_PATH_MYSQL56 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryAllMysql57() {
    testSimpleTableRangeQueryAll(IBD_FILE_BASE_PATH_MYSQL57 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryAllMysql80() {
    testSimpleTableRangeQueryAll(IBD_FILE_BASE_PATH_MYSQL80 + "simple/tb01.ibd");
  }

  public void testSimpleTableRangeQueryAll(String path) {
    try (TableReader reader = new TableReader(path, getSchema())) {
      reader.open();

      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(0, 11);
      assertThat(recordList.size(), is(10));

      int index = 0;
      for (int i = 1; i <= 10; i++) {
        GenericRecord record = recordList.get(index++);
        Object[] values = record.getValues();
        System.out.println(Arrays.asList(values));
        assertThat(values[0], is(i));
        assertThat(values[1], is(i * 2L));
        assertThat(values[2], is(StringUtils.repeat('A', 16)));
        assertThat(values[3], is(StringUtils.repeat('C', 8) + (char) (97 + i)));
      }
    }
  }

  @Test
  public void testSimpleTableRangeQueryLowerNothing() {
    try (TableReader reader = new TableReader(IBD_FILE_BASE_PATH + "simple/tb01.ibd", getSchema())) {
      reader.open();
      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(-1, 0);
      assertThat(recordList.size(), is(0));

      recordList = reader.rangeQueryByPrimaryKey(0, 0);
      assertThat(recordList.size(), is(0));

      recordList = reader.rangeQueryByPrimaryKey(11, 11);
      assertThat(recordList.size(), is(0));

      recordList = reader.rangeQueryByPrimaryKey(12, 20);
      assertThat(recordList.size(), is(0));
    }
  }

  @Test
  public void testSimpleTableRangeQueryPartMysql56() {
    testSimpleTableRangeQueryPart(IBD_FILE_BASE_PATH_MYSQL56 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryPartMysql57() {
    testSimpleTableRangeQueryPart(IBD_FILE_BASE_PATH_MYSQL57 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryPartMysql80() {
    testSimpleTableRangeQueryPart(IBD_FILE_BASE_PATH_MYSQL80 + "simple/tb01.ibd");
  }

  public void testSimpleTableRangeQueryPart(String path) {
    try (TableReader reader = new TableReader(path, getSchema())) {
      reader.open();
      rangeQuery(reader, 3, 8);
      rangeQuery(reader, 1, 9);
      rangeQuery(reader, 6, 6);
    }
  }

  private void rangeQuery(TableReader reader, int start, int end) {
    List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(start, end);
    if (end == start) {
      end++;
    }
    assertThat(recordList.size(), is(end - start));
    int index = 0;
    for (int i = start; i < end; i++) {
      GenericRecord record = recordList.get(index++);
      Object[] values = record.getValues();
      System.out.println(Arrays.asList(values));
      assertThat(values[0], is(i));
      assertThat(values[1], is(i * 2L));
      assertThat(values[2], is(StringUtils.repeat('A', 16)));
      assertThat(values[3], is(StringUtils.repeat('C', 8) + (char) (97 + i)));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSimpleTableRangeQueryLowerUpperNotValid() {
    try (TableReader reader = new TableReader(IBD_FILE_BASE_PATH + "simple/tb01.ibd", getSchema())) {
      reader.open();
      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(12, 5);
    }
  }

  @Test
  public void testSimpleTableRangeQueryHalfOpenHalfCloseMysql56() {
    testSimpleTableRangeQueryHalfOpenHalfClose(IBD_FILE_BASE_PATH_MYSQL56 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryHalfOpenHalfCloseMysql57() {
    testSimpleTableRangeQueryHalfOpenHalfClose(IBD_FILE_BASE_PATH_MYSQL57 + "simple/tb01.ibd");
  }

  @Test
  public void testSimpleTableRangeQueryHalfOpenHalfCloseMysql80() {
    testSimpleTableRangeQueryHalfOpenHalfClose(IBD_FILE_BASE_PATH_MYSQL80 + "simple/tb01.ibd");
  }

  public void testSimpleTableRangeQueryHalfOpenHalfClose(String path) {
    try (TableReader reader = new TableReader(path, getSchema())) {
      reader.open();

      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(0, null);
      assertThat(recordList.size(), is(10));

      recordList = reader.rangeQueryByPrimaryKey(1, null);
      assertThat(recordList.size(), is(10));

      recordList = reader.rangeQueryByPrimaryKey(5, null);
      assertThat(recordList.size(), is(6));

      recordList = reader.rangeQueryByPrimaryKey(null, 100);
      assertThat(recordList.size(), is(10));

      recordList = reader.rangeQueryByPrimaryKey(null, 6);
      assertThat(recordList.size(), is(5));

      recordList = reader.rangeQueryByPrimaryKey(null, 1);
      assertThat(recordList.size(), is(0));

      recordList = reader.rangeQueryByPrimaryKey(null, null);
      assertThat(recordList.size(), is(10));
    }
  }

  @Test
  public void testSimpleTableRangeQueryStringKey() {
    try (TableReader reader = new TableReader(IBD_FILE_BASE_PATH_MYSQL56 + "simple/tb01.ibd", getSchema())) {
      reader.open();

      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey("0", null);
      assertThat(recordList.size(), is(10));

      recordList = reader.rangeQueryByPrimaryKey("1", 8);
      assertThat(recordList.size(), is(7));
    }
  }

  @Test
  public void testSimpleTableRangeQueryWithRecordPredicate() {
    try (TableReader reader = new TableReader(IBD_FILE_BASE_PATH_MYSQL56 + "simple/tb01.ibd", getSchema())) {
      reader.open();

      Predicate<GenericRecord> predicate = r -> (long) (r.get("a")) == 12L;

      List<GenericRecord> recordList = reader.rangeQueryByPrimaryKey(4, 9, predicate);
      assertThat(recordList.size(), is(1));
      assertThat(recordList.get(0).getPrimaryKey(), is(6));
      assertThat(recordList.get(0).get("a"), is(12L));
    }
  }
}
