package com.mycompany.view.admin;

import java.awt.*;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class SimpleLineChart extends JPanel {

    private List<Object[]> data = new ArrayList<>();
    // Màu chủ đạo (Xanh dương đậm)
    private final Color LINE_COLOR = new Color(41, 128, 185); 
    // Màu tô nền (Xanh dương nhạt, trong suốt)
    private final Color FILL_COLOR_TOP = new Color(52, 152, 219, 150); 
    private final Color FILL_COLOR_BOTTOM = new Color(255, 255, 255, 50); 

    public SimpleLineChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 250)); // Kích thước mặc định
    }

    public void setData(List<Object[]> data) {
        this.data = data;
        repaint(); // Vẽ lại khi có dữ liệu
    }

    // Hàm rút gọn số tiền (VD: 150.2M)
    private String formatMoneyShort(double value) {
        if (value >= 1000000000) return String.format("%.1fB", value / 1000000000);
        if (value >= 1000000) return String.format("%.1fM", value / 1000000);
        if (value >= 1000) return String.format("%.0fK", value / 1000);
        return String.valueOf((int)value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Bật khử răng cưa cho đường nét mịn màng
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("Chưa có dữ liệu thống kê", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }

        // 1. Tính toán kích thước và tỉ lệ
        int width = getWidth();
        int height = getHeight();
        int padding = 50; // Lề xung quanh
        int usableWidth = width - 2 * padding;
        int usableHeight = height - 2 * padding;

        // Tìm giá trị lớn nhất để chia tỉ lệ trục Y
        double maxVal = 0;
        for (Object[] row : data) {
            maxVal = Math.max(maxVal, (Double) row[1]);
        }
        if (maxVal == 0) maxVal = 1; else maxVal = maxVal * 1.1; // Tăng đỉnh thêm 10% cho thoáng

        int numPoints = data.size();
        int stepX = usableWidth / (numPoints - 1); // Khoảng cách giữa các điểm trên trục X

        // Lưu tọa độ các điểm để vẽ
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            double value = (Double) data.get(i)[1];
            int x = padding + i * stepX;
            // Công thức tính Y: Đảo ngược vì trục Y của Java đi từ trên xuống
            int y = padding + usableHeight - (int)((value / maxVal) * usableHeight);
            points.add(new Point(x, y));
        }

        // 2. Vẽ vùng màu tô bên dưới đường (Area Fill)
        Path2D areaPath = new Path2D.Double();
        areaPath.moveTo(padding, padding + usableHeight); // Bắt đầu từ góc dưới trái
        for (Point p : points) {
            areaPath.lineTo(p.x, p.y); // Nối đến các điểm dữ liệu
        }
        areaPath.lineTo(padding + usableWidth, padding + usableHeight); // Kết thúc ở góc dưới phải
        areaPath.closePath();
        
        // Tạo màu gradient tô từ trên xuống dưới
        GradientPaint gradient = new GradientPaint(0, padding, FILL_COLOR_TOP, 0, height - padding, FILL_COLOR_BOTTOM);
        g2.setPaint(gradient);
        g2.fill(areaPath);

        // 3. Vẽ đường nối chính (Line)
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(3f)); // Độ dày nét vẽ = 3
        for (int i = 0; i < numPoints - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // 4. Vẽ các điểm tròn và Nhãn (Dots & Labels)
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < numPoints; i++) {
            Point p = points.get(i);
            String date = (String) data.get(i)[0];
            double value = (Double) data.get(i)[1];

            // Vẽ điểm tròn
            int dotSize = 10;
            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - dotSize/2, p.y - dotSize/2, dotSize, dotSize); // Nền trắng
            g2.setColor(LINE_COLOR);
            g2.drawOval(p.x - dotSize/2, p.y - dotSize/2, dotSize, dotSize); // Viền xanh

            // Vẽ nhãn số tiền bên trên điểm
            g2.setColor(Color.DARK_GRAY);
            String labelVal = formatMoneyShort(value);
            int labelWidth = fm.stringWidth(labelVal);
            g2.drawString(labelVal, p.x - labelWidth / 2, p.y - 12);

            // Vẽ nhãn ngày bên dưới trục hoành
            g2.setColor(Color.GRAY);
            int dateWidth = fm.stringWidth(date);
            g2.drawString(date, p.x - dateWidth / 2, height - padding + 25);
        }
        
        // Vẽ trục hoành mờ mờ cho đẹp
        g2.setColor(new Color(220, 220, 220));
        g2.drawLine(padding, height - padding, width - padding, height - padding);
    }
}