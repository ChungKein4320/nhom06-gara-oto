package gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

final class BoLocTrangThai extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTable table;
    private final Map<Integer, JComboBox<String>> filters = new LinkedHashMap<>();
    private final Map<Integer, String> defaultStates = new HashMap<>();
    private final Map<Integer, List<String>> predefinedOptions = new LinkedHashMap<>();
    private String search = "";
    private boolean rebuilding, queued;
    private Runnable changed = () -> {};

    BoLocTrangThai(JTable table) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 3));
        this.table = table;
        setOpaque(false);
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            String name = table.getModel().getColumnName(i);
            if (!OChonTimKiem.normalize(NhanCot.forField(name)).contains("trang thai")) continue;
            JComboBox<String> cmbStatus = new OChonTimKiem<>();
            cmbStatus.setEditable(false);
            cmbStatus.setPreferredSize(new Dimension(190, 30));
            add(GiaoDien.label(NhanCot.forField(name), 12, true, GiaoDien.NAV));
            add(cmbStatus);
            filters.put(i, cmbStatus);
            cmbStatus.addActionListener(e -> {
                if (!rebuilding) apply();
            });
        }
        setVisible(!filters.isEmpty());
        table.getModel().addTableModelListener(e -> {
            if (rebuilding) return;
            if (!queued) {
                queued = true;
                SwingUtilities.invokeLater(() -> {
                    queued = false;
                    if (!rebuilding) refresh();
                });
            }
        });
        refresh();
    }

    void onChange(Runnable changed) {
        this.changed = changed;
    }

    void search(String value) {
        search = OChonTimKiem.normalize(value.trim());
        apply();
    }

    void reset() {
        rebuilding = true;
        try {
            search = "";
            for (JComboBox<String> combo : filters.values()) {
                combo.setSelectedItem("Tất cả");
            }
        } finally {
            rebuilding = false;
        }
        if (table.getRowSorter() != null) table.getRowSorter().setSortKeys(null);
        table.clearSelection();
        apply();
    }

    void setPredefinedOptions(int column, List<String> options) {
        if (options != null && !options.isEmpty()) {
            predefinedOptions.put(column, new ArrayList<>(options));
        } else {
            predefinedOptions.remove(column);
        }
        refresh();
    }

    void selectDefault(int column, String state) {
        JComboBox<String> combo = filters.get(column);
        if (combo == null) return;

        defaultStates.put(column, state);
        combo.setSelectedItem(state);
        apply();
    }

    JComboBox<String> getFilter(int column) {
        return filters.get(column);
    }

    List<String> getFilterOptions(int column) {
        JComboBox<String> combo = filters.get(column);
        if (combo == null) return Collections.emptyList();
        List<String> res = new ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            res.add(combo.getItemAt(i));
        }
        return res;
    }

    void refresh() {
        rebuilding = true;
        try {
            for (var entry : filters.entrySet()) {
                int column = entry.getKey();
                JComboBox<String> combo = entry.getValue();
                Object selected = combo.getSelectedItem();

                List<String> fixed = predefinedOptions.get(column);
                Collection<String> values;
                if (fixed != null && !fixed.isEmpty()) {
                    values = new LinkedHashSet<>(fixed);
                } else {
                    values = new TreeSet<>();
                    for (int row = 0; row < table.getModel().getRowCount(); row++) {
                        Object value = table.getModel().getValueAt(row, column);
                        if (value != null && !value.toString().isBlank()) {
                            values.add(value.toString());
                        }
                    }

                    String defaultState = defaultStates.get(column);
                    if (defaultState != null) {
                        values.add(defaultState);
                    }

                    if (selected != null && !"Tất cả".equals(selected)) {
                        values.add(selected.toString());
                    }
                }

                String targetSelection;
                if (selected != null && values.contains(selected.toString())) {
                    targetSelection = selected.toString();
                } else if (selected != null && "Tất cả".equals(selected) && !defaultStates.containsKey(column)) {
                    targetSelection = "Tất cả";
                } else if (defaultStates.containsKey(column)) {
                    targetSelection = defaultStates.get(column);
                } else {
                    targetSelection = "Tất cả";
                }

                Vector<String> items = new Vector<>();
                items.add("Tất cả");
                items.addAll(values);

                boolean modelMatches = false;
                if (combo.getItemCount() == items.size()) {
                    modelMatches = true;
                    for (int i = 0; i < items.size(); i++) {
                        if (!Objects.equals(combo.getItemAt(i), items.get(i))) {
                            modelMatches = false;
                            break;
                        }
                    }
                }

                if (!modelMatches) {
                    combo.setModel(new DefaultComboBoxModel<>(items));
                }
                if (!Objects.equals(combo.getSelectedItem(), targetSelection)) {
                    combo.setSelectedItem(targetSelection);
                }
            }
        } finally {
            rebuilding = false;
        }
        apply();
    }

    @SuppressWarnings("unchecked")
    private void apply() {
        try {
            TableRowSorter<TableModel> sorter;
            if (table.getRowSorter() instanceof TableRowSorter<?> current) {
                sorter = (TableRowSorter<TableModel>) current;
            } else {
                sorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(sorter);
            }
            sorter.setRowFilter(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> row) {
                    for (var filter : filters.entrySet()) {
                        Object state = filter.getValue().getSelectedItem();
                        if (state != null && !state.equals("Tất cả") && !state.toString().equals(row.getStringValue(filter.getKey()))) {
                            return false;
                        }
                    }
                    if (search.isEmpty()) return true;
                    for (int col = 0; col < row.getValueCount(); col++) {
                        if (OChonTimKiem.normalize(row.getStringValue(col)).contains(search)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception ignored) {
        }
        changed.run();
    }
}
