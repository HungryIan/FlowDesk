package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Reservation;

/**
 * Reservation queue list view-only panel. Delegates refresh via provided runnable.
 */
public class ReservationQueuePanelView extends JPanel {

    private final Color panelBg;
    private final Color inputBg;
    private final Color accentBlue;
    private final Color textColor;
    private final Color textSecondary;

    private JPanel reservationQueuePanel;

    public ReservationQueuePanelView(
            Color panelBg,
            Color inputBg,
            Color accentBlue,
            Color textColor,
            Color textSecondary,
            Runnable onRefresh,
            List<Reservation> initialQueue) {
        this.panelBg = panelBg;
        this.inputBg = inputBg;
        this.accentBlue = accentBlue;
        this.textColor = textColor;
        this.textSecondary = textSecondary;

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
        setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(500, Integer.MAX_VALUE));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(panelBg);

        JLabel titleLabel = new JLabel("Reservation Queue");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textColor);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        refreshBtn.setForeground(textColor);
        refreshBtn.setBackground(inputBg);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(80, 25));
        refreshBtn.addActionListener(e -> {
            if (onRefresh != null) onRefresh.run();
        });
        titlePanel.add(refreshBtn, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titlePanel, BorderLayout.NORTH);

        reservationQueuePanel = new JPanel();
        reservationQueuePanel.setLayout(new BoxLayout(reservationQueuePanel, BoxLayout.Y_AXIS));
        reservationQueuePanel.setBackground(panelBg);

        JScrollPane scrollPane = new JScrollPane(reservationQueuePanel);
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

        updateQueue(initialQueue);
    }

    public void updateQueue(List<Reservation> queueList) {
        if (reservationQueuePanel == null) return;
        reservationQueuePanel.removeAll();

        List<Reservation> safeList = queueList == null ? new ArrayList<>() : queueList;
        if (safeList.isEmpty()) {
            JLabel emptyLabel = new JLabel("Queue is empty");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(textSecondary);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            reservationQueuePanel.add(emptyLabel);
        } else {
            int position = 1;
            for (Reservation reservation : safeList) {
                if (reservation != null) {
                    reservationQueuePanel.add(createReservationEntry(reservation, position++));
                    reservationQueuePanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        reservationQueuePanel.revalidate();
        reservationQueuePanel.repaint();
        var parent = reservationQueuePanel.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private JPanel createReservationEntry(Reservation reservation, int position) {
        JPanel entry = new JPanel(new BorderLayout(15, 0));
        entry.setBackground(inputBg);
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setBackground(inputBg);

        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        leftPanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(inputBg);

        JLabel nameLabel = new JLabel(reservation.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(textColor);
        infoPanel.add(nameLabel);

        JLabel roomLabel = new JLabel(reservation.getRoom() + " - " + reservation.getTimeSlot());
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomLabel.setForeground(textSecondary);
        infoPanel.add(roomLabel);

        JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(reservation.getStatus().equals("APPROVED") ?
            new Color(120, 200, 140) : textSecondary);
        infoPanel.add(statusLabel);

        leftPanel.add(infoPanel, BorderLayout.CENTER);
        entry.add(leftPanel, BorderLayout.CENTER);

        JLabel queueLabel = new JLabel(String.valueOf(position));
        queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        queueLabel.setForeground(accentBlue);
        queueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(queueLabel, BorderLayout.EAST);

        return entry;
    }
}

