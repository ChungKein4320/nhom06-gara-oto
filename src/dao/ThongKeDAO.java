package dao;

import connectDB.KetNoiCSDL;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ThongKeDAO {
    public record Bucket(String label, double value) implements java.io.Serializable {
        @java.io.Serial private static final long serialVersionUID = 1L;
    }
    public record Result(List<Bucket> buckets, BigDecimal total) { }
    private ThongKeDAO() { }

    public static Result load(int category, LocalDate from, LocalDate to, boolean daily) throws SQLException {
        if (from.isAfter(to)) throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        String date = category == 1 ? "ngayTiepNhan" : "ngayLap";
        String group = "CONVERT(char(" + (daily ? "10" : "7") + "), " + date + ", 120)";
        String source;
        String value;
        if (category == 0) {
            source = " FROM dbo.HoaDon WHERE trangThaiThanhToan=N'Đã thanh toán' AND ngayLap>=? AND ngayLap<?";
            value = "SUM(tienPhuTung+tienCong+dichVuKhac+thueVAT-giamGia)";
        } else if (category == 1) {
            source = " FROM dbo.PhieuTiepNhan WHERE ngayTiepNhan>=? AND ngayTiepNhan<?";
            value = "COUNT(DISTINCT maKhachHang)";
        } else if (category == 2) {
            source = " FROM dbo.ChiTietHoaDon ct JOIN dbo.HoaDon h ON h.maHoaDon=ct.maHoaDon "
                    + "JOIN dbo.DichVu d ON d.maDichVu=ct.maDichVu WHERE h.trangThaiThanhToan=N'Đã thanh toán' AND h.ngayLap>=? AND h.ngayLap<?";
            // Mỗi chi tiết có khóa (hóa đơn, dịch vụ); số lượng ở chi tiết có thể là số phụ tùng.
            value = "COUNT(*)";
            group = "d.maDichVu, d.tenDichVu";
        } else if (category >= 3 && category <= 12) {
            // Thống kê theo phiếu sửa chữa hoặc hóa đơn đã thanh toán; không nhân bản số liệu do JOIN.
            String completed = "p.trangThai IN (N'Đã sửa xong',N'Chờ thanh toán',N'Đã thanh toán',N'Hoàn tất')";
            String completedItem = "ct.trangThaiHangMuc=N'Hoàn thành' AND p.trangThai<>N'Đã hủy'";
            String repair = " FROM dbo.ChiTietPhieuSuaChua ct JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC ";
            String paid = "h.trangThaiThanhToan=N'Đã thanh toán'";
            switch (category) {
                case 3, 4, 5, 6 -> {
                    source = repair + "JOIN dbo.PhuTung pt ON pt.maPhuTung=ct.maPhuTung WHERE ct.maPhuTung<>'PT000' AND p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<?";
                    String condition = category == 4 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%lốp%' OR LOWER(pt.loaiPhuTung) LIKE N'%lốp%' OR LOWER(pt.tenPhuTung) LIKE N'%vỏ xe%')"
                            : category == 5 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%nhớt%' OR LOWER(pt.tenPhuTung) LIKE N'%dầu động cơ%' OR LOWER(pt.loaiPhuTung) LIKE N'%nhớt%' OR LOWER(pt.loaiPhuTung) LIKE N'%dầu nhờn%')" : "";
                    source += condition;
                    group = category == 6 ? "pt.loaiPhuTung" : "pt.maPhuTung,pt.tenPhuTung";
                    value = "SUM(ct.soLuongPhuTung)";
                }
                case 7, 8 -> {
                    source = " FROM dbo.PhieuSuaChua p JOIN dbo.PhuongTien x ON x.VIN=p.VIN WHERE p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<?";
                    group = category == 7 ? "x.hangXe" : "x.hangXe,x.dongXe";
                    value = "COUNT(DISTINCT p.VIN)";
                }
                case 9 -> {
                    source = " FROM dbo.HoaDon h JOIN dbo.KhachHang k ON k.maKhachHang=h.maKhachHang WHERE " + paid + " AND h.ngayLap>=? AND h.ngayLap<?";
                    group = "k.maKhachHang,k.hoTenKH";
                    value = "SUM(h.tienPhuTung+h.tienCong+h.dichVuKhac+h.thueVAT-COALESCE(h.giamGia,0))";
                }
                case 10 -> {
                    source = " FROM dbo.PhieuSuaChua p JOIN dbo.NhanVien n ON n.maNhanVien=p.maNhanVien WHERE p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<?";
                    group = "n.maNhanVien,n.hoTenNV";
                    value = "COUNT(DISTINCT p.maPSC)";
                }
                case 11 -> {
                    source = " FROM dbo.HoaDon h JOIN dbo.NhanVien n ON n.maNhanVien=h.maNhanVien WHERE " + paid + " AND h.ngayLap>=? AND h.ngayLap<?";
                    group = "n.maNhanVien,n.hoTenNV";
                    value = "SUM(h.tienPhuTung+h.tienCong+h.dichVuKhac+h.thueVAT-COALESCE(h.giamGia,0))";
                }
                default -> {
                    source = " FROM dbo.HoaDon h WHERE " + paid + " AND h.ngayLap>=? AND h.ngayLap<?";
                    group = "h.maHoaDon";
                    value = "SUM(h.tienPhuTung+h.tienCong+h.dichVuKhac+h.thueVAT-COALESCE(h.giamGia,0))";
                }
            }
        } else throw new IllegalArgumentException("Phân loại không hợp lệ.");

        String label = switch (category) {
            case 2 -> "CONCAT(d.maDichVu,N' — ',d.tenDichVu)";
            case 3,4,5 -> "CONCAT(pt.maPhuTung,N' — ',pt.tenPhuTung)";
            case 6 -> "pt.loaiPhuTung";
            case 7 -> "x.hangXe";
            case 8 -> "CONCAT(x.hangXe,N' — ',x.dongXe)";
            case 9 -> "CONCAT(k.maKhachHang,N' — ',k.hoTenKH)";
            case 10,11 -> "CONCAT(n.maNhanVien,N' — ',n.hoTenNV)";
            default -> group;
        };
        String sql = "SELECT " + label + " nhom," + value + " giaTri" + source
                + " GROUP BY " + group + (category == 2 ? " ORDER BY giaTri DESC,d.maDichVu" : category >= 3 ? " ORDER BY giaTri DESC" : " ORDER BY nhom");
        List<Bucket> buckets = new ArrayList<>();
        BigDecimal total;
        try (Connection conn = KetNoiCSDL.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                range(ps, from, to);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) buckets.add(new Bucket(rs.getString(1), rs.getDouble(2)));
                }
            }
            // Khách hàng được đếm duy nhất cho cả kỳ, không cộng trùng giữa các ngày/tháng.
            try (PreparedStatement ps = conn.prepareStatement("SELECT " + value + source)) {
                range(ps, from, to);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next(); total = rs.getBigDecimal(1);
                    if (total == null) total = BigDecimal.ZERO;
                }
            }
        }
        if (category < 2 && !buckets.isEmpty()) {
            java.util.Map<String, Double> byDate = new java.util.HashMap<>();
            for (Bucket bucket : buckets) byDate.put(bucket.label(), bucket.value());
            buckets.clear();
            LocalDate cursor = daily ? from : from.withDayOfMonth(1);
            while (!cursor.isAfter(to)) {
                String key = daily ? cursor.toString() : cursor.toString().substring(0, 7);
                buckets.add(new Bucket(key, byDate.getOrDefault(key, 0.0)));
                cursor = daily ? cursor.plusDays(1) : cursor.plusMonths(1);
            }
        }
        return new Result(List.copyOf(buckets), total);
    }


    /** Số VIN khác nhau có hạng mục phụ tùng hoàn thành trong kỳ. */
    public static long distinctVehicles(int category, LocalDate from, LocalDate to) throws SQLException {
        if (category != 3 && category != 4 && category != 5 && category != 6) return 0;
        String condition = category == 4 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%lốp%' OR LOWER(pt.loaiPhuTung) LIKE N'%lốp%' OR LOWER(pt.tenPhuTung) LIKE N'%vỏ xe%')"
                : category == 5 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%nhớt%' OR LOWER(pt.tenPhuTung) LIKE N'%dầu động cơ%' OR LOWER(pt.loaiPhuTung) LIKE N'%nhớt%' OR LOWER(pt.loaiPhuTung) LIKE N'%dầu nhờn%')" : "";
        String sql = "SELECT COUNT(DISTINCT p.VIN) FROM dbo.ChiTietPhieuSuaChua ct "
                + "JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC "
                + "JOIN dbo.PhuTung pt ON pt.maPhuTung=ct.maPhuTung "
                + "WHERE ct.maPhuTung<>'PT000' AND ct.trangThaiHangMuc=N'Hoàn thành' AND p.trangThai<>N'Đã hủy' "
                + "AND p.ngayLap>=? AND p.ngayLap<?" + condition;
        try (Connection c=KetNoiCSDL.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            range(ps,from,to);
            try(ResultSet rs=ps.executeQuery()) { return rs.next()?rs.getLong(1):0; }
        }
    }

    /** Bảng dữ liệu đa cột, dùng chung đúng kỳ lọc với biểu đồ. */
    public record BangChiTiet(String[] cot, List<Object[]> dong) { }
    public static BangChiTiet taiBang(int loai, LocalDate tu, LocalDate den) throws SQLException {
        String sql;
        boolean coNgay = true;
        String tongHD = "(h.tienPhuTung+h.tienCong+h.dichVuKhac+h.thueVAT-COALESCE(h.giamGia,0))";
        String daTra = "h.trangThaiThanhToan=N'Đã thanh toán' AND h.ngayLap>=? AND h.ngayLap<?";
        String sua = "ct.trangThaiHangMuc=N'Hoàn thành' AND p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<?";
        String suaPhuTung = sua + " AND ct.maPhuTung<>'PT000'";
        String nhom = loai == 4 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%lốp%' OR LOWER(pt.loaiPhuTung) LIKE N'%lốp%' OR LOWER(pt.tenPhuTung) LIKE N'%vỏ xe%')" : loai == 5 ? " AND (LOWER(pt.tenPhuTung) LIKE N'%nhớt%' OR LOWER(pt.tenPhuTung) LIKE N'%dầu động cơ%' OR LOWER(pt.loaiPhuTung) LIKE N'%nhớt%' OR LOWER(pt.loaiPhuTung) LIKE N'%dầu nhờn%')" : "";
        String[] cot;
        switch(loai) {
            case 0 -> { cot=new String[]{"Tháng", "Số hóa đơn", "Tiền phụ tùng (đ)", "Tiền công (đ)", "Dịch vụ khác (đ)", "VAT (đ)", "Giảm giá (đ)", "Doanh thu (đ)"};
                sql="SELECT CONVERT(char(7),h.ngayLap,120),COUNT(*),SUM(h.tienPhuTung),SUM(h.tienCong),SUM(h.dichVuKhac),SUM(h.thueVAT),SUM(COALESCE(h.giamGia,0)),SUM("+tongHD+") FROM dbo.HoaDon h WHERE "+daTra+" GROUP BY CONVERT(char(7),h.ngayLap,120) ORDER BY 1"; }
            case 1 -> { cot=new String[]{"Tháng", "Khách hàng duy nhất", "Số lượt tiếp nhận"};
                sql="SELECT CONVERT(char(7),ngayTiepNhan,120),COUNT(DISTINCT maKhachHang),COUNT(*) FROM dbo.PhieuTiepNhan WHERE ngayTiepNhan>=? AND ngayTiepNhan<? GROUP BY CONVERT(char(7),ngayTiepNhan,120) ORDER BY 1"; }
            case 2 -> { cot=new String[]{"Mã dịch vụ", "Tên dịch vụ", "Số lượt thực hiện", "Số xe", "Số phiếu sửa chữa"};
                sql="SELECT d.maDichVu,d.tenDichVu,COUNT(*),COUNT(DISTINCT p.VIN),COUNT(DISTINCT p.maPSC) FROM dbo.ChiTietPhieuSuaChua ct JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC JOIN dbo.DichVu d ON d.maDichVu=ct.maDichVu WHERE "+sua+" GROUP BY d.maDichVu,d.tenDichVu ORDER BY 3 DESC"; }
            case 3,4,5 -> { cot=new String[]{"Mã phụ tùng", "Tên phụ tùng", "Loại", "Số lượng đã thay", "Số xe", "Số phiếu", "Giá trị theo đơn giá (đ)"};
                sql="SELECT pt.maPhuTung,pt.tenPhuTung,pt.loaiPhuTung,SUM(ct.soLuongPhuTung),COUNT(DISTINCT p.VIN),COUNT(DISTINCT p.maPSC),SUM(ct.soLuongPhuTung*pt.donGia) FROM dbo.ChiTietPhieuSuaChua ct JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC JOIN dbo.PhuTung pt ON pt.maPhuTung=ct.maPhuTung WHERE "+suaPhuTung+nhom+" GROUP BY pt.maPhuTung,pt.tenPhuTung,pt.loaiPhuTung ORDER BY 4 DESC"; }
            case 6 -> { cot=new String[]{"Loại phụ tùng", "Số loại mã", "Số lượng đã thay", "Số xe", "Giá trị theo đơn giá (đ)"};
                sql="SELECT pt.loaiPhuTung,COUNT(DISTINCT pt.maPhuTung),SUM(ct.soLuongPhuTung),COUNT(DISTINCT p.VIN),SUM(ct.soLuongPhuTung*pt.donGia) FROM dbo.ChiTietPhieuSuaChua ct JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC JOIN dbo.PhuTung pt ON pt.maPhuTung=ct.maPhuTung WHERE "+suaPhuTung+" GROUP BY pt.loaiPhuTung ORDER BY 3 DESC"; }
            case 7,8 -> { cot=new String[]{loai==7?"Hãng xe":"Hãng xe",loai==7?"Số xe duy nhất":"Dòng xe", "Số phiếu sửa chữa"};
                if(loai==7) {cot=new String[]{"Hãng xe","Số xe duy nhất","Số phiếu sửa chữa"};sql="SELECT x.hangXe,COUNT(DISTINCT p.VIN),COUNT(DISTINCT p.maPSC) FROM dbo.PhieuSuaChua p JOIN dbo.PhuongTien x ON x.VIN=p.VIN WHERE p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<? GROUP BY x.hangXe ORDER BY 2 DESC";}
                else {cot=new String[]{"Hãng xe","Dòng xe","Số xe duy nhất","Số phiếu sửa chữa"};sql="SELECT x.hangXe,x.dongXe,COUNT(DISTINCT p.VIN),COUNT(DISTINCT p.maPSC) FROM dbo.PhieuSuaChua p JOIN dbo.PhuongTien x ON x.VIN=p.VIN WHERE p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<? GROUP BY x.hangXe,x.dongXe ORDER BY 3 DESC";} }
            case 9 -> {cot=new String[]{"Mã khách hàng","Tên khách hàng","Số hóa đơn","Tổng thanh toán (đ)","Bình quân/hóa đơn (đ)"};sql="SELECT k.maKhachHang,k.hoTenKH,COUNT(*),SUM("+tongHD+"),AVG("+tongHD+") FROM dbo.HoaDon h JOIN dbo.KhachHang k ON k.maKhachHang=h.maKhachHang WHERE "+daTra+" GROUP BY k.maKhachHang,k.hoTenKH ORDER BY 4 DESC";}
            case 10 -> {cot=new String[]{"Mã nhân viên","Nhân viên kỹ thuật","Số phiếu hoàn thành","Số xe","Số hạng mục hoàn thành"};sql="SELECT n.maNhanVien,n.hoTenNV,COUNT(DISTINCT p.maPSC),COUNT(DISTINCT p.VIN),COUNT(ct.maDichVu) FROM dbo.PhieuSuaChua p JOIN dbo.NhanVien n ON n.maNhanVien=p.maNhanVien LEFT JOIN dbo.ChiTietPhieuSuaChua ct ON ct.maPSC=p.maPSC AND ct.trangThaiHangMuc=N'Hoàn thành' WHERE p.trangThai IN (N'Đã sửa xong',N'Chờ thanh toán',N'Đã thanh toán',N'Hoàn tất') AND p.ngayLap>=? AND p.ngayLap<? GROUP BY n.maNhanVien,n.hoTenNV ORDER BY 3 DESC";}
            case 11 -> {cot=new String[]{"Mã nhân viên","Nhân viên thu ngân","Số hóa đơn đã thu","Tổng tiền đã thu (đ)","Bình quân/hóa đơn (đ)"};sql="SELECT n.maNhanVien,n.hoTenNV,COUNT(*),SUM("+tongHD+"),AVG("+tongHD+") FROM dbo.HoaDon h JOIN dbo.NhanVien n ON n.maNhanVien=h.maNhanVien WHERE "+daTra+" GROUP BY n.maNhanVien,n.hoTenNV ORDER BY 4 DESC";}
            default -> {cot=new String[]{"Mã hóa đơn","Ngày lập","Mã khách hàng","Tiền phụ tùng (đ)","Tiền công (đ)","Dịch vụ khác (đ)","VAT (đ)","Giảm giá (đ)","Tổng thanh toán (đ)"};sql="SELECT h.maHoaDon,CONVERT(char(10),h.ngayLap,103),h.maKhachHang,h.tienPhuTung,h.tienCong,h.dichVuKhac,h.thueVAT,COALESCE(h.giamGia,0),"+tongHD+" FROM dbo.HoaDon h WHERE "+daTra+" ORDER BY 9 DESC";}
        }
        List<Object[]> dong=new ArrayList<>();
        try(Connection c=KetNoiCSDL.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            if(coNgay)range(ps,tu,den);
            try(ResultSet rs=ps.executeQuery()) {while(rs.next()) {Object[] r=new Object[cot.length];for(int i=0;i<r.length;i++)r[i]=rs.getObject(i+1);dong.add(r);}}
        }
        return new BangChiTiet(cot,dong);
    }

    public static List<String> danhSachLoc(boolean phuTung) throws SQLException {
        String sql=phuTung?"SELECT DISTINCT loaiPhuTung FROM dbo.PhuTung WHERE loaiPhuTung IS NOT NULL ORDER BY loaiPhuTung":"SELECT DISTINCT hangXe FROM dbo.PhuongTien WHERE hangXe IS NOT NULL ORDER BY hangXe";
        List<String> ds=new ArrayList<>();
        try(Connection c=KetNoiCSDL.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {
            while(rs.next()) ds.add(rs.getString(1));
        }
        return ds;
    }
    /** Thống kê từng xe theo VIN, hiển thị hãng và biển số để nhận diện. */
    public static BangChiTiet thongKeTungXe(LocalDate tu,LocalDate den,String hang) throws SQLException {
        String sql="SELECT x.VIN,x.bienSo,x.hangXe,x.dongXe,COUNT(DISTINCT p.maPSC) "
                +"FROM dbo.PhieuSuaChua p JOIN dbo.PhuongTien x ON x.VIN=p.VIN "
                +"WHERE p.trangThai<>N'Đã hủy' AND p.ngayLap>=? AND p.ngayLap<? "
                +(hang==null || "Tất cả".equals(hang)?"":"AND x.hangXe=? ")
                +"GROUP BY x.VIN,x.bienSo,x.hangXe,x.dongXe ORDER BY 5 DESC,x.hangXe,x.bienSo";
        List<Object[]> dong=new ArrayList<>();
        try(Connection c=KetNoiCSDL.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            range(ps,tu,den);
            if(hang!=null && !"Tất cả".equals(hang)) ps.setString(3,hang);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    Object[] r=new Object[5];for(int i=0;i<5;i++)r[i]=rs.getObject(i+1);dong.add(r);
                }
            }
        }
        return new BangChiTiet(new String[]{"Mã phương tiện (VIN)","Biển số xe","Hãng xe","Dòng xe","Số lần sửa chữa"},dong);
    }
    public static BangChiTiet taiBangLoc(int loai,LocalDate tu,LocalDate den,String loc,boolean theoMa) throws SQLException {
        if(loai==8 && theoMa) return thongKeTungXe(tu,den,loc);
        return taiBangLoc(loai,tu,den,loc);
    }
    public static Result taiBieuDoLoc(int loai,LocalDate tu,LocalDate den,boolean daily,String loc,boolean theoMa) throws SQLException {
        if(loai==8 && theoMa) {
            BangChiTiet bang=thongKeTungXe(tu,den,loc);
            List<Bucket> buckets=new ArrayList<>();
            for(Object[] r:bang.dong()) buckets.add(new Bucket(r[2]+" — "+r[1],((Number)r[4]).doubleValue()));
            return new Result(buckets,BigDecimal.valueOf(buckets.stream().mapToDouble(Bucket::value).sum()));
        }
        return taiBieuDoLoc(loai,tu,den,daily,loc);
    }
    public static BangChiTiet taiBangLoc(int loai,LocalDate tu,LocalDate den,String loc) throws SQLException {
        BangChiTiet bang=taiBang(loai,tu,den);
        if("Tất cả".equals(loc) || loc==null || (loai!=3 && loai!=8)) return bang;
        List<Object[]> dong=new ArrayList<>();
        for(Object[] r:bang.dong()) {
            String giaTri=String.valueOf(r[loai==3?2:0]);
            if(loc.equals(giaTri)) dong.add(r);
        }
        return new BangChiTiet(bang.cot(),dong);
    }
    public static Result taiBieuDoLoc(int loai,LocalDate tu,LocalDate den,boolean daily,String loc) throws SQLException {
        Result ketQua=load(loai,tu,den,daily);
        if("Tất cả".equals(loc) || loc==null || (loai!=3 && loai!=8)) return ketQua;
        BangChiTiet bang=taiBangLoc(loai,tu,den,loc);
        List<Bucket> nhom=new ArrayList<>();
        for(Object[] r:bang.dong()) {
            if(loai==3) nhom.add(new Bucket(r[0]+" — "+r[1],((Number)r[3]).doubleValue()));
            else nhom.add(new Bucket(r[0]+" — "+r[1],((Number)r[2]).doubleValue()));
        }
        nhom.sort((a,b)->Double.compare(b.value(),a.value()));
        return new Result(nhom,BigDecimal.valueOf(nhom.stream().mapToDouble(Bucket::value).sum()));
    }

    private static void range(PreparedStatement ps, LocalDate from, LocalDate to) throws SQLException {
        ps.setDate(1, Date.valueOf(from));
        ps.setDate(2, Date.valueOf(to.plusDays(1)));
    }
}
