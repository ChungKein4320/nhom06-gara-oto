package gui;

import java.awt.Component;
import java.util.Map;

// ===== GUI: FORM CHỈNH SỬA PHIẾU DÙNG CHUNG =====

/** Chỉ điều hướng đến trình sửa phiếu riêng; không có mã biểu mẫu dùng chung. */
final class TrinhSuaPhieu {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, VaiTro, String) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        switch (table) {
        case "PhieuTiepNhan" -> TrinhSuaPhieuTiepNhan.show(parent, table, header, role, employeeId);
        case "PhieuKiemTra" -> TrinhSuaPhieuKiemTra.show(parent, table, header, role, employeeId);
        case "PhieuBaoGia" -> TrinhSuaPhieuBaoGia.show(parent, table, header, role, employeeId);
        case "PhieuSuaChua" -> TrinhSuaPhieuSuaChua.show(parent, table, header, role, employeeId);
        case "YeuCauNhapKho" -> TrinhSuaYeuCauNhapKho.show(parent, table, header, role, employeeId);
        case "PhieuNhapKho" -> TrinhSuaPhieuNhapKho.show(parent, table, header, role, employeeId);
        case "HoaDon" -> TrinhSuaHoaDon.show(parent, table, header, role, employeeId);
        default -> throw new IllegalArgumentException("Không có chi tiết cho " + table);
        }
    }

    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, java.util.List<Map<String,Object>>, VaiTro, String) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent, String table, Map<String, Object> header,
            java.util.List<Map<String, Object>> lines, VaiTro role, String employeeId) {
        switch (table) {
        case "PhieuTiepNhan" -> TrinhSuaPhieuTiepNhan.show(parent, table, header, lines, role, employeeId);
        case "PhieuKiemTra" -> TrinhSuaPhieuKiemTra.show(parent, table, header, lines, role, employeeId);
        case "PhieuBaoGia" -> TrinhSuaPhieuBaoGia.show(parent, table, header, lines, role, employeeId);
        case "PhieuSuaChua" -> TrinhSuaPhieuSuaChua.show(parent, table, header, lines, role, employeeId);
        case "YeuCauNhapKho" -> TrinhSuaYeuCauNhapKho.show(parent, table, header, lines, role, employeeId);
        case "PhieuNhapKho" -> TrinhSuaPhieuNhapKho.show(parent, table, header, lines, role, employeeId);
        case "HoaDon" -> TrinhSuaHoaDon.show(parent, table, header, lines, role, employeeId);
        default -> throw new IllegalArgumentException("Không có chi tiết cho " + table);
        }
    }

    // ===== PHƯƠNG THỨC: view(Component, String, Map<String,Object>) — Mở màn hình xem chi tiết dữ liệu. =====
    static void view(Component parent, String table, Map<String, Object> header) {
        switch (table) {
        case "PhieuTiepNhan" -> TrinhSuaPhieuTiepNhan.view(parent, table, header);
        case "PhieuKiemTra" -> TrinhSuaPhieuKiemTra.view(parent, table, header);
        case "PhieuBaoGia" -> TrinhSuaPhieuBaoGia.view(parent, table, header);
        case "PhieuSuaChua" -> TrinhSuaPhieuSuaChua.view(parent, table, header);
        case "YeuCauNhapKho" -> TrinhSuaYeuCauNhapKho.view(parent, table, header);
        case "PhieuNhapKho" -> TrinhSuaPhieuNhapKho.view(parent, table, header);
        case "HoaDon" -> TrinhSuaHoaDon.view(parent, table, header);
        default -> throw new IllegalArgumentException("Không có chi tiết cho " + table);
        }
    }

    // ===== PHƯƠNG THỨC: review(Component, String, Map<String,Object>, VaiTro, String) — Mở chức năng xem xét và xử lý chứng từ. =====
    static void review(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        switch (table) {
        case "PhieuTiepNhan" -> TrinhSuaPhieuTiepNhan.review(parent, table, header, role, employeeId);
        case "PhieuKiemTra" -> TrinhSuaPhieuKiemTra.review(parent, table, header, role, employeeId);
        case "PhieuBaoGia" -> TrinhSuaPhieuBaoGia.review(parent, table, header, role, employeeId);
        case "PhieuSuaChua" -> TrinhSuaPhieuSuaChua.review(parent, table, header, role, employeeId);
        case "YeuCauNhapKho" -> TrinhSuaYeuCauNhapKho.review(parent, table, header, role, employeeId);
        case "PhieuNhapKho" -> TrinhSuaPhieuNhapKho.review(parent, table, header, role, employeeId);
        case "HoaDon" -> TrinhSuaHoaDon.review(parent, table, header, role, employeeId);
        default -> throw new IllegalArgumentException("Không có chi tiết cho " + table);
        }
    }
}
