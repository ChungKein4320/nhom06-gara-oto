package gui;

import java.awt.*;
import java.text.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

/** Chỉ định dạng hiển thị, giữ nguyên giá trị trong model để sửa và lưu dữ liệu. */
final class DinhDangBang extends DefaultTableCellRenderer {
    private final NumberFormat numbers=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    DinhDangBang() { numbers.setMaximumFractionDigits(2); putClientProperty("html.disable",Boolean.TRUE); }
    static String name(JTable table,int column) {
        String name=table.getModel().getColumnName(table.convertColumnIndexToModel(column));
        return Normalizer.normalize(NhanCot.forField(name),Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT);
    }
    static int alignment(JTable table,int column) {
        return SwingConstants.CENTER;
    }
    static boolean isMoneyColumn(JTable table,int column) {
        String name=name(table,column);
        if(name.startsWith("ma ")) return false;
        return name.startsWith("tong ") || name.startsWith("don gia") || name.equals("thanh tien") || name.contains("chi phi")
                || name.equals("muc giam") || name.equals("tien phu tung") || name.equals("tien cong")
                || name.equals("dich vu khac") || name.contains("thue") || name.equals("giam gia");
    }
    static int contentAlignment(JTable table,int column) {
        String name=name(table,column);
        if(name.startsWith("ma ") || name.equals("ma") || name.equals("stt") || name.contains("ngay") || name.contains("thoi gian") || name.contains("gioi tinh") || name.contains("trang thai") || name.contains("ket qua kiem tra") || name.contains("nghiem thu") || name.contains("bien so") || name.contains("vin") || name.contains("nam san xuat") || name.contains("odo")) return SwingConstants.CENTER;
        if(isMoneyColumn(table,column) || name.startsWith("tong ") || name.contains("tien") || name.contains("gia") || name.contains("so luong") || name.equals("sl") || name.contains("diem") || name.contains("thue") || name.contains("so km") || name.contains("thoi han")) return SwingConstants.RIGHT;
        return SwingConstants.LEFT;
    }
    @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column) {
        super.getTableCellRendererComponent(table,value,selected,focus,row,column);
        String text;
        if(value==null) text="—";
        else if(value instanceof java.sql.Date date) text=date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        else if(value instanceof java.sql.Timestamp date) text=date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        else if(value instanceof java.util.Date date) text=new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        else if(value instanceof Boolean flag) text=name(table,column).contains("gioi tinh") ? (flag ? "Nam" : "Nữ") : (flag ? "Có" : "Không");
        else if(value instanceof Number number && contentAlignment(table,column)==SwingConstants.RIGHT) text=numbers.format(number);
        else text=value.toString();
        boolean money=value!=null && isMoneyColumn(table,column);
        setText(text); setToolTipText(value==null ? null : text);
        setHorizontalAlignment(money ? SwingConstants.RIGHT : alignment(table,column));
        setVerticalAlignment(SwingConstants.CENTER);
        setFont(table.getFont()); setForeground(selected ? table.getSelectionForeground() : GiaoDien.TEXT);
        setBackground(selected ? table.getSelectionBackground() : row%2==0 ? GiaoDien.WHITE : new Color(0xF5F8F7));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,GiaoDien.LINE),BorderFactory.createEmptyBorder(5,10,5,money ? 16 : 10)));
        return this;
    }
    static void header(JTable table) {
        JTableHeader header=table.getTableHeader(); TableCellRenderer original=header.getDefaultRenderer();
        header.setDefaultRenderer((tbl,value,selected,focus,row,column) -> {
            Component component=original.getTableCellRendererComponent(tbl,value,selected,focus,row,column);
            if(component instanceof JLabel label) {
                label.setHorizontalAlignment(alignment(tbl,column)); label.setVerticalAlignment(SwingConstants.CENTER);
                label.setFont(GiaoDien.font(12,true)); label.setForeground(GiaoDien.TEAL); label.setBackground(new Color(0xEDF3F1)); label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,1,GiaoDien.LINE),BorderFactory.createEmptyBorder(8,10,8,10)));
                label.setToolTipText(String.valueOf(value));
            }
            return component;
        });
    }
}
