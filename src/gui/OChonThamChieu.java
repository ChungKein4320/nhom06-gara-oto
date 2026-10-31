package gui;

import dao.Schema;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/** Danh sách khóa ngoại: hiển thị mã và mô tả, giá trị lưu chỉ là mã. */
final class OChonThamChieu extends OChonTimKiem<LuaChonDuLieu.Choice> {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final String owner;
    private final String field;
    private final String target;
    private final String baseQuery;
    private String query;
    private String[] queryParams = new String[0];
    private String preservedId;
    private boolean refreshing;
    @Override protected void fireActionEvent() { if(!refreshing) super.fireActionEvent(); }

    static String targetTable(String owner, String field) {
        if (field.equals(Schema.PRIMARY.get(owner))) return null;
        for (Map.Entry<String, String> entry : Schema.PRIMARY.entrySet()) {
            if (!entry.getKey().startsWith("ChiTiet") && entry.getValue().equals(field)) return entry.getKey();
        }
        return null;
    }

    static OChonThamChieu create(String owner, String field) {
        return create(owner, field, null);
    }

    static OChonThamChieu create(String owner, String field, String initialValue) {
        String target = targetTable(owner, field);
        return target == null ? null : new OChonThamChieu(owner, field, target, initialValue);
    }

    private OChonThamChieu(String owner, String field, String target, String initialValue) {
        this.owner = owner;
        this.field = field;
        this.target = target;
        this.preservedId = (initialValue != null && !initialValue.isBlank()) ? initialValue.trim() : null;
        String primary = Schema.PRIMARY.get(target);
        String description = switch (target) {
            case "NhanVien" -> "hoTenNV";
            case "KhachHang" -> "hoTenKH";
            case "PhuongTien" -> "CONCAT(bienSo, N' — ', hangXe, N' ', dongXe)";
            case "DichVu" -> "tenDichVu";
            case "PhuTung" -> "tenPhuTung";
            case "NhaCungCap" -> "tenNCC";
            case "PhuongThucThanhToan" -> "tenPhuongThuc";
            case "PhieuTiepNhan" -> "CONCAT(maKhachHang, N' — ', VIN, N' — ', CONVERT(nvarchar(10),ngayTiepNhan,103))";
            case "PhieuKiemTra" -> "CONCAT(maKhachHang, N' — ', VIN, N' — ', CONVERT(nvarchar(10),ngayKiemTra,103))";
            case "PhieuBaoGia", "PhieuSuaChua" -> "CONCAT(VIN, N' — ', CONVERT(nvarchar(10),ngayLap,103), N' — ', trangThai)";
            case "YeuCauNhapKho" -> "CONCAT(maNCC, N' — ', CONVERT(nvarchar(10),ngayTao,103), N' — ', trangThai)";
            case "HoaDon" -> "CONCAT(maKhachHang, N' — ', CONVERT(nvarchar(10),ngayLap,103), N' — ', trangThaiThanhToan)";
            case "MaGiamGia" -> "CONCAT(maKhachHang, N' — ', mucGiam, N' — ', trangThai)";
            default -> primary;
        };
        String filter = "LichBaoHanh".equals(owner) && "maPSC".equals(field)
                ? " WHERE [trangThai]=N'Hoàn tất'" : "";
        if ("PhuTung".equals(target)
                && Set.of("TonKho", "ChiTietPhieuNhapKho", "ChiTietYeuCauNhapKho", "ChiTietHoaDon").contains(owner))
            filter = " WHERE [maPhuTung]<>'PT000'";
        baseQuery = "SELECT [" + primary + "], CONCAT([" + primary + "], N' — ', " + description
                + ") FROM dbo.[" + target + "]" + filter + " ORDER BY [" + primary + "]";
        query = baseQuery;
        setFont(GiaoDien.font(12, false));
        setEditable(true);
        setMaximumRowCount(10);
        setPrototypeDisplayValue(new LuaChonDuLieu.Choice("", "Mã — Thông tin lựa chọn"));
        try { refresh(); }
        catch (SQLException ex) { setToolTipText("Không tải được danh sách: " + ex.getMessage()); }
        addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try { refresh(); }
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(OChonThamChieu.this, "Không tải được "
                            + NhanCot.forField(field).toLowerCase() + ": " + ex.getMessage(),
                            "Lỗi tải danh sách", JOptionPane.ERROR_MESSAGE);
                }
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent event) { }
            @Override public void popupMenuCanceled(PopupMenuEvent event) { }
        });
    }

    private boolean shouldFilterHidden() {
        return ("YeuCauNhapKho".equals(owner) && "maNCC".equals(field))
            || ("ChiTietYeuCauNhapKho".equals(owner) && "maPhuTung".equals(field));
    }

    void refresh() throws SQLException {
        String previous = selectedId();
        refreshing=true;
        try {
            LuaChonDuLieu.load(this, query, target, preservedId, shouldFilterHidden(), queryParams);
            insertItemAt(new LuaChonDuLieu.Choice("", "— Chọn " + NhanCot.forField(field).toLowerCase() + " —"), 0);
            setSelectedIndex(0);
            if (!previous.isEmpty()) {
                for (int i = 1; i < getItemCount(); i++) {
                    if (previous.equals(getItemAt(i).id())) { setSelectedIndex(i); break; }
                }
            }
            setToolTipText(getItemCount() == 1 ? "Chưa có dữ liệu để chọn" : null);
        } catch (SQLException ex) {
            removeAllItems();
            throw ex;
        } finally {
            refreshing=false;
            if(!previous.equals(selectedId())) super.fireActionEvent();
        }
    }

    void filterVehiclesByCustomer(String customerId) throws SQLException {
        if (!"PhieuTiepNhan".equals(owner) || !"VIN".equals(field) || !"PhuongTien".equals(target))
            throw new IllegalStateException("Bộ lọc khách hàng chỉ áp dụng cho danh sách phương tiện của phiếu tiếp nhận.");

        if (customerId == null || customerId.isBlank()) {
            int orderBy = baseQuery.lastIndexOf(" ORDER BY ");
            query = baseQuery.substring(0, orderBy) + " WHERE 1=0" + baseQuery.substring(orderBy);
            queryParams = new String[0];
        } else {
            int orderBy = baseQuery.lastIndexOf(" ORDER BY ");
            String beforeOrder = baseQuery.substring(0, orderBy);
            String predicate = beforeOrder.contains(" WHERE ") ? " AND " : " WHERE ";
            query = beforeOrder + predicate + "[maKhachHang]=?" + baseQuery.substring(orderBy);
            queryParams = new String[] { customerId.trim() };
        }
        refresh();
        if (customerId != null && !customerId.isBlank() && getItemCount() == 2)
            setSelectedIndex(1);
    }

    void clearVehicleCustomerFilter() throws SQLException {
        if (!"PhieuTiepNhan".equals(owner) || !"VIN".equals(field) || !"PhuongTien".equals(target))
            throw new IllegalStateException("Bộ lọc khách hàng chỉ áp dụng cho danh sách phương tiện của phiếu tiếp nhận.");
        query = baseQuery;
        queryParams = new String[0];
        refresh();
    }

    String selectedId() {
        String id = LuaChonDuLieu.id(this);
        return id == null ? "" : id;
    }

    void selectId(String id) {
        if (id == null || id.isBlank()) { if(getItemCount()>0) setSelectedIndex(0); else setSelectedIndex(-1); return; }
        for(int i=0;i<getItemCount();i++) if(id.equals(getItemAt(i).id())) { setSelectedIndex(i); return; }
        if (shouldFilterHidden() && (preservedId == null || !preservedId.equals(id.trim()))) {
            preservedId = id.trim();
            try {
                refresh();
                for(int i=0;i<getItemCount();i++) if(id.equals(getItemAt(i).id())) { setSelectedIndex(i); return; }
            } catch (SQLException ignored) { }
        }
        addItem(new LuaChonDuLieu.Choice(id,id)); setSelectedIndex(getItemCount()-1);
    }

    void validateSelection() throws KiemTraBieuMau.Issue {
        if(isEnabled() && getSelectedItem()==null) throw new KiemTraBieuMau.Issue(field,"Vui lòng chọn một kết quả hợp lệ cho "+NhanCot.forField(field).toLowerCase()+".");
        String previous = selectedId();
        try { refresh(); }
        catch (SQLException ex) {
            throw new KiemTraBieuMau.Issue(field, "Không kiểm tra được " + NhanCot.forField(field).toLowerCase() + ": " + ex.getMessage());
        }
        if (!previous.isEmpty() && !previous.equals(selectedId())) {
            throw new KiemTraBieuMau.Issue(field, "Lựa chọn " + previous + " không còn trong database. Vui lòng chọn lại "
                    + NhanCot.forField(field).toLowerCase() + ".");
        }
    }
}
