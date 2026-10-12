package gui;
public enum Role {
    KY_THUAT("Nhân viên kỹ thuật","Châu"),KHO("Nhân viên kho","Kiên"),THU_NGAN("Thu ngân","Minh"),QUAN_LY("Nhân viên quản lý","Khanh");
    public final String title,name;
    Role(String t,String n){
        title=t;
        name=n;
    }
    public String toString(){
        return title;
    }
}
