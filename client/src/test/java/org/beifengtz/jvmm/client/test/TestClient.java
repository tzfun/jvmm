package org.beifengtz.jvmm.client.test;

import org.beifengtz.jvmm.client.fomatter.TableFormatter;
import org.junit.jupiter.api.Test;

/**
 * description TODO
 * date 17:59 2023/9/6
 *
 * @author beifengtz
 */
public class TestClient {
    @Test
    public void testTableFormatter() {
        TableFormatter table = new TableFormatter();
        table.setHead("ID", "Name");
        table.addRow("1", "1");
        table.addRow("1", "12");
        table.addRow("1", "123");
        table.addRow("1", "1234");
        table.addRow("1", "12345");
        table.addRow("12", "12345");
        table.addRow("123", "12345");
        table.addRow("1234", "12345");
        table.addRow("12345", "12345");
        table.print();
    }
}
