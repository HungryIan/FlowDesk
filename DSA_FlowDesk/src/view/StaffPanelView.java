package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Reservation;

/**
 * Staff panel view: lists queue entries with approve/remove and search.
 */
public class StaffPanelView extends JPanel {

    private final Color panelBg;
    private final Color inputBg;
    private final Color accentBlue;
    private final Color textColor;
    private final Color textSecondary;

    private JPanel queueManagementPanel;
    private JTextField searchField;

    private Consumer<String> onSearch;
    private Consumer<Reservation> onApprove;
    private Consumer<Reservation> onRemove;
    private Runnable onLogout;

    public StaffPanelView(
            Color panelBg,
            Color inputBg,
            Color accentBlue,
            Color textColor,
            Color textSecondary,
            Runnable onLogout,
            Consumer<String> onSearch,
            Consumer<Reservation> onApprove,
            Consumer<Reservation> onRemove) {
        this.panelBg = panelBg;
        this.inputBg = inputBg;
        this.accentBlue = accentBlue;
        this.textColor = textColor;
        this.textSecondary = textSecondary;
        this.onLogout = onLogout;
        this.onSearch = onSearch;
        this.onApprove = onApprove;
        this.onRemove = onRemove;

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTitleBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildTitleBar() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(panelBg);

        JLabel titleLabel = new JLabel("Staff Panel - Queue Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(200, 80, 80));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(80, 30));
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });
        titlePanel.add(logoutBtn, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        return titlePanel;
    }

    private JPanel buildContent() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(panelBg);

        JPanel searchSection = new JPanel(new BorderLayout(10, 10));
        searchSection.setBackground(panelBg);
        searchSection.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel searchLabel = new JLabel("Search Queue Position or Customer:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(textColor);
        searchLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        searchSection.add(searchLabel, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setBackground(panelBg);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(textColor);
        searchField.setBackground(inputBg);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        searchField.setCaretColor(textColor);
        searchField.setToolTipText("Search by name, contact, queue number, or room...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { fireSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { fireSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { fireSearch(); }
        });

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setForeground(textSecondary);
        clearBtn.setBackground(panelBg);
        clearBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            fireSearch();
        });

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(clearBtn, BorderLayout.EAST);
        searchSection.add(searchRow, BorderLayout.CENTER);
        container.add(searchSection, BorderLayout.NORTH);

        queueManagementPanel = new JPanel();
        queueManagementPanel.setLayout(new BoxLayout(queueManagementPanel, BoxLayout.Y_AXIS));
        queueManagementPanel.setBackground(panelBg);

        JScrollPane scrollPane = new JScrollPane(queueManagementPanel);
        scrollPane.setBackground(panelBg);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(panelBg);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void fireSearch() {
        if (onSearch != null) {
            onSearch.accept(searchField.getText().trim());
        }
    }

    public void renderQueue(List<Reservation> reservations, String searchQuery) {
        queueManagementPanel.removeAll();
        if (reservations == null || reservations.isEmpty()) {
            JLabel emptyLabel = new JLabel("Queue is empty");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(textSecondary);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            queueManagementPanel.add(emptyLabel);
        } else {
            int matchCount = 0;
            for (Reservation reservation : reservations) {
                if (reservation != null) {
                    queueManagementPanel.add(createStaffReservationEntry(reservation));
                    queueManagementPanel.add(Box.createVerticalStrut(10));
                    matchCount++;
                }
            }
            if (searchQuery != null && !searchQuery.isEmpty()) {
                JLabel resultsLabel = new JLabel("Found " + matchCount + " result(s)");
                resultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                resultsLabel.setForeground(textSecondary);
                resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
                queueManagementPanel.add(resultsLabel);
                queueManagementPanel.add(Box.createVerticalStrut(5));
            }
        }
        queueManagementPanel.revalidate();
        queueManagementPanel.repaint();
    }

    private JPanel createStaffReservationEntry(Reservation reservation) {
        JPanel entry = new JPanel(new BorderLayout(15, 0));
        Color bgColor = new Color(inputBg.getRed() + 10, inputBg.getGreen() + 10, inputBg.getBlue() + 15);
        entry.setBackground(bgColor);
        Color borderColor = accentBlue;
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(inputBg);

        JLabel nameLabel = new JLabel("Name: " + reservation.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(textColor);
        infoPanel.add(nameLabel);

        JLabel contactLabel = new JLabel("Contact: " + reservation.getContactNumber() + " | Age: " + reservation.getAge());
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactLabel.setForeground(textSecondary);
        infoPanel.add(contactLabel);

        JLabel roomLabel = new JLabel("Room: " + reservation.getRoom() + " | Time: " + reservation.getTimeSlot());
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomLabel.setForeground(textSecondary);
        infoPanel.add(roomLabel);

        JLabel queueLabel = new JLabel("Queue: Q-" + reservation.getQueueNumber() + " | Status: " + reservation.getStatus());
        queueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        queueLabel.setForeground(reservation.getStatus().equals("APPROVED") ?
            new Color(120, 200, 140) : textSecondary);
        infoPanel.add(queueLabel);

        entry.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(inputBg);

        if ("WAITING".equals(reservation.getStatus())) {
            JButton approveBtn = new JButton("Approve");
            approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            approveBtn.setForeground(Color.WHITE);
            approveBtn.setBackground(new Color(120, 200, 140));
            approveBtn.setBorderPainted(false);
            approveBtn.setFocusPainted(false);
            approveBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            approveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            approveBtn.addActionListener(e -> {
                if (onApprove != null) onApprove.accept(reservation);
            });
            buttonPanel.add(approveBtn);
            buttonPanel.add(Box.createVerticalStrut(5));
        }

        JButton removeBtn = new JButton("Remove");
        removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setBackground(new Color(200, 80, 80));
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.addActionListener(e -> {
            if (onRemove != null) onRemove.accept(reservation);
        });
        buttonPanel.add(removeBtn);

        entry.add(buttonPanel, BorderLayout.EAST);
        return entry;
    }
}

