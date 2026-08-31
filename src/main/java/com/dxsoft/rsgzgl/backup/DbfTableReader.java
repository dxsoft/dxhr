package com.dxsoft.rsgzgl.backup;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DbfTableReader {

    static final Charset DBF_CHARSET = Charset.forName("GBK");

    private DbfTableReader() {
    }

    public static List<Map<String, Object>> readAllRows(Path dbfPath) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (InputStream in = new BufferedInputStream(Files.newInputStream(dbfPath));
                DBFReader reader = new DBFReader(in, DBF_CHARSET)) {
            int fieldCount = reader.getFieldCount();
            List<String> fieldNames = new ArrayList<>(fieldCount);
            for (int i = 0; i < fieldCount; i++) {
                fieldNames.add(reader.getField(i).getName().trim().toLowerCase(Locale.ROOT));
            }
            Object[] record;
            while ((record = reader.nextRecord()) != null) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < fieldCount; i++) {
                    DBFField field = reader.getField(i);
                    row.put(fieldNames.get(i), normalizeValue(field, record[i]));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static Object normalizeValue(DBFField field, Object raw) {
        if (raw == null) {
            return null;
        }
        if (field.getType() == DBFDataType.LOGICAL) {
            boolean flag = raw instanceof Boolean b ? b : "T".equalsIgnoreCase(String.valueOf(raw));
            return flag ? "1" : "0";
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        if (raw instanceof Date date) {
            return new java.sql.Timestamp(date.getTime());
        }
        if (raw instanceof Number number) {
            if (field.getType() == DBFDataType.NUMERIC || field.getType() == DBFDataType.FLOATING_POINT) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            return number;
        }
        return raw;
    }
}
