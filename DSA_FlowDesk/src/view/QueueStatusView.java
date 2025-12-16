package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import model.Reservation;

/**
 * Queue status card view. Delegates cancel action via callback.
 */
public class QueueStatusView extends JPanel {

    private final Color panelBg;
    private final Color accentBlue;
    private final Color textColor;
    private final Color textSecondary;

    public QueueStatusView(
            Color panelBg,
            Color accentBlue,
            Color textColor,
            Color textSecondary,
            Reservation userReservation,
            int position,
            Runnable onCancel) {
        this.panelBg = panelBg;
        this.accentBlue = accentBlue;
        this.textColor = textColor;
        this.textSecondary = textSecondary;

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = createQueueStatusCard(userReservation, position, onCancel);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(panelBg);
        wrapper.add(Box.createVerticalGlue());
        wrapper.add(card);
        wrapper.add(Box.createVerticalGlue());
        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createQueueStatusCard(Reservation userReservation, int position, Runnable onCancel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(panelBg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (userReservation == null) {
            JLabel noQueueLabel = new JLabel("You are not in the queue");
            noQueueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            noQueueLabel.setForeground(textSecondary);
            noQueueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(noQueueLabel);

            JLabel hintLabel = new JLabel("Go to 'Search & Reserve' to join a queue");
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            hintLabel.setForeground(textSecondary);
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            hintLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
            card.add(hintLabel);
            return card;
        }

        boolean isFirst = position == 0;
        String positionText = (position + 1) + getOrdinalSuffix(position + 1);
        JLabel positionLabel = new JLabel(positionText);
        positionLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        positionLabel.setForeground(isFirst ? new Color(76, 175, 80) : accentBlue);
        positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        positionLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        card.add(positionLabel);

        String statusText = isFirst ? "IT'S YOUR TURN NOW" : "POSITION IN QUEUE";
        JLabel statusTextLabel = new JLabel(statusText);
        statusTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusTextLabel.setForeground(isFirst ? new Color(76, 175, 80) : accentBlue);
        statusTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusTextLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        card.add(statusTextLabel);

        card.add(Box.createVerticalStrut(10));
        JPanel separatorPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8}, 0));
                g2d.setColor(new Color(100, 130, 180));
                int centerY = getHeight() / 2;
                int circleRadius = 6;
                g2d.fillOval(0, centerY - circleRadius, circleRadius * 2, circleRadius * 2);
                g2d.fillOval(getWidth() - circleRadius * 2, centerY - circleRadius, circleRadius * 2, circleRadius * 2);
                g2d.drawLine(circleRadius * 2, centerY, getWidth() - circleRadius * 2, centerY);
            }
        };
        separatorPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
        separatorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        separatorPanel.setBackground(panelBg);
        card.add(separatorPanel);
        card.add(Box.createVerticalStrut(20));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(panelBg);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locationPanel.setBackground(panelBg);
        locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel locationIcon = new JLabel("\uD83D\uDCCD");
        locationIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        locationPanel.add(locationIcon);
        JLabel locationLabel = new JLabel(userReservation.getRoom() + ", " + userReservation.getTimeSlot());
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        locationLabel.setForeground(textColor);
        locationPanel.add(locationLabel);
        detailsPanel.add(locationPanel);
        detailsPanel.add(Box.createVerticalStrut(15));

        JPanel doctorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        doctorPanel.setBackground(panelBg);
        doctorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel personIcon = new JLabel("\uD83D\uDC64");
        personIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        doctorPanel.add(personIcon);
        JLabel doctorLabel = new JLabel("Staff Member");
        doctorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        doctorLabel.setForeground(textColor);
        doctorPanel.add(doctorLabel);
        detailsPanel.add(doctorPanel);
        card.add(detailsPanel);

        if ("WAITING".equals(userReservation.getStatus())) {
            card.add(Box.createVerticalStrut(30));
            JButton cancelBtn = new JButton("Cancel Reservation");
            cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBackground(new Color(200, 80, 80));
            cancelBtn.setBorderPainted(false);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            cancelBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            cancelBtn.setPreferredSize(new Dimension(200, 40));
            cancelBtn.setMaximumSize(new Dimension(200, 40));
            cancelBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel your reservation?",
                        "Cancel Reservation",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && onCancel != null) {
                    onCancel.run();
                }
            });
            card.add(cancelBtn);
        }

        return card;
    }

    private String getOrdinalSuffix(int number) {
        if (number >= 11 && number <= 13) {
            return "th";
        }
        switch (number % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}

