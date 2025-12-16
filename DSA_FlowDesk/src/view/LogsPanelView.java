package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Logs view-only panel. Renders staff-only message when not logged in.
 */
public class LogsPanelView extends JPanel {

    private final Color panelBg;
    private final Color textColor;
    private final Color textSecondary;

    private JPanel logsListPanel;

    public LogsPanelView(Color panelBg, Color textColor, Color textSecondary, boolean isStaffLoggedIn, List<String> systemLogs) {
        this.panelBg = panelBg;
        this.textColor = textColor;
        this.textSecondary = textSecondary;

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("System Logs" + (isStaffLoggedIn ? "" : " (Staff Only)"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        logsListPanel = new JPanel();
        logsListPanel.setLayout(new BoxLayout(logsListPanel, BoxLayout.Y_AXIS));
        logsListPanel.setBackground(panelBg);

        JScrollPane scrollPane = new JScrollPane(logsListPanel);
        scrollPane.setBackground(panelBg);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(panelBg);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(panelBg);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 80, 120);
                this.trackColor = panelBg;
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        setLogs(isStaffLoggedIn, systemLogs);
    }

    public void setLogs(boolean isStaffLoggedIn, List<String> systemLogs) {
        logsListPanel.removeAll();
        if (!isStaffLoggedIn) {
            JLabel accessDeniedLabel = new JLabel("Access Restricted: Please log in as staff to view system logs.");
            accessDeniedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            accessDeniedLabel.setForeground(new Color(200, 100, 100));
            accessDeniedLabel.setAlignmentX(LEFT_ALIGNMENT);
            logsListPanel.add(accessDeniedLabel);

            JLabel hintLabel = new JLabel("Click 'Staff Panel' in the navigation bar to log in.");
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hintLabel.setForeground(textSecondary);
            hintLabel.setAlignmentX(LEFT_ALIGNMENT);
            hintLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            logsListPanel.add(hintLabel);
        } else {
            List<String> logs = systemLogs == null ? new ArrayList<>() : systemLogs;
            if (logs.isEmpty()) {
                JLabel emptyLabel = new JLabel("No system logs yet");
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                emptyLabel.setForeground(textSecondary);
                emptyLabel.setAlignmentX(LEFT_ALIGNMENT);
                logsListPanel.add(emptyLabel);
            } else {
                for (String log : logs) {
                    JLabel logLabel = new JLabel(log);
                    logLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    logLabel.setForeground(textSecondary);
                    logLabel.setAlignmentX(LEFT_ALIGNMENT);
                    logLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
                    logsListPanel.add(logLabel);
                }
            }
        }
        logsListPanel.revalidate();
        logsListPanel.repaint();
    }
}

