package gui;

import dao.StatisticsDAO;
import dao.StatisticsDAO.Bucket;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Thống kê theo kỳ; số liệu được tải từ database ngoài luồng giao diện. */
public final class ManHinhThongKe extends JPanel {
    @java.io.Serial private static final long serialVersionUID = 1L;
    private final JComboBox<String> cmbCategory = new OChonTimKiem<>(new String[]{"Doanh thu","Khách hàng","Dịch vụ"});
    private final JComboBox<String> cmbPeriod = new OChonTimKiem<>(new String[]{"Theo tháng","Theo quý","Theo năm","Thời gian tùy chọn"});
    private final JComboBox<String> cmbMonth = new OChonTimKiem<>();
    private final JComboBox<String> cmbQuarter = new OChonTimKiem<>(new String[]{"Quý I","Quý II","Quý III","Quý IV"});
    private final JSpinner spnYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),1900,9998,1));
    private final OChonNgay pnlStart = new OChonNgay(LocalDate.now().withDayOfMonth(1).toString());
    private final OChonNgay pnlEnd = new OChonNgay(LocalDate.now().toString());
    private final JPanel pnlMonthCell, pnlQuarterCell, pnlYearCell, pnlCustom;
    private final JLabel[] lblMetricTitles = new JLabel[3];
    private final JLabel[] lblMetricValues = new JLabel[3];
    private final JLabel lblChartTitle = GiaoDien.label("",18,true,GiaoDien.NAV);
    private final JLabel lblChartHint = GiaoDien.label("",12,false,GiaoDien.MUTED);
    private final JLabel lblSummary = GiaoDien.label("",12,false,GiaoDien.MUTED);
    private final Chart pnlChart = new Chart();
    private final JButton btnView = GiaoDien.button("Xem thống kê",GiaoDien.TEAL,Color.WHITE);
    private int requestVersion;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NUMBER = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public ManHinhThongKe() {
        super(new BorderLayout(0,20));
        setBackground(GiaoDien.BG); setBorder(new EmptyBorder(26,28,28,28));
        JPanel pnlHeading = new JPanel(new GridLayout(2,1,0,6)); pnlHeading.setOpaque(false);
        pnlHeading.add(GiaoDien.label("THINKING AUTO / BÁO CÁO",11,true,GiaoDien.TEAL));
        pnlHeading.add(GiaoDien.label("Thống kê hoạt động gara",27,true,GiaoDien.NAV));
        add(pnlHeading,BorderLayout.NORTH);
        JPanel pnlBody = new JPanel(new BorderLayout(0,18)); pnlBody.setOpaque(false);
        JPanel pnlTop = new JPanel(new BorderLayout(0,18)); pnlTop.setOpaque(false);
        JPanel pnlFilters = GiaoDien.card(new BorderLayout(0,12));
        JPanel pnlSelectors = new JPanel(new GridLayout(1,2,16,0)); pnlSelectors.setOpaque(false);
        pnlSelectors.add(cell("Phân loại",cmbCategory)); pnlSelectors.add(cell("Thời gian",cmbPeriod));
        pnlFilters.add(pnlSelectors,BorderLayout.NORTH);
        for(int i=1;i<=12;i++) cmbMonth.addItem("Tháng "+i);
        cmbMonth.setSelectedIndex(LocalDate.now().getMonthValue()-1);
        cmbQuarter.setSelectedIndex((LocalDate.now().getMonthValue()-1)/3);
        cmbPeriod.setSelectedIndex(2);
        spnYear.setEditor(new JSpinner.NumberEditor(spnYear,"#"));
        spnYear.setPreferredSize(new Dimension(140,36));
        spnYear.setFont(GiaoDien.font(13,false));
        ((JSpinner.NumberEditor)spnYear.getEditor()).getTextField().setFont(GiaoDien.font(13,false));
        JPanel pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); pnlOptions.setOpaque(false);
        pnlMonthCell=cell("Tháng",cmbMonth); pnlQuarterCell=cell("Quý",cmbQuarter); pnlYearCell=cell("Năm",spnYear);
        pnlOptions.add(pnlMonthCell); pnlOptions.add(pnlQuarterCell); pnlOptions.add(pnlYearCell);
        pnlCustom=new JPanel(new GridLayout(1,2,12,0)); pnlCustom.setOpaque(false);
        pnlCustom.add(cell("Từ ngày",pnlStart)); pnlCustom.add(cell("Đến ngày",pnlEnd)); pnlOptions.add(pnlCustom);
        JPanel pnlFilterBottom=new JPanel(new BorderLayout(12,0)); pnlFilterBottom.setOpaque(false);
        pnlFilterBottom.add(pnlOptions,BorderLayout.CENTER);
        JPanel pnlButtonWrap=new JPanel(new BorderLayout()); pnlButtonWrap.setOpaque(false);
        pnlButtonWrap.setBorder(new EmptyBorder(17,0,0,0)); pnlButtonWrap.add(btnView,BorderLayout.NORTH);
        pnlFilterBottom.add(pnlButtonWrap,BorderLayout.EAST); pnlFilters.add(pnlFilterBottom,BorderLayout.CENTER);
        pnlTop.add(pnlFilters,BorderLayout.NORTH);
        JPanel pnlMetrics=new JPanel(new GridLayout(1,3,16,0)); pnlMetrics.setOpaque(false);
        for(int i=0;i<3;i++) {
            JPanel pnlCard=GiaoDien.card(new BorderLayout(0,12));
            lblMetricTitles[i]=GiaoDien.label("",11,true,GiaoDien.MUTED);
            lblMetricValues[i]=GiaoDien.label("—",25,true,i==0?GiaoDien.TEAL:GiaoDien.NAV);
            pnlCard.add(lblMetricTitles[i],BorderLayout.NORTH); pnlCard.add(lblMetricValues[i],BorderLayout.CENTER);
            pnlMetrics.add(pnlCard);
        }
        pnlTop.add(pnlMetrics,BorderLayout.SOUTH); pnlBody.add(pnlTop,BorderLayout.NORTH);
        JPanel pnlChartCard=GiaoDien.card(new BorderLayout(0,14));
        JPanel pnlChartHeading=new JPanel(new GridLayout(2,1,0,6)); pnlChartHeading.setOpaque(false);
        pnlChartHeading.add(lblChartTitle); pnlChartHeading.add(lblChartHint); pnlChartCard.add(pnlChartHeading,BorderLayout.NORTH);
        pnlChartCard.add(pnlChart,BorderLayout.CENTER); pnlChartCard.add(lblSummary,BorderLayout.SOUTH);
        pnlBody.add(pnlChartCard,BorderLayout.CENTER); add(pnlBody,BorderLayout.CENTER);
        for(JComboBox<String> cmbCombo:List.of(cmbCategory,cmbPeriod,cmbMonth,cmbQuarter)) {
            cmbCombo.setFont(GiaoDien.font(13,false)); cmbCombo.setPreferredSize(new Dimension(160,36));
        }
        btnView.addActionListener(e->load());
        cmbCategory.addActionListener(e->load());
        cmbPeriod.addActionListener(e->{ updatePeriod(); load(); });
        cmbMonth.addActionListener(e->load()); cmbQuarter.addActionListener(e->load()); spnYear.addChangeListener(e->load());
        updatePeriod(); load();
    }

    private static JPanel cell(String label,JComponent component) {
        JPanel pnlCell=new JPanel(new BorderLayout(0,7)); pnlCell.setOpaque(false);
        pnlCell.add(GiaoDien.label(label,12,true,GiaoDien.TEXT),BorderLayout.NORTH); pnlCell.add(component,BorderLayout.CENTER);
        return pnlCell;
    }
    private void updatePeriod() {
        int p=cmbPeriod.getSelectedIndex();
        pnlMonthCell.setVisible(p==0); pnlQuarterCell.setVisible(p==1); pnlYearCell.setVisible(p!=3); pnlCustom.setVisible(p==3);
        revalidate(); repaint();
    }

    static LocalDate[] dateRange(int mode,int year,int month,int quarter,String from,String to) {
        LocalDate first,last;
        if(mode==0) { YearMonth ym=YearMonth.of(year,month); first=ym.atDay(1); last=ym.atEndOfMonth(); }
        else if(mode==1) {
            if(quarter<1 || quarter>4) throw new IllegalArgumentException("Quý không hợp lệ.");
            first=LocalDate.of(year,(quarter-1)*3+1,1); last=first.plusMonths(3).minusDays(1);
        } else if(mode==2) { first=LocalDate.of(year,1,1); last=LocalDate.of(year,12,31); }
        else if(mode==3) {
            if(from.isBlank() || to.isBlank()) throw new IllegalArgumentException("Vui lòng chọn đầy đủ Từ ngày và Đến ngày.");
            first=LocalDate.parse(from); last=LocalDate.parse(to);
        } else throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
        if(first.isAfter(last)) throw new IllegalArgumentException("Từ ngày phải trước hoặc bằng Đến ngày.");
        if(last.getYear()>9998) throw new IllegalArgumentException("Năm phải nhỏ hơn 9999.");
        return new LocalDate[]{first,last};
    }
    private void load() {
        final int version=++requestVersion;
        final int type=cmbCategory.getSelectedIndex();
        final LocalDate[] range;
        try {
            spnYear.commitEdit();
            range=dateRange(cmbPeriod.getSelectedIndex(),((Number)spnYear.getValue()).intValue(),cmbMonth.getSelectedIndex()+1,
                    cmbQuarter.getSelectedIndex()+1,pnlStart.getDateText(),pnlEnd.getDateText());
        } catch(Exception ex) {
            clear("Khoảng thời gian chưa hợp lệ.");
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Kiểm tra thời gian",JOptionPane.WARNING_MESSAGE); return;
        }
        boolean daily=cmbPeriod.getSelectedIndex()==0 || (cmbPeriod.getSelectedIndex()==3 && ChronoUnit.DAYS.between(range[0],range[1])<=62);
        lblChartTitle.setText(type==0?"Xu hướng doanh thu":type==1?"Khách hàng sử dụng dịch vụ":"Dịch vụ được sử dụng nhiều nhất");
        lblChartHint.setText(type==0?"Doanh thu từ hóa đơn đã thanh toán • Đường xu hướng và vùng diện tích"
                :type==1?"Khách hàng duy nhất trong từng mốc thời gian • Biểu đồ cột"
                :"Số hóa đơn có sử dụng từng dịch vụ • Tối đa 12 dịch vụ trên biểu đồ");
        lblMetricTitles[0].setText(type==0?"TỔNG DOANH THU":type==1?"KHÁCH HÀNG DUY NHẤT":"TỔNG LƯỢT DỊCH VỤ");
        lblMetricTitles[1].setText(type==2?"DỊCH VỤ PHÁT SINH":"MỐC THỜI GIAN");
        lblMetricTitles[2].setText(type==2?"LƯỢT CAO NHẤT":"GIÁ TRỊ CAO NHẤT");
        clear("Đang tải số liệu…"); btnView.setEnabled(false);
        new SwingWorker<StatisticsDAO.Result,Void>() {
            @Override protected StatisticsDAO.Result doInBackground() throws Exception { return StatisticsDAO.load(type,range[0],range[1],daily); }
            @Override protected void done() {
                if(version!=requestVersion) return;
                btnView.setEnabled(true);
                try {
                    StatisticsDAO.Result result=get();
                    List<Bucket> buckets=result.buckets();
                    lblMetricValues[0].setText(format(result.total(),type==0));
                    lblMetricValues[1].setText(NUMBER.format(buckets.size()));
                    double peak=buckets.stream().mapToDouble(Bucket::value).max().orElse(0);
                    lblMetricValues[2].setText(format(BigDecimal.valueOf(peak),type==0));
                    pnlChart.update(buckets,type,daily);
                    lblSummary.setText(DATE.format(range[0])+" – "+DATE.format(range[1])
                            +(buckets.isEmpty()?"  •  Không có dữ liệu trong kỳ đã chọn.":type==1?"  •  Tổng kỳ không cộng trùng khách giữa các mốc.":""));
                } catch(Exception ex) {
                    Throwable cause=ex.getCause()==null?ex:ex.getCause();
                    clear("Không tải được số liệu. Bạn có thể bấm Xem thống kê để thử lại.");
                    JOptionPane.showMessageDialog(ManHinhThongKe.this,cause.getMessage(),"Lỗi thống kê",JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    private void clear(String message) {
        for(JLabel lblValue:lblMetricValues) lblValue.setText("—");
        pnlChart.update(List.of(),cmbCategory.getSelectedIndex(),false); lblSummary.setText(message);
        btnView.setEnabled(true);
    }
    private static String format(BigDecimal value,boolean money) { return NUMBER.format(value)+(money?" đ":""); }

    private static final class Chart extends JPanel {
        @java.io.Serial private static final long serialVersionUID = 1L;
        private ArrayList<Bucket> data=new ArrayList<>();
        private int type;
        private boolean daily;
        Chart() { setBackground(Color.WHITE); setPreferredSize(new Dimension(800,410)); }
        void update(List<Bucket> buckets,int category,boolean days) {
            data=new ArrayList<>(buckets); type=category; daily=days;
            setPreferredSize(new Dimension(800,category==2?Math.max(380,Math.min(12,data.size())*36+40):410));
            revalidate(); repaint();
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(GiaoDien.font(12,false));
            if(data.isEmpty()) {
                g.setColor(GiaoDien.MUTED); String text="Chưa có số liệu để vẽ biểu đồ";
                g.drawString(text,Math.max(12,(getWidth()-g.getFontMetrics().stringWidth(text))/2),getHeight()/2); g.dispose(); return;
            }
            if(type==2) drawHorizontal(g); else drawTimeline(g);
            g.dispose();
        }
        private void drawHorizontal(Graphics2D g) {
            int count=Math.min(12,data.size()),left=Math.min(300,getWidth()/3),right=getWidth()-75;
            double max=Math.max(1,data.stream().mapToDouble(Bucket::value).max().orElse(1));
            int slot=Math.max(30,(getHeight()-24)/count);
            for(int i=0;i<count;i++) {
                Bucket item=data.get(i); int y=12+i*slot,barHeight=Math.min(24,slot-9);
                g.setColor(GiaoDien.TEXT); g.drawString(ellipsize(g,item.label(),left-18),0,y+17);
                g.setColor(new Color(0xEDF3F1)); g.fillRoundRect(left,y,Math.max(1,right-left),barHeight,10,10);
                int width=(int)((right-left)*item.value()/max);
                g.setPaint(new GradientPaint(left,y,GiaoDien.TEAL,right,y,GiaoDien.MINT));
                if(width>0) g.fillRoundRect(left,y,Math.max(2,width),barHeight,10,10);
                g.setColor(GiaoDien.NAV); g.drawString(NUMBER.format(item.value()),right+12,y+17);
            }
        }
        private void drawTimeline(Graphics2D g) {
            int left=82,right=Math.max(left+1,getWidth()-30),top=32,bottom=Math.max(top+1,getHeight()-62);
            int width=right-left,height=bottom-top;
            double max=data.stream().mapToDouble(Bucket::value).max().orElse(1);
            double axis=type==1?Math.max(4,Math.ceil(max/4)*4):Math.max(1,max*1.15);
            for(int i=0;i<=4;i++) {
                int y=bottom-i*height/4;
                g.setColor(new Color(0xE8EFED)); g.drawLine(left,y,right,y);
                g.setColor(GiaoDien.MUTED); String tick=compact(axis*i/4);
                g.drawString(tick,left-10-g.getFontMetrics().stringWidth(tick),y+4);
            }
            int count=data.size(),stride=Math.max(1,(int)Math.ceil(count/10.0));
            double slot=width/(double)count;
            int[] xs=new int[count],ys=new int[count];
            for(int i=0;i<count;i++) {
                xs[i]=left+(int)((i+.5)*slot); ys[i]=bottom-(int)(height*data.get(i).value()/axis);
                if(i%stride==0 || i==count-1) {
                    String raw=data.get(i).label();
                    String label=daily?raw.substring(8,10)+"/"+raw.substring(5,7):raw.substring(5,7)+"/"+raw.substring(0,4);
                    g.setColor(GiaoDien.MUTED); g.drawString(label,xs[i]-g.getFontMetrics().stringWidth(label)/2,bottom+24);
                }
            }
            if(type==0) {
                Path2D area=new Path2D.Double(); area.moveTo(xs[0],bottom);
                for(int i=0;i<count;i++) area.lineTo(xs[i],ys[i]);
                area.lineTo(xs[count-1],bottom); area.closePath();
                g.setPaint(new GradientPaint(0,top,new Color(0x83B4AE),0,bottom,new Color(0xF4F9F7))); g.fill(area);
                g.setColor(GiaoDien.TEAL); g.setStroke(new BasicStroke(3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                for(int i=1;i<count;i++) g.drawLine(xs[i-1],ys[i-1],xs[i],ys[i]);
                for(int i=0;i<count;i++) if(count<=35 || i%stride==0 || i==count-1) {
                    g.setColor(Color.WHITE); g.fillOval(xs[i]-4,ys[i]-4,8,8);
                    g.setColor(GiaoDien.TEAL); g.drawOval(xs[i]-4,ys[i]-4,8,8);
                }
            } else {
                int bar=Math.max(2,Math.min(44,(int)(slot*.55)));
                for(int i=0;i<count;i++) {
                    g.setPaint(new GradientPaint(0,ys[i],GiaoDien.TEAL,0,bottom,GiaoDien.MINT));
                    if(ys[i]<bottom) g.fillRoundRect(xs[i]-bar/2,ys[i],bar,bottom-ys[i],8,8);
                    if(count<=15) { g.setColor(GiaoDien.NAV); String value=NUMBER.format(data.get(i).value()); g.drawString(value,xs[i]-g.getFontMetrics().stringWidth(value)/2,ys[i]-9); }
                }
            }
        }
        private static String compact(double value) {
            if(value>=1_000_000_000) return String.format(Locale.forLanguageTag("vi-VN"),"%.1f tỷ",value/1_000_000_000);
            if(value>=1_000_000) return String.format(Locale.forLanguageTag("vi-VN"),"%.1f tr",value/1_000_000);
            if(value>=1_000) return String.format(Locale.forLanguageTag("vi-VN"),"%.0f nghìn",value/1_000);
            return NUMBER.format(value);
        }
        private static String ellipsize(Graphics2D g,String value,int width) {
            if(g.getFontMetrics().stringWidth(value)<=width) return value;
            int end=value.length();
            while(end>0 && g.getFontMetrics().stringWidth(value.substring(0,end)+"…")>width) end--;
            return value.substring(0,end)+"…";
        }
    }
}
