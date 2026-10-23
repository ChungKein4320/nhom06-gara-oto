package gui;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

/** Cột co giãn để lấp đầy vùng hiển thị, chỉ cuộn ngang khi dữ liệu thật sự quá rộng. */
public final class BoCucBang {
    private BoCucBang() { }

    public static void install(JTable tblTable, JScrollPane scrScroll) {
        if (Boolean.TRUE.equals(tblTable.getClientProperty("tableLayout.installed"))) return;
        tblTable.putClientProperty("tableLayout.installed",Boolean.TRUE);
        scrScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
            }
        });
        tblTable.getModel().addTableModelListener(e -> SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll)));
        SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
    }

    public static void fit(JTable tblTable, JScrollPane scrScroll) {
        int columns = tblTable.getColumnCount();
        int available = scrScroll.getViewport().getWidth();
        if (columns == 0 || available <= 0) return;
        int[] widths = new int[columns];
        Object fixedProperty=tblTable.getClientProperty("tableLayout.fixedWidths");
        java.util.Map<?,?> fixed=fixedProperty instanceof java.util.Map<?,?> map ? map : java.util.Map.of();
        int total = 0;
        for (int col = 0; col < columns; col++) {
            TableColumn column = tblTable.getColumnModel().getColumn(col);
            int width = Math.max(90, tblTable.getFontMetrics(tblTable.getFont())
                    .stringWidth(String.valueOf(column.getHeaderValue())) + 28);
            int limit = Math.min(tblTable.getRowCount(), 120);
            for (int row = 0; row < limit; row++) {
                Component rendered=tblTable.prepareRenderer(tblTable.getCellRenderer(row,col),row,col);
                width = Math.max(width,rendered.getPreferredSize().width+8);
            }
            widths[col] = Math.min(width, DinhDangBang.contentAlignment(tblTable,col)==javax.swing.SwingConstants.LEFT ? 320 : 210);
            if(fixed.get(column.getModelIndex()) instanceof Number fixedWidth) widths[col]=fixedWidth.intValue();
            total += widths[col];
        }
        if (total < available) {
            int spare = available - total;
            int flexible=0,last=-1;
            for(int i=0;i<columns;i++) if(!fixed.containsKey(tblTable.convertColumnIndexToModel(i))) { flexible++; last=i; }
            if(flexible>0) {
                int each=spare/flexible;
                for(int i=0;i<columns;i++) if(!fixed.containsKey(tblTable.convertColumnIndexToModel(i))) widths[i]+=each;
                widths[last]+=spare-each*flexible;
            }
        }
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columns; i++) {
            tblTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            tblTable.getColumnModel().getColumn(i).setWidth(widths[i]);
        }
        tblTable.revalidate();
    }
}
