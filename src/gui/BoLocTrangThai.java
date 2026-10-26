package gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/** Kết hợp tìm kiếm và từng cột trạng thái mà không thay đổi model dữ liệu. */
final class BoLocTrangThai extends JPanel {
    private final JTable table;
    private final Map<Integer,JComboBox<String>> filters=new LinkedHashMap<>();
    private final Map<Integer, String> defaultStates = new HashMap<>();
    private String search="";
    private boolean rebuilding,queued;
    private Runnable changed=() -> {};
    BoLocTrangThai(JTable table) {
        super(new FlowLayout(FlowLayout.LEFT,8,3)); this.table=table; setOpaque(false);
        for(int i=0;i<table.getModel().getColumnCount();i++) {
            String name=table.getModel().getColumnName(i);
            if(!OChonTimKiem.normalize(NhanCot.forField(name)).contains("trang thai")) continue;
            JComboBox<String> cmbStatus=new OChonTimKiem<>(); cmbStatus.setPreferredSize(new Dimension(190,30));
            add(GiaoDien.label(NhanCot.forField(name),12,true,GiaoDien.NAV)); add(cmbStatus); filters.put(i,cmbStatus);
            cmbStatus.addActionListener(e -> { if(!rebuilding) apply(); });
        }
        setVisible(!filters.isEmpty());
        table.getModel().addTableModelListener(e -> {
            if(!queued) { queued=true; SwingUtilities.invokeLater(() -> { queued=false; refresh(); }); }
        });
        refresh();
    }
    void onChange(Runnable changed) { this.changed=changed; }
    void search(String value) { search=OChonTimKiem.normalize(value.trim()); apply(); }
    void reset() {
        rebuilding=true;
        try {
            search="";
            for(JComboBox<String> combo:filters.values()) combo.setSelectedItem("Tất cả");
        } finally { rebuilding=false; }
        if(table.getRowSorter()!=null) table.getRowSorter().setSortKeys(null);
        table.clearSelection(); apply();
    }
    void selectDefault(int column, String state) {
        JComboBox<String> combo = filters.get(column);
        if (combo == null) return;

        defaultStates.put(column, state);
        refresh();
        combo.setSelectedItem(state);
    }
    void refresh() {
        rebuilding = true;
        try {
            for (var entry : filters.entrySet()) {
                int column = entry.getKey();
                JComboBox<String> combo = entry.getValue();
                Object selected = combo.getSelectedItem();

                Set<String> values = new TreeSet<>();
                for (int row = 0; row < table.getModel().getRowCount(); row++) {
                    Object value = table.getModel().getValueAt(row, column);
                    if (value != null && !value.toString().isBlank()) {
                        values.add(value.toString());
                    }
                }

                String defaultState = defaultStates.get(column);
                if (defaultState != null) {
                    // Vẫn hiện trạng thái mặc định dù chưa có bản ghi tương ứng.
                    values.add(defaultState);

                    // Giữ lựa chọn của người dùng khi dữ liệu được tải lại.
                    if (selected != null && !"Tất cả".equals(selected)) {
                        values.add(selected.toString());
                    }
                }

                combo.removeAllItems();
                combo.addItem("Tất cả");
                values.forEach(combo::addItem);
                combo.setSelectedItem(
                        selected != null && values.contains(selected.toString())
                                ? selected : "Tất cả"
                );
            }
        } finally {
            rebuilding = false;
        }
        apply();
    }
    @SuppressWarnings("unchecked") private void apply() {
        TableRowSorter<TableModel> sorter;
        if(table.getRowSorter() instanceof TableRowSorter<?> current) sorter=(TableRowSorter<TableModel>)current;
        else { sorter=new TableRowSorter<>(table.getModel()); table.setRowSorter(sorter); }
        sorter.setRowFilter(new RowFilter<>() {
            @Override public boolean include(Entry<? extends TableModel,? extends Integer> row) {
                for(var filter:filters.entrySet()) {
                    Object state=filter.getValue().getSelectedItem();
                    if(state!=null && !state.equals("Tất cả") && !state.toString().equals(row.getStringValue(filter.getKey()))) return false;
                }
                if(search.isEmpty()) return true;
                for(int col=0;col<row.getValueCount();col++) if(OChonTimKiem.normalize(row.getStringValue(col)).contains(search)) return true;
                return false;
            }
        });
        changed.run();
    }
}
