package gui;

import javax.swing.plaf.basic.BasicScrollBarUI;

import java.awt.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;

// ===== GUI: Ô CHỌN CÓ TÌM KIẾM =====

class OChonTimKiem<T> extends JComboBox<T> {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // ===== Tạo ô chọn danh mục từ dữ liệu đang có trong cơ sở dữ liệu =====
    static JComboBox<String> chonDanhMuc(String table, String field, String value) {
            if (!Set.of("PhuongTien", "PhuTung", "MaGiamGia").contains(table)
                    || !Set.of("hangXe", "dongXe", "mauXe", "namSanXuat", "loaiPhuTung", "mucGiam", "mocDiem").contains(field))
                return null;
            java.util.LinkedHashSet<String> choices = new java.util.LinkedHashSet<>();
            choices.add("");
            if (field.equals("namSanXuat")) {
                for (int year = java.time.Year.now().getValue(); year >= 1886; year--) choices.add(String.valueOf(year));
            } else {
                try (java.sql.Connection conn = connectDB.KetNoiCSDL.getConnection();
                        java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT [" + field + "] FROM dbo.[" + table + "] WHERE [" + field + "] IS NOT NULL ORDER BY [" + field + "]");
                        java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) choices.add(rs.getString(1).trim());
                } catch (java.sql.SQLException ex) { throw new IllegalStateException("Không tải được danh mục " + field, ex); }
            }
            if (value != null && !value.isBlank()) choices.add(value);
            String placeholder = switch (field) {
                case "hangXe" -> "— Chọn hãng xe —";
                case "dongXe" -> "— Chọn dòng xe —";
                case "mauXe" -> "— Chọn màu xe —";
                default -> "";
            };
            JComboBox<String> combo = new OChonTimKiem<>(choices.toArray(new String[0]), placeholder);
            combo.setSelectedItem(value == null ? "" : value);
            return combo;
        }

        static JButton nutThemGiaTri(JComboBox<String> combo, String tenTruong) {
            JButton button = new JButton("+") {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D shape = (Graphics2D) graphics.create();
                    shape.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    shape.setColor(getModel().isRollover() ? GiaoDien.TEAL.brighter() : GiaoDien.TEAL);
                    shape.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                    shape.dispose();
                    super.paintComponent(graphics);
                }
            };
            button.setFont(GiaoDien.font(20, true));
            button.setForeground(Color.WHITE);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 17));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> {
                String value = JOptionPane.showInputDialog(button, "Nhập " + tenTruong +" mới");
                if (value == null || value.isBlank()) {
                    return;
                }

                value = value.trim();
                for (int i = 0; i < combo.getItemCount(); i++) {
                    String item = combo.getItemAt(i);
                    if (value.equalsIgnoreCase(item)) {
                        combo.setSelectedItem(item);
                        return;
                    }
                }

                combo.addItem(value);
                combo.setSelectedItem(value);
            });
            return button;
        }

    private static final long serialVersionUID = 1L;
    private static final int CELL_HEIGHT = 34;
    private JTextField txtEditor;
    private JPopupMenu popup;
    private JScrollPane scrResults;
    private JList<T> lstResults;
    private DefaultListModel<T> results;
    private boolean changing, queued;
    private long popupClosedAt;

    static class FlatComboBoxUI extends BasicComboBoxUI {
        // ===== PHƯƠNG THỨC: createArrowButton() — Tạo nút mũi tên cho bộ chọn. =====
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                private static final long serialVersionUID = 1L;

                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = comboBox != null ? comboBox.getBackground() : Color.WHITE;
                    g2.setColor(bg != null ? bg : Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    boolean enabled = comboBox != null && comboBox.isEnabled();
                    boolean open = comboBox != null && comboBox.isPopupVisible();
                    boolean active = getModel().isRollover() || open;
                    g2.setColor(!enabled ? GiaoDien.MUTED : (active ? GiaoDien.TEAL : GiaoDien.NAV));
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    if (open) {
                        g2.drawLine(cx - 4, cy + 2, cx, cy - 2);
                        g2.drawLine(cx, cy - 2, cx + 4, cy + 2);
                    } else {
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    }
                    g2.dispose();
                }
            };
            btn.setName("ComboBox.arrowButton");
            btn.setBorder(null);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setFocusable(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setRolloverEnabled(true);
            return btn;
        }

        // ===== PHƯƠNG THỨC: configureArrowButton() — Thiết lập nút mũi tên của bộ chọn. =====
        @Override
        public void configureArrowButton() {
            super.configureArrowButton();
            if (arrowButton != null) {
                arrowButton.setBorder(null);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setOpaque(false);
                arrowButton.setFocusPainted(false);
                arrowButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        // ===== PHƯƠNG THỨC: installDefaults() — Thiết lập giao diện mặc định cho thành phần. =====
        @Override
        protected void installDefaults() {
            super.installDefaults();
            comboBox.setOpaque(true);
            comboBox.setBackground(Color.WHITE);
            comboBox.setForeground(GiaoDien.TEXT);
            comboBox.setBorder(new LineBorder(GiaoDien.LINE, 1, true));
        }
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: OChonTimKiem() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    OChonTimKiem() {
        super();
        setup();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: OChonTimKiem(T[]) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    OChonTimKiem(T[] values) {
        this(values, "");
    }

    private OChonTimKiem(T[] values, String placeholder) {
        super(values);
        if (!placeholder.isEmpty()) {
            setEditor(new BasicComboBoxEditor() {
                @Override
                protected JTextField createEditorComponent() {
                    return new JTextField() {
                        private final JLabel hintLabel = new JLabel(placeholder);
                        private final CellRendererPane hintRenderer = new CellRendererPane();

                        @Override
                        protected void paintComponent(Graphics graphics) {
                            super.paintComponent(graphics);
                            if (!getText().isEmpty() || isFocusOwner()) {
                                return;
                            }
                            hintLabel.setFont(getFont());
                            hintLabel.setForeground(getForeground());
                            Insets insets = getInsets();
                            hintRenderer.paintComponent(graphics, hintLabel, this,
                                    insets.left, insets.top, getWidth() - insets.left - insets.right,
                                    getHeight() - insets.top - insets.bottom);
                        }
                    };
                }
            });
        }
        setup();
    }

    // ===== PHƯƠNG THỨC: normalize(String) — Chuẩn hóa dữ liệu trước khi so sánh hoặc lưu. =====
    static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd')
                .replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ===== PHƯƠNG THỨC: updateUI() — Cập nhật ui. =====
    @Override
    public void updateUI() {
        setUI(new FlatComboBoxUI());
        setBackground(isEnabled() ? Color.WHITE : DinhDangKeThua.BACKGROUND);
        if (txtEditor != null) {
            configureEditorComponent();
        }
        installArrowButtonHandler();
    }

    // ===== PHƯƠNG THỨC: configureEditorComponent() — Thiết lập thành phần chỉnh sửa ô bảng. =====
    private void configureEditorComponent() {
        if (txtEditor == null)
            return;

        boolean locked = !isEnabled();

        txtEditor.setFont(GiaoDien.font(12, locked));
        txtEditor.setForeground(locked ? DinhDangKeThua.TEXT : GiaoDien.TEXT);
        txtEditor.setDisabledTextColor(DinhDangKeThua.TEXT);
        txtEditor.setCaretColor(GiaoDien.TEXT);
        txtEditor.setBorder(new EmptyBorder(0, 10, 0, 4));
        txtEditor.setBackground(locked ? DinhDangKeThua.BACKGROUND : Color.WHITE);
        txtEditor.setOpaque(true);
    }

    // ===== PHƯƠNG THỨC: installArrowButtonHandler() — Thiết lập xử lý nút mũi tên của bộ chọn. =====
    private void installArrowButtonHandler() {
        for (Component comp : getComponents()) {
            if (comp instanceof AbstractButton btn) {
                for (MouseListener ml : btn.getMouseListeners()) {
                    if (ml.getClass().getName().contains("ComboPopup"))
                        btn.removeMouseListener(ml);
                }
                for (MouseMotionListener mml : btn.getMouseMotionListeners()) {
                    if (mml.getClass().getName().contains("ComboPopup"))
                        btn.removeMouseMotionListener(mml);
                }
                if (!Boolean.TRUE.equals(btn.getClientProperty("oChon.arrowBound"))) {
                    btn.putClientProperty("oChon.arrowBound", Boolean.TRUE);
                    btn.setRolloverEnabled(true);
                    btn.addMouseListener(new MouseAdapter() {
                        // ===== PHƯƠNG THỨC: mouseEntered(MouseEvent) — Mở entered. =====
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            btn.repaint();
                        }

                        // ===== PHƯƠNG THỨC: mouseExited(MouseEvent) — Mở exited. =====
                        @Override
                        public void mouseExited(MouseEvent e) {
                            btn.repaint();
                        }

                        // ===== PHƯƠNG THỨC: mousePressed(MouseEvent) — Xử lý khi người dùng nhấn chuột. =====
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (!SwingUtilities.isLeftMouseButton(e) || !isEnabled())
                                return;
                            e.consume();
                            if (isPopupVisible()) {
                                setPopupVisible(false);
                            } else if (System.currentTimeMillis() - popupClosedAt > 150) {
                                if (txtEditor != null)
                                    txtEditor.requestFocusInWindow();
                                open(false);
                            }
                        }
                    });
                }
            }
        }
    }

    // ===== PHƯƠNG THỨC: setup() — Gán nghiệp vụ tương ứng. =====
    private void setup() {
        setEditable(true);
        txtEditor = (JTextField) getEditor().getEditorComponent();
        txtEditor.setToolTipText("Gõ mã hoặc tên để lọc; bấm kết quả để chọn ngay, hoặc nhấn Enter.");
        configureEditorComponent();
        setBorder(new LineBorder(GiaoDien.LINE, 1, true));
        setBackground(Color.WHITE);

        setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: getListCellRendererComponent(JList<?>, Object, int, boolean, boolean) — Lấy list ô renderer thành phần. =====
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        false);
                Font base = OChonTimKiem.this.getFont() != null ? OChonTimKiem.this.getFont()
                        : GiaoDien.font(12, false);
                lbl.setFont(GiaoDien.font(base.getSize(), isSelected));
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setBackground(isSelected ? GiaoDien.TEAL : GiaoDien.WHITE);
                lbl.setForeground(isSelected ? Color.WHITE : GiaoDien.TEXT);
                return lbl;
            }
        });

        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            // ===== PHƯƠNG THỨC: update() — Cập nhật bản ghi trong cơ sở dữ liệu. =====
            private void update() {
                if (changing || queued)
                    return;
                queued = true;
                SwingUtilities.invokeLater(() -> {
                    queued = false;
                    if (changing || !isEnabled() || !txtEditor.isFocusOwner())
                        return;
                    Object selected = OChonTimKiem.super.getSelectedItem();
                    if (selected != null && txtEditor.getText().equals(selected.toString()))
                        return;
                    fireActionEvent();
                    open(true);
                });
            }

            // ===== PHƯƠNG THỨC: insertUpdate(DocumentEvent) — Xử lý khi nội dung được thêm vào. =====
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            // ===== PHƯƠNG THỨC: removeUpdate(DocumentEvent) — Xử lý khi nội dung bị xóa. =====
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            // ===== PHƯƠNG THỨC: changedUpdate(DocumentEvent) — Cập nhật giao diện khi nội dung thay đổi. =====
            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });

        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pickResult");
        txtEditor.getActionMap().put("pickResult", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: actionPerformed(ActionEvent) — Xử lý thao tác của người dùng. =====
            @Override
            public void actionPerformed(ActionEvent e) {
                choose();
            }
        });
        for (int key : new int[] { KeyEvent.VK_DOWN, KeyEvent.VK_UP }) {
            String action = "result" + key;
            txtEditor.getInputMap().put(KeyStroke.getKeyStroke(key, 0), action);
            txtEditor.getActionMap().put(action, new AbstractAction() {
                private static final long serialVersionUID = 1L;

                // ===== PHƯƠNG THỨC: actionPerformed(ActionEvent) — Xử lý thao tác của người dùng. =====
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isPopupVisible()) {
                        open(true);
                        return;
                    }
                    if (results != null && !results.isEmpty()) {
                        int next = Math.max(0, Math.min(results.size() - 1,
                                lstResults.getSelectedIndex() + (key == KeyEvent.VK_DOWN ? 1 : -1)));
                        lstResults.setSelectedIndex(next);
                        lstResults.ensureIndexIsVisible(next);
                    }
                }
            });
        }
        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "cancelSearch");
        txtEditor.getActionMap().put("cancelSearch", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: actionPerformed(ActionEvent) — Xử lý thao tác của người dùng. =====
            @Override
            public void actionPerformed(ActionEvent e) {
                setPopupVisible(false);
                restore();
            }
        });

        txtEditor.addFocusListener(new FocusAdapter() {
            // ===== PHƯƠNG THỨC: focusGained(FocusEvent) — Cập nhật giao diện khi thành phần nhận tiêu điểm. =====
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(new LineBorder(GiaoDien.TEAL, 1, true));
                repaint();
                SwingUtilities.invokeLater(txtEditor::selectAll);
            }

            // ===== PHƯƠNG THỨC: focusLost(FocusEvent) — Kiểm tra và cập nhật dữ liệu khi rời khỏi thành phần. =====
            @Override
            public void focusLost(FocusEvent e) {
                if (!isPopupVisible()) {
                    setBorder(new LineBorder(GiaoDien.LINE, 1, true));
                    repaint();
                }
            }
        });

        installArrowButtonHandler();
    }

    // ===== PHƯƠNG THỨC: setEnabled(boolean) — Gán trạng thái bật. =====
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setBackground(enabled ? Color.WHITE : DinhDangKeThua.BACKGROUND);
        setForeground(enabled ? GiaoDien.TEXT : DinhDangKeThua.TEXT);

        if (txtEditor != null) {
            txtEditor.setEnabled(enabled);
            configureEditorComponent();
        }

        repaint();
    }

    // ===== PHƯƠNG THỨC: getSelectedItem() — Lấy mục đang được chọn. =====
    @Override
    public Object getSelectedItem() {
        Object selected = super.getSelectedItem();
        String t = txtEditor != null ? txtEditor.getText() : null;
        if (!changing && txtEditor != null && isEnabled() && isEditable()
                && (selected == null || t == null || !t.equals(selected.toString())))
            return null;
        return selected;
    }

    // ===== PHƯƠNG THỨC: setSelectedItem(Object) — Gán selected item. =====
    @Override
    public void setSelectedItem(Object value) {
        if (txtEditor != null && value != null) {
            Object matched = null;
            for (int i = 0; i < getItemCount(); i++)
                if (Objects.equals(getItemAt(i), value) || getItemAt(i).toString().equals(value.toString())) {
                    matched = getItemAt(i);
                    break;
                }
            if (matched == null)
                return;
            value = matched;
        }
        boolean previous = changing;
        changing = true;
        try {
            if (txtEditor != null)
                getEditor().setItem(value);
            if (!Objects.equals(super.getSelectedItem(), value))
                super.setSelectedItem(value);
            if (txtEditor != null)
                getEditor().setItem(super.getSelectedItem());
        } finally {
            changing = previous;
        }
    }

    // ===== PHƯƠNG THỨC: setPopupVisible(boolean) — Gán popup trạng thái hiển thị. =====
    @Override
    public void setPopupVisible(boolean visible) {
        if (visible) {
            open(false);
        } else {
            if (popup != null && popup.isVisible()) {
                popupClosedAt = System.currentTimeMillis();
                popup.setVisible(false);
            }
            if (super.isPopupVisible())
                super.setPopupVisible(false);
            setBorder(new LineBorder(
                    txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1, true));
            repaint();
        }
    }

    // ===== PHƯƠNG THỨC: isPopupVisible() — Kiểm tra popup trạng thái hiển thị. =====
    @Override
    public boolean isPopupVisible() {
        return (popup != null && popup.isVisible()) || super.isPopupVisible();
    }

    // ===== PHƯƠNG THỨC: restore() — Khôi phục trạng thái giao diện đã lưu. =====
    private void restore() {
        changing = true;
        try {
            Object selected = super.getSelectedItem();
            txtEditor.setText(selected == null ? "" : selected.toString());
        } finally {
            changing = false;
        }
    }

    // ===== PHƯƠNG THỨC: open(boolean) — Mở nghiệp vụ tương ứng. =====
    private void open(boolean filtering) {
        if (txtEditor == null || !isEnabled() || !isShowing())
            return;
        if (super.isPopupVisible())
            super.setPopupVisible(false);
        setBorder(new LineBorder(GiaoDien.TEAL, 1, true));
        repaint();
        String typed = txtEditor.getText();
        if (!isPopupVisible()) {
            changing = true;
            try {
                firePopupMenuWillBecomeVisible();
                if (filtering)
                    txtEditor.setText(typed);
            } finally {
                changing = false;
            }
            popup = new JPopupMenu();
            popup.setFocusable(false);
            popup.setLayout(new BorderLayout());
            popup.setBackground(GiaoDien.WHITE);
            popup.setBorder(
                    new CompoundBorder(new LineBorder(GiaoDien.LINE, 1, true), new EmptyBorder(3, 0, 3, 0)));
            results = new DefaultListModel<>();
            lstResults = new JList<>(results);
            lstResults.setFocusable(false);
            lstResults.setFixedCellHeight(CELL_HEIGHT);
            lstResults.setFont(getFont());
            lstResults.setBackground(GiaoDien.WHITE);
            lstResults.setSelectionBackground(GiaoDien.TEAL);
            lstResults.setSelectionForeground(Color.WHITE);
            lstResults.setCellRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                // ===== PHƯƠNG THỨC: getListCellRendererComponent(JList<?>, Object, int, boolean, boolean) — Lấy list ô renderer thành phần. =====
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            false);
                    Font base = OChonTimKiem.this.getFont() != null ? OChonTimKiem.this.getFont()
                            : GiaoDien.font(13, false);
                    lbl.setFont(GiaoDien.font(base.getSize(), isSelected));
                    lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                    lbl.setBackground(isSelected ? GiaoDien.TEAL : GiaoDien.WHITE);
                    lbl.setForeground(isSelected ? Color.WHITE : GiaoDien.TEXT);
                    return lbl;
                }
            });
            lstResults.addMouseListener(new MouseAdapter() {
                // ===== PHƯƠNG THỨC: mouseReleased(MouseEvent) — Xử lý khi người dùng thả chuột. =====
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e))
                        return;
                    int index = lstResults.locationToIndex(e.getPoint());
                    if (index >= 0 && lstResults.getCellBounds(index, index).contains(e.getPoint())) {
                        lstResults.setSelectedIndex(index);
                        choose();
                        e.consume();
                    }
                }
            });
            scrResults = new JScrollPane(lstResults, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrResults.setBorder(null);
            scrResults.getViewport().setBackground(GiaoDien.WHITE);
            scrResults.getVerticalScrollBar().setUnitIncrement(17);
            GiaoDien.styleScroll(scrResults, false);
            popup.add(scrResults);
            popup.addPopupMenuListener(new PopupMenuListener() {
                // ===== PHƯƠNG THỨC: popupMenuWillBecomeVisible(PopupMenuEvent) — Chuẩn bị nội dung trước khi menu ngữ cảnh hiện. =====
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                // ===== PHƯƠNG THỨC: popupMenuWillBecomeInvisible(PopupMenuEvent) — Cập nhật giao diện khi menu ngữ cảnh ẩn. =====
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popupClosedAt = System.currentTimeMillis();
                    setBorder(new LineBorder(
                            txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1,
                            true));
                    repaint();
                    firePopupMenuWillBecomeInvisible();
                }

                // ===== PHƯƠNG THỨC: popupMenuCanceled(PopupMenuEvent) — Khôi phục trạng thái giao diện khi đóng menu ngữ cảnh. =====
                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    popupClosedAt = System.currentTimeMillis();
                    setBorder(new LineBorder(
                            txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1,
                            true));
                    repaint();
                    firePopupMenuCanceled();
                }
            });
        }
        String term = filtering ? normalize(typed.trim()) : "";
        results.clear();
        for (int i = 0; i < getItemCount(); i++) {
            T value = getItemAt(i);
            if (normalize(value.toString()).contains(term))
                results.addElement(value);
        }
        if (scrResults != null) {
            int maxRows = Math.max(8, getMaximumRowCount());
            int visibleRows = Math.max(1, Math.min(results.size(), maxRows));
            int popupHeight = visibleRows * CELL_HEIGHT + 2;
            Font itemFont = getFont() != null ? GiaoDien.font(getFont().getSize(), true)
                    : GiaoDien.font(13, true);
            FontMetrics fm = lstResults.getFontMetrics(itemFont);
            int maxTextWidth = 0;
            for (int i = 0; i < results.size(); i++) {
                T val = results.getElementAt(i);
                if (val != null)
                    maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(val.toString()));
            }
            int popupWidth = Math.max(Math.max(180, getWidth()), Math.min(560, maxTextWidth + 36));
            scrResults.setPreferredSize(new Dimension(popupWidth, popupHeight));
            popup.pack();
        }
        if (!results.isEmpty())
            lstResults.setSelectedIndex(0);
        if (!filtering)
            lstResults.setSelectedValue(super.getSelectedItem(), true);
        if (!isPopupVisible())
            popup.show(this, 0, getHeight() + 2);
    }

    // ===== PHƯƠNG THỨC: choose() — Hiển thị lựa chọn và trả về mục được chọn. =====
    private void choose() {
        if (isPopupVisible() && lstResults != null && lstResults.getSelectedValue() != null) {
            T value = lstResults.getSelectedValue();
            setPopupVisible(false);
            setSelectedItem(value);
            txtEditor.requestFocusInWindow();
            return;
        }
        String text = txtEditor.getText();
        for (int i = 0; i < getItemCount(); i++) {
            T value = getItemAt(i);
            if (value != null && value.toString().equalsIgnoreCase(text)) {
                setPopupVisible(false);
                setSelectedItem(value);
                return;
            }
        }
        open(true);
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color MINT = new Color(0x83B4AE);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: styleScroll(JScrollPane, boolean) — Thiết lập kiểu hiển thị cho thanh cuộn. =====
        public static void styleScroll(JScrollPane scr, boolean dark) {
            if (scr == null)
                return;
            styleScrollBar(scr.getVerticalScrollBar(), dark);
            styleScrollBar(scr.getHorizontalScrollBar(), dark);
            if (scr.getViewport() != null && scr.getViewport().getView() instanceof JTable) {
                scr.setBorder(new LineBorder(LINE, 1, true));
                scr.getViewport().setBackground(WHITE);
            }
        }

        // ===== PHƯƠNG THỨC: styleScrollBar(JScrollBar, boolean) — Thiết lập giao diện cho một thanh cuộn. =====
        public static void styleScrollBar(JScrollBar bar, boolean dark) {
            if (bar == null)
                return;
            Boolean applied = (Boolean) bar.getClientProperty("giaoDien.styled");
            Boolean wasDark = (Boolean) bar.getClientProperty("giaoDien.dark");
            if (Boolean.TRUE.equals(applied) && java.util.Objects.equals(wasDark, dark))
                return;
            bar.putClientProperty("giaoDien.styled", Boolean.TRUE);
            bar.putClientProperty("giaoDien.dark", dark);
            bar.setOpaque(true);
            bar.setBackground(dark ? NAV : WHITE);
            bar.setPreferredSize(new Dimension(dark ? 6 : 8, dark ? 6 : 8));
            bar.setUI(new BasicScrollBarUI() {
                // ===== PHƯƠNG THỨC: configureScrollBarColors() — Thiết lập màu thanh cuộn. =====
                @Override
                protected void configureScrollBarColors() {
                    trackColor = dark ? NAV : new Color(0xF0F4F3);
                    thumbColor = dark ? new Color(0x3D5E63) : new Color(0xB8C9C6);
                }

                // ===== PHƯƠNG THỨC: zeroButton() — Tạo nút xóa nội dung ô nhập liệu. =====
                private JButton zeroButton() {
                    JButton b = new JButton();
                    Dimension d = new Dimension(0, 0);
                    b.setPreferredSize(d);
                    b.setMinimumSize(d);
                    b.setMaximumSize(d);
                    b.setBorder(null);
                    b.setFocusable(false);
                    b.setOpaque(false);
                    return b;
                }

                // ===== PHƯƠNG THỨC: createDecreaseButton(int) — Tạo nút giảm giá trị. =====
                @Override
                protected JButton createDecreaseButton(int orientation) {
                    return zeroButton();
                }

                // ===== PHƯƠNG THỨC: createIncreaseButton(int) — Tạo nút tăng giá trị. =====
                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return zeroButton();
                }

                // ===== PHƯƠNG THỨC: paintTrack(Graphics, JComponent, Rectangle) — Vẽ rãnh của thanh cuộn. =====
                @Override
                protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(dark ? NAV
                            : (c.getParent() instanceof JScrollPane sp && sp.getViewport() != null
                                    && sp.getViewport().getView() instanceof JTable ? WHITE : BG));
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.dispose();
                }

                // ===== PHƯƠNG THỨC: paintThumb(Graphics, JComponent, Rectangle) — Vẽ tay nắm của thanh cuộn. =====
                @Override
                protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                    if (r.isEmpty() || !scrollbar.isEnabled())
                        return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isDragging || isThumbRollover() ? (dark ? MINT : TEAL)
                            : (dark ? new Color(0x3D5E63) : new Color(0xB8C9C6)));
                    int arc = 6;
                    if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
                        int w = Math.max(4, r.width - 2);
                        int x = r.x + (r.width - w) / 2;
                        g2.fillRoundRect(x, r.y + 1, w, Math.max(12, r.height - 2), arc, arc);
                    } else {
                        int h = Math.max(4, r.height - 2);
                        int y = r.y + (r.height - h) / 2;
                        g2.fillRoundRect(r.x + 1, y, Math.max(12, r.width - 2), h, arc, arc);
                    }
                    g2.dispose();
                }
            });
        }
    }
}
