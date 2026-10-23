package gui;

import java.awt.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/** Gõ ngay trong ô chọn, chỉ danh sách kết quả xuất hiện phía dưới. */
class OChonTimKiem<T> extends JComboBox<T> {
    private JTextField txtEditor;
    private JPopupMenu popup;
    private JList<T> lstResults;
    private DefaultListModel<T> results;
    private boolean changing,queued;
    OChonTimKiem() { super(); setup(); }
    OChonTimKiem(T[] values) { super(values); setup(); }
    static String normalize(String text) { return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT); }
    private void setup() {
        setEditable(true); txtEditor=(JTextField)getEditor().getEditorComponent();
        txtEditor.setFont(GiaoDien.font(12,false)); txtEditor.setToolTipText("Gõ mã hoặc tên để lọc; bấm kết quả để chọn ngay, hoặc nhấn Enter.");
        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if(changing || queued) return; queued=true;
                SwingUtilities.invokeLater(() -> {
                    queued=false; if(changing || !isEnabled() || !txtEditor.isFocusOwner()) return;
                    Object selected=OChonTimKiem.super.getSelectedItem();
                    if(selected!=null && txtEditor.getText().equals(selected.toString())) return;
                    // Không để thông tin kế thừa tiếp tục dùng lựa chọn cũ khi đang gõ.
                    fireActionEvent(); open(true);
                });
            }
            public void insertUpdate(DocumentEvent e){update();} public void removeUpdate(DocumentEvent e){update();} public void changedUpdate(DocumentEvent e){update();}
        });
        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"pickResult");
        txtEditor.getActionMap().put("pickResult",new AbstractAction(){public void actionPerformed(ActionEvent e){choose();}});
        for(int key:new int[]{KeyEvent.VK_DOWN,KeyEvent.VK_UP}) {
            String action="result"+key; txtEditor.getInputMap().put(KeyStroke.getKeyStroke(key,0),action);
            txtEditor.getActionMap().put(action,new AbstractAction(){public void actionPerformed(ActionEvent e){
                if(!isPopupVisible()) { open(true); return; }
                if(results!=null && !results.isEmpty()) { int next=Math.max(0,Math.min(results.size()-1,lstResults.getSelectedIndex()+(key==KeyEvent.VK_DOWN ? 1 : -1))); lstResults.setSelectedIndex(next); lstResults.ensureIndexIsVisible(next); }
            }});
        }
        txtEditor.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),"cancelSearch");
        txtEditor.getActionMap().put("cancelSearch",new AbstractAction(){public void actionPerformed(ActionEvent e){setPopupVisible(false); restore();}});
        txtEditor.addFocusListener(new FocusAdapter(){@Override public void focusGained(FocusEvent e){SwingUtilities.invokeLater(txtEditor::selectAll);}});
    }
    @Override public Object getSelectedItem() {
        Object selected=super.getSelectedItem();
        if(!changing && txtEditor!=null && isEnabled() && isEditable() && (selected==null || !txtEditor.getText().equals(selected.toString()))) return null;
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
            // Action listeners must see the new editor text as soon as selection is committed.
            if(txtEditor!=null) getEditor().setItem(value);
            super.setSelectedItem(value);
            if(txtEditor!=null) getEditor().setItem(super.getSelectedItem());
        } finally {changing=previous;}
    }
    @Override public void setPopupVisible(boolean visible) { if(visible) open(false); else if(popup!=null) popup.setVisible(false); }
    @Override public boolean isPopupVisible() { return popup!=null && popup.isVisible(); }
    private void restore() { changing=true; try {Object selected=super.getSelectedItem(); txtEditor.setText(selected==null ? "" : selected.toString());} finally {changing=false;} }
    private void open(boolean filtering) {
        if(txtEditor==null || !isEnabled() || !isShowing()) return;
        String typed=txtEditor.getText();
        if(!isPopupVisible()) {
            changing=true;
            try {firePopupMenuWillBecomeVisible(); if(filtering) txtEditor.setText(typed);} finally {changing=false;}
            popup=new JPopupMenu(); popup.setFocusable(false); popup.setLayout(new BorderLayout());
            results=new DefaultListModel<>(); lstResults=new JList<>(results); lstResults.setFocusable(false); lstResults.setFixedCellHeight(28); lstResults.setFont(getFont());
            lstResults.addMouseListener(new MouseAdapter(){@Override public void mouseReleased(MouseEvent e){if(!SwingUtilities.isLeftMouseButton(e)) return; int index=lstResults.locationToIndex(e.getPoint());if(index>=0 && lstResults.getCellBounds(index,index).contains(e.getPoint())) {lstResults.setSelectedIndex(index);choose();e.consume();}}});
            JScrollPane scrResults=new JScrollPane(lstResults); scrResults.setPreferredSize(new Dimension(Math.max(240,getWidth()-4),210)); popup.add(scrResults);
            popup.addPopupMenuListener(new PopupMenuListener(){public void popupMenuWillBecomeVisible(PopupMenuEvent e){}public void popupMenuWillBecomeInvisible(PopupMenuEvent e){firePopupMenuWillBecomeInvisible();}public void popupMenuCanceled(PopupMenuEvent e){firePopupMenuCanceled();}});
        }
        String term=filtering ? normalize(typed.trim()) : ""; results.clear();
        for(int i=0;i<getItemCount();i++) {T value=getItemAt(i);if(normalize(value.toString()).contains(term)) results.addElement(value);}
        if(!results.isEmpty()) lstResults.setSelectedIndex(0);
        if(!filtering) lstResults.setSelectedValue(super.getSelectedItem(),true);
        if(!isPopupVisible()) popup.show(this,0,getHeight());
    }
    private void choose() {
        if(isPopupVisible() && lstResults.getSelectedValue()!=null) {T value=lstResults.getSelectedValue();setPopupVisible(false);setSelectedItem(value);txtEditor.requestFocusInWindow();return;}
        String text=txtEditor.getText();
        for(int i=0;i<getItemCount();i++) {T value=getItemAt(i); if(value.toString().equalsIgnoreCase(text)) {setSelectedItem(value);return;}}
        open(true);
    }
}
