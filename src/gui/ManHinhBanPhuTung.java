package gui;

import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.text.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Component;

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

// ===== GUI: MÀN HÌNH BÁN PHỤ TÙNG =====

/** Quầy bán phụ tùng: giỏ hàng, nhập mã và bàn phím bên trái, danh mục bên phải. */
public final class ManHinhBanPhuTung extends JPanel {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    record Product(String code, String name, String category, BigDecimal price, int stock) {
    }

    private final Map<String, Product> products = new LinkedHashMap<>();
    private final Map<String, Integer> quantities = new LinkedHashMap<>();
    private final Map<String, BigDecimal> cartPrices = new HashMap<>();
    private final NumberFormat money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final DefaultTableModel cartModel=new DefaultTableModel(new String[]{"Mã","Tên phụ tùng","SL","Đơn giá","Thành tiền"},0){
    // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
    @Override public boolean isCellEditable(int row,int column){return false;}};
    private final JTable tblCart = GiaoDien.table(cartModel);
    private final JTextField txtCode = GiaoDien.field("Nhập mã phụ tùng, ví dụ PT001 rồi Enter");
    private final JComboBox<MucKhachHang> cmbCustomers = new OChonTimKiem<>();
    private final JTextField txtCustomerName = new JTextField();
    private final JTextField txtCustomerPhone = new JTextField();
    private final JComboBox<String> cmbCustomerGender = new JComboBox<>(new String[] { "Nam", "Nữ" });
    private final JTextField txtCustomerEmail = new JTextField();
    private final JTextField txtCustomerAddress = new JTextField();
    private final String employeeId;
    private final JPanel pnlCategories = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    private final JPanel pnlProducts=new JPanel(){
    // ===== PHƯƠNG THỨC: getPreferredSize() — Tính kích thước ưu tiên của thành phần. =====
    @Override public Dimension getPreferredSize(){Dimension preferredSize=super.getPreferredSize();if(getParent()instanceof JViewport viewport&&viewport.getWidth()>0){return new Dimension(viewport.getWidth(),preferredSize.height);}return preferredSize;}};
    private final JLabel lblTotal = GiaoDien.label("", 15, true, GiaoDien.TEAL);
    private final JLabel lblStatus = GiaoDien.label("Chọn phụ tùng bên phải hoặc nhập mã.", 12, false,
            GiaoDien.MUTED);
    private final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JButton btnConfirm = GiaoDien.button("Xác nhận bán", GiaoDien.TEAL, Color.WHITE);
    private String category = "Tất cả";
    private boolean loading;
    private boolean updatingCustomerFields;
    private Set<String> blockedParts = new HashSet<>();
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhBanPhuTung() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ManHinhBanPhuTung() {
        this(null, true);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhBanPhuTung(String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ManHinhBanPhuTung(String employeeId) {
        this(employeeId, true);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhBanPhuTung(String, boolean) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private ManHinhBanPhuTung(String employeeId, boolean fetch) {
        super(new BorderLayout(0, 8));
        this.employeeId = employeeId;
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(12, 14, 12, 14));
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.add(GiaoDien.label("Bán phụ tùng", 26, true, GiaoDien.NAV), BorderLayout.WEST);
        JButton btnRefresh = GiaoDien.button("Làm mới dữ liệu", GiaoDien.CREAM, GiaoDien.NAV);
        btnRefresh.addActionListener(e -> lamMoi());
        pnlHeader.add(btnRefresh, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setOpaque(false);
        JPanel pnlCart = GiaoDien.card(new BorderLayout(0, 10));
        pnlCart.setBorder(new EmptyBorder(10, 12, 10, 12));
        pnlCart.add(GiaoDien.label("PHỤ TÙNG ĐÃ CHỌN", 14, true, GiaoDien.NAV), BorderLayout.NORTH);
        tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCart.setAutoCreateRowSorter(true);
        JScrollPane scrCart = new JScrollPane(tblCart);
        scrCart.setPreferredSize(new Dimension(510, 215));
        scrCart.setColumnHeaderView(tblCart.getTableHeader());
        BoCucBang.install(tblCart, scrCart);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(40);
        pnlCart.add(scrCart, BorderLayout.CENTER);
        JPanel pnlEdit = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlEdit.setOpaque(false);
        pnlEdit.add(new JLabel("Số lượng"));
        spnQuantity.setPreferredSize(new Dimension(75, 32));
        pnlEdit.add(spnQuantity);
        JButton btnUpdate = GiaoDien.button("Cập nhật", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnRemove = GiaoDien.button("Bỏ dòng", GiaoDien.CREAM, GiaoDien.NAV);
        btnUpdate.addActionListener(e -> updateQuantity());
        btnRemove.addActionListener(e -> removeSelected());
        pnlEdit.add(btnUpdate);
        pnlEdit.add(btnRemove);
        JButton btnClear = GiaoDien.button("Xóa giỏ", GiaoDien.CREAM, GiaoDien.NAV);
        btnClear.addActionListener(e -> {
            if (!quantities.isEmpty() && JOptionPane.showConfirmDialog(this, "Xóa toàn bộ phụ tùng đã chọn?",
                    "Xóa giỏ", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                quantities.clear();
                cartPrices.clear();
                refreshCart();
            }
        });
        pnlEdit.add(btnClear);
        pnlCart.add(pnlEdit, BorderLayout.SOUTH);
        pnlLeft.add(pnlCart, BorderLayout.CENTER);

        JPanel pnlEntry = new JPanel(new BorderLayout(0, 9));
        pnlEntry.setOpaque(false);
        JPanel pnlCode = GiaoDien.card(new BorderLayout(8, 6));
        pnlCode.setBorder(new EmptyBorder(8, 12, 8, 12));
        pnlCode.add(GiaoDien.label("Mã phụ tùng", 12, true, GiaoDien.NAV), BorderLayout.WEST);
        txtCode.setPreferredSize(new Dimension(260, 34));
        pnlCode.add(txtCode, BorderLayout.CENTER);
        JButton btnAdd = GiaoDien.button("Thêm", GiaoDien.TEAL, Color.WHITE);
        btnAdd.addActionListener(e -> addCode());
        pnlCode.add(btnAdd, BorderLayout.EAST);
        txtCode.addActionListener(e -> addCode());
        pnlEntry.add(pnlCode, BorderLayout.NORTH);
        pnlEntry.add(keyboard(), BorderLayout.CENTER);
        pnlLeft.add(pnlEntry, BorderLayout.SOUTH);

        JPanel pnlRight = GiaoDien.card(new BorderLayout(0, 10));
        pnlRight.setBorder(new EmptyBorder(10, 12, 10, 12));
        pnlCategories.setOpaque(false);
        JScrollPane scrCategories = new JScrollPane(pnlCategories,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrCategories.setPreferredSize(new Dimension(420, 48));
        scrCategories.setBorder(null);
        scrCategories.getHorizontalScrollBar().setUnitIncrement(20);
        pnlRight.add(scrCategories, BorderLayout.NORTH);
        scrCategories.getViewport().setBackground(GiaoDien.WHITE);
        pnlProducts.setLayout(new BoxLayout(pnlProducts, BoxLayout.Y_AXIS));
        pnlProducts.setOpaque(false);
        JScrollPane scrProducts = new JScrollPane(pnlProducts,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrProducts.setBorder(null);
        scrProducts.getVerticalScrollBar().setUnitIncrement(20);
        pnlRight.add(scrProducts, BorderLayout.CENTER);
        scrProducts.getViewport().setBackground(GiaoDien.WHITE);
        JPanel pnlSale = new JPanel(new BorderLayout(12, 0));
        pnlSale.setOpaque(false);
        pnlRight.setPreferredSize(new Dimension(450, 0));
        pnlLeft.setMinimumSize(new Dimension(0, 0));
        pnlRight.setMinimumSize(new Dimension(0, 0));
        pnlSale.add(pnlLeft, BorderLayout.CENTER);
        pnlSale.add(pnlRight, BorderLayout.EAST);
        add(pnlSale, BorderLayout.CENTER);

        JPanel pnlFooter = GiaoDien.card(new BorderLayout(10, 6));
        pnlFooter.setBorder(new EmptyBorder(8, 12, 8, 12));
        JPanel pnlChoices = new JPanel(new GridBagLayout());
        pnlChoices.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 8);
        pnlChoices.add(
                GiaoDien.label("Khách hàng có sẵn (để trống nếu tạo khách mới)", 12, true, GiaoDien.TEXT),
                constraints);
        constraints.weightx = 1;
        pnlChoices.add(cmbCustomers, constraints);
        pnlFooter.add(pnlChoices, BorderLayout.NORTH);
        JPanel pnlCustomerForm = new JPanel(new GridLayout(2, 5, 8, 3));
        pnlCustomerForm.setOpaque(false);
        pnlCustomerForm.add(GiaoDien.label("Họ tên khách hàng *", 11, true, GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Số điện thoại *", 11, true, GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Giới tính *", 11, true, GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Email *", 11, true, GiaoDien.TEXT));
        pnlCustomerForm.add(GiaoDien.label("Địa chỉ", 11, true, GiaoDien.TEXT));
        pnlCustomerForm.add(txtCustomerName);
        pnlCustomerForm.add(txtCustomerPhone);
        pnlCustomerForm.add(cmbCustomerGender);
        pnlCustomerForm.add(txtCustomerEmail);
        pnlCustomerForm.add(txtCustomerAddress);
        pnlFooter.add(pnlCustomerForm, BorderLayout.CENTER);
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 4));
        pnlBottom.setOpaque(false);
        JPanel pnlSummary = new JPanel(new BorderLayout(10, 4));
        pnlSummary.setOpaque(false);
        pnlSummary.add(lblTotal, BorderLayout.CENTER);
        pnlSummary.add(btnConfirm, BorderLayout.EAST);
        pnlBottom.add(pnlSummary, BorderLayout.CENTER);
        pnlBottom.add(lblStatus, BorderLayout.SOUTH);
        pnlFooter.add(pnlBottom, BorderLayout.SOUTH);
        add(pnlFooter, BorderLayout.SOUTH);
        cmbCustomers.addActionListener(e -> {
            if (!loading) {
                loadCustomerForm();
                loadInvoiceContext();
            }
        });
        javax.swing.event.DocumentListener customerInputListener = new javax.swing.event.DocumentListener() {
            // ===== PHƯƠNG THỨC: changed() — Cập nhật xử lý khi dữ liệu thay đổi. =====
            private void changed() {
                if (!updatingCustomerFields)
                    refreshCart();
            }

            // ===== PHƯƠNG THỨC: insertUpdate(javax.swing.event.DocumentEvent) — Xử lý khi nội dung được thêm vào. =====
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            // ===== PHƯƠNG THỨC: removeUpdate(javax.swing.event.DocumentEvent) — Xử lý khi nội dung bị xóa. =====
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            // ===== PHƯƠNG THỨC: changedUpdate(javax.swing.event.DocumentEvent) — Cập nhật giao diện khi nội dung thay đổi. =====
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        };
        for (JTextField field : List.of(txtCustomerName, txtCustomerPhone, txtCustomerEmail,
                txtCustomerAddress))
            field.getDocument().addDocumentListener(customerInputListener);
        cmbCustomerGender.addActionListener(e -> {
            if (!updatingCustomerFields)
                refreshCart();
        });
        tblCart.getSelectionModel().addListSelectionListener(e -> {
            int row = selectedRow();
            if (row >= 0)
                spnQuantity.setValue(quantities.get(cartModel.getValueAt(row, 0).toString()));
        });
        btnConfirm.addActionListener(e -> sell());
        refreshCart();
        if (fetch)
            load();
    }

    // ===== PHƯƠNG THỨC: keyboard() — Xử lý thao tác bàn phím trên giao diện. =====
    private JPanel keyboard() {
        JPanel pnlKeyboard = GiaoDien.card(new GridLayout(0, 10, 5, 5));
        pnlKeyboard.setBorder(new EmptyBorder(8, 12, 8, 12));
        pnlKeyboard.setPreferredSize(new Dimension(0, 104));
        String[] keys = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "Q", "W", "E", "R", "T", "Y", "U",
                "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "-", "Z", "X", "C", "V", "B", "N",
                "M", "Lùi", "Xóa", "Enter" };
        for (String key : keys) {
            JButton btnKey = GiaoDien.button(key, key.equals("Enter") ? GiaoDien.TEAL : GiaoDien.CREAM,
                    key.equals("Enter") ? Color.WHITE : GiaoDien.NAV);
            btnKey.setIcon(null);
            btnKey.setDisabledIcon(null);
            btnKey.setPressedIcon(null);
            btnKey.setRolloverIcon(null);
            btnKey.setBorder(new EmptyBorder(4, 1, 4, 1));
            btnKey.setFont(GiaoDien.font(12, true));
            btnKey.setFocusable(false);
            btnKey.addActionListener(e -> {
                switch (key) {
                case "Enter" -> addCode();
                case "Xóa" -> txtCode.setText("");
                case "Lùi" -> {
                    int start = txtCode.getSelectionStart(), end = txtCode.getSelectionEnd();
                    if (start != end)
                        txtCode.replaceSelection("");
                    else if (start > 0) {
                        txtCode.select(start - 1, start);
                        txtCode.replaceSelection("");
                    }
                }
                default -> txtCode.replaceSelection(key);
                }
                txtCode.requestFocusInWindow();
            });
            pnlKeyboard.add(btnKey);
        }
        return pnlKeyboard;
    }

    // ===== PHƯƠNG THỨC: lamMoi() — Tải lại dữ liệu và cập nhật nội dung đang hiển thị. =====
    private void lamMoi() {
        txtCode.setText("");
        category = "Tất cả";
        if (cmbCustomers.getItemCount() > 0)
            cmbCustomers.setSelectedIndex(0);
        tblCart.clearSelection();
        if (tblCart.getRowSorter() != null)
            tblCart.getRowSorter().setSortKeys(null);
        spnQuantity.setValue(1);
        load();
    }

    // ===== PHƯƠNG THỨC: load() — Tải dữ liệu cho bảng hiện tại. =====
    private void load() {
        String oldCustomer = maDangChon(cmbCustomers);
        loading = true;
        btnConfirm.setEnabled(false);
        try {
            Map<String, Product> fresh = new LinkedHashMap<>();
            try (Connection conn = KetNoiCSDL.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT p.maPhuTung,p.tenPhuTung,p.loaiPhuTung,p.donGia,COALESCE(t.soLuongHienTai,0) AS ton FROM dbo.PhuTung p LEFT JOIN dbo.TonKho t ON t.maPhuTung=p.maPhuTung ORDER BY p.maPhuTung");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getBigDecimal(4), rs.getInt(5));
                    fresh.put(p.code(), p);
                }
            }
            products.clear();
            products.putAll(fresh);
            napKhachHang(cmbCustomers);
            cmbCustomers.insertItemAt(new MucKhachHang("", "— Chọn khách hàng —"), 0);
            cmbCustomers.setSelectedIndex(0);
            if (oldCustomer != null)
                for (int i = 0; i < cmbCustomers.getItemCount(); i++)
                    if (oldCustomer.equals(cmbCustomers.getItemAt(i).id()))
                        cmbCustomers.setSelectedIndex(i);
            buildCategories();
        } catch (SQLException ex) {
            products.clear();
            cmbCustomers.removeAllItems();
            error(ex);
        } finally {
            loading = false;
        }
        loadCustomerForm();
        loadInvoiceContext();
        refreshCart();
    }

    // ===== PHƯƠNG THỨC: loadCustomerForm() — Tải khách hàng biểu mẫu. =====
    private void loadCustomerForm() {
        String customer = maDangChon(cmbCustomers);
        updatingCustomerFields = true;
        try {
            boolean isExisting = customer != null && !customer.isBlank();
            for (JComponent input : List.of(txtCustomerName, txtCustomerPhone, cmbCustomerGender,
                    txtCustomerEmail, txtCustomerAddress))
                input.setEnabled(!isExisting);
            if (!isExisting) {
                txtCustomerName.setText("");
                txtCustomerPhone.setText("");
                cmbCustomerGender.setSelectedItem("Nam");
                txtCustomerEmail.setText("");
                txtCustomerAddress.setText("");
                lblStatus.setText("Nhập thông tin khách hàng mới hoặc chọn khách hàng có sẵn.");
                return;
            }
            try (Connection conn = KetNoiCSDL.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT hoTenKH,soDienThoai,gioiTinh,email,diaChi FROM dbo.KhachHang WHERE maKhachHang=?")) {
                ps.setString(1, customer);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new SQLException(
                                "Không tìm thấy khách hàng đã chọn. Vui lòng tải lại danh sách.");
                    txtCustomerName.setText(rs.getString("hoTenKH"));
                    txtCustomerPhone.setText(rs.getString("soDienThoai"));
                    cmbCustomerGender.setSelectedItem(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
                    txtCustomerEmail.setText(rs.getString("email"));
                    txtCustomerAddress.setText(Objects.toString(rs.getString("diaChi"), ""));
                }
            }
        } catch (SQLException ex) {
            lblStatus.setText(ex.getMessage());
        } finally {
            updatingCustomerFields = false;
        }
        refreshCart();
    }

    // ===== PHƯƠNG THỨC: newCustomerFormValid() — Kiểm tra dữ liệu biểu mẫu khách hàng mới. =====
    private boolean newCustomerFormValid() {
        String name = txtCustomerName.getText().trim(), phone = txtCustomerPhone.getText().trim();
        String email = txtCustomerEmail.getText().trim(), address = txtCustomerAddress.getText().trim();
        return !name.isBlank() && name.length() <= 100 && phone.matches("0[35789][0-9]{8}")
                && email.length() <= 254 && email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                && address.length() <= 255;
    }

    // ===== PHƯƠNG THỨC: newCustomerValues() — Thu thập dữ liệu khách hàng mới từ biểu mẫu. =====
    private Map<String, Object> newCustomerValues() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("hoTenKH", txtCustomerName.getText().trim());
        customer.put("soDienThoai", txtCustomerPhone.getText().trim());
        customer.put("gioiTinh", "Nam".equals(cmbCustomerGender.getSelectedItem()));
        customer.put("email", txtCustomerEmail.getText().trim());
        String address = txtCustomerAddress.getText().trim();
        if (!address.isBlank())
            customer.put("diaChi", address);
        return customer;
    }

    // ===== PHƯƠNG THỨC: loadInvoiceContext() — Tải hóa đơn context. =====
    private void loadInvoiceContext() {
        blockedParts = new HashSet<>();
        lblStatus.setText("Chọn khách hàng, xác nhận bán rồi thanh toán hóa đơn.");
        renderProducts();
        refreshCart();
    }

    // ===== PHƯƠNG THỨC: buildCategories() — Tạo danh sách danh mục hàng hóa. =====
    private void buildCategories() {
        pnlCategories.removeAll();
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Tất cả");
        for (Product p : products.values())
            categories.add(p.category());
        if (!categories.contains(category))
            category = "Tất cả";
        ButtonGroup group = new ButtonGroup();
        for (String name : categories) {
            JToggleButton tglCategory = new JToggleButton(name);
            group.add(tglCategory);
            tglCategory.setSelected(name.equals(category));
            tglCategory.setFont(GiaoDien.font(12, true));
            tglCategory.setToolTipText(name);
            tglCategory.addActionListener(e -> {
                category = name;
                renderProducts();
            });
            pnlCategories.add(tglCategory);
        }
        pnlCategories.revalidate();
        pnlCategories.repaint();
    }

    // ===== PHƯƠNG THỨC: renderProducts() — Hiển thị products. =====
    private void renderProducts() {
        pnlProducts.removeAll();
        int count = 0;
        for (Product p : products.values()) {
            if (!category.equals("Tất cả") && !category.equals(p.category()))
                continue;
            count++;
            JPanel pnlProduct = new JPanel(new BorderLayout(10, 6));
            pnlProduct.setBackground(GiaoDien.WHITE);
            pnlProduct.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GiaoDien.LINE), new EmptyBorder(10, 12, 10, 12)));
            JPanel pnlText = new JPanel(new GridLayout(0, 1, 0, 4));
            pnlText.setOpaque(false);
            JLabel lblProduct = GiaoDien.label(p.code() + " • " + p.name(), 12, true, GiaoDien.NAV);
            lblProduct.setToolTipText(p.code() + " • " + p.name());
            pnlText.add(lblProduct);
            pnlText.add(
                    GiaoDien.label(format(p.price()) + "  |  Tồn: " + p.stock(), 13, true, GiaoDien.TEAL));
            boolean blocked = blockedParts.contains(p.code());
            if (blocked)
                pnlText.add(GiaoDien.label("Đã có trong hóa đơn/phiếu sửa", 11, false, GiaoDien.ORANGE));
            else if (p.stock() == 0)
                pnlText.add(GiaoDien.label("Hết hàng", 11, true, GiaoDien.ORANGE));
            pnlProduct.add(pnlText, BorderLayout.CENTER);
            JButton btnAdd = GiaoDien.button("+", GiaoDien.TEAL, Color.WHITE);
            btnAdd.setToolTipText("Thêm " + p.code() + " vào giỏ");
            btnAdd.setIcon(null);
            btnAdd.setDisabledIcon(null);
            btnAdd.setPressedIcon(null);
            btnAdd.setRolloverIcon(null);
            btnAdd.setBorder(new EmptyBorder(8, 14, 8, 14));
            btnAdd.setFont(GiaoDien.font(14, true));
            btnAdd.setEnabled(p.stock() > 0 && !blocked);
            btnAdd.addActionListener(e -> addProduct(p.code()));
            pnlProduct.add(btnAdd, BorderLayout.EAST);
            pnlProduct.setMaximumSize(new Dimension(Integer.MAX_VALUE, blocked || p.stock() == 0 ? 106 : 86));
            pnlProduct.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlProducts.add(pnlProduct);
            pnlProducts.add(Box.createVerticalStrut(8));
        }
        if (count == 0)
            pnlProducts.add(GiaoDien.label("Không có phụ tùng trong nhóm này.", 13, false, GiaoDien.MUTED));
        pnlProducts.revalidate();
        pnlProducts.repaint();
    }

    // ===== PHƯƠNG THỨC: addCode() — Thêm mã. =====
    private void addCode() {
        if (addProduct(txtCode.getText().trim().toUpperCase(Locale.ROOT)))
            txtCode.setText("");
    }

    // ===== PHƯƠNG THỨC: addProduct(String) — Thêm sản phẩm. =====
    private boolean addProduct(String code) {
        Product p = products.get(code);
        if (p == null) {
            lblStatus.setText("Không tìm thấy mã phụ tùng: " + code);
            return false;
        }
        if (blockedParts.contains(code)) {
            lblStatus.setText(code + " đã có trong hóa đơn hoặc đã cấp cho phiếu sửa.");
            return false;
        }
        int old = quantities.getOrDefault(code, 0);
        if (old >= p.stock()) {
            lblStatus.setText("Không đủ tồn kho cho " + code + ".");
            return false;
        }
        quantities.put(code, old + 1);
        cartPrices.putIfAbsent(code, p.price());
        refreshCart();
        lblStatus.setText("Đã thêm " + code + " vào giỏ.");
        return true;
    }

    // ===== PHƯƠNG THỨC: selectedRow() — Chọn bản ghi. =====
    private int selectedRow() {
        int row = tblCart.getSelectedRow();
        return row < 0 ? -1 : tblCart.convertRowIndexToModel(row);
    }

    // ===== PHƯƠNG THỨC: updateQuantity() — Cập nhật quantity. =====
    private void updateQuantity() {
        int row = selectedRow();
        if (row < 0) {
            lblStatus.setText("Chọn dòng cần sửa số lượng.");
            return;
        }
        try {
            spnQuantity.commitEdit();
        } catch (java.text.ParseException ex) {
            lblStatus.setText("Số lượng phải là số nguyên dương.");
            return;
        }
        String code = cartModel.getValueAt(row, 0).toString();
        Product p = products.get(code);
        int quantity = ((Number) spnQuantity.getValue()).intValue();
        if (p == null || quantity > p.stock()) {
            lblStatus.setText("Không đủ tồn kho cho " + code + ".");
            return;
        }
        quantities.put(code, quantity);
        refreshCart();
    }

    // ===== PHƯƠNG THỨC: removeSelected() — Gỡ selected. =====
    private void removeSelected() {
        int row = selectedRow();
        if (row >= 0) {
            String code = cartModel.getValueAt(row, 0).toString();
            quantities.remove(code);
            cartPrices.remove(code);
            refreshCart();
        }
    }

    // ===== PHƯƠNG THỨC: refreshCart() — Làm mới cart. =====
    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean valid = !quantities.isEmpty();
        for (var entry : quantities.entrySet()) {
            String code = entry.getKey();
            Product p = products.get(code);
            BigDecimal price = cartPrices.get(code),
                    amount = price.multiply(BigDecimal.valueOf(entry.getValue()));
            subtotal = subtotal.add(amount);
            boolean stale = p == null || entry.getValue() > p.stock() || price.compareTo(p.price()) != 0
                    || blockedParts.contains(code);
            valid &= !stale;
            cartModel.addRow(new Object[] { code,
                    p == null ? "Không còn trong danh mục" : p.name() + (stale ? " (cần chọn lại)" : ""),
                    entry.getValue(), format(price), format(amount) });
        }
        HoaDon hoaDon = new HoaDon();
        hoaDon.setTienPhuTung(subtotal);
        hoaDon.setTienCong(BigDecimal.ZERO);
        hoaDon.setDichVuKhac(BigDecimal.ZERO);
        BigDecimal vat = hoaDon.tinhThueVAT();
        lblTotal.setText("<html>Tiền hàng: " + format(subtotal) + " &nbsp; | &nbsp; VAT 8%: " + format(vat)
                + "<br>Cộng: " + format(subtotal.add(vat)) + "</html>");
        lblTotal.setToolTipText("Tiền hàng và VAT 8%; mã giảm giá được chọn trong chi tiết hóa đơn.");
        String customer = maDangChon(cmbCustomers);
        boolean hasExistingCustomer = customer != null && !customer.isBlank();
        boolean customerValid = hasExistingCustomer || newCustomerFormValid();
        btnConfirm.setEnabled(!loading && valid && customerValid);
        if (!hasExistingCustomer && !newCustomerFormValid())
            lblStatus.setText("Nhập họ tên, số điện thoại hợp lệ và email để lập hóa đơn.");
        else if (!quantities.isEmpty() && !valid)
            lblStatus.setText("Có dòng đổi giá, thiếu tồn hoặc đã có trong hóa đơn. Bỏ dòng và chọn lại.");
    }

    // ===== PHƯƠNG THỨC: format(BigDecimal) — Định dạng nghiệp vụ tương ứng. =====
    private String format(BigDecimal amount) {
        return money.format(amount) + " đ";
    }

    // ===== PHƯƠNG THỨC: sell() — Ghi nhận bán dịch vụ hoặc phụ tùng vào hóa đơn. =====
    private void sell() {
        if (!btnConfirm.isEnabled())
            return;
        String customer = maDangChon(cmbCustomers);
        String customerName = customer == null || customer.isBlank() ? txtCustomerName.getText().trim()
                : String.valueOf(cmbCustomers.getSelectedItem());
        if (JOptionPane.showConfirmDialog(this,
                "Lập hóa đơn bán " + quantities.size() + " loại phụ tùng cho " + customerName + "?",
                "Xác nhận bán phụ tùng", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        List<ThuongMaiDAO.SaleItem> items = quantities.entrySet().stream()
                .map(e -> new ThuongMaiDAO.SaleItem(e.getKey(), e.getValue(), cartPrices.get(e.getKey())))
                .toList();
        btnConfirm.setEnabled(false);
        try {
            String invoice = customer == null || customer.isBlank()
                    ? dao.HoaDonDAO.createPartsInvoice(newCustomerValues(), employeeId, items)
                    : dao.HoaDonDAO.createPartsInvoice(customer, employeeId, items);
            quantities.clear();
            cartPrices.clear();
            load();
            lblStatus.setText("Đã lập " + invoice + "; tiếp tục thanh toán trong chi tiết hóa đơn.");
            showSaleInvoice(invoice);
        } catch (SQLException ex) {
            error(ex);
            load();
        }
    }

    // ===== PHƯƠNG THỨC: showSaleInvoice(String) — Hiển thị bán hàng hóa đơn. =====
    private void showSaleInvoice(String invoice) {
        JDialog winInvoice = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Hóa đơn bán phụ tùng " + invoice, Dialog.ModalityType.APPLICATION_MODAL);
        ManHinhHoaDon pnlInvoice = new ManHinhHoaDon(invoice);
        JScrollPane scrInvoice = new JScrollPane(pnlInvoice);
        scrInvoice.getVerticalScrollBar().setUnitIncrement(20);
        winInvoice.setContentPane(scrInvoice);
        winInvoice.setSize(1060, 760);
        winInvoice.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(pnlInvoice::openPayment);
        winInvoice.setVisible(true);
    }

    // ===== PHƯƠNG THỨC: error(SQLException) — Hiển thị lỗi cho người dùng. =====
    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể xử lý bán phụ tùng",
                JOptionPane.ERROR_MESSAGE);
    }

    private static final class BoCucBang {

        // ===== PHƯƠNG THỨC: install(JTable, JScrollPane) — Đăng ký bộ điều chỉnh độ rộng cột cho bảng. =====
        public static void install(JTable tblTable, JScrollPane scrScroll) {
            if (Boolean.TRUE.equals(tblTable.getClientProperty("tableLayout.installed")))
                return;
            tblTable.putClientProperty("tableLayout.installed", Boolean.TRUE);
            scrScroll.getViewport().addComponentListener(new ComponentAdapter() {
                // ===== PHƯƠNG THỨC: componentResized(ComponentEvent) — Cập nhật bố cục khi kích thước thành phần thay đổi. =====
                @Override
                public void componentResized(ComponentEvent event) {
                    SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
                }
            });
            tblTable.getModel()
                    .addTableModelListener(e -> SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll)));
            SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
        }

        // ===== PHƯƠNG THỨC: fit(JTable, JScrollPane) — Điều chỉnh độ rộng cột theo vùng hiển thị của bảng. =====
        public static void fit(JTable tblTable, JScrollPane scrScroll) {
            int columns = tblTable.getColumnCount();
            int available = scrScroll.getViewport().getWidth();
            if (columns == 0 || available <= 0)
                return;
            int[] widths = new int[columns];
            Object fixedProperty = tblTable.getClientProperty("tableLayout.fixedWidths");
            java.util.Map<?, ?> fixed = fixedProperty instanceof java.util.Map<?, ?> map ? map
                    : java.util.Map.of();
            int total = 0;
            for (int col = 0; col < columns; col++) {
                TableColumn column = tblTable.getColumnModel().getColumn(col);
                int width = Math.max(90, tblTable.getFontMetrics(tblTable.getFont())
                        .stringWidth(String.valueOf(column.getHeaderValue())) + 28);
                int limit = Math.min(tblTable.getRowCount(), 120);
                for (int row = 0; row < limit; row++) {
                    Component rendered = tblTable.prepareRenderer(tblTable.getCellRenderer(row, col), row,
                            col);
                    width = Math.max(width, rendered.getPreferredSize().width + 8);
                }
                widths[col] = Math.min(width,
                        DinhDangBang.contentAlignment(tblTable, col) == javax.swing.SwingConstants.LEFT ? 320
                                : 210);
                if (fixed.get(column.getModelIndex()) instanceof Number fixedWidth)
                    widths[col] = fixedWidth.intValue();
                total += widths[col];
            }
            if (total < available) {
                int spare = available - total;
                int flexible = 0, last = -1;
                for (int i = 0; i < columns; i++)
                    if (!fixed.containsKey(tblTable.convertColumnIndexToModel(i))) {
                        flexible++;
                        last = i;
                    }
                if (flexible > 0) {
                    int each = spare / flexible;
                    for (int i = 0; i < columns; i++)
                        if (!fixed.containsKey(tblTable.convertColumnIndexToModel(i)))
                            widths[i] += each;
                    widths[last] += spare - each * flexible;
                }
            }
            tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < columns; i++) {
                tblTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
                tblTable.getColumnModel().getColumn(i).setWidth(widths[i]);
            }
            tblTable.revalidate();
        }
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color ORANGE = new Color(0xCB6330);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: rounded(Color, int) — Tạo đường viền bo góc cho thành phần. =====
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(c);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                    q.dispose();
                }
            };
        }

        // ===== PHƯƠNG THỨC: button(String, Color, Color) — Tạo nút giao diện theo kiểu chỉ định. =====
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(getModel().isRollover() ? bg.brighter() : bg);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                    q.dispose();
                    super.paintComponent(g);
                }
            };
            btnB.setForeground(fg);
            BieuTuong.apply(btnB, s, fg);
            btnB.setContentAreaFilled(false);
            btnB.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
            btnB.setFocusPainted(false);
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btnB;
        }

        // ===== PHƯƠNG THỨC: label(String, int, boolean, Color) — Tạo nhãn giao diện. =====
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        // ===== PHƯƠNG THỨC: field(String) — Tạo thành phần nhập liệu cho một trường. =====
        public static JTextField field(String hint) {
            JTextField txtT = new JTextField();
            txtT.putClientProperty("JTextField.placeholderText", hint);
            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                    new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
            return txtT;
        }

        // ===== PHƯƠNG THỨC: table(DefaultTableModel) — Lấy bảng dữ liệu tương ứng với màn hình. =====
        public static JTable table(DefaultTableModel m) {
            JTable tblT = new JTable(m) {
                private final TableCellRenderer cells = new DinhDangBang();

                // ===== PHƯƠNG THỨC: getCellRenderer(int, int) — Lấy ô renderer. =====
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    return cells;
                }

                // ===== PHƯƠNG THỨC: createDefaultColumnsFromModel() — Tạo các cột mặc định theo mô hình bảng. =====
                @Override
                public void createDefaultColumnsFromModel() {
                    super.createDefaultColumnsFromModel();
                    for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                        TableColumn column = getColumnModel().getColumn(i);
                        column.setHeaderValue(nhanCot(getModel().getColumnName(column.getModelIndex())));
                    }
                }
            };
            tblT.setRowHeight(40);
            tblT.setShowGrid(false);
            tblT.setIntercellSpacing(new Dimension(0, 0));
            tblT.setSelectionBackground(new Color(0xE3F1EF));
            tblT.setSelectionForeground(TEXT);
            tblT.setForeground(TEXT);
            tblT.setFont(font(12, false));
            tblT.setFillsViewportHeight(true);
            tblT.getTableHeader().setBackground(new Color(0xEDF3F1));
            tblT.getTableHeader().setForeground(TEAL);
            tblT.getTableHeader().setPreferredSize(new Dimension(0, 43));
            tblT.getTableHeader().setReorderingAllowed(false);
            DinhDangBang.header(tblT);
            tblT.setDefaultEditor(Object.class, null);
            return tblT;
        }

        // ===== PHƯƠNG THỨC: card(LayoutManager) — Tạo khung giao diện dạng thẻ. =====
        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }
    }

    private static final class DinhDangBang extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private final NumberFormat numbers = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        // ===== KHỞI TẠO ĐỐI TƯỢNG: DinhDangBang() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
        DinhDangBang() {
            numbers.setMaximumFractionDigits(2);
            putClientProperty("html.disable", Boolean.TRUE);
        }

        // ===== PHƯƠNG THỨC: name(JTable, int) — Lấy tên hiển thị của đối tượng. =====
        static String name(JTable table, int column) {
            String name = table.getModel().getColumnName(table.convertColumnIndexToModel(column));
            return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd')
                    .replace('Đ', 'D').toLowerCase(Locale.ROOT);
        }

        // ===== PHƯƠNG THỨC: alignment(JTable, int) — Căn chỉnh nội dung trong cột bảng. =====
        static int alignment(JTable table, int column) {
            return SwingConstants.CENTER;
        }

        // ===== PHƯƠNG THỨC: isMoneyColumn(JTable, int) — Kiểm tra money cột. =====
        static boolean isMoneyColumn(JTable table, int column) {
            String name = name(table, column);
            if (name.startsWith("ma "))
                return false;
            return name.startsWith("tong ") || name.startsWith("don gia") || name.equals("thanh tien")
                    || name.contains("chi phi") || name.equals("muc giam") || name.equals("tien phu tung")
                    || name.equals("tien cong") || name.equals("dich vu khac") || name.contains("thue")
                    || name.equals("giam gia");
        }

        // ===== PHƯƠNG THỨC: contentAlignment(JTable, int) — Căn chỉnh nội dung hiển thị trong ô bảng. =====
        static int contentAlignment(JTable table, int column) {
            String name = name(table, column);
            if (name.startsWith("ma ") || name.equals("ma") || name.equals("stt") || name.contains("ngay")
                    || name.contains("thoi gian") || name.contains("gioi tinh") || name.contains("trang thai")
                    || name.contains("ket qua kiem tra") || name.contains("nghiem thu")
                    || name.contains("bien so") || name.contains("vin") || name.contains("nam san xuat")
                    || name.contains("odo"))
                return SwingConstants.CENTER;
            if (isMoneyColumn(table, column) || name.startsWith("tong ") || name.contains("tien")
                    || name.contains("gia") || name.contains("so luong") || name.equals("sl")
                    || name.contains("diem") || name.contains("thue") || name.contains("so km")
                    || name.contains("thoi han"))
                return SwingConstants.RIGHT;
            return SwingConstants.LEFT;
        }

        // ===== PHƯƠNG THỨC: getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int) — Lấy bảng ô renderer thành phần. =====
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            String text;
            if (value == null)
                text = "—";
            else if (value instanceof java.sql.Date date)
                text = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            else if (value instanceof java.sql.Timestamp date)
                text = date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            else if (value instanceof java.util.Date date)
                text = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
            else if (value instanceof Boolean flag)
                text = name(table, column).contains("gioi tinh") ? (flag ? "Nam" : "Nữ")
                        : (flag ? "Có" : "Không");
            else if (value instanceof Number number
                    && contentAlignment(table, column) == SwingConstants.RIGHT)
                text = numbers.format(number);
            else
                text = value.toString();
            boolean money = value != null && isMoneyColumn(table, column);
            setText(text);
            setToolTipText(value == null ? null : text);
            setHorizontalAlignment(money ? SwingConstants.RIGHT : alignment(table, column));
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(table.getFont());
            setForeground(selected ? table.getSelectionForeground() : GiaoDien.TEXT);
            setBackground(selected ? table.getSelectionBackground()
                    : row % 2 == 0 ? GiaoDien.WHITE : new Color(0xF5F8F7));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, GiaoDien.LINE),
                    BorderFactory.createEmptyBorder(5, 10, 5, money ? 16 : 10)));
            return this;
        }

        // ===== PHƯƠNG THỨC: header(JTable) — Thiết lập tiêu đề và cách hiển thị cột bảng. =====
        static void header(JTable table) {
            JTableHeader header = table.getTableHeader();
            TableCellRenderer original = header.getDefaultRenderer();
            header.setDefaultRenderer((tbl, value, selected, focus, row, column) -> {
                Component component = original.getTableCellRendererComponent(tbl, value, selected, focus, row,
                        column);
                if (component instanceof JLabel label) {
                    label.setHorizontalAlignment(alignment(tbl, column));
                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setFont(GiaoDien.font(12, true));
                    label.setForeground(GiaoDien.TEAL);
                    label.setBackground(new Color(0xEDF3F1));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 1, GiaoDien.LINE),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                    label.setToolTipText(String.valueOf(value));
                }
                return component;
            });
        }
    }

    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "khachHangHienThi" -> "Khách hàng";
        case "tongThanhToan" -> "Tổng thanh toán";
        case "tongDiemTichLuy" -> "Tổng điểm tích lũy";
        case "gioiTinh" -> "Giới tính";
        case "soKM" -> "Số km";
        case "ketQuaChuanDoan" -> "Kết quả chẩn đoán";
        case "maNhanVien" -> "Mã nhân viên";
        case "maNV" -> "Mã nhân viên";
        case "hoTenNV" -> "Họ tên nhân viên";
        case "ngaySinh" -> "Ngày sinh";
        case "soDienThoai" -> "Số điện thoại";
        case "email" -> "Email";
        case "diaChi" -> "Địa chỉ";
        case "vaiTro" -> "Vai trò";
        case "trangThai" -> "Trạng thái";
        case "maKhachHang" -> "Mã khách hàng";
        case "hoTenKH" -> "Họ tên khách hàng";
        case "bienSoXe" -> "Biển số xe";
        case "maNguoiDuyetHuy" -> "Nhân viên duyệt hủy";
        case "lyDoHuy" -> "Lý do hủy";
        case "VIN" -> "Số khung (VIN)";
        case "bienSo" -> "Biển số";
        case "hangXe" -> "Hãng xe";
        case "dongXe" -> "Dòng xe";
        case "mauXe" -> "Màu xe";
        case "namSanXuat" -> "Năm sản xuất";
        case "soMay" -> "Số máy";
        case "soKm" -> "Số km";
        case "maDichVu" -> "Mã dịch vụ";
        case "tenDichVu" -> "Tên dịch vụ";
        case "moTa" -> "Mô tả";
        case "donGia" -> "Đơn giá";
        case "maPhuTung" -> "Mã phụ tùng";
        case "tenPhuTung" -> "Tên phụ tùng";
        case "loaiPhuTung" -> "Loại phụ tùng";
        case "maNCC" -> "Mã nhà cung cấp";
        case "tenNCC" -> "Tên nhà cung cấp";
        case "maPhuongThuc" -> "Mã phương thức thanh toán";
        case "tenPhuongThuc" -> "Tên phương thức thanh toán";
        case "maGiamGia" -> "Mã giảm giá";
        case "mucGiam" -> "Mức giảm";
        case "ngayTao" -> "Ngày tạo";
        case "ngayHetHan" -> "Ngày hết hạn";
        case "maTaiKhoan" -> "Mã tài khoản";
        case "tenDangNhap" -> "Tên đăng nhập";
        case "matKhau" -> "Mật khẩu";
        case "maTonKho" -> "Mã tồn kho";
        case "soLuongHienTai" -> "Số lượng hiện tại";
        case "soLuongToiThieu" -> "Số lượng tối thiểu";
        case "maPTN" -> "Mã phiếu tiếp nhận";
        case "ngayTiepNhan" -> "Ngày tiếp nhận";
        case "soODO" -> "Số ODO";
        case "maPKT" -> "Mã phiếu kiểm tra";
        case "ngayKiemTra" -> "Ngày kiểm tra";
        case "maBaoGia" -> "Mã báo giá";
        case "ngayLap" -> "Ngày lập";
        case "thoiGianXacNhan" -> "Thời gian xác nhận";
        case "maPSC" -> "Mã phiếu sửa chữa";
        case "ngayHoanThanh" -> "Ngày hoàn thành";
        case "maYCNK" -> "Mã yêu cầu nhập kho";
        case "maNhanVienDuyet" -> "Nhân viên duyệt";
        case "ngayDuyet" -> "Ngày duyệt";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "donGiaDuKien" -> "Đơn giá dự kiến";
        case "soLuongThucNhan" -> "Số lượng thực nhận";
        case "maPNK" -> "Mã phiếu nhập kho";
        case "ngayNhap" -> "Ngày nhập";
        case "yeuCauDichVu" -> "Yêu cầu dịch vụ";
        case "hangMucKiemTra" -> "Hạng mục kiểm tra";
        case "ketQuaChanDoan" -> "Kết quả chẩn đoán";
        case "ketQuaKiemTra" -> "Kết quả kiểm tra";
        case "soLuongPhuTung" -> "Số lượng phụ tùng";
        case "donGiaPhuTung" -> "Đơn giá phụ tùng";
        case "soLuongCapPhat" -> "Số lượng cấp phát";
        case "soLuongSuDung" -> "Số lượng sử dụng";
        case "soLuongHoanTra" -> "Số lượng hoàn trả";
        case "ngayCapPhat" -> "Ngày cấp phát";
        case "maNhanVienCapPhat" -> "Nhân viên cấp phát";
        case "ngayHoanTra" -> "Ngày hoàn trả";
        case "maNhanVienXacNhanHoanTra" -> "Nhân viên xác nhận hoàn trả";
        case "chiPhiNhanCong" -> "Chi phí nhân công";
        case "trangThaiHangMuc" -> "Trạng thái hạng mục";
        case "ketQuaNghiemThu" -> "Kết quả nghiệm thu";
        case "ngayBatDauBaoHanh" -> "Ngày bắt đầu bảo hành";
        case "thoiHanBaoHanh" -> "Thời hạn bảo hành";
        case "ngayHetHanBaoHanh" -> "Ngày hết hạn bảo hành";
        case "soLuongNhap" -> "Số lượng nhập";
        case "donGiaNhap" -> "Đơn giá nhập";
        case "maHoaDon" -> "Mã hóa đơn";
        case "tienPhuTung" -> "Tiền phụ tùng";
        case "tienCong" -> "Tiền công";
        case "dichVuKhac" -> "Dịch vụ khác";
        case "thueVAT" -> "Thuế VAT";
        case "giamGia" -> "Giảm giá";
        case "trangThaiThanhToan" -> "Trạng thái thanh toán";
        case "soLuong" -> "Số lượng";
        case "maLBH" -> "Mã lịch bảo hành";
        case "ngayXuLy" -> "Ngày xử lý";
        case "mocDiem" -> "Mốc điểm";
        case "ngayBatDauChuKy" -> "Ngày đầu chu kỳ";
        case "ketQuaXuLy" -> "Kết quả xử lý";
        case "ghiChu" -> "Ghi chú";
        case "maLichNhac" -> "Mã lịch nhắc";
        case "ngayNhac" -> "Ngày nhắc";
        case "noiDung" -> "Nội dung";
        case "maLichSuTD" -> "Mã lịch sử tích điểm";
        case "ngayTichDiem" -> "Ngày tích điểm";
        case "soDiem" -> "Số điểm";
        case "lyDo" -> "Lý do";
        default -> field;
        };
    }

    private record MucKhachHang(String id, String label) implements java.io.Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return label;
    }

    }

    // ===== PHƯƠNG THỨC: napKhachHang(JComboBox<MucKhachHang>) — Tải thông tin khách hàng vào biểu mẫu. =====
    private static void napKhachHang(JComboBox<MucKhachHang> combo) throws SQLException {
        combo.removeAllItems();
        for (var entry : new dao.KhachHangDAO().layKhachHangChoManHinhBanPhuTung().entrySet()) {
            combo.addItem(new MucKhachHang(entry.getKey(), entry.getValue()));
        }
    }

    // ===== PHƯƠNG THỨC: maDangChon(JComboBox<MucKhachHang>) — Lấy mã của mục đang được chọn. =====
    private static String maDangChon(JComboBox<MucKhachHang> combo) {
        MucKhachHang selected = (MucKhachHang) combo.getSelectedItem();
        return selected == null ? null : selected.id();
    }
}
