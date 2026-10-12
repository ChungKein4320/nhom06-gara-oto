package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class ObjectPanel extends JPanel {
    private final String name;
    private final SampleData.Definition def;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField search;
    public ObjectPanel(String name){
        this.name=name;
        def=SampleData.get(name);
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(28,30,30,30));
        JPanel content=Theme.stack(0);
        content.setAlignmentX(LEFT_ALIGNMENT);
        JPanel top=new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel heading=Theme.stack(0);
        heading.add(Theme.label("QUẢN LÝ NGHIỆP VỤ/"+name.toUpperCase(),11,true,Theme.TEAL));
        Theme.gap(heading,8);
        heading.add(Theme.label(name,26,true,Theme.NAV));
        Theme.gap(heading,6);
        heading.add(Theme.label("Theo dõi, tra cứu và xử lý thông tin "+name.toLowerCase()+" tại gara.",13,false,Theme.MUTED));
        top.add(heading,BorderLayout.CENTER);
        JButton add=Theme.button(def.action,Theme.TEAL,Color.WHITE);
        add.addActionListener(e->form(-1));
        JPanel addWrap=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        addWrap.setOpaque(false);
        add.setPreferredSize(new Dimension(Math.min(205,105+def.action.length()*6),38));
        addWrap.add(add);
        top.add(addWrap,BorderLayout.EAST);
        content.add(top);
        Theme.gap(content,24);
        JPanel stats=new JPanel(new GridLayout(1,3,15,0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(9999,105));
        stats.add(stat("TỔNG BẢN GHI",String.valueOf(def.rows.length),Theme.TEAL));
        stats.add(stat("ĐANG THEO DÕI",String.valueOf(def.rows.length),Theme.ORANGE));
        stats.add(stat("BỘ PHẬN", "THINKING AUTO",Theme.NAV));
        content.add(stats);
        Theme.gap(content,20);
        JPanel card=Theme.card(new BorderLayout(0,18));
        JPanel tools=new JPanel(new BorderLayout(12,0));
        tools.setOpaque(false);
        search=Theme.field("Tìm theo mã, tên, biển số hoặc trạng thái...");
        search.setPreferredSize(new Dimension(440,42));
        tools.add(search,BorderLayout.CENTER);
        JButton reset=Theme.button("Làm mới",new Color(0xE6EFED),Theme.TEAL);
        tools.add(reset,BorderLayout.EAST);
        card.add(tools,BorderLayout.NORTH);
        model=new DefaultTableModel(def.rows,def.cols){
            public boolean isCellEditable(int r,int c){
                return false;
            }
        };
        table=Theme.table(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScroll=new JScrollPane(table);
        tableScroll.setBorder(new LineBorder(Theme.LINE));
        tableScroll.setPreferredSize(new Dimension(450,Math.max(255,Math.min(410,def.rows.length*45+50))));
        card.add(tableScroll,BorderLayout.CENTER);
        JPanel foot=new JPanel(new BorderLayout());
        foot.setOpaque(false);
        JLabel count=Theme.label("Danh sách dữ liệu minh họa • Chưa kết nối SQL",12,false,Theme.MUTED);
        foot.add(count,BorderLayout.WEST);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        actions.setOpaque(false);
        JButton detail=Theme.button("Xem chi tiết",Theme.TEAL,Color.WHITE),edit=Theme.button("Chỉnh sửa",Theme.CREAM,Theme.NAV),delete=Theme.button("Xóa",Theme.ORANGE,Color.WHITE);
        actions.add(detail);
        actions.add(edit);
        actions.add(delete);
        foot.add(actions,BorderLayout.EAST);
        card.add(foot,BorderLayout.SOUTH);
        content.add(card);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){
                filter();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e){
                filter();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e){
                filter();
            }
        });
        reset.addActionListener(e->search.setText(""));
        detail.addActionListener(e->detail());
        edit.addActionListener(e->form(table.getSelectedRow()));
        delete.addActionListener(e->delete());
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2)detail();
            }
        });
        add(content,BorderLayout.NORTH);
    }
    private JPanel stat(String k,String v,Color c){
        JPanel p=Theme.card(new BorderLayout());
        p.add(Theme.label(k,11,true,Theme.MUTED),BorderLayout.NORTH);
        p.add(Theme.label(v,23,true,c),BorderLayout.CENTER);
        return p;
    }
    private int index(){
        int r=table.getSelectedRow();
        if(r<0)JOptionPane.showMessageDialog(this,"Vui lòng chọn một dòng trong danh sách.","Thông báo",JOptionPane.INFORMATION_MESSAGE);
        return r;
    }
    private void detail(){
        int r=index();
        if(r<0)return;
        StringBuilder b=new StringBuilder("<html><div style='width:410px'>");
        for(int i=0;i<model.getColumnCount();i++)b.append("<p><b>").append(model.getColumnName(i)).append(":</b> ").append(model.getValueAt(r,i)).append("</p>");
        b.append("</div></html>");
        JOptionPane.showMessageDialog(this,new JLabel(b.toString()),"Chi tiết • "+name,JOptionPane.INFORMATION_MESSAGE);
    }
    private void form(int row) {
        int real = row < 0 ? -1 : table.convertRowIndexToModel(row);
        String[] previous = null;
        if (real >= 0) {
            previous = new String[model.getColumnCount()];
            for (int i = 0; i < previous.length; i++) {
                previous[i] = String.valueOf(model.getValueAt(real, i));
            }
        }
        String[] values = EntryDialog.show(this, name,
                real < 0 ? def.action : "Chỉnh sửa " + name, def.cols, previous);
        if (values == null) {
            return;
        }
        if (real < 0) {
            model.addRow(values);
        } else {
            for (int i = 0; i < values.length; i++) {
                model.setValueAt(values[i], real, i);
            }
        }
    }
    private void delete(){
        int r=index();
        if(r>=0&&JOptionPane.showConfirmDialog(this,"Xóa bản ghi đã chọn khỏi dữ liệu minh họa?","Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)model.removeRow(table.convertRowIndexToModel(r));
    }
    private void filter(){
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(search.getText().trim())));
    }
}
