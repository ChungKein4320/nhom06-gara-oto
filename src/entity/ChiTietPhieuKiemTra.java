package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ChiTietPhieuKiemTra {
    private String maPKT;
    private String maDichVu;
    private String hangMucKiemTra;
    private String ketQuaChuanDoan;
    private String ketQuaKiemTra;

    public ChiTietPhieuKiemTra() { }

    public String getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(String value) {
        this.maPKT = value;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String value) {
        this.maDichVu = value;
    }

    public String getHangMucKiemTra() {
        return hangMucKiemTra;
    }

    public void setHangMucKiemTra(String value) {
        this.hangMucKiemTra = value;
    }

    public String getKetQuaChanDoan() {
        return ketQuaChuanDoan;
    }

    public void setKetQuaChanDoan(String value) {
        this.ketQuaChuanDoan = value;
    }

    public String getKetQuaChuanDoan() { return ketQuaChuanDoan; }
    public void setKetQuaChuanDoan(String value) { ketQuaChuanDoan=value; }

    public String getKetQuaKiemTra() {
        return ketQuaKiemTra;
    }

    public void setKetQuaKiemTra(String value) {
        this.ketQuaKiemTra = value;
    }

}
