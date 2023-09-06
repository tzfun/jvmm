package org.beifengtz.jvmm.client.fomatter;

import java.util.ArrayList;
import java.util.List;

/**
 * description TODO
 * date 17:28 2023/9/6
 *
 * @author beifengtz
 */
public class TableFormatter implements Formatter {

    private String[] head;
    private final List<String[]> rows = new ArrayList<>();
    int column = 0;

    private void checkColumn(String... row) {
        if (column == 0) {
            column = row.length;
        } else if (column != row.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    public TableFormatter setHead(String... head) {
        checkColumn(head);
        this.head = head;
        return this;
    }

    public TableFormatter addRow(String... row) {
        checkColumn(row);
        rows.add(row);
        return this;
    }

    public TableFormatter deleteRow(int index) {
        rows.remove(index);
        if (head == null && rows.isEmpty()) {
            column = 0;
        }
        return this;
    }

    public TableFormatter setRow(int index, String... row) {
        checkColumn(row);
        rows.set(index, row);
        return this;
    }

    @Override
    public String toString() {
        if (column == 0) {
            return "";
        }
        int[] colLen = new int[column];
        if (head != null) {
            calculateMaxColLen(colLen, head);
        }

        for (String[] row : rows) {
            calculateMaxColLen(colLen, row);
        }

        for (int i = 0; i < colLen.length; i++) {
            colLen[i]++;
        }

        StringBuilder table = new StringBuilder();
        table.append(drawBorder(colLen)).append("\n");
        if (head != null) {
            table.append(drawLine(colLen, head)).append("\n");
        }
        table.append(drawBorder(colLen)).append("\n");
        for (String[] row : rows) {
            table.append(drawLine(colLen, row)).append("\n");
        }
        table.append(drawBorder(colLen));
        return table.toString();
    }

    @Override
    public void print() {
        if (column == 0) {
            return;
        }
        System.out.println(this);
    }

    private void calculateMaxColLen(int[] colLenContainer, String... row) {
        for (int i = 0; i < row.length; i++) {
            String content = row[i];
            if (content == null) {
                continue;
            }
            colLenContainer[i] = Math.max(colLenContainer[i], content.codePointCount(0, content.length()));
        }
    }

    private String drawBorder(int[] colLen) {
        StringBuilder line = new StringBuilder("+");
        for (int len : colLen) {
            for (int i = 0; i < len; i++) {
                line.append("-");
            }
            line.append("+");
        }
        return line.toString();
    }

    private String drawLine(int[] colLen, String[] row) {
        StringBuilder line = new StringBuilder("|");
        for (int i = 0; i < row.length; i++) {
            String s = row[i];
            line.append(s);
            int fill = colLen[i] - s.length();
            for (int j = 0; j < fill; j++) {
                line.append(" ");
            }
            line.append("|");
        }
        return line.toString();
    }
}
