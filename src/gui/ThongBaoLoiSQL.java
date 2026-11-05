package gui;

import dao.ChonDAO;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ===== GUI: THÔNG BÁO LỖI CƠ SỞ DỮ LIỆU =====

/** Kiểm tra biểu mẫu và xác định ô cần sửa khi lưu thất bại. */
final class ThongBaoLoiSQL {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    static final class Issue extends SQLException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        final String field;

        // ===== KHỞI TẠO ĐỐI TƯỢNG: Issue(String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
        Issue(String field, String message) {
            super(message);
            this.field = field;
        }
    }
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ThongBaoLoiSQL() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private ThongBaoLoiSQL() {
    }

    // ===== PHƯƠNG THỨC: userMessage(Throwable, String) — Chuyển lỗi kỹ thuật thành nội dung dễ hiểu cho người dùng. =====
    static String userMessage(Throwable error, String table) {
        String message = error == null || error.getMessage() == null ? "Dữ liệu chưa hợp lệ."
                : error.getMessage();
        if (error instanceof Issue || error instanceof dao.LoiDuLieu)
            return message;
        if (message.toUpperCase(Locale.ROOT).contains("CK_CHITIETPHIEUKIEMTRA_KETQUA"))
            return "Kết quả kiểm tra không hợp lệ. Hãy chọn “Đạt”, “Không đạt” hoặc để trống nếu chưa có kết quả.";
        if (table.equals("PhieuTiepNhan")
                && message.toUpperCase(Locale.ROOT).contains("CK_PHIEUTIEPNHAN_NGAY"))
            return "Ngày tiếp nhận không hợp lệ. Vui lòng chọn ngày, giờ hiện tại hoặc trước đó.";
        if (table.equals("PhieuTiepNhan") && message.toLowerCase(Locale.ROOT)
                .matches("(?s).*incorrect syntax near (the )?keyword ['\"]?as['\"]?.*"))
            return "Không thể lưu phiếu do lỗi xử lý dữ liệu. Đây là lỗi của ứng dụng, không phải thông tin bạn nhập; vui lòng cập nhật ứng dụng rồi thử lại.";
        if (table.equals("PhieuTiepNhan")
                && message.toLowerCase(Locale.ROOT).matches("(?s).*incorrect syntax near.*"))
            return "Không thể lưu phiếu do lỗi xử lý dữ liệu. Đây là lỗi của ứng dụng, không phải thông tin bạn nhập; vui lòng cập nhật ứng dụng rồi thử lại.";
        if (error instanceof SQLException sqlError) {
            int code = sqlError.getErrorCode();
            String state = sqlError.getSQLState();
            if (code == 547 && message.toUpperCase(Locale.ROOT).contains("CK_"))
                return "Thông tin phiếu không hợp lệ. Một giá trị chưa đáp ứng quy định; vui lòng kiểm tra lại các trường và hạng mục.";
            if (code == 547 && message.toUpperCase(Locale.ROOT).contains("FK_"))
                return "Không thể lưu phiếu vì một thông tin liên quan không còn tồn tại. Hãy tải lại danh sách và chọn lại dữ liệu liên quan.";
            if (code == 2601 || code == 2627)
                return "Mã hoặc thông tin này đã tồn tại. Hãy kiểm tra lại dữ liệu rồi lưu lại.";
            if (code == 515)
                return "Còn thiếu thông tin bắt buộc. Hãy điền đầy đủ các trường có dấu * rồi lưu lại.";
            if (code == 2628 || code == 8152)
                return "Một nội dung nhập vượt quá độ dài cho phép. Hãy rút ngắn nội dung rồi lưu lại.";
            if (code == 245 || code == 8114 || code == 8115)
                return "Một giá trị số không đúng định dạng hoặc vượt giới hạn. Hãy kiểm tra lại các ô số.";
            if (code == 1205)
                return "Phiếu chưa được lưu vì hệ thống đang xử lý một giao dịch khác. Vui lòng thử lưu lại.";
            if ((state != null && state.startsWith("08")) || code == 53 || code == 2)
                return "Không thể kết nối cơ sở dữ liệu nên phiếu chưa được lưu. Hãy kiểm tra kết nối rồi thử lại.";
            if (code == 102 || code == 156)
                return "Không thể lưu phiếu do lỗi xử lý dữ liệu trong ứng dụng. Vui lòng cập nhật ứng dụng hoặc liên hệ quản trị viên.";
            if (code != 0 || state != null) {
                System.err.println("Lỗi cơ sở dữ liệu khi lưu phiếu. SQLState=" + state + ", mã lỗi=" + code);
                return "Không thể lưu phiếu do lỗi cơ sở dữ liệu. Phiếu chưa được ghi; vui lòng thử lại hoặc liên hệ quản trị viên (mã lỗi "
                        + code + ").";
            }
        }
        return message;
    }

    // ===== PHƯƠNG THỨC: errorField(Throwable, String) — Gắn tên trường dữ liệu vào lỗi truy vấn. =====
    static String errorField(Throwable error, String table) {
        if (error == null)
            return null;
        if (error instanceof Issue issue)
            return issue.field;
        if (error instanceof dao.LoiDuLieu issue)
            return issue.field;
        String message = error.getMessage() == null ? "" : error.getMessage();
        Matcher constraint = Pattern.compile("\\b(FK|UQ|CK|PK)_([A-Za-z0-9_]+)").matcher(message);
        if (constraint.find()) {
            String kind = constraint.group(1);
            String name = constraint.group(2);
            if (kind.equals("PK"))
                return ChonDAO.khoaChinh(table);
            String suffix = name.substring(name.lastIndexOf('_') + 1);
            if (kind.equals("FK")) {
                String target = switch (suffix) {
                case "PSC" -> "PhieuSuaChua";
                default -> suffix;
                };
                String primary = ChonDAO.khoaChinh(target);
                Map<String, String> types = ChonDAO.kieuCot(table);
                if (primary != null && types != null && types.containsKey(primary))
                    return primary;
            }
            if ("SDT".equalsIgnoreCase(suffix))
                return "soDienThoai";
            if ("Ten".equalsIgnoreCase(suffix) && table.equals("PhuongThucThanhToan"))
                return "tenPhuongThuc";
            if ("SoLuong".equalsIgnoreCase(suffix) && table.equals("TonKho"))
                return "soLuongHienTai";
            if ("Ngay".equalsIgnoreCase(suffix)) {
                if (table == null)
                    return "ngayHetHan";
                return switch (table) {
                case "PhieuSuaChua" -> "ngayHoanThanh";
                case "PhieuTiepNhan" -> "ngayTiepNhan";
                default -> "ngayHetHan";
                };
            }
            String[] columns = ChonDAO.cacCot(table);
            if (columns != null) {
                for (String field : columns) {
                    if (field.equalsIgnoreCase(suffix) || field.equalsIgnoreCase("ten" + suffix)
                            || field.equalsIgnoreCase("ma" + suffix))
                        return field;
                }
            }
        }
        if (table == null)
            return null;
        for (String field : ChonDAO.cacCot(table)) {
            if (Pattern.compile("(?i)(?<![A-Za-z0-9_])" + Pattern.quote(field) + "(?![A-Za-z0-9_])")
                    .matcher(message).find())
                return field;
        }
        if (message.contains("Số điện thoại"))
            return "soDienThoai";
        if (message.contains("Email"))
            return "email";
        if (message.contains("VIN"))
            return "VIN";
        if (message.startsWith("Mã ") && message.contains("đã thay đổi"))
            return ChonDAO.khoaChinh(table);
        return null;
    }
}
