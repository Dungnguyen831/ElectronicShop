package com.mycompany.view.admin;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class SimpleBarChart extends JPanel {

    private List<Object[]> data = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,##0"); // Format số tiền (rút gọn)

    public SimpleBarChart() {
        setBackground(Color.WHITE);
        // Set chiều cao mặc định cho biểu đồ
        setPreferredSize(new Dimension(800, 250)); 
    }

    // Hàm nhận dữ liệu từ bên ngoài vào
    public void setData(List<Object[]> data) {
        this.data = data;
        repaint(); // Vẽ lại biểu đồ khi có dữ liệu mới
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Chuyển sang Graphics2D để vẽ đẹp hơn (khử răng cưa)
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2.drawString("Chưa có dữ liệu thống kê", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        // 1. Tính toán kích thước
        int width = getWidth();
        int height = getHeight();
        int padding = 40; // Khoảng cách lề
        int chartBottom = height - padding;
        int chartLeft = padding;
        
        // Tìm giá trị doanh thu lớn nhất để chia tỉ lệ chiều cao cột
        double maxVal = 0;
        for (Object[] row : data) {
            maxVal = Math.max(maxVal, (Double) row[1]);
        }
        if (maxVal == 0) maxVal = 1; // Tránh chia cho 0

        // 2. Vẽ các cột
        int numBars = data.size();
        // Tính độ rộng mỗi cột
        int barWidth = (width - 2 * padding) / (numBars + 1); 
        int gap = barWidth / 4; // Khoảng cách giữa các cột

        for (int i = 0; i < numBars; i++) {
            String date = (String) data.get(i)[0];
            double value = (Double) data.get(i)[1];

            // Tính chiều cao cột dựa trên tỉ lệ với maxVal
            int barHeight = (int) ((value / maxVal) * (chartBottom - padding - 20));
            
            // Tọa độ vẽ cột
            int x = chartLeft + (i * (barWidth + gap)) + gap;
            int y = chartBottom - barHeight;

            // --- Vẽ Cột ---
            // Gradient màu xanh dương đẹp mắt
            GradientPaint gp = new GradientPaint(x, y, new Color(52, 152, 219), x, chartBottom, new Color(41, 128, 185));
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10); // Vẽ cột bo tròn góc

            // --- Vẽ Số tiền trên đầu cột ---
            g2.setColor(Color.BLACK);
            FontMetrics fm = g2.getFontMetrics();
            String labelVal = formatMoneyShort(value); // Rút gọn số tiền (VD: 1tr, 500k)
            int labelWidth = fm.stringWidth(labelVal);
            g2.drawString(labelVal, x + (barWidth - labelWidth) / 2, y - 5);

            // --- Vẽ Ngày dưới chân cột ---
            g2.setColor(Color.GRAY);
            int dateWidth = fm.stringWidth(date);
            g2.drawString(date, x + (barWidth - dateWidth) / 2, chartBottom + 20);
        }

        // 3. Vẽ đường trục hoành (Kẻ ngang dưới cùng)
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(padding, chartBottom, width - padding, chartBottom);
    }

    // Hàm rút gọn số tiền hiển thị cho đỡ dài (Ví dụ: 1000000 -> 1M)
    private String formatMoneyShort(double value) {
        if (value >= 1000000000) return String.format("%.1fB", value / 1000000000);
        if (value >= 1000000) return String.format("%.1fM", value / 1000000);
        if (value >= 1000) return String.format("%.0fK", value / 1000);
        return String.valueOf((int)value);
    }
}