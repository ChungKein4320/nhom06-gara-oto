package gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
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
                        && windowEvent.getID()==java.awt.event.WindowEvent.WINDOW_OPENED) {
                    styleAllScrollPanes(windowEvent.getWindow());
                    if(windowEvent.getWindow() instanceof Dialog dialog) {
                        dialog.setLocationRelativeTo(null);
                    }
                } else if(event instanceof java.awt.event.ContainerEvent containerEvent
                        && containerEvent.getID()==java.awt.event.ContainerEvent.COMPONENT_ADDED) {
                    styleAllScrollPanes(containerEvent.getChild());
                }
            },AWTEvent.WINDOW_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
            dialogCenteringInstalled=true;
        }
        UIManager.put("Label.font",font(13,false));
        UIManager.put("Button.font",font(13,true));
        UIManager.put("TextField.font",font(13,false));
        UIManager.put("Table.font",font(13,false));
        UIManager.put("TableHeader.font",font(12,true));
        UIManager.put("OptionPane.messageFont",font(13,false));
        UIManager.put("ScrollBar.width",8);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", TEAL);
        UIManager.put("ComboBox.selectionForeground", WHITE);
        UIManager.put("ComboBox.font", font(12, false));
    }
    public static void styleAllScrollPanes(Component root) {
        if(root==null) return;
        if(root instanceof JScrollPane scr) {
            Component view=scr.getViewport()!=null ? scr.getViewport().getView() : null;
            boolean dark=view!=null && NAV.equals(view.getBackground());
            styleScroll(scr,dark);
        }
        if(root instanceof Container container) {
            for(Component child:container.getComponents()) styleAllScrollPanes(child);
        }
    }
    public static void styleScroll(JScrollPane scr,boolean dark) {
        if(scr==null) return;
        styleScrollBar(scr.getVerticalScrollBar(),dark);
        styleScrollBar(scr.getHorizontalScrollBar(),dark);
        if(scr.getViewport()!=null && scr.getViewport().getView() instanceof JTable) {
            scr.setBorder(new LineBorder(LINE,1,true));
            scr.getViewport().setBackground(WHITE);
        }
    }
    public static void styleScrollBar(JScrollBar bar,boolean dark) {
        if(bar==null) return;
        Boolean applied=(Boolean)bar.getClientProperty("giaoDien.styled");
        Boolean wasDark=(Boolean)bar.getClientProperty("giaoDien.dark");
        if(Boolean.TRUE.equals(applied) && java.util.Objects.equals(wasDark,dark)) return;
        bar.putClientProperty("giaoDien.styled",Boolean.TRUE);
        bar.putClientProperty("giaoDien.dark",dark);
        bar.setOpaque(true);
        bar.setBackground(dark ? NAV : WHITE);
        bar.setPreferredSize(new Dimension(dark ? 6 : 8,dark ? 6 : 8));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                trackColor=dark ? NAV : new Color(0xF0F4F3);
                thumbColor=dark ? new Color(0x3D5E63) : new Color(0xB8C9C6);
            }
            private JButton zeroButton() {
                JButton b=new JButton();
                Dimension d=new Dimension(0,0);
                b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d);
                b.setBorder(null); b.setFocusable(false); b.setOpaque(false);
                return b;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(dark ? NAV : (c.getParent() instanceof JScrollPane sp && sp.getViewport()!=null && sp.getViewport().getView() instanceof JTable ? WHITE : BG));
                g2.fillRect(r.x,r.y,r.width,r.height);
                g2.dispose();
            }
            @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r) {
                if(r.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging || isThumbRollover() ? (dark ? MINT : TEAL) : (dark ? new Color(0x3D5E63) : new Color(0xB8C9C6)));
                int arc=6;
                if(scrollbar.getOrientation()==Adjustable.VERTICAL) {
                    int w=Math.max(4,r.width-2);
                    int x=r.x+(r.width-w)/2;
                    g2.fillRoundRect(x,r.y+1,w,Math.max(12,r.height-2),arc,arc);
                } else {
                    int h=Math.max(4,r.height-2);
                    int y=r.y+(r.height-h)/2;
                    g2.fillRoundRect(r.x+1,y,Math.max(12,r.width-2),h,arc,arc);
                }
                g2.dispose();
            }
        });
    }
    public static JPanel panel(Color c){
        JPanel pnlP=new JPanel();
        pnlP.setBackground(c);
        return pnlP;
    }
    public static JPanel rounded(Color c,int r){
        return new JPanel(){
            @Override
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
            @Override
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
