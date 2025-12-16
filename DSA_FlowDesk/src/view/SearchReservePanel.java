package view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import model.Seat;

public class SearchReservePanel extends JPanel {

    private final Color PANEL_BG;
    private final Color INPUT_BG;
    private final Color ACCENT_BLUE;
    private final Color SELECTED_BLUE;
    private final Color TEXT_COLOR;
    private final Color TEXT_SECONDARY;

    private JTextField searchField;
    private JPanel seatsListPanel;
    private List<Seat> allSeats;
    private JLabel resultsSummaryLabel;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> timeFilter;
    private java.util.function.Consumer<String[]> joinQueueCallback;

    public SearchReservePanel(
            Color panelBg,
            Color inputBg,
            Color accentBlue,
            Color selectedBlue,
            Color textColor,
            Color textSecondary,
            java.util.function.Consumer<String[]> joinQueueCallback
    ) {
        this.PANEL_BG = panelBg;
        this.INPUT_BG = inputBg;
        this.ACCENT_BLUE = accentBlue;
        this.SELECTED_BLUE = selectedBlue;
        this.TEXT_COLOR = textColor;
        this.TEXT_SECONDARY = textSecondary;
        this.joinQueueCallback = joinQueueCallback;

        setLayout(new BorderLayout());
        setBackground(new Color(5, 5, 10)); // Dark blue background like in image
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initializeSeatData();
        createHeader();
        createContent();
    }

    private void initializeSeatData() {
        allSeats = new ArrayList<>();

        allSeats.add(new Seat("A-201", "Main Building", "10:00 - 12:00", 10, 0, "PC • Airconditioned"));
        allSeats.add(new Seat("A-102", "Main Building", "14:00 - 16:00", 8, 0, "Silent Zone"));
        allSeats.add(new Seat("B-101", "Annex", "13:00 - 15:00", 6, 2, "Near Window"));
        allSeats.add(new Seat("B-202", "Annex", "09:00 - 11:00", 12, 4, "Group Study"));
        allSeats.add(new Seat("C-301", "Library Wing", "15:00 - 17:00", 20, 8, "PC • Projector"));
        allSeats.add(new Seat("C-105", "Library Wing", "08:00 - 10:00", 5, 0, "Silent Zone • Individual"));
    }

    private void createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(5, 5, 10)); // Dark blue background

        // Breadcrumb-style label
        JLabel breadcrumb = new JLabel("FLOWDESK / RESERVATIONS");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        breadcrumb.setForeground(TEXT_SECONDARY);
        breadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);
        breadcrumb.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(breadcrumb);

        JLabel titleLabel = new JLabel("Search & Reserve");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Search rooms, refine by building or time, and reserve an available seat.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.add(subtitleLabel);

        // Search row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setBackground(new Color(5, 5, 10)); // Dark blue background
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setBackground(new Color(5, 5, 10)); // Dark blue background

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_COLOR);
        searchField.setBackground(INPUT_BG);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        searchField.setCaretColor(TEXT_COLOR);
        searchField.setToolTipText("Search rooms, buildings, or features...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterSeats();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterSeats();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterSeats();
            }
        });

        searchContainer.add(searchField, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearButton.setForeground(TEXT_SECONDARY);
        clearButton.setBackground(PANEL_BG);
        clearButton.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterSeats();
        });
        searchContainer.add(clearButton, BorderLayout.EAST);

        searchRow.add(searchContainer, BorderLayout.CENTER);

        header.add(searchRow);

        // Filter row: building + time, neatly aligned with proper spacing
        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        filtersRow.setBackground(new Color(5, 5, 10)); // Dark blue background
        filtersRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtersRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Building filter group
        JPanel buildingGroup = new JPanel();
        buildingGroup.setLayout(new BoxLayout(buildingGroup, BoxLayout.Y_AXIS));
        buildingGroup.setBackground(new Color(5, 5, 10)); // Dark blue background
        buildingGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel buildingLabel = new JLabel("Building");
        buildingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        buildingLabel.setForeground(TEXT_SECONDARY);
        buildingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildingGroup.add(buildingLabel);
        buildingGroup.add(Box.createVerticalStrut(4));

        buildingFilter = new JComboBox<>(new String[]{"All", "Main Building", "Annex", "Library Wing"});
        buildingFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        buildingFilter.setBackground(INPUT_BG);
        buildingFilter.setForeground(TEXT_COLOR);
        buildingFilter.setMaximumSize(new Dimension(180, buildingFilter.getPreferredSize().height));
        buildingFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildingFilter.setOpaque(true);
        buildingFilter.setEditable(true); // Make editable so we can control the editor
        buildingFilter.addActionListener(e -> filterSeats());
        
        // Custom editor with visible text
        buildingFilter.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField editor = new JTextField("", 9);
                editor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                editor.setForeground(TEXT_COLOR);
                editor.setBackground(INPUT_BG);
                editor.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                editor.setEditable(false); // Read-only
                return editor;
            }
        });
        
        // Custom renderer for dropdown items
        buildingFilter.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
                label.setBackground(isSelected ? ACCENT_BLUE : INPUT_BG);
                label.setOpaque(true);
                return label;
            }
        });
        
        // Custom UI to style the arrow button
        buildingFilter.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setBackground(INPUT_BG);
                button.setForeground(TEXT_COLOR);
                return button;
            }
        });
        
        buildingGroup.add(buildingFilter);
        
        filtersRow.add(buildingGroup);

        // Time filter group
        JPanel timeGroup = new JPanel();
        timeGroup.setLayout(new BoxLayout(timeGroup, BoxLayout.Y_AXIS));
        timeGroup.setBackground(new Color(5, 5, 10)); // Dark blue background
        timeGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel timeLabel = new JLabel("Time slot");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(TEXT_SECONDARY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeGroup.add(timeLabel);
        timeGroup.add(Box.createVerticalStrut(4));

        timeFilter = new JComboBox<>(new String[]{
                "All",
                "08:00 - 10:00",
                "09:00 - 11:00",
                "10:00 - 12:00",
                "13:00 - 15:00",
                "14:00 - 16:00",
                "15:00 - 17:00"
        });
        timeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeFilter.setBackground(INPUT_BG);
        timeFilter.setForeground(TEXT_COLOR);
        timeFilter.setMaximumSize(new Dimension(180, timeFilter.getPreferredSize().height));
        timeFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeFilter.setOpaque(true);
        timeFilter.setEditable(true); // Make editable so we can control the editor
        timeFilter.addActionListener(e -> filterSeats());
        
        // Custom editor with visible text
        timeFilter.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField editor = new JTextField("", 9);
                editor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                editor.setForeground(TEXT_COLOR);
                editor.setBackground(INPUT_BG);
                editor.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
                editor.setEditable(false); // Read-only
                return editor;
            }
        });
        
        // Custom renderer for dropdown items
        timeFilter.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
                label.setBackground(isSelected ? ACCENT_BLUE : INPUT_BG);
                label.setOpaque(true);
                return label;
            }
        });
        
        // Custom UI to style the arrow button
        timeFilter.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setBackground(INPUT_BG);
                button.setForeground(TEXT_COLOR);
                return button;
            }
        });
        
        timeGroup.add(timeFilter);
        
        filtersRow.add(timeGroup);

        header.add(Box.createVerticalStrut(8));
        header.add(filtersRow);

        // Results summary under the filters
        resultsSummaryLabel = new JLabel();
        resultsSummaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resultsSummaryLabel.setForeground(TEXT_SECONDARY);
        resultsSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsSummaryLabel.setBorder(new EmptyBorder(6, 2, 0, 0));
        header.add(resultsSummaryLabel);

        add(header, BorderLayout.NORTH);
    }

    private void createContent() {
        seatsListPanel = new JPanel();
        seatsListPanel.setLayout(new java.awt.GridLayout(0, 2, 15, 15)); // 2 columns with spacing
        seatsListPanel.setBackground(new Color(5, 5, 10)); // Dark blue background
        seatsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        seatsListPanel.setOpaque(true); // Ensure panel is opaque

        JScrollPane scrollPane = new JScrollPane(seatsListPanel);
        scrollPane.setBackground(new Color(5, 5, 10)); // Dark blue background
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(5, 5, 10)); // Dark blue background
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 80, 120);
                this.trackColor = new Color(5, 5, 10); // Dark blue background
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // Populate seats immediately - ensure this happens
        SwingUtilities.invokeLater(() -> {
            if (allSeats == null || allSeats.isEmpty()) {
                initializeSeatData();
            }
            populateSeatCards(allSeats);
        });
    }

    private void populateSeatCards(List<Seat> seats) {
        if (seatsListPanel == null) {
            return;
        }
        
        seatsListPanel.removeAll();

        if (seats == null || seats.isEmpty()) {
            // For empty state, switch back to vertical layout
            seatsListPanel.setLayout(new BoxLayout(seatsListPanel, BoxLayout.Y_AXIS));
            JLabel emptyLabel = new JLabel("No seats available.");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(180, 180, 180));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(10, 5, 0, 0));
            seatsListPanel.add(emptyLabel);
        } else {
            // Use grid layout for cards (2 columns)
            seatsListPanel.setLayout(new java.awt.GridLayout(0, 2, 15, 15));
            for (Seat seat : seats) {
                if (seat != null) {
                    JPanel card = createSeatCard(seat);
                    if (card != null) {
                        seatsListPanel.add(card);
                    }
                }
            }
        }

        seatsListPanel.revalidate();
        seatsListPanel.repaint();
        
        // Force the scroll pane to update
        Container parent = seatsListPanel.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }

        updateResultsSummary(seats != null ? seats.size() : 0);
    }

    private JPanel createSeatCard(Seat seat) {
        // Light blue-grey card background with rounded corners
        Color cardBg = new Color(220, 230, 240); // Light blue-grey
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setOpaque(true);
        card.setPreferredSize(new Dimension(900, 180));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Header section: Room code (bold, larger) and building name below
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(cardBg);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel roomCodeLabel = new JLabel(seat.getRoomCode());
        roomCodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        roomCodeLabel.setForeground(new Color(30, 50, 80));
        roomCodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(roomCodeLabel);
        
        JLabel buildingLabel = new JLabel(seat.getBuilding());
        buildingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        buildingLabel.setForeground(new Color(100, 120, 140));
        buildingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildingLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
        headerPanel.add(buildingLabel);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout(20, 10));
        contentPanel.setBackground(cardBg);
        
        // Left side details
        JPanel leftDetailsPanel = new JPanel();
        leftDetailsPanel.setLayout(new BoxLayout(leftDetailsPanel, BoxLayout.Y_AXIS));
        leftDetailsPanel.setBackground(cardBg);
        leftDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Seats info with icon
        JPanel seatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        seatsPanel.setBackground(cardBg);
        JLabel seatIcon = new JLabel("🪑");
        seatIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        seatsPanel.add(seatIcon);
        JLabel seatsLabel = new JLabel("Seats: " + seat.getAvailableSeats() + "/" + seat.getCapacity());
        seatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        seatsLabel.setForeground(new Color(50, 70, 90));
        seatsPanel.add(seatsLabel);
        leftDetailsPanel.add(seatsPanel);
        leftDetailsPanel.add(Box.createVerticalStrut(8));
        
        // Feature info
        String[] features = seat.getFeatures().split("•");
        if (features.length > 0) {
            JLabel featureLabel = new JLabel("Feature: " + features[0].trim());
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            featureLabel.setForeground(new Color(50, 70, 90));
            featureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftDetailsPanel.add(featureLabel);
            leftDetailsPanel.add(Box.createVerticalStrut(8));
        }
        
        // PC info (if present)
        boolean hasPC = seat.getFeatures().toLowerCase().contains("pc");
        if (hasPC) {
            JLabel pcLabel = new JLabel("PC");
            pcLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            pcLabel.setForeground(new Color(50, 70, 90));
            pcLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftDetailsPanel.add(pcLabel);
        }
        
        contentPanel.add(leftDetailsPanel, BorderLayout.WEST);
        
        // Right side: Icon + feature text
        JPanel rightDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rightDetailsPanel.setBackground(cardBg);
        JLabel pcIcon = new JLabel("💻");
        pcIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        rightDetailsPanel.add(pcIcon);
        String rightFeatureText = seat.getFeatures().replace("•", " ");
        JLabel rightFeatureLabel = new JLabel(rightFeatureText);
        rightFeatureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rightFeatureLabel.setForeground(new Color(50, 70, 90));
        rightDetailsPanel.add(rightFeatureLabel);
        contentPanel.add(rightDetailsPanel, BorderLayout.CENTER);
        
        // Reserve button at bottom right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(cardBg);
        
        boolean isFull = seat.getAvailableSeats() <= 0;
        String buttonText = isFull ? "Join Queue" : "Reserve";
        
        JButton reserveButton = new JButton(buttonText);
        reserveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        reserveButton.setForeground(Color.WHITE);
        reserveButton.setBackground(ACCENT_BLUE);
        reserveButton.setBorderPainted(false);
        reserveButton.setFocusPainted(false);
        reserveButton.setOpaque(true);
        reserveButton.setContentAreaFilled(true);
        reserveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reserveButton.setPreferredSize(new Dimension(120, 40));
        reserveButton.setEnabled(true); // Always enabled - users can join queue even when full
        
        // Rounded corners for button
        reserveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        // Hover effect - always enabled
        reserveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                reserveButton.setBackground(new Color(
                    Math.min(ACCENT_BLUE.getRed() + 20, 255),
                    Math.min(ACCENT_BLUE.getGreen() + 20, 255),
                    Math.min(ACCENT_BLUE.getBlue() + 20, 255)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                reserveButton.setBackground(ACCENT_BLUE);
            }
        });

        reserveButton.addActionListener(e -> {
            // Always allow joining queue, even when seats are full
            if (joinQueueCallback != null) {
                joinQueueCallback.accept(new String[]{seat.getRoomCode(), seat.getBuilding(), seat.getTimeSlot()});
            } else {
                String message = isFull 
                    ? "Room " + seat.getRoomCode() + " (" + seat.getBuilding() + ") at " + seat.getTimeSlot() + 
                      " is currently full. You will be added to the waiting queue."
                    : "You selected room " + seat.getRoomCode() +
                      " (" + seat.getBuilding() + ") at " + seat.getTimeSlot() + ".\n" +
                      "This is a demo reservation action. In a full system, this would place you in the queue.";
                JOptionPane.showMessageDialog(
                        SearchReservePanel.this,
                        message,
                        isFull ? "Join Queue" : "Reserve Seat",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        buttonPanel.add(reserveButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Hover effect for card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(
                        Math.min(cardBg.getRed() + 5, 255),
                        Math.min(cardBg.getGreen() + 5, 255),
                        Math.min(cardBg.getBlue() + 5, 255)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(cardBg);
            }
        });
        
        card.revalidate();
        card.repaint();
        
        return card;
    }

    private void updateResultsSummary(int count) {
        if (resultsSummaryLabel == null) {
            return;
        }
        String text = count == 1
                ? "Showing 1 available option"
                : "Showing " + count + " available options";
        resultsSummaryLabel.setText(text);
    }

    private void filterSeats() {
        String query = searchField.getText();
        if (query == null) {
            query = "";
        }
        String normalized = query.trim().toLowerCase();

        List<Seat> filtered = new ArrayList<>();
        for (Seat seat : allSeats) {
            if (!normalized.isEmpty() && !matchesQuery(seat, normalized)) {
                continue;
            }

            // Building filter
            if (buildingFilter != null) {
                String buildingSelection = (String) buildingFilter.getSelectedItem();
                if (buildingSelection != null && !"All".equals(buildingSelection)) {
                    if (!seat.getBuilding().equalsIgnoreCase(buildingSelection)) {
                        continue;
                    }
                }
            }

            // Time filter
            if (timeFilter != null) {
                String timeSelection = (String) timeFilter.getSelectedItem();
                if (timeSelection != null && !"All".equals(timeSelection)) {
                    if (!seat.getTimeSlot().equalsIgnoreCase(timeSelection)) {
                        continue;
                    }
                }
            }

            filtered.add(seat);
        }

        populateSeatCards(filtered);
    }

    private boolean matchesQuery(Seat seat, String query) {
        return seat.getRoomCode().toLowerCase().contains(query) ||
                seat.getBuilding().toLowerCase().contains(query) ||
                seat.getTimeSlot().toLowerCase().contains(query) ||
                seat.getFeatures().toLowerCase().contains(query);
    }
    
    public void decreaseSeatAvailability(String roomCode, String timeSlot) {
        // Find the matching seat and decrease availability
        for (Seat seat : allSeats) {
            if (seat.getRoomCode().equals(roomCode) && seat.getTimeSlot().equals(timeSlot)) {
                seat.decreaseAvailableSeats();
                // Refresh the display
                filterSeats();
                break;
            }
        }
    }
    
    public boolean hasAvailableSeats(String roomCode, String timeSlot) {
        // Find the matching seat and check availability
        for (Seat seat : allSeats) {
            if (seat.getRoomCode().equals(roomCode) && seat.getTimeSlot().equals(timeSlot)) {
                return seat.getAvailableSeats() > 0;
            }
        }
        return false;
    }
}



