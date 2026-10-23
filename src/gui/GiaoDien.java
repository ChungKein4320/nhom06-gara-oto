package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
public final class GiaoDien {
    private static boolean dialogCenteringInstalled;
    public static final Color NAV=new Color(0x13272E),TEAL=new Color(0x487674),MINT=new Color(0x83B4AE),CREAM=new Color(0xE8C69B),PEACH=new Color(0xD69369),ORANGE=new Color(0xCB6330),BG=new Color(0xF4F7F6),WHITE=Color.WHITE,TEXT=new Color(0x21383C),MUTED=new Color(0x718387),LINE=new Color(0xE4EBE9);
    public static Font font(int size,boolean bold){
        return new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size);
    }
    public static void init(){
        if(!dialogCenteringInstalled) {
            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                if(event instanceof java.awt.event.WindowEvent windowEvent
                        && windowEvent.getID()==java.awt.event.WindowEvent.WINDOW_OPENED
                        && windowEvent.getWindow() instanceof Dialog dialog) {
                    dialog.setLocationRelativeTo(null);
                }
            },AWTEvent.WINDOW_EVENT_MASK);
            dialogCenteringInstalled=true;
        }
        UIManager.put("Label.font",font(13,false));
        UIManager.put("Button.font",font(13,true));
        UIManager.put("TextField.font",font(13,false));
        UIManager.put("Table.font",font(13,false));
        UIManager.put("TableHeader.font",font(12,true));
        UIManager.put("OptionPane.messageFont",font(13,false));
    }
    public static JPanel panel(Color c){
        JPanel pnlP=new JPanel();
        pnlP.setBackground(c);
        return pnlP;
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
        JButton btnB=new JButton(s){
            protected void paintComponent(Graphics g){
                Graphics2D q=(Graphics2D)g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                q.setColor(getModel().isRollover()?bg.brighter():bg);
                q.fillRoundRect(0,0,getWidth(),getHeight(),13,13);
                q.dispose();
                super.paintComponent(g);
            }
        };
        btnB.setForeground(fg);
        BieuTuong.apply(btnB,s,fg);
        btnB.setContentAreaFilled(false);
        btnB.setBorder(BorderFactory.createEmptyBorder(10,17,10,17));
        btnB.setFocusPainted(false);
        btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btnB;
    }
    public static JLabel label(String s,int size,boolean bold,Color c){
        JLabel lblL=new JLabel(s);
        lblL.setFont(font(size,bold));
        lblL.setForeground(c);
        return lblL;
    }
    public static JTextField field(String hint){
        JTextField txtT=new JTextField();
        txtT.putClientProperty("JTextField.placeholderText",hint);
        txtT.setFont(font(13,false));
        txtT.setForeground(TEXT);
        txtT.setBorder(new CompoundBorder(new LineBorder(LINE,1,true),new EmptyBorder(10,12,10,12)));
        return txtT;
    }
    public static JTable table(DefaultTableModel m){
        JTable tblT=new JTable(m){
            private final TableCellRenderer cells=new DinhDangBang();
            @Override public TableCellRenderer getCellRenderer(int row,int column) { return cells; }
            @Override public void createDefaultColumnsFromModel(){
                super.createDefaultColumnsFromModel();
                for(int i=0;i<getColumnModel().getColumnCount();i++){
                    TableColumn column=getColumnModel().getColumn(i);
                    column.setHeaderValue(NhanCot.forField(
                            getModel().getColumnName(column.getModelIndex())));
                }
            }
        };
        tblT.setRowHeight(40);
        tblT.setShowGrid(false);
        tblT.setIntercellSpacing(new Dimension(0,0));
        tblT.setSelectionBackground(new Color(0xE3F1EF));
        tblT.setSelectionForeground(TEXT);
        tblT.setForeground(TEXT);
        tblT.setFont(font(12,false));
        tblT.setFillsViewportHeight(true);
        tblT.getTableHeader().setBackground(new Color(0xEDF3F1));
        tblT.getTableHeader().setForeground(TEAL);
        tblT.getTableHeader().setPreferredSize(new Dimension(0,43));
        tblT.getTableHeader().setReorderingAllowed(false);
        DinhDangBang.header(tblT);
        tblT.setDefaultEditor(Object.class,null);
        return tblT;
    }
    public static JPanel card(LayoutManager l){
        JPanel pnlP=rounded(WHITE,18);
        pnlP.setLayout(l);
        pnlP.setBorder(new EmptyBorder(20,22,20,22));
        pnlP.setOpaque(false);
        return pnlP;
    }
    public static JPanel stack(int gap){
        JPanel pnlP=panel(BG);
        pnlP.setLayout(new BoxLayout(pnlP,BoxLayout.Y_AXIS));
        return pnlP;
    }
    public static void gap(JPanel pnlP,int h){
        pnlP.add(Box.createVerticalStrut(h));
    }
}
