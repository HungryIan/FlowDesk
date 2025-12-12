import java.awt.*;
import java.util.Queue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class QueueVisualizationPanel extends JPanel {
    private Queue<Reservation> queue;
    private JPanel visualizationPanel;
    private java.util.function.Consumer<String> logCallback;
    
    private final Color PANEL_BG;
    private final Color INPUT_BG;
    private final Color ACCENT_BLUE;
    private final Color TEXT_COLOR;
    private final Color TEXT_SECONDARY;
    
    public QueueVisualizationPanel(
            Queue<Reservation> queue,
            Color panelBg,
            Color inputBg,
            Color accentBlue,
            Color textColor,
            Color textSecondary,
            java.util.function.Consumer<String> logCallback
    ) {
        this.queue = queue;
        this.PANEL_BG = panelBg;
        this.INPUT_BG = inputBg;
        this.ACCENT_BLUE = accentBlue;
        this.TEXT_COLOR = textColor;
        this.TEXT_SECONDARY = textSecondary;
        this.logCallback = logCallback;
        
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        createVisualization();
    }
    
    private void createVisualization() {
        // Title
        JLabel titleLabel = new JLabel("Queue Data Structure Simulation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Visualization area
        visualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                drawQueue(g2d);
            }
        };
        visualizationPanel.setBackground(PANEL_BG);
        visualizationPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
        visualizationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JScrollPane scrollPane = new JScrollPane(visualizationPanel);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Operations panel
        JPanel operationsPanel = createOperationsPanel();
        add(operationsPanel, BorderLayout.SOUTH);
    }
    
    private void drawQueue(Graphics2D g2d) {
        int startX = 30;
        int startY = 80;
        int boxWidth = 100;
        int boxHeight = 60;
        int spacing = 20;
        int arrowLength = 30;
        
        // Draw queue label
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("FRONT (Dequeue) ->", startX, startY - 30);
        g2d.drawString("<- REAR (Enqueue)", startX + (queue.size() * (boxWidth + spacing)) - 50, startY - 30);
        
        if (queue.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.setColor(TEXT_SECONDARY);
            g2d.drawString("Queue is empty", startX + 50, startY + 35);
            return;
        }
        
        // Draw queue elements
        int index = 0;
        for (Reservation reservation : queue) {
            int x = startX + index * (boxWidth + spacing);
            int y = startY;
            
            // Draw box
            g2d.setColor(INPUT_BG);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            g2d.setColor(ACCENT_BLUE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            
            // Draw queue number
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.setColor(ACCENT_BLUE);
            g2d.drawString("Q-" + reservation.getQueueNumber(), x + 20, y + 25);
            
            // Draw name (truncated if too long)
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.setColor(TEXT_COLOR);
            String name = reservation.getName();
            if (name.length() > 10) {
                name = name.substring(0, 8) + "..";
            }
            g2d.drawString(name, x + 10, y + 45);
            
            // Draw arrow to next element (except for last)
            if (index < queue.size() - 1) {
                int arrowX = x + boxWidth;
                int arrowY = y + boxHeight / 2;
                
                g2d.setColor(ACCENT_BLUE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(arrowX, arrowY, arrowX + arrowLength, arrowY);
                
                // Arrowhead
                Polygon arrowHead = new Polygon();
                arrowHead.addPoint(arrowX + arrowLength, arrowY);
                arrowHead.addPoint(arrowX + arrowLength - 8, arrowY - 5);
                arrowHead.addPoint(arrowX + arrowLength - 8, arrowY + 5);
                g2d.fillPolygon(arrowHead);
            }
            
            index++;
        }
        
        // Update panel size based on queue size
        int totalWidth = startX + queue.size() * (boxWidth + spacing) + 50;
        visualizationPanel.setPreferredSize(new Dimension(Math.max(totalWidth, 600), 200));
        visualizationPanel.revalidate();
    }
    
    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Enqueue button
        JButton enqueueBtn = new JButton("Enqueue (Add to Rear)");
        enqueueBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        enqueueBtn.setForeground(Color.WHITE);
        enqueueBtn.setBackground(new Color(120, 200, 140));
        enqueueBtn.setBorderPainted(false);
        enqueueBtn.setFocusPainted(false);
        enqueueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enqueueBtn.setPreferredSize(new Dimension(180, 35));
        enqueueBtn.addActionListener(e -> {
            if (logCallback != null) {
                logCallback.accept("ENQUEUE Operation: New reservation added to the rear of the queue (FIFO)");
            }
            updateVisualization();
        });
        panel.add(enqueueBtn);
        
        // Dequeue button
        JButton dequeueBtn = new JButton("Dequeue (Remove from Front)");
        dequeueBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dequeueBtn.setForeground(Color.WHITE);
        dequeueBtn.setBackground(new Color(200, 80, 80));
        dequeueBtn.setBorderPainted(false);
        dequeueBtn.setFocusPainted(false);
        dequeueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dequeueBtn.setPreferredSize(new Dimension(200, 35));
        dequeueBtn.addActionListener(e -> {
            if (!queue.isEmpty()) {
                Reservation removed = queue.poll();
                if (logCallback != null) {
                    logCallback.accept("DEQUEUE Operation: Removed Q-" + removed.getQueueNumber() + " (" + removed.getName() + ") from the front of the queue (FIFO)");
                }
                updateVisualization();
            } else {
                if (logCallback != null) {
                    logCallback.accept("DEQUEUE Operation Failed: Queue is empty");
                }
            }
        });
        panel.add(dequeueBtn);
        
        // Display/Traverse button
        JButton displayBtn = new JButton("Display Queue (Traverse)");
        displayBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        displayBtn.setForeground(Color.WHITE);
        displayBtn.setBackground(ACCENT_BLUE);
        displayBtn.setBorderPainted(false);
        displayBtn.setFocusPainted(false);
        displayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        displayBtn.setPreferredSize(new Dimension(180, 35));
        displayBtn.addActionListener(e -> {
            if (queue.isEmpty()) {
                if (logCallback != null) {
                    logCallback.accept("DISPLAY Operation: Queue is empty");
                }
            } else {
                StringBuilder sb = new StringBuilder("DISPLAY Operation: Queue contents (FRONT to REAR): ");
                int index = 1;
                for (Reservation r : queue) {
                    sb.append(index).append(". Q-").append(r.getQueueNumber())
                      .append(" (").append(r.getName()).append(")");
                    if (index < queue.size()) sb.append(" -> ");
                    index++;
                }
                if (logCallback != null) {
                    logCallback.accept(sb.toString());
                }
            }
            updateVisualization();
        });
        panel.add(displayBtn);
        
        // Queue info label
        JLabel infoLabel = new JLabel("Queue Size: " + queue.size());
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_SECONDARY);
        panel.add(infoLabel);
        
        return panel;
    }
    
    public void updateVisualization() {
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
            
            // Update queue size label
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel opsPanel = (JPanel) comp;
                    Component[] opsComponents = opsPanel.getComponents();
                    for (Component opComp : opsComponents) {
                        if (opComp instanceof JLabel && ((JLabel) opComp).getText().startsWith("Queue Size:")) {
                            ((JLabel) opComp).setText("Queue Size: " + queue.size());
                        }
                    }
                }
            }
        }
    }
}

