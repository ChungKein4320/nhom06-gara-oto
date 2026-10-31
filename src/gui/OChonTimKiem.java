package gui;

import java.awt.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

class OChonTimKiem<T> extends JComboBox<T> {
    private static final long serialVersionUID = 1L;
    private static final int CELL_HEIGHT = 34;
    private JTextField txtEditor;
    private JPopupMenu popup;
    private JScrollPane scrResults;
    private JList<T> lstResults;
    private DefaultListModel<T> results;
    private boolean changing,queued;
    private long popupClosedAt;

    static class FlatComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                private static final long serialVersionUID = 1L;
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

        @Override
        protected void installDefaults() {
            super.installDefaults();
            comboBox.setOpaque(true);
            comboBox.setBackground(Color.WHITE);
            comboBox.setForeground(GiaoDien.TEXT);
            comboBox.setBorder(new LineBorder(GiaoDien.LINE, 1, true));
        }
    }

    OChonTimKiem() { super(); setup(); }
    OChonTimKiem(T[] values) { super(values); setup(); }
    static String normalize(String text) { return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT); }

    @Override public void updateUI() {
        setUI(new FlatComboBoxUI());
        setBackground(
        isEnabled() ? Color.WHITE : DinhDangKeThua.BACKGROUND);
        if (txtEditor != null) {
            configureEditorComponent();
        }
        installArrowButtonHandler();
    }

    private void configureEditorComponent() {
        if (txtEditor == null) return;

        boolean locked = !isEnabled();

        txtEditor.setFont(GiaoDien.font(12, locked));
        txtEditor.setForeground(
                locked ? DinhDangKeThua.TEXT : GiaoDien.TEXT
        );
        txtEditor.setDisabledTextColor(DinhDangKeThua.TEXT);
        txtEditor.setCaretColor(GiaoDien.TEXT);
        txtEditor.setBorder(new EmptyBorder(0, 10, 0, 4));
        txtEditor.setBackground(
                locked ? DinhDangKeThua.BACKGROUND : Color.WHITE
        );
        txtEditor.setOpaque(true);
    }

    private void installArrowButtonHandler() {
        for(Component comp:getComponents()) {
            if(comp instanceof AbstractButton btn) {
                for(MouseListener ml:btn.getMouseListeners()) {
                    if(ml.getClass().getName().contains("ComboPopup")) btn.removeMouseListener(ml);
                }
                for(MouseMotionListener mml:btn.getMouseMotionListeners()) {
                    if(mml.getClass().getName().contains("ComboPopup")) btn.removeMouseMotionListener(mml);
                }
                if(!Boolean.TRUE.equals(btn.getClientProperty("oChon.arrowBound"))) {
                    btn.putClientProperty("oChon.arrowBound",Boolean.TRUE);
                    btn.setRolloverEnabled(true);
                    btn.addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
                        @Override public void mouseExited(MouseEvent e) { btn.repaint(); }
                        @Override public void mousePressed(MouseEvent e) {
                            if(!SwingUtilities.isLeftMouseButton(e) || !isEnabled()) return;
                            e.consume();
                            if(isPopupVisible()) {
                                setPopupVisible(false);
                            } else if(System.currentTimeMillis()-popupClosedAt>150) {
                                if(txtEditor!=null) txtEditor.requestFocusInWindow();
                                open(false);
                            }
                        }
                    });
                }
            }
        }
    }

    private void setup() {
        setEditable(true);
        txtEditor=(JTextField)getEditor().getEditorComponent();
        txtEditor.setToolTipText("Gõ mã hoặc tên để lọc; bấm kết quả để chọn ngay, hoặc nhấn Enter.");
        configureEditorComponent();
        setBorder(new LineBorder(GiaoDien.LINE, 1, true));
        setBackground(Color.WHITE);

        setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
                Font base = OChonTimKiem.this.getFont() != null ? OChonTimKiem.this.getFont() : GiaoDien.font(12, false);
                lbl.setFont(GiaoDien.font(base.getSize(), isSelected));
                lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                lbl.setBackground(isSelected ? GiaoDien.TEAL : GiaoDien.WHITE);
                lbl.setForeground(isSelected ? Color.WHITE : GiaoDien.TEXT);
                return lbl;
            }
        });

        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if(changing || queued) return; queued=true;
                SwingUtilities.invokeLater(() -> {
                    queued=false; if(changing || !isEnabled() || !txtEditor.isFocusOwner()) return;
                    Object selected=OChonTimKiem.super.getSelectedItem();
                    if(selected!=null && txtEditor.getText().equals(selected.toString())) return;
                    fireActionEvent(); open(true);
                });
            }
            @Override
            public void insertUpdate(DocumentEvent e){update();}
            @Override
            public void removeUpdate(DocumentEvent e){update();}
            @Override
            public void changedUpdate(DocumentEvent e){update();}
        });

        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"pickResult");
        txtEditor.getActionMap().put("pickResult",new AbstractAction(){
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e){choose();}});
        for(int key:new int[]{KeyEvent.VK_DOWN,KeyEvent.VK_UP}) {
            String action="result"+key; txtEditor.getInputMap().put(KeyStroke.getKeyStroke(key,0),action);
            txtEditor.getActionMap().put(action,new AbstractAction(){
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e){
                if(!isPopupVisible()) { open(true); return; }
                if(results!=null && !results.isEmpty()) { int next=Math.max(0,Math.min(results.size()-1,lstResults.getSelectedIndex()+(key==KeyEvent.VK_DOWN ? 1 : -1))); lstResults.setSelectedIndex(next); lstResults.ensureIndexIsVisible(next); }
            }});
        }
        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),"cancelSearch");
        txtEditor.getActionMap().put("cancelSearch",new AbstractAction(){
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e){setPopupVisible(false); restore();}});

        txtEditor.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){
                setBorder(new LineBorder(GiaoDien.TEAL, 1, true));
                repaint();
                SwingUtilities.invokeLater(txtEditor::selectAll);
            }
            @Override public void focusLost(FocusEvent e){
                if(!isPopupVisible()) {
                    setBorder(new LineBorder(GiaoDien.LINE, 1, true));
                    repaint();
                }
            }
        });

        installArrowButtonHandler();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setBackground(
                enabled ? Color.WHITE : DinhDangKeThua.BACKGROUND
        );
        setForeground(
                enabled ? GiaoDien.TEXT : DinhDangKeThua.TEXT
        );

        if (txtEditor != null) {
            txtEditor.setEnabled(enabled);
            configureEditorComponent();
        }

        repaint();
    }

    @Override public Object getSelectedItem() {
        Object selected=super.getSelectedItem();
        String t = txtEditor != null ? txtEditor.getText() : null;
        if(!changing && txtEditor!=null && isEnabled() && isEditable() && (selected==null || t==null || !t.equals(selected.toString()))) return null;
        return selected;
    }

    @Override public void setSelectedItem(Object value) {
        if(txtEditor!=null && value!=null) {
            Object matched=null;
            for(int i=0;i<getItemCount();i++) if(Objects.equals(getItemAt(i),value) || getItemAt(i).toString().equals(value.toString())) {matched=getItemAt(i);break;}
            if(matched==null) return;
            value=matched;
        }
        boolean previous=changing; changing=true;
        try {
            if(txtEditor!=null) getEditor().setItem(value);
            if(!Objects.equals(super.getSelectedItem(),value)) super.setSelectedItem(value);
            if(txtEditor!=null) getEditor().setItem(super.getSelectedItem());
        } finally {changing=previous;}
    }

    @Override public void setPopupVisible(boolean visible) {
        if(visible) {
            open(false);
        } else {
            if(popup!=null && popup.isVisible()) {
                popupClosedAt=System.currentTimeMillis();
                popup.setVisible(false);
            }
            if(super.isPopupVisible()) super.setPopupVisible(false);
            setBorder(new LineBorder(txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1, true));
            repaint();
        }
    }

    @Override public boolean isPopupVisible() { return (popup!=null && popup.isVisible()) || super.isPopupVisible(); }

    private void restore() {
        changing=true;
        try {
            Object selected=super.getSelectedItem();
            txtEditor.setText(selected==null ? "" : selected.toString());
        } finally {changing=false;}
    }

    private void open(boolean filtering) {
        if(txtEditor==null || !isEnabled() || !isShowing()) return;
        if(super.isPopupVisible()) super.setPopupVisible(false);
        setBorder(new LineBorder(GiaoDien.TEAL, 1, true));
        repaint();
        String typed=txtEditor.getText();
        if(!isPopupVisible()) {
            changing=true;
            try {firePopupMenuWillBecomeVisible(); if(filtering) txtEditor.setText(typed);} finally {changing=false;}
            popup=new JPopupMenu(); popup.setFocusable(false); popup.setLayout(new BorderLayout());
            popup.setBackground(GiaoDien.WHITE);
            popup.setBorder(new CompoundBorder(new LineBorder(GiaoDien.LINE, 1, true), new EmptyBorder(3, 0, 3, 0)));
            results=new DefaultListModel<>(); lstResults=new JList<>(results); lstResults.setFocusable(false); lstResults.setFixedCellHeight(CELL_HEIGHT); lstResults.setFont(getFont());
            lstResults.setBackground(GiaoDien.WHITE);
            lstResults.setSelectionBackground(GiaoDien.TEAL);
            lstResults.setSelectionForeground(Color.WHITE);
            lstResults.setCellRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
                    Font base = OChonTimKiem.this.getFont() != null ? OChonTimKiem.this.getFont() : GiaoDien.font(13, false);
                    lbl.setFont(GiaoDien.font(base.getSize(), isSelected));
                    lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                    lbl.setBackground(isSelected ? GiaoDien.TEAL : GiaoDien.WHITE);
                    lbl.setForeground(isSelected ? Color.WHITE : GiaoDien.TEXT);
                    return lbl;
                }
            });
            lstResults.addMouseListener(new MouseAdapter(){@Override public void mouseReleased(MouseEvent e){if(!SwingUtilities.isLeftMouseButton(e)) return; int index=lstResults.locationToIndex(e.getPoint());if(index>=0 && lstResults.getCellBounds(index,index).contains(e.getPoint())) {lstResults.setSelectedIndex(index);choose();e.consume();}}});
            scrResults=new JScrollPane(lstResults, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrResults.setBorder(null);
            scrResults.getViewport().setBackground(GiaoDien.WHITE);
            scrResults.getVerticalScrollBar().setUnitIncrement(17);
            GiaoDien.styleScroll(scrResults, false);
            popup.add(scrResults);
            popup.addPopupMenuListener(new PopupMenuListener(){
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
                    popupClosedAt=System.currentTimeMillis();
                    setBorder(new LineBorder(txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1, true));
                    repaint();
                    firePopupMenuWillBecomeInvisible();
                }
                @Override
                public void popupMenuCanceled(PopupMenuEvent e){
                    popupClosedAt=System.currentTimeMillis();
                    setBorder(new LineBorder(txtEditor != null && txtEditor.isFocusOwner() ? GiaoDien.TEAL : GiaoDien.LINE, 1, true));
                    repaint();
                    firePopupMenuCanceled();
                }
            });
        }
        String term=filtering ? normalize(typed.trim()) : ""; results.clear();
        for(int i=0;i<getItemCount();i++) {T value=getItemAt(i);if(normalize(value.toString()).contains(term)) results.addElement(value);}
        if(scrResults != null) {
            int maxRows = Math.max(8, getMaximumRowCount());
            int visibleRows = Math.max(1, Math.min(results.size(), maxRows));
            int popupHeight = visibleRows * CELL_HEIGHT + 2;
            Font itemFont = getFont() != null ? GiaoDien.font(getFont().getSize(), true) : GiaoDien.font(13, true);
            FontMetrics fm = lstResults.getFontMetrics(itemFont);
            int maxTextWidth = 0;
            for(int i=0;i<results.size();i++) {
                T val = results.getElementAt(i);
                if(val != null) maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(val.toString()));
            }
            int popupWidth = Math.max(Math.max(180, getWidth()), Math.min(560, maxTextWidth + 36));
            scrResults.setPreferredSize(new Dimension(popupWidth, popupHeight));
            popup.pack();
        }
        if(!results.isEmpty()) lstResults.setSelectedIndex(0);
        if(!filtering) lstResults.setSelectedValue(super.getSelectedItem(),true);
        if(!isPopupVisible()) popup.show(this,0,getHeight()+2);
    }

    private void choose() {
        if(isPopupVisible() && lstResults!=null && lstResults.getSelectedValue()!=null) {T value=lstResults.getSelectedValue();setPopupVisible(false);setSelectedItem(value);txtEditor.requestFocusInWindow();return;}
        String text=txtEditor.getText();
        for(int i=0;i<getItemCount();i++) {T value=getItemAt(i); if(value!=null && value.toString().equalsIgnoreCase(text)) {setPopupVisible(false);setSelectedItem(value);return;}}
        open(true);
    }
}
