package gui;
import connectDB.KetNoiCSDL;
import java.sql.*;
import javax.swing.JComboBox;
final class LuaChonDuLieu {
    record Choice(String id, String label) implements java.io.Serializable {
        @java.io.Serial private static final long serialVersionUID = 1L;
        @Override public String toString() { return label; }
    }
    private LuaChonDuLieu() { }
    static void load(JComboBox<Choice> cmbCombo, String sql, String targetTable, String preservedId, boolean filterHidden, String... params) throws SQLException {
        cmbCombo.removeAllItems();
        try (Connection conn = KetNoiCSDL.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i=0; i<params.length; i++) ps.setString(i+1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    String label = rs.getString(2);
                    if (filterHidden && targetTable != null && dao.HiddenRecords.isHidden(targetTable, id)) {
                        if (preservedId != null && !preservedId.isBlank() && preservedId.equals(id)) {
                            cmbCombo.addItem(new Choice(id, "[Đã ẩn] " + label));
                        }
                        continue;
                    }
                    cmbCombo.addItem(new Choice(id, label));
                }
            }
        } catch (SQLException ex) { cmbCombo.removeAllItems(); throw ex; }
    }
    static void load(JComboBox<Choice> cmbCombo, String sql, String... params) throws SQLException {
        load(cmbCombo, sql, null, null, false, params);
    }
    static String id(JComboBox<Choice> cmbCombo) {
        Choice choice = (Choice) cmbCombo.getSelectedItem();
        return choice == null ? null : choice.id();
    }
}
