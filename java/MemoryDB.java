import java.util.*;
import java.io.*;

public class MemoryDB {
    private Node head;
    private List<String> header;

    public void setHeader(List<String> header) { this.header = header; }
    public List<String> getHeader() { return header; }
    public Node getHead() { return head; }

    // --- Recursive load of rows into linked list ---
    public void loadRecursively(List<List<String>> rows) {
        head = loadHelper(rows, 0);
    }
    private Node loadHelper(List<List<String>> rows, int i) {
        if (i >= rows.size()) return null;
        Node node = new Node(rows.get(i));
        node.next = loadHelper(rows, i + 1);
        return node;
    }

    // --- Column resolver: name (case-insens) or 0-based index string ---
    public int resolveColumn(String col) {
        try { return Integer.parseInt(col); } catch (Exception ignored) {}
        for (int i = 0; i < header.size(); i++)
            if (header.get(i).equalsIgnoreCase(col)) return i;
        throw new IllegalArgumentException("Column not found: " + col);
    }

    private Double tryParseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }
    private int cmp(Node a, Node b, int colIdx, boolean asc) {
        String sa = a.row.get(colIdx);
        String sb = b.row.get(colIdx);
        int c;
        Double da = tryParseDouble(sa), db = tryParseDouble(sb);
        if (da != null && db != null) c = Double.compare(da, db);
        else c = sa.compareToIgnoreCase(sb);
        return asc ? c : -c;
    }

    // --- Stable bubble sort (swap payloads) ---
    public void bubbleSort(int colIdx, boolean asc) {
        if (head == null) return;
        boolean swapped;
        do {
            swapped = false;
            for (Node cur = head; cur != null && cur.next != null; cur = cur.next) {
                if (cmp(cur, cur.next, colIdx, asc) > 0) {
                    List<String> tmp = cur.row;
                    cur.row = cur.next.row;
                    cur.next.row = tmp;
                    swapped = true;
                }
            }
        } while (swapped);
    }

    // --- Stable insertion sort (relink nodes) ---
    public void insertionSort(int colIdx, boolean asc) {
        Node dummy = new Node(null);
        Node cur = head;
        while (cur != null) {
            Node nxt = cur.next;
            Node prev = dummy;
            // keep <= to be stable (insert after equals)
            while (prev.next != null && cmp(prev.next, cur, colIdx, asc) <= 0) prev = prev.next;
            cur.next = prev.next;
            prev.next = cur;
            cur = nxt;
        }
        head = dummy.next;
    }

    // --- Recursive export to CSV ---
    public void exportRecursively(Writer w) throws IOException {
        w.write(String.join(",", header) + "\n");
        exportHelper(w, head);
    }
    private void exportHelper(Writer w, Node n) throws IOException {
        if (n == null) return;
        List<String> escaped = new ArrayList<>();
        for (String s : n.row) {
            if (s.contains(",") || s.contains("\"")) escaped.add("\"" + s.replace("\"", "\"\"") + "\"");
            else escaped.add(s);
        }
        w.write(String.join(",", escaped) + "\n");
        exportHelper(w, n.next);
    }
}
