package gui;

import java.awt.*;
import javax.swing.*;

/** Màu riêng cho dữ liệu tự điền, vẫn đọc rõ khi control bị khóa. */
final class DinhDangKeThua {
    static final Color TEXT=new Color(0x145A75), BACKGROUND=new Color(0xEAF5FA);
    private DinhDangKeThua() {}
    static void lock(JComponent control) {
        control.setBackground(BACKGROUND); control.setForeground(TEXT);
        control.setFont(GiaoDien.font(12,true));
        if(control instanceof JTextField txtValue) {
            txtValue.setEditable(false); txtValue.setDisabledTextColor(TEXT);
        } else if(control instanceof JComboBox<?> cmbValue) {
            cmbValue.setEnabled(false);
            if(cmbValue.getEditor().getEditorComponent() instanceof JTextField txtEditor) {
                txtEditor.setDisabledTextColor(TEXT); txtEditor.setForeground(TEXT); txtEditor.setBackground(BACKGROUND); txtEditor.setFont(GiaoDien.font(12,true));
            }
            cmbValue.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                    super.getListCellRendererComponent(list,value,index,selected,focus);
                    setForeground(TEXT); setBackground(BACKGROUND); setFont(GiaoDien.font(12,true)); return this;
                }
            });
            // BasicComboBoxUI mặc định ghi đè màu chữ khi disabled.
            cmbValue.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override public void paintCurrentValue(Graphics graphics,Rectangle bounds,boolean focus) {
                    Component rendered=comboBox.getRenderer().getListCellRendererComponent(listBox,comboBox.getSelectedItem(),-1,false,false);
                    rendered.setForeground(TEXT); rendered.setBackground(BACKGROUND);
                    currentValuePane.paintComponent(graphics,rendered,comboBox,bounds.x,bounds.y,bounds.width,bounds.height,true);
                }
            });
        } else control.setEnabled(false);
    }
}
