package dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/** Kiểm tra dùng chung cho biểu mẫu và DAO trước khi ghi SQL. */
public final class DataRules {
    private DataRules() {}
    public static void validate(String table, Map<String,Object> row, boolean creating) throws SQLException {
        for (String field : row.keySet()) if (!Schema.TYPES.get(table).containsKey(field)) fail(field,"Trường không hợp lệ: " + field);
        for (String field : Schema.REQUIRED.get(table)) {
            if (Schema.IDENTITY.contains(table) && field.equals(Schema.PRIMARY.get(table))) continue;
            if (!creating && !row.containsKey(field)) continue;
            if (row.get(field)==null || row.get(field).toString().isBlank()) fail(field,"Không được bỏ trống: " + field);
        }
        for (var entry : row.entrySet()) {
            String field=entry.getKey(); Object value=entry.getValue();
            if(value==null) continue;
            String raw=value.toString().trim(); String[] spec=Schema.TYPES.get(table).get(field).split(":",2);
            if ((spec[0].equals("varchar") || spec[0].equals("nvarchar")) && raw.length()>Integer.parseInt(spec[1])) fail(field,"Vượt quá độ dài cho phép: " + field);
            if(spec[0].equals("bit") && !(value instanceof Boolean)) fail(field,"Giới tính phải là Nam hoặc Nữ.");
            if(value instanceof Number number) {
                BigDecimal n=new BigDecimal(number.toString());
                if(n.signum()<0) fail(field,"Giá trị không được âm: " + field);
                if(field.equals("soLuongNhap") && n.signum()==0) fail(field,"Số lượng phải lớn hơn 0.");
                if(field.equals("soLuongPhuTung")) {
                    boolean noPart="PT000".equals(String.valueOf(row.get("maPhuTung")));
                    if((noPart && n.signum()!=0) || (!noPart && n.signum()==0))
                        fail(field,noPart ? "PT000 phải có số lượng phụ tùng bằng 0." : "Số lượng phụ tùng phải lớn hơn 0.");
                }
                if(field.equals("mucGiam") && n.compareTo(new BigDecimal("100"))>0) fail(field,"Mức giảm phải từ 0 đến 100%.");
                if(spec[0].equals("decimal")) {
                    String[] size=spec[1].split(","); BigDecimal normalized=n.stripTrailingZeros();
                    if(Math.max(0,normalized.scale())>Integer.parseInt(size[1].trim()) || Math.max(0,normalized.precision()-normalized.scale())>Integer.parseInt(size[0].trim())-Integer.parseInt(size[1].trim())) fail(field,"Số tiền vượt độ chính xác cho phép: " + field);
                }
            }
            
            if (field.equals("maNhanVien") && !raw.matches("NV[0-9]{3}"))
                fail(field, "Mã nhân viên phải có dạng NV001.");

            if (field.equals("maKhachHang") && !raw.matches("KH[0-9]{3}"))
                fail(field, "Mã khách hàng phải có dạng KH001.");

            if (field.equals("maNCC") && !raw.matches("NCC[0-9]{3}"))
                fail(field, "Mã nhà cung cấp phải có dạng NCC001.");

            if (field.equals("maTaiKhoan") && !raw.matches("TK[0-9]{3}"))
                fail(field, "Mã tài khoản phải có dạng TK001.");

            if (field.equals("maGiamGia") && !raw.matches("MGG[0-9]{3}"))
                fail(field, "Mã giảm giá phải có dạng MGG001.");

            if (field.equals("maDichVu") && !raw.matches("DV[0-9]{3}"))
                fail(field, "Mã dịch vụ phải có dạng DV001.");

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                fail(field, "Mã phụ tùng phải có dạng PT001.");

            if (table.equals("NhanVien") && field.equals("vaiTro")
                    && !java.util.Set.of(
                        "Nhân viên kỹ thuật",
                        "Nhân viên kho",
                        "Nhân viên bán hàng",
                        "Nhân viên quản lý"
                    ).contains(raw))
                fail(field, "Vai trò nhân viên không hợp lệ.");

            if (table.equals("NhanVien") && field.equals("trangThai")
                    && !java.util.Set.of("Đang làm", "Đã nghỉ").contains(raw))
                fail(field, "Trạng thái nhân viên không hợp lệ.");

            if (table.equals("TaiKhoan") && field.equals("trangThai")
                    && !java.util.Set.of(
                        "Đang hoạt động", "Ngừng hoạt động"
                    ).contains(raw))
                fail(field, "Trạng thái tài khoản không hợp lệ.");

            if (table.equals("NhaCungCap") && field.equals("tenNCC")
                    && !raw.matches("(?s).*[\\p{L}].*"))
                fail(field, "Tên nhà cung cấp phải chứa ít nhất một chữ cái.");

            if ((table.equals("KhachHang") && field.equals("hoTenKH"))
                    || (table.equals("NhanVien") && field.equals("hoTenNV"))) {

                if (!raw.matches("[\\p{L}\\p{M}]+(?:[ ]+[\\p{L}\\p{M}]+)*"))
                    fail(field, "Họ tên chỉ được chứa chữ cái và khoảng trắng.");
            }

            if ((table.equals("DichVu") && field.equals("tenDichVu"))
                    || (table.equals("PhuTung") && field.equals("tenPhuTung"))) {

                if (!raw.matches("(?s).*[\\p{L}].*"))
                    fail(field, "Tên dịch vụ hoặc phụ tùng phải chứa ít nhất một chữ cái.");
            }

            if (table.equals("DichVu") && field.equals("trangThai")
                    && !java.util.Set.of(
                        "Hoạt động", "Ngừng hoạt động"
                    ).contains(raw))
                fail(field, "Trạng thái dịch vụ chỉ được chọn Hoạt động hoặc Ngừng hoạt động.");

            if(field.equals("soDienThoai") && !raw.matches("0[35789][0-9]{8}")) fail(field,"Số điện thoại gồm 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09.");
            if(field.equals("email") && !raw.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) fail(field,"Email không hợp lệ.");
            if(field.equals("VIN") && !raw.matches("[A-HJ-NPR-Z0-9]{17}")) fail(field,"VIN phải gồm 17 ký tự hợp lệ.");
            if(field.equals("bienSo") && !raw.matches("[0-9]{2}[A-Z][0-9]{5}")) fail(field,"Biển số phải theo dạng 51A12345.");
            if(field.equals("soODO") && !raw.matches("[0-9]{6}")) fail(field,"Số ODO phải gồm đúng 6 chữ số.");
            if(field.equals("namSanXuat") && (Integer.parseInt(raw)<1886 || Integer.parseInt(raw)>LocalDate.now().getYear())) fail(field,"Năm sản xuất không hợp lệ.");
            if(field.equals("ngaySinh") && table.equals("NhanVien")) {
                LocalDate date=LocalDate.parse(raw.substring(0,10)); LocalDate today=LocalDate.now();
                if(date.isAfter(today.minusYears(18)) || !date.isAfter(today.minusYears(66))) fail(field,"Nhân viên phải từ 18 đến dưới 66 tuổi.");
            }
            // CHECK của SQL Server kiểm tra ngày tương lai bằng SYSDATETIME().
            // Không so ngày do server sinh với đồng hồ máy chạy Java.
        }
        dates(row,"ngayTao","ngayHetHan",true); dates(row,"ngayLap","ngayHetHan",false);
        dates(row,"ngayLap","ngayHoanThanh",false); dates(row,"ngayLap","thoiGianXacNhan",false);
        dates(row,"ngayBatDauBaoHanh","ngayHetHanBaoHanh",false);
        if(table.equals("ChiTietHoaDon") && row.get("maDichVu")==null && row.get("maPhuTung")==null && creating) fail("maDichVu","Chọn ít nhất dịch vụ hoặc phụ tùng.");
        if("PT000".equals(String.valueOf(row.get("maPhuTung")))) {
            if(table.equals("PhuTung") && !creating) fail("maPhuTung","PT000 là mã hệ thống, không được sửa.");
            if(table.equals("TonKho") || table.equals("ChiTietPhieuNhapKho") || table.equals("ChiTietHoaDon"))
                fail("maPhuTung","PT000 chỉ dùng để biểu thị hạng mục không sử dụng phụ tùng; không được nhập kho hoặc bán như phụ tùng.");
            if(table.equals("ChiTietPhieuBaoGia") && row.get("donGiaPhuTung") instanceof Number price && new BigDecimal(price.toString()).signum()!=0)
                fail("donGiaPhuTung","Đơn giá phụ tùng của PT000 phải bằng 0.");
        }
    }
    private static void dates(Map<String,Object> row,String start,String end,boolean strict) throws SQLException {
        if(row.get(start) instanceof java.util.Date a && row.get(end) instanceof java.util.Date b && (strict ? !b.after(a) : b.before(a))) fail(end,"Ngày kết thúc không hợp lệ.");
    }
    public static final class Invalid extends SQLException {
        private static final long serialVersionUID = 1L;
        public final String field;
        Invalid(String field,String message) { super(message); this.field=field; }
    }
    private static void fail(String field,String message) throws SQLException { throw new Invalid(field,message); }
}
