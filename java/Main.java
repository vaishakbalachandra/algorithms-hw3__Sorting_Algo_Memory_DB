import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    static class SortKey {
        String algo;   // "b" or "i"
        String col;    // name or index
        SortKey(String a, String c){ algo=a; col=c; }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        // 1) Import file (prompt)
        System.out.print("Enter CSV path [data/student-data.csv]: ");
        String input = sc.nextLine().trim();
        if (input.isEmpty()) input = "data/student-data.csv";

        CsvUtils.Table table = CsvUtils.readCsv(input);
        MemoryDB db = new MemoryDB();
        db.setHeader(table.header);
        db.loadRecursively(table.rows);
        System.out.println("Loaded rows: " + table.rows.size());

        // outputs dir
        String outDir = "java/out/outputs";
        Files.createDirectories(Paths.get(outDir));

        // store multi-level sort keys (first chosen is highest priority)
        List<SortKey> keys = new ArrayList<>();

        while (true) {
            // 2) Ask sort algorithm
            System.out.print("Choose sort algorithm [b=bubble, i=insertion]: ");
            String algo = sc.nextLine().trim().toLowerCase();
            while (!(algo.equals("b") || algo.equals("i"))) {
                System.out.print("Please enter 'b' or 'i': ");
                algo = sc.nextLine().trim().toLowerCase();
            }

            // 3) Ask column (name or 0-based index)
            System.out.print("Sort by which column (name or 0-based index): ");
            String col = sc.nextLine().trim();
            if (col.isEmpty()) {
                System.out.println("Column cannot be empty. Try again.");
                continue;
            }

            // Add to chain (first chosen is primary)
            keys.add(new SortKey(algo, col));

            // Apply multi-level sorting:
            // To keep the FIRST key as highest priority, apply stable sorts in REVERSE order.
            for (int k = keys.size() - 1; k >= 0; k--) {
                SortKey sk = keys.get(k);
                int colIdx = db.resolveColumn(sk.col);
                if (sk.algo.equals("b")) db.bubbleSort(colIdx, true);
                else db.insertionSort(colIdx, true);
            }
            System.out.println("Sorted by keys (primary→secondary): " + describeKeys(keys));

            // 4) Options
            System.out.print("Options: [e=export+quit, ec=export+continue, c=continue, q=quit] : ");
            String opt = sc.nextLine().trim().toLowerCase();
            if (opt.equals("e") || opt.equals("ec")) {
                String outPath = makeExportPath(keys);
                try (BufferedWriter w = Files.newBufferedWriter(Paths.get(outPath))) {
                    db.exportRecursively(w);
                }
                System.out.println("Exported: " + outPath);
                if (opt.equals("e")) { System.out.println("Bye!"); break; }
                // else continue
            } else if (opt.equals("q")) {
                System.out.println("Bye!"); break;
            } else if (opt.equals("c") || opt.isEmpty()) {
                // loop to add another key
            } else {
                System.out.println("Unknown option; continuing.");
            }
        }
    }

    private static String describeKeys(List<SortKey> keys) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<keys.size();i++) {
            if (i>0) sb.append(" > ");
            sb.append(keys.get(i).algo.equals("b") ? "bubble" : "insertion")
              .append("(").append(keys.get(i).col).append(")");
        }
        return sb.toString();
    }

    private static String makeExportPath(List<SortKey> keys) {
        String base = "java/out/outputs/sorted";
        for (SortKey k : keys) base += "_" + (k.algo.equals("b") ? "b" : "i") + "-" + sanitize(k.col);
        base += "_" + System.currentTimeMillis() + ".csv";
        return base;
    }
    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9_-]", "");
    }
}
