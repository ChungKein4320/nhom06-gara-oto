package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
public final class Theme {
    public static final Color NAV=new Color(0x13272E),TEAL=new Color(0x487674),MINT=new Color(0x83B4AE),CREAM=new Color(0xE8C69B),PEACH=new Color(0xD69369),ORANGE=new Color(0xCB6330),BG=new Color(0xF4F7F6),WHITE=Color.WHITE,TEXT=new Color(0x21383C),MUTED=new Color(0x718387),LINE=new Color(0xE4EBE9);
    public static Font font(int size,boolean bold){
        return new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size);
    }
    public static void init(){
        UIManager.put("Label.font",font(13,false));
        UIManager.put("Button.font",font(13,true));
        UIManager.put("TextField.font",font(13,false));
        UIManager.put("Table.font",font(13,false));
        UIManager.put("TableHeader.font",font(12,true));
        UIManager.put("OptionPane.messageFont",font(13,false));
    }
    public static JPanel panel(Color c){
        JPanel p=new JPanel();
        p.setBackground(c);
        return p;
    }
    public static JPanel rounded(Color c,int r){
        return new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D q=(Graphics2D)g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                q.setColor(c);
                q.fillRoundRect(0,0,getWidth(),getHeight(),r,r);
                q.dispose();
            }
        };
    }
    public static JButton button(String s,Color bg,Color fg){
        JButton b=new JButton(s){
            protected void paintComponent(Graphics g){
                Graphics2D q=(Graphics2D)g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                q.setColor(getModel().isRollover()?bg.brighter():bg);
                q.fillRoundRect(0,0,getWidth(),getHeight(),13,13);
                q.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(fg);
        Icons.apply(b,s,fg);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,17,10,17));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public static JLabel label(String s,int size,boolean bold,Color c){
        JLabel l=new JLabel(s);
        l.setFont(font(size,bold));
        l.setForeground(c);
        return l;
    }
    public static JTextField field(String hint){
        JTextField t=new JTextField();
        t.putClientProperty("JTextField.placeholderText",hint);
        t.setFont(font(13,false));
        t.setForeground(TEXT);
        t.setBorder(new CompoundBorder(new LineBorder(LINE,1,true),new EmptyBorder(10,12,10,12)));
        return t;
    }
    public static JTable table(DefaultTableModel m){
        JTable t=new JTable(m);
        t.setRowHeight(45);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0,0));
        t.setSelectionBackground(new Color(0xE3F1EF));
        t.setSelectionForeground(TEXT);
        t.setForeground(TEXT);
        t.setFont(font(12,false));
        t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(new Color(0xEDF3F1));
        t.getTableHeader().setForeground(TEAL);
        t.getTableHeader().setPreferredSize(new Dimension(0,43));
        t.getTableHeader().setReorderingAllowed(false);
        t.setDefaultEditor(Object.class,null);
        return t;
    }
    public static JPanel card(LayoutManager l){
        JPanel p=rounded(WHITE,18);
        p.setLayout(l);
        p.setBorder(new EmptyBorder(20,22,20,22));
        p.setOpaque(false);
        return p;
    }
    public static JPanel stack(int gap){
        JPanel p=panel(BG);
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        return p;
    }
    public static void gap(JPanel p,int h){
        p.add(Box.createVerticalStrut(h));
    }
}
