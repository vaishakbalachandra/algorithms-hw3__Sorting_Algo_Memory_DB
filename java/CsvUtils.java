import java.nio.file.*;
import java.util.*;

public class CsvUtils {
    public static class Table {
        public List<String> header;
        public List<List<String>> rows;
    }

    public static Table readCsv(String path) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        if (lines.isEmpty()) throw new IllegalArgumentException("Empty CSV: " + path);
        Table t = new Table();
        t.header = parseLine(lines.get(0));
        t.rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String ln = lines.get(i).trim();
            if (!ln.isEmpty()) t.rows.add(parseLine(lines.get(i)));
        }
        return t;
    }

    // CSV parse with quotes
    private static List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i=0; i<line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c=='"') {
                    if (i+1<line.length() && line.charAt(i+1)=='"') { cur.append('"'); i++; }
                    else inQuotes=false;
                } else cur.append(c);
            } else {
                if (c==',') { out.add(cur.toString()); cur.setLength(0); }
                else if (c=='"') inQuotes=true;
                else cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
