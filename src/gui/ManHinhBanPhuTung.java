package gui;

import connectDB.KetNoiCSDL;
import dao.ThuongMaiDAO;
import entity.HoaDon;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Quầy bán phụ tùng: giỏ hàng, nhập mã và bàn phím bên trái, danh mục bên phải. */
public final class ManHinhBanPhuTung extends JPanel {
    @java.io.Serial private static final long serialVersionUID=1L;
    record Product(String code,String name,String category,BigDecimal price,int stock) {}
    private final Map<String,Product> products=new LinkedHashMap<>();
    private final Map<String,Integer> quantities=new LinkedHashMap<>();
    private final Map<String,BigDecimal> cartPrices=new HashMap<>();
    private final NumberFormat money=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final DefaultTableModel cartModel=new DefaultTableModel(new String[]{"Mã","Tên phụ tùng","SL","Đơn giá","Thành tiền"},0) {
        @Override public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable tblCart=GiaoDien.table(cartModel);
    private final JTextField txtCode=GiaoDien.field("Nhập mã phụ tùng, ví dụ PT001 rồi Enter");
    private final JComboBox<LuaChonDuLieu.Choice> cmbCustomers=new OChonTimKiem<>();
    private final JTextField txtCustomerName=new JTextField();
    private final JTextField txtCustomerPhone=new JTextField();
    private final JComboBox<String> cmbCustomerGender=new JComboBox<>(new String[]{"Nam","Nữ"});
    private final JTextField txtCustomerEmail=new JTextField();
    private final JTextField txtCustomerAddress=new JTextField();
    private final String employeeId;
    private final JPanel pnlCategories=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
    private final JPanel pnlProducts=new JPanel() {
        @Override public Dimension getPreferredSize() {
            Dimension d=super.getPreferredSize();
            if(getParent() instanceof JViewport vp && vp.getWidth()>0) return new Dimension(vp.getWidth(),d.height);
            return d;
        }
    };
    private final JLabel lblTotal=GiaoDien.label("",15,true,GiaoDien.TEAL);
    private final JLabel lblStatus=GiaoDien.label("Chọn phụ tùng bên phải hoặc nhập mã.",12,false,GiaoDien.MUTED);
    private final JSpinner spnQuantity=new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
    private final JButton btnConfirm=GiaoDien.button("Xác nhận bán",GiaoDien.TEAL,Color.WHITE);
    private String category="Tất cả";
    private boolean loading;
    private boolean updatingCustomerFields;
    private Set<String> blockedParts=new HashSet<>();

    public ManHinhBanPhuTung() { this(null,true); }
    public ManHinhBanPhuTung(String employeeId) { this(employeeId,true); }
    private ManHinhBanPhuTung(String employeeId,boolean fetch) {
        super(new BorderLayout(0,8)); this.employeeId=employeeId; setBackground(GiaoDien.BG); setBorder(new EmptyBorder(12,14,12,14));
        JPanel pnlHeader=new JPanel(new BorderLayout()); pnlHeader.setOpaque(false);
        pnlHeader.add(GiaoDien.label("Bán phụ tùng",26,true,GiaoDien.NAV),BorderLayout.WEST);
        JButton btnRefresh=GiaoDien.button("Làm mới dữ liệu",GiaoDien.CREAM,GiaoDien.NAV);
        btnRefresh.addActionListener(e -> lamMoi()); pnlHeader.add(btnRefresh,BorderLayout.EAST); add(pnlHeader,BorderLayout.NORTH);

        JPanel pnlLeft=new JPanel(new BorderLayout(0,10)); pnlLeft.setOpaque(false);
        JPanel pnlCart=GiaoDien.card(new BorderLayout(0,10));
        pnlCart.setBorder(new EmptyBorder(10,12,10,12));
        pnlCart.add(GiaoDien.label("PHỤ TÙNG ĐÃ CHỌN",14,true,GiaoDien.NAV),BorderLayout.NORTH);
        tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tblCart.setAutoCreateRowSorter(true);
        JScrollPane scrCart=new JScrollPane(tblCart); scrCart.setPreferredSize(new Dimension(510,215));
        scrCart.setColumnHeaderView(tblCart.getTableHeader());
        BoCucBang.install(tblCart,scrCart);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(40);
        pnlCart.add(scrCart,BorderLayout.CENTER);
        JPanel pnlEdit=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); pnlEdit.setOpaque(false);
        pnlEdit.add(new JLabel("Số lượng")); spnQuantity.setPreferredSize(new Dimension(75,32)); pnlEdit.add(spnQuantity);
        JButton btnUpdate=GiaoDien.button("Cập nhật",GiaoDien.CREAM,GiaoDien.NAV);
        JButton btnRemove=GiaoDien.button("Bỏ dòng",GiaoDien.CREAM,GiaoDien.NAV);
        btnUpdate.addActionListener(e -> updateQuantity()); btnRemove.addActionListener(e -> removeSelected());
        pnlEdit.add(btnUpdate); pnlEdit.add(btnRemove);
        JButton btnClear=GiaoDien.button("Xóa giỏ",GiaoDien.CREAM,GiaoDien.NAV);
        btnClear.addActionListener(e -> { if(!quantities.isEmpty() && JOptionPane.showConfirmDialog(this,"Xóa toàn bộ phụ tùng đã chọn?","Xóa giỏ",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { quantities.clear(); cartPrices.clear(); refreshCart(); } });
        pnlEdit.add(btnClear); pnlCart.add(pnlEdit,BorderLayout.SOUTH); pnlLeft.add(pnlCart,BorderLayout.CENTER);

        JPanel pnlEntry=new JPanel(new BorderLayout(0,9)); pnlEntry.setOpaque(false);
        JPanel pnlCode=GiaoDien.card(new BorderLayout(8,6));
        pnlCode.setBorder(new EmptyBorder(8,12,8,12));
        pnlCode.add(GiaoDien.label("Mã phụ tùng",12,true,GiaoDien.NAV),BorderLayout.WEST);
        txtCode.setPreferredSize(new Dimension(260,34)); pnlCode.add(txtCode,BorderLayout.CENTER);
        JButton btnAdd=GiaoDien.button("Thêm",GiaoDien.TEAL,Color.WHITE); btnAdd.addActionListener(e -> addCode()); pnlCode.add(btnAdd,BorderLayout.EAST);
        txtCode.addActionListener(e -> addCode()); pnlEntry.add(pnlCode,BorderLayout.NORTH);
        pnlEntry.add(keyboard(),BorderLayout.CENTER); pnlLeft.add(pnlEntry,BorderLayout.SOUTH);

        JPanel pnlRight=GiaoDien.card(new BorderLayout(0,10));
        pnlRight.setBorder(new EmptyBorder(10,12,10,12));
        pnlCategories.setOpaque(false);
        JScrollPane scrCategories=new JScrollPane(pnlCategories,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrCategories.setPreferredSize(new Dimension(420,48)); scrCategories.setBorder(null);
        scrCategories.getHorizontalScrollBar().setUnitIncrement(20);
        pnlRight.add(scrCategories,BorderLayout.NORTH);
        scrCategories.getViewport().setBackground(GiaoDien.WHITE);
        pnlProducts.setLayout(new BoxLayout(pnlProducts,BoxLayout.Y_AXIS)); pnlProducts.setOpaque(false);
        JScrollPane scrProducts=new JScrollPane(pnlProducts,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrProducts.setBorder(null); scrProducts.getVerticalScrollBar().setUnitIncrement(20); pnlRight.add(scrProducts,BorderLayout.CENTER);
        scrProducts.getViewport().setBackground(GiaoDien.WHITE);
        JPanel pnlSale=new JPanel(new BorderLayout(12,0)); pnlSale.setOpaque(false);
        pnlRight.setPreferredSize(new Dimension(450,0));
        pnlLeft.setMinimumSize(new Dimension(0,0)); pnlRight.setMinimumSize(new Dimension(0,0));
        pnlSale.add(pnlLeft,BorderLayout.CENTER); pnlSale.add(pnlRight,BorderLayout.EAST);
        add(pnlSale,BorderLayout.CENTER);

        JPanel pnlFooter=GiaoDien.card(new BorderLayout(10,6));
        pnlFooter.setBorder(new EmptyBorder(8,12,8,12));
        JPanel pnlChoices=new JPanel(new GridBagLayout()); pnlChoices.setOpaque(false);
        GridBagConstraints constraints=new GridBagConstraints(); constraints.fill=GridBagConstraints.HORIZONTAL; constraints.insets=new Insets(0,0,0,8);
        pnlChoices.add(GiaoDien.label("Khách hàng có sẵn (để trống nếu tạo khách mới)",12,true,GiaoDien.TEXT),constraints);
        constraints.weightx=1; pnlChoices.add(cmbCustomers,constraints);
        pnlFooter.add(pnlChoices,BorderLayout.NORTH);
        JPanel pnlCustomerForm=new JPanel(new GridLayout(2,5,8,3)); pnlCustomerForm.setOpaque(false);
        pnlCustomerForm.add(GiaoDien.label("Họ tên khách hàng *",11,true,GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Số điện thoại *",11,true,GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Giới tính *",11,true,GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Email *",11,true,GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Địa chỉ",11,true,GiaoDien.TEXT));
        pnlCustomerForm.add(txtCustomerName); pnlCustomerForm.add(txtCustomerPhone);
        pnlCustomerForm.add(cmbCustomerGender); pnlCustomerForm.add(txtCustomerEmail); pnlCustomerForm.add(txtCustomerAddress);
        pnlFooter.add(pnlCustomerForm,BorderLayout.CENTER);
        JPanel pnlBottom=new JPanel(new BorderLayout(10,4)); pnlBottom.setOpaque(false);
        JPanel pnlSummary=new JPanel(new BorderLayout(10,4)); pnlSummary.setOpaque(false); pnlSummary.add(lblTotal,BorderLayout.CENTER); pnlSummary.add(btnConfirm,BorderLayout.EAST);
        pnlBottom.add(pnlSummary,BorderLayout.CENTER); pnlBottom.add(lblStatus,BorderLayout.SOUTH); pnlFooter.add(pnlBottom,BorderLayout.SOUTH); add(pnlFooter,BorderLayout.SOUTH);
        cmbCustomers.addActionListener(e -> { if(!loading) { loadCustomerForm(); loadInvoiceContext(); } });
        javax.swing.event.DocumentListener customerInputListener=new javax.swing.event.DocumentListener() {
            private void changed() { if(!updatingCustomerFields) refreshCart(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        };
        for(JTextField field:List.of(txtCustomerName,txtCustomerPhone,txtCustomerEmail,txtCustomerAddress)) field.getDocument().addDocumentListener(customerInputListener);
        cmbCustomerGender.addActionListener(e -> { if(!updatingCustomerFields) refreshCart(); });
        tblCart.getSelectionModel().addListSelectionListener(e -> { int row=selectedRow(); if(row>=0) spnQuantity.setValue(quantities.get(cartModel.getValueAt(row,0).toString())); });
        btnConfirm.addActionListener(e -> sell()); refreshCart();
        if(fetch) load();
    }
    private JPanel keyboard() {
        JPanel pnlKeyboard=GiaoDien.card(new GridLayout(0,10,5,5));
        pnlKeyboard.setBorder(new EmptyBorder(8,12,8,12));
        pnlKeyboard.setPreferredSize(new Dimension(0,104));
        String[] keys={"1","2","3","4","5","6","7","8","9","0","Q","W","E","R","T","Y","U","I","O","P","A","S","D","F","G","H","J","K","L","-","Z","X","C","V","B","N","M","Lùi","Xóa","Enter"};
        for(String key:keys) {
            JButton btnKey=GiaoDien.button(key,key.equals("Enter") ? GiaoDien.TEAL : GiaoDien.CREAM,key.equals("Enter") ? Color.WHITE : GiaoDien.NAV);
            btnKey.setIcon(null); btnKey.setDisabledIcon(null); btnKey.setPressedIcon(null); btnKey.setRolloverIcon(null);
            btnKey.setBorder(new EmptyBorder(4,1,4,1)); btnKey.setFont(GiaoDien.font(12,true)); btnKey.setFocusable(false);
            btnKey.addActionListener(e -> {
                switch(key) {
                    case "Enter" -> addCode();
                    case "Xóa" -> txtCode.setText("");
                    case "Lùi" -> { int start=txtCode.getSelectionStart(),end=txtCode.getSelectionEnd(); if(start!=end) txtCode.replaceSelection(""); else if(start>0) { txtCode.select(start-1,start); txtCode.replaceSelection(""); } }
                    default -> txtCode.replaceSelection(key);
                }
                txtCode.requestFocusInWindow();
            }); pnlKeyboard.add(btnKey);
        }
        return pnlKeyboard;
    }
    private void lamMoi() {
        txtCode.setText(""); category="Tất cả";
        if(cmbCustomers.getItemCount()>0) cmbCustomers.setSelectedIndex(0);
        tblCart.clearSelection();
        if(tblCart.getRowSorter()!=null) tblCart.getRowSorter().setSortKeys(null);
        spnQuantity.setValue(1);
        load();
    }
    private void load() {
        String oldInvoice=LuaChonDuLieu.id(cmbCustomers); loading=true; btnConfirm.setEnabled(false);
        try {
            Map<String,Product> fresh=new LinkedHashMap<>();
            try(Connection conn=KetNoiCSDL.getConnection(); PreparedStatement ps=conn.prepareStatement("SELECT p.maPhuTung,p.tenPhuTung,p.loaiPhuTung,p.donGia,COALESCE(t.soLuongHienTai,0) AS ton FROM dbo.PhuTung p LEFT JOIN dbo.TonKho t ON t.maPhuTung=p.maPhuTung ORDER BY p.maPhuTung"); ResultSet rs=ps.executeQuery()) {
                while(rs.next()) { Product p=new Product(rs.getString(1),rs.getString(2),rs.getString(3),rs.getBigDecimal(4),rs.getInt(5)); fresh.put(p.code(),p); }
            }
            products.clear(); products.putAll(fresh);
            LuaChonDuLieu.load(cmbCustomers,"SELECT maKhachHang,maKhachHang+N' — '+hoTenKH FROM dbo.KhachHang ORDER BY maKhachHang");
            cmbCustomers.insertItemAt(new LuaChonDuLieu.Choice("","— Chọn khách hàng —"),0); cmbCustomers.setSelectedIndex(0);
            if(oldInvoice!=null) for(int i=0;i<cmbCustomers.getItemCount();i++) if(oldInvoice.equals(cmbCustomers.getItemAt(i).id())) cmbCustomers.setSelectedIndex(i);
            buildCategories();
        } catch(SQLException ex) { products.clear(); cmbCustomers.removeAllItems(); error(ex); }
        finally { loading=false; }
        loadCustomerForm(); loadInvoiceContext(); refreshCart();
    }
    private void loadCustomerForm() {
        String customer=LuaChonDuLieu.id(cmbCustomers);
        updatingCustomerFields=true;
        try {
            boolean isExisting=customer!=null && !customer.isBlank();
            for(JComponent input:List.of(txtCustomerName,txtCustomerPhone,cmbCustomerGender,txtCustomerEmail,txtCustomerAddress)) input.setEnabled(!isExisting);
            if(!isExisting) {
                txtCustomerName.setText(""); txtCustomerPhone.setText(""); cmbCustomerGender.setSelectedItem("Nam");
                txtCustomerEmail.setText(""); txtCustomerAddress.setText("");
                lblStatus.setText("Nhập thông tin khách hàng mới hoặc chọn khách hàng có sẵn.");
                return;
            }
            try(Connection conn=KetNoiCSDL.getConnection(); PreparedStatement ps=conn.prepareStatement("SELECT hoTenKH,soDienThoai,gioiTinh,email,diaChi FROM dbo.KhachHang WHERE maKhachHang=?")) {
                ps.setString(1,customer);
                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) throw new SQLException("Không tìm thấy khách hàng đã chọn. Vui lòng tải lại danh sách.");
                    txtCustomerName.setText(rs.getString("hoTenKH")); txtCustomerPhone.setText(rs.getString("soDienThoai"));
                    cmbCustomerGender.setSelectedItem(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
                    txtCustomerEmail.setText(rs.getString("email")); txtCustomerAddress.setText(Objects.toString(rs.getString("diaChi"),""));
                }
            }
        } catch(SQLException ex) { lblStatus.setText(ex.getMessage()); }
        finally { updatingCustomerFields=false; }
        refreshCart();
    }
    private boolean newCustomerFormValid() {
        String name=txtCustomerName.getText().trim(), phone=txtCustomerPhone.getText().trim();
        String email=txtCustomerEmail.getText().trim(), address=txtCustomerAddress.getText().trim();
        return !name.isBlank() && name.length()<=100
            && phone.matches("0[35789][0-9]{8}")
            && email.length()<=254 && email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
            && address.length()<=255;
    }
    private Map<String,Object> newCustomerValues() {
        Map<String,Object> customer=new LinkedHashMap<>();
        customer.put("hoTenKH",txtCustomerName.getText().trim());
        customer.put("soDienThoai",txtCustomerPhone.getText().trim());
        customer.put("gioiTinh","Nam".equals(cmbCustomerGender.getSelectedItem()));
        customer.put("email",txtCustomerEmail.getText().trim());
        String address=txtCustomerAddress.getText().trim();
        if(!address.isBlank()) customer.put("diaChi",address);
        return customer;
    }
    private void loadInvoiceContext() {
        blockedParts=new HashSet<>();
        lblStatus.setText("Chọn khách hàng, xác nhận bán rồi thanh toán hóa đơn.");
        renderProducts(); refreshCart();
    }
    private void buildCategories() {
        pnlCategories.removeAll(); Set<String> categories=new LinkedHashSet<>(); categories.add("Tất cả");
        for(Product p:products.values()) categories.add(p.category());
        if(!categories.contains(category)) category="Tất cả";
        ButtonGroup group=new ButtonGroup();
        for(String name:categories) {
            JToggleButton tglCategory=new JToggleButton(name); group.add(tglCategory); tglCategory.setSelected(name.equals(category));
            tglCategory.setFont(GiaoDien.font(12,true));
            tglCategory.setToolTipText(name);
            tglCategory.addActionListener(e -> { category=name; renderProducts(); }); pnlCategories.add(tglCategory);
        }
        pnlCategories.revalidate(); pnlCategories.repaint();
    }
    private void renderProducts() {
        pnlProducts.removeAll(); int count=0;
        for(Product p:products.values()) {
            if(!category.equals("Tất cả") && !category.equals(p.category())) continue;
            count++;
            JPanel pnlProduct=new JPanel(new BorderLayout(10,6)); pnlProduct.setBackground(GiaoDien.WHITE);
            pnlProduct.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GiaoDien.LINE),new EmptyBorder(10,12,10,12)));
            JPanel pnlText=new JPanel(new GridLayout(0,1,0,4)); pnlText.setOpaque(false);
            JLabel lblProduct=GiaoDien.label(p.code()+" • "+p.name(),12,true,GiaoDien.NAV);
            lblProduct.setToolTipText(p.code()+" • "+p.name()); pnlText.add(lblProduct);
            pnlText.add(GiaoDien.label(format(p.price())+"  |  Tồn: "+p.stock(),13,true,GiaoDien.TEAL));
            boolean blocked=blockedParts.contains(p.code());
            if(blocked) pnlText.add(GiaoDien.label("Đã có trong hóa đơn/phiếu sửa",11,false,GiaoDien.ORANGE));
            else if(p.stock()==0) pnlText.add(GiaoDien.label("Hết hàng",11,true,GiaoDien.ORANGE));
            pnlProduct.add(pnlText,BorderLayout.CENTER);
            JButton btnAdd=GiaoDien.button("+",GiaoDien.TEAL,Color.WHITE); btnAdd.setToolTipText("Thêm "+p.code()+" vào giỏ");
            btnAdd.setIcon(null); btnAdd.setDisabledIcon(null); btnAdd.setPressedIcon(null); btnAdd.setRolloverIcon(null);
            btnAdd.setBorder(new EmptyBorder(8,14,8,14)); btnAdd.setFont(GiaoDien.font(14,true));
            btnAdd.setEnabled(p.stock()>0 && !blocked); btnAdd.addActionListener(e -> addProduct(p.code())); pnlProduct.add(btnAdd,BorderLayout.EAST);
            pnlProduct.setMaximumSize(new Dimension(Integer.MAX_VALUE,blocked || p.stock()==0 ? 106 : 86));
            pnlProduct.setAlignmentX(Component.LEFT_ALIGNMENT); pnlProducts.add(pnlProduct); pnlProducts.add(Box.createVerticalStrut(8));
        }
        if(count==0) pnlProducts.add(GiaoDien.label("Không có phụ tùng trong nhóm này.",13,false,GiaoDien.MUTED));
        pnlProducts.revalidate(); pnlProducts.repaint();
    }
    private void addCode() { if(addProduct(txtCode.getText().trim().toUpperCase(Locale.ROOT))) txtCode.setText(""); }
    private boolean addProduct(String code) {
        Product p=products.get(code);
        if(p==null) { lblStatus.setText("Không tìm thấy mã phụ tùng: "+code); return false; }
        if(blockedParts.contains(code)) { lblStatus.setText(code+" đã có trong hóa đơn hoặc đã cấp cho phiếu sửa."); return false; }
        int old=quantities.getOrDefault(code,0);
        if(old>=p.stock()) { lblStatus.setText("Không đủ tồn kho cho "+code+"."); return false; }
        quantities.put(code,old+1); cartPrices.putIfAbsent(code,p.price()); refreshCart(); lblStatus.setText("Đã thêm "+code+" vào giỏ."); return true;
    }
    private int selectedRow() { int row=tblCart.getSelectedRow(); return row<0 ? -1 : tblCart.convertRowIndexToModel(row); }
    private void updateQuantity() {
        int row=selectedRow(); if(row<0) { lblStatus.setText("Chọn dòng cần sửa số lượng."); return; }
        try { spnQuantity.commitEdit(); } catch(java.text.ParseException ex) { lblStatus.setText("Số lượng phải là số nguyên dương."); return; }
        String code=cartModel.getValueAt(row,0).toString(); Product p=products.get(code); int quantity=((Number)spnQuantity.getValue()).intValue();
        if(p==null || quantity>p.stock()) { lblStatus.setText("Không đủ tồn kho cho "+code+"."); return; }
        quantities.put(code,quantity); refreshCart();
    }
    private void removeSelected() { int row=selectedRow(); if(row>=0) { String code=cartModel.getValueAt(row,0).toString(); quantities.remove(code); cartPrices.remove(code); refreshCart(); } }
    private void refreshCart() {
        cartModel.setRowCount(0); BigDecimal subtotal=BigDecimal.ZERO; boolean valid=!quantities.isEmpty();
        for(var entry:quantities.entrySet()) {
            String code=entry.getKey(); Product p=products.get(code); BigDecimal price=cartPrices.get(code),amount=price.multiply(BigDecimal.valueOf(entry.getValue())); subtotal=subtotal.add(amount);
            boolean stale=p==null || entry.getValue()>p.stock() || price.compareTo(p.price())!=0 || blockedParts.contains(code);
            valid &= !stale;
            cartModel.addRow(new Object[]{code,p==null ? "Không còn trong danh mục" : p.name()+(stale ? " (cần chọn lại)" : ""),entry.getValue(),format(price),format(amount)});
        }
        HoaDon hoaDon=new HoaDon();
        hoaDon.setTienPhuTung(subtotal);
        hoaDon.setTienCong(BigDecimal.ZERO);
        hoaDon.setDichVuKhac(BigDecimal.ZERO);
        BigDecimal vat=hoaDon.tinhThueVAT();
        lblTotal.setText("<html>Tiền hàng: "+format(subtotal)+" &nbsp; | &nbsp; VAT 8%: "+format(vat)+"<br>Cộng: "+format(subtotal.add(vat))+"</html>");
        lblTotal.setToolTipText("Tiền hàng và VAT 8%; mã giảm giá được chọn trong chi tiết hóa đơn.");
        String customer=LuaChonDuLieu.id(cmbCustomers);
        boolean hasExistingCustomer=customer!=null && !customer.isBlank();
        boolean customerValid=hasExistingCustomer || newCustomerFormValid();
        btnConfirm.setEnabled(!loading && valid && customerValid);
        if(!hasExistingCustomer && !newCustomerFormValid()) lblStatus.setText("Nhập họ tên, số điện thoại hợp lệ và email để lập hóa đơn.");
        else if(!quantities.isEmpty() && !valid) lblStatus.setText("Có dòng đổi giá, thiếu tồn hoặc đã có trong hóa đơn. Bỏ dòng và chọn lại.");
    }
    private String format(BigDecimal amount) { return money.format(amount)+" đ"; }
    private void sell() {
        if(!btnConfirm.isEnabled()) return;
        String customer=LuaChonDuLieu.id(cmbCustomers);
        String customerName=customer==null || customer.isBlank() ? txtCustomerName.getText().trim() : String.valueOf(cmbCustomers.getSelectedItem());
        if(JOptionPane.showConfirmDialog(this,"Lập hóa đơn bán "+quantities.size()+" loại phụ tùng cho "+customerName+"?","Xác nhận bán phụ tùng",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        List<ThuongMaiDAO.SaleItem> items=quantities.entrySet().stream().map(e -> new ThuongMaiDAO.SaleItem(e.getKey(),e.getValue(),cartPrices.get(e.getKey()))).toList();
        btnConfirm.setEnabled(false);
        try {
            String invoice=customer==null || customer.isBlank()
                ? ThuongMaiDAO.createPartsInvoice(newCustomerValues(),employeeId,items)
                : ThuongMaiDAO.createPartsInvoice(customer,employeeId,items);
            quantities.clear(); cartPrices.clear(); load(); lblStatus.setText("Đã lập "+invoice+"; tiếp tục thanh toán trong chi tiết hóa đơn.");
            showSaleInvoice(invoice);
        } catch(SQLException ex) { error(ex); load(); }
    }
    private void showSaleInvoice(String invoice) {
        JDialog winInvoice=new JDialog(SwingUtilities.getWindowAncestor(this),"Hóa đơn bán phụ tùng "+invoice,Dialog.ModalityType.APPLICATION_MODAL);
        ManHinhHoaDon pnlInvoice=new ManHinhHoaDon(invoice);
        JScrollPane scrInvoice=new JScrollPane(pnlInvoice); scrInvoice.getVerticalScrollBar().setUnitIncrement(20);
        winInvoice.setContentPane(scrInvoice); winInvoice.setSize(1060,760); winInvoice.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(pnlInvoice::openPayment);
        winInvoice.setVisible(true);
    }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Không thể xử lý bán phụ tùng",JOptionPane.ERROR_MESSAGE); }
}
