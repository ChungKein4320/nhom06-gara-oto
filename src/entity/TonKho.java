package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TonKho {
    private String maTonKho;
    private String maPhuTung;
    private Integer soLuongHienTai;
    private Integer soLuongToiThieu;

    public TonKho() { }

    public String getMaTonKho() {
        return maTonKho;
    }

    public void setMaTonKho(String value) {
        this.maTonKho = value;
    }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public Integer getSoLuongHienTai() {
        return soLuongHienTai;
    }

    public void setSoLuongHienTai(Integer value) {
        this.soLuongHienTai = value;
    }

    public Integer getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(Integer value) {
        this.soLuongToiThieu = value;
    }

}