package gui;

import dao.CoSoDAO;
import dao.Schema;
import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Tra cứu bằng các trường tùy chọn, không có thao tác ghi dữ liệu. */
public final class ManHinhTimKiem extends JPanel {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final List<String> nhomDuocPhep;
    private final String nhom;
    static List<String> nhomTheoVaiTro(VaiTro role) {
        Objects.requireNonNull(role,"Phải xác định vai trò tìm kiếm.");
        List<String> kyThuat=List.of("Phiếu tiếp nhận","Phiếu kiểm tra","Phiếu báo giá","Phiếu sửa chữa","Bảo hành","Lịch nhắc bảo dưỡng");
        List<String> kho=List.of("Tồn kho","Nhập kho phụ tùng");
        List<String> banHang=List.of("Hóa đơn");
        List<String> quanLy=List.of("Nhà cung cấp","Nhân viên","Khách hàng","Phương tiện","Dịch vụ","Phụ tùng","Tài khoản","Khuyến mãi");
        return switch(role) {
            case KY_THUAT -> kyThuat;
            case KHO -> kho;
            case THU_NGAN -> banHang;
            case QUAN_LY -> quanLy;
            case ADMIN -> {
                List<String> all=new ArrayList<>(); all.addAll(kyThuat); all.addAll(kho); all.addAll(banHang); all.addAll(quanLy);
                yield List.copyOf(all);
            }
        };
    }
    private final JPanel pnlBieuMau = new JPanel(new GridBagLayout());
    private final Map<String,JTextField> inputs = new LinkedHashMap<>();
    private final JRadioButton radTatCa = new JRadioButton("Tất cả",true);
    private final JRadioButton radNam = new JRadioButton("Nam");
    private final JRadioButton radNu = new JRadioButton("Nữ");
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row,int column) { return false; }
    };
    private final JTable tblKetQua = GiaoDien.table(model);
    private final JButton btnTimKiem = GiaoDien.button("Tìm kiếm",GiaoDien.TEAL,Color.WHITE);
    private final JButton btnLamMoi = GiaoDien.button("Làm mới",GiaoDien.CREAM,GiaoDien.NAV);
    private final JLabel lblKetQua = GiaoDien.label("Chưa tìm kiếm",12,false,GiaoDien.MUTED);
    private final JScrollPane scrBieuMau = new JScrollPane(pnlBieuMau);
    private SwingWorker<List<LinkedHashMap<String,Object>>,Void> worker;
    private String table;
    private String[] fields;
    private int generation;

    public ManHinhTimKiem(VaiTro role) {
        this(role,nhomTheoVaiTro(role).get(0));
    }
    public ManHinhTimKiem(VaiTro role,String nhom) {
        super(new BorderLayout(0,14));
        nhomDuocPhep=nhomTheoVaiTro(role);
        if(nhom==null || !nhomDuocPhep.contains(nhom))
            throw new SecurityException("Không có quyền tìm kiếm nhóm dữ liệu: "+nhom);
        this.nhom=nhom;
        ButtonGroup grpGioiTinh=new ButtonGroup();
        grpGioiTinh.add(radTatCa); grpGioiTinh.add(radNam); grpGioiTinh.add(radNu);
        setBackground(GiaoDien.BG); setBorder(new EmptyBorder(24,25,24,25));
        setPreferredSize(new Dimension(960,740));
        JPanel pnlHeader = new JPanel(new BorderLayout(10,14)); pnlHeader.setOpaque(false);
        JLabel lblTitle = GiaoDien.label("Tìm kiếm: "+nhom,25,true,GiaoDien.NAV);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnlHeader.add(lblTitle,BorderLayout.NORTH); add(pnlHeader,BorderLayout.NORTH);
        JPanel pnlDieuKien = GiaoDien.card(new BorderLayout(0,8));
        pnlDieuKien.add(GiaoDien.label("Nhập thông tin cần tìm",13,true,GiaoDien.TEXT),BorderLayout.NORTH);
        pnlBieuMau.setBackground(GiaoDien.WHITE); scrBieuMau.setBorder(null); scrBieuMau.getVerticalScrollBar().setUnitIncrement(20);
        pnlDieuKien.add(scrBieuMau,BorderLayout.CENTER);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); pnlButtons.setOpaque(false);
        pnlButtons.add(btnTimKiem); pnlButtons.add(btnLamMoi); pnlDieuKien.add(pnlButtons,BorderLayout.SOUTH);
        JPanel pnlKetQua = GiaoDien.card(new BorderLayout(0,8)); pnlKetQua.add(lblKetQua,BorderLayout.NORTH);
        tblKetQua.setAutoCreateRowSorter(true);
        JScrollPane scrKetQua = new JScrollPane(tblKetQua); BoCucBang.install(tblKetQua,scrKetQua); pnlKetQua.add(scrKetQua,BorderLayout.CENTER);
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0,14)); pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlDieuKien,BorderLayout.NORTH); pnlNoiDung.add(pnlKetQua,BorderLayout.CENTER); add(pnlNoiDung,BorderLayout.CENTER);
        btnTimKiem.addActionListener(e -> timKiem()); btnLamMoi.addActionListener(e -> lamMoi());
        taoBieuMau();
    }
    private void taoBieuMau() {
        String selected=nhom;
        if(!nhomDuocPhep.contains(selected)) return;
        table=selected.equals("Dịch vụ") ? "DichVu" : ManHinhDuLieu.tableFor(selected);
        fields=Arrays.stream(Schema.COLUMNS.get(table)).filter(field -> !field.equals("matKhau")).toArray(String[]::new);
        inputs.clear(); pnlBieuMau.removeAll();
        for(int i=0;i<fields.length;i++) {
            String field=fields[i],type=Schema.TYPES.get(table).get(field).split(":",2)[0];
            JComponent input;
            if(field.equals("gioiTinh")) {
                JPanel pnlGioiTinh=new JPanel(new FlowLayout(FlowLayout.LEFT,14,4)); pnlGioiTinh.setOpaque(false);
                for(JRadioButton radGioiTinh:new JRadioButton[]{radTatCa,radNam,radNu}) {
                    radGioiTinh.setOpaque(false); radGioiTinh.setFont(GiaoDien.font(13,false));
                    pnlGioiTinh.add(radGioiTinh);
                }
                pnlGioiTinh.setToolTipText("Chọn Tất cả, Nam hoặc Nữ để lọc giới tính.");
                pnlGioiTinh.setPreferredSize(new Dimension(260,38)); input=pnlGioiTinh;
            } else {
                JTextField txtGiaTri=GiaoDien.field(""); txtGiaTri.setPreferredSize(new Dimension(260,38));
                txtGiaTri.setToolTipText(type.equals("date") || type.startsWith("datetime") ? "Nhập ngày dd/MM/yyyy, yyyy-MM-dd hoặc một phần ngày cần tìm."
                        : "Có thể nhập một phần thông tin; không bắt buộc.");
                txtGiaTri.addActionListener(e -> timKiem()); inputs.put(field,txtGiaTri);
                input=txtGiaTri;
            }
            JPanel pnlCell=new JPanel(new BorderLayout(0,5)); pnlCell.setOpaque(false);
            pnlCell.add(GiaoDien.label(NhanCot.forField(field),12,true,GiaoDien.TEXT),BorderLayout.NORTH); pnlCell.add(input,BorderLayout.CENTER);
            GridBagConstraints c=new GridBagConstraints(); c.gridx=i%2; c.gridy=i/2; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(6,8,6,8);
            pnlBieuMau.add(pnlCell,c);
        }
        scrBieuMau.setPreferredSize(new Dimension(800,Math.min(280,((fields.length+1)/2)*78+12)));
        lamMoi(); pnlBieuMau.revalidate(); pnlBieuMau.repaint();
        SwingUtilities.invokeLater(() -> scrBieuMau.getVerticalScrollBar().setValue(0));
    }
    private void lamMoi() {
        generation++; if(worker!=null) worker.cancel(true);
        inputs.values().forEach(input -> input.setText("")); model.setDataVector(new Object[0][],NhanCot.forFields(fields));
        radTatCa.setSelected(true);
        tblKetQua.clearSelection();
        tblKetQua.scrollRectToVisible(new Rectangle(0,0,1,1));
        lblKetQua.setText("Chưa tìm kiếm"); btnTimKiem.setEnabled(true); scrBieuMau.getVerticalScrollBar().setValue(0);
        if(tblKetQua.getRowSorter()!=null) tblKetQua.getRowSorter().setSortKeys(null);
        timKiem(); // Tải lại toàn bộ dữ liệu theo quyền hiện tại, không chỉ xóa bảng.
    }
    private void timKiem() {
        if(!btnTimKiem.isEnabled()) return;
        if(!nhomDuocPhep.contains(nhom)) { JOptionPane.showMessageDialog(this,"Vui lòng chọn nhóm dữ liệu được phép tìm kiếm."); return; }
        Map<String,String> criteria=new LinkedHashMap<>();
        inputs.forEach((field,input) -> { if(!input.getText().isBlank()) criteria.put(field,input.getText().trim()); });
        if(Arrays.asList(fields).contains("gioiTinh") && !radTatCa.isSelected())
            criteria.put("gioiTinh",radNam.isSelected() ? "Nam" : "Nữ");
        final String selectedTable=table; final String[] selectedFields=fields.clone(); final int request=++generation;
        btnTimKiem.setEnabled(false); model.setRowCount(0); lblKetQua.setText("Đang tìm kiếm…");
        worker=new SwingWorker<>() {
            @Override protected List<LinkedHashMap<String,Object>> doInBackground() throws Exception {
                List<LinkedHashMap<String,Object>> rows=new CoSoDAO(selectedTable).getAll();
                rows.removeIf(row -> !khopDieuKien(row,criteria)); return rows;
            }
            @Override protected void done() {
                if(request!=generation || isCancelled()) return;
                try {
                    List<LinkedHashMap<String,Object>> rows=get();
                    for(Map<String,Object> row:rows) {
                        Object[] values=new Object[selectedFields.length];
                        for(int i=0;i<selectedFields.length;i++) {
                            Object value=row.get(selectedFields[i]);
                            values[i]=selectedFields[i].equals("gioiTinh") && value!=null ? (Boolean.TRUE.equals(value) || "1".equals(value.toString()) ? "Nam" : "Nữ") : value;
                        }
                        model.addRow(values);
                    }
                    lblKetQua.setText(rows.isEmpty() ? "Không tìm thấy dữ liệu phù hợp." : "Tìm thấy: "+rows.size()+" bản ghi");
                } catch(CancellationException ignored) { }
                catch(Exception ex) {
                    lblKetQua.setText("Không tải được kết quả."); Throwable cause=ex.getCause()==null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(ManHinhTimKiem.this,cause.getMessage(),"Lỗi tìm kiếm",JOptionPane.ERROR_MESSAGE);
                } finally { if(request==generation) btnTimKiem.setEnabled(true); }
            }
        }; worker.execute();
    }
    static boolean khopDieuKien(Map<String,Object> row,Map<String,String> criteria) {
        for(var condition:criteria.entrySet()) {
            Object value=row.get(condition.getKey()); if(value==null) return false;
            String text=value.toString();
            if(condition.getKey().equals("gioiTinh")) text=Boolean.TRUE.equals(value) || "1".equals(text) ? "Nam 1" : "Nữ 0";
            else if(value instanceof Timestamp date) text+=" "+date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            else if(value instanceof java.sql.Date date) text+=" "+date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if(!OChonTimKiem.normalize(text).contains(OChonTimKiem.normalize(condition.getValue().trim()))) return false;
        }
        return true;
    }
}
