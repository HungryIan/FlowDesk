import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FlowDesk extends JFrame {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel bottomPanel;
    
    // Navigation buttons
    private JButton userInfoBtn;
    private JButton queueStatusBtn;
    private JButton searchReserveBtn;
    private JButton staffBtn;
    
    // User Information fields
    private JTextField fullNameField;
    private JTextField contactNumberField;
    private JTextField ageField;
    
    // Reservation Queue panel
    private JPanel reservationQueuePanel;
    private SearchReservePanel searchReservePanel;
    
    // Notification bar
    private JPanel notificationBar;
    private JLabel notificationLabel;
    private javax.swing.Timer notificationTimer;
    
    // Bottom tabs
    private JButton recentTransactionsBtn;
    private JButton systemLogsBtn;
    private JPanel transactionsPanel;
    private JPanel logsPanel;
    
    // Data - Using Queue for FIFO principle
    private Queue<Reservation> reservationQueue;
    private List<Reservation> approvedReservations;
    private List<Transaction> transactions;
    private List<String> systemLogs;
    private String currentUserName;
    private String currentContactNumber;
    private int currentAge;
    private int nextQueueNumber;
    private boolean isStaffMode;
    private boolean isStaffLoggedIn;
    private static final String STAFF_PASSWORD = "staff123"; // Default password for demo
    
    // Colors
    private final Color DARK_BG = new Color(5, 5, 10);
    private final Color PANEL_BG = new Color(30, 50, 80);
    private final Color INPUT_BG = new Color(40, 65, 100);
    private final Color ACCENT_BLUE = new Color(70, 130, 200);
    private final Color SELECTED_BLUE = new Color(100, 150, 255);
    private final Color TEXT_COLOR = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(180, 180, 180);
    
    public FlowDesk() {
        reservationQueue = new LinkedList<>();
        approvedReservations = new ArrayList<>();
        transactions = new ArrayList<>();
        systemLogs = new ArrayList<>();
        nextQueueNumber = 1;
        isStaffMode = false;
        isStaffLoggedIn = false;
        currentUserName = "";
        currentContactNumber = "";
        currentAge = 0;
        
        // Initialize sample data
        initializeSampleData();
        
        setTitle("FlowDesk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        createNotificationBar();
        createHeader();
        createContentArea();
        createBottomPanel();
        
        // Fix layout - notification bar should be at top, header below it
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(DARK_BG);
        topPanel.add(notificationBar, BorderLayout.NORTH);
        topPanel.add(headerPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set User Information as default view
        showUserInformation();
    }
    
    private void initializeSampleData() {
        // Initialize with empty queue - no sample data
        addSystemLog("System initialized - Queue data structure ready");
        addSystemLog("Queue operations: Enqueue (add to rear), Dequeue (remove from front)");
    }
    
    private void addTransaction(String userName, String description) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(new Date());
        String time = timeFormat.format(new Date());
        String id = "T" + String.format("%03d", transactions.size() + 1);
        transactions.add(0, new Transaction(id, userName, description, date, time));
    }
    
    private void addSystemLog(String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        systemLogs.add(0, "[" + timestamp + "] " + message);
        // Keep only last 100 logs
        if (systemLogs.size() > 100) {
            systemLogs.remove(systemLogs.size() - 1);
        }
    }
    
    private void createNotificationBar() {
        notificationBar = new JPanel(new BorderLayout());
        notificationBar.setBackground(new Color(70, 130, 200)); // Accent blue background
        notificationBar.setBorder(new EmptyBorder(12, 20, 12, 20));
        notificationBar.setVisible(false); // Hidden by default
        
        notificationLabel = new JLabel();
        notificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        notificationLabel.setForeground(Color.WHITE);
        notificationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Close button
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(70, 130, 200));
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(30, 30));
        closeBtn.addActionListener(e -> {
            notificationBar.setVisible(false);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        
        notificationBar.add(notificationLabel, BorderLayout.CENTER);
        notificationBar.add(closeBtn, BorderLayout.EAST);
        
        // Create timer to check queue position every 2 seconds
        notificationTimer = new javax.swing.Timer(2000, e -> updateNotificationBar());
        notificationTimer.start();
    }
    
    private void updateNotificationBar() {
        if (currentUserName == null || currentUserName.isEmpty()) {
            notificationBar.setVisible(false);
            return;
        }
        
        Reservation userReservation = findUserReservation();
        if (userReservation == null) {
            notificationBar.setVisible(false);
            return;
        }
        
        int position = getQueuePosition(userReservation);
        boolean isFirst = position == 0;
        
        if (isFirst && userReservation.getStatus().equals("WAITING")) {
            notificationLabel.setText("🔔 IT'S YOUR TURN NOW! Please proceed to " + userReservation.getRoom() + " at " + userReservation.getTimeSlot());
            notificationBar.setBackground(new Color(76, 175, 80)); // Green for your turn
            notificationBar.setVisible(true);
        } else {
            notificationBar.setVisible(false);
        }
        
        // Force repaint if notification bar visibility changed
        if (notificationBar.isVisible()) {
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
    
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Title
        JLabel titleLabel = new JLabel("FlowDesk", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Navigation buttons - left aligned
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        navPanel.setBackground(DARK_BG);
        
        userInfoBtn = createNavButton("User Information");
        queueStatusBtn = createNavButton("My Queue Status");
        searchReserveBtn = createNavButton("Search & Reserve");
        staffBtn = createNavButton("Staff Panel");
        
        userInfoBtn.addActionListener(e -> showUserInformation());
        queueStatusBtn.addActionListener(e -> showQueueStatus());
        searchReserveBtn.addActionListener(e -> showSearchReserve());
        staffBtn.addActionListener(e -> toggleStaffMode());
        
        navPanel.add(userInfoBtn);
        navPanel.add(queueStatusBtn);
        navPanel.add(searchReserveBtn);
        navPanel.add(staffBtn);
        
        headerPanel.add(navPanel, BorderLayout.CENTER);
    }
    
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(PANEL_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.getBackground() != SELECTED_BLUE) {
                    btn.setBackground(new Color(PANEL_BG.getRed() + 15, PANEL_BG.getGreen() + 15, PANEL_BG.getBlue() + 20));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.getBackground() != SELECTED_BLUE) {
                    btn.setBackground(PANEL_BG);
                }
            }
        });
        
        return btn;
    }
    
    private void createContentArea() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(DARK_BG);
        contentPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
    }
    
    private void showUserInformation() {
        setNavButtonSelected(userInfoBtn);
        setNavButtonUnselected(queueStatusBtn);
        setNavButtonUnselected(searchReserveBtn);
        setNavButtonUnselected(staffBtn);
        isStaffMode = false;
        
        contentPanel.removeAll();
        
        // Left Panel - User Information
        JPanel userInfoPanel = createUserInformationPanel();
        
        // Right Panel - Reservation Queue
        JPanel queuePanel = createReservationQueuePanel();
        
        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setBackground(DARK_BG);
        centerPanel.add(userInfoPanel, BorderLayout.WEST);
        centerPanel.add(queuePanel, BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // Ensure queue panel is updated - the panel should now exist
        SwingUtilities.invokeLater(() -> {
            // Force refresh - panel should exist now since we just created it
            if (reservationQueuePanel != null) {
                populateReservationQueuePanel();
            }
        });
    }
    
    private void showQueueStatus() {
        setNavButtonUnselected(userInfoBtn);
        setNavButtonSelected(queueStatusBtn);
        setNavButtonUnselected(searchReserveBtn);
        setNavButtonUnselected(staffBtn);
        isStaffMode = false;
        
        contentPanel.removeAll();
        JPanel queueStatusPanel = createQueueStatusPanel();
        queueStatusPanel.setPreferredSize(new Dimension(1000, Integer.MAX_VALUE));
        
        contentPanel.add(queueStatusPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // Force refresh of queue status after panel is added
        SwingUtilities.invokeLater(() -> {
            if (queueStatusPanel != null) {
                queueStatusPanel.revalidate();
                queueStatusPanel.repaint();
                // Also refresh the content to ensure it shows current data
        contentPanel.revalidate();
        contentPanel.repaint();
            }
        });
    }
    
    private JPanel createQueueStatusPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("My Queue Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Content panel with scroll
        JPanel queueContent = new JPanel();
        queueContent.setLayout(new BoxLayout(queueContent, BoxLayout.Y_AXIS));
        queueContent.setBackground(DARK_BG);
        
        // Create card-style queue status panel
        JPanel queueCard = createQueueStatusCard();
        queueContent.add(Box.createVerticalGlue());
        queueContent.add(queueCard);
        queueContent.add(Box.createVerticalGlue());
        
        // Remove scrollbars - just add content directly
        mainPanel.add(queueContent, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createQueueStatusCard() {
        // Find user's reservation in queue
        Reservation userReservation = findUserReservation();
        
        // Main card panel with dark blue/black background
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (userReservation == null) {
            // Show helpful message
            JLabel noQueueLabel = new JLabel("You are not in the queue");
            noQueueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            noQueueLabel.setForeground(TEXT_SECONDARY);
            noQueueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(noQueueLabel);
            
            if (currentUserName != null && !currentUserName.isEmpty()) {
                JLabel hintLabel = new JLabel("Go to 'Search & Reserve' to join a queue");
                hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                hintLabel.setForeground(TEXT_SECONDARY);
                hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                hintLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
                card.add(hintLabel);
            } else {
                JLabel hintLabel = new JLabel("Please save your information first");
                hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                hintLabel.setForeground(TEXT_SECONDARY);
                hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                hintLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
                card.add(hintLabel);
            }
            return card;
        }
        
        // Header section (optional - can add title here if needed)
        // For now, we'll skip header and go straight to queue position
        
        // Calculate position in queue
        int position = getQueuePosition(userReservation);
        boolean isFirst = position == 0;
        
        // Large queue position number
        String positionText = (position + 1) + getOrdinalSuffix(position + 1);
        JLabel positionLabel = new JLabel(positionText);
        positionLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        positionLabel.setForeground(isFirst ? new Color(76, 175, 80) : ACCENT_BLUE); // Green if first, blue otherwise
        positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        positionLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        card.add(positionLabel);
        
        // Status text below position
        String statusText = isFirst ? "IT'S YOUR TURN NOW" : "POSITION IN QUEUE";
        JLabel statusTextLabel = new JLabel(statusText);
        statusTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusTextLabel.setForeground(isFirst ? new Color(76, 175, 80) : ACCENT_BLUE);
        statusTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusTextLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        card.add(statusTextLabel);
        
        // Dashed separator line
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
                
                // Left circle
                g2d.fillOval(0, centerY - circleRadius, circleRadius * 2, circleRadius * 2);
                // Right circle
                g2d.fillOval(getWidth() - circleRadius * 2, centerY - circleRadius, circleRadius * 2, circleRadius * 2);
                // Dashed line
                g2d.drawLine(circleRadius * 2, centerY, getWidth() - circleRadius * 2, centerY);
            }
        };
        separatorPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
        separatorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        separatorPanel.setBackground(PANEL_BG);
        card.add(separatorPanel);
        card.add(Box.createVerticalStrut(20));
        
        // Appointment details section
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(PANEL_BG);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Location and time
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        locationPanel.setBackground(PANEL_BG);
        locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel locationIcon = new JLabel("\uD83D\uDCCD");
        locationIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        locationPanel.add(locationIcon);
        
        String roomInfo = userReservation.getRoom();
        String timeSlot = userReservation.getTimeSlot();
        JLabel locationLabel = new JLabel(roomInfo + ", " + timeSlot);
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        locationLabel.setForeground(TEXT_COLOR);
        locationPanel.add(locationLabel);
        
        detailsPanel.add(locationPanel);
        detailsPanel.add(Box.createVerticalStrut(15));
        
        // Doctor/Staff info (using placeholder since we don't have doctor data)
        JPanel doctorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        doctorPanel.setBackground(PANEL_BG);
        doctorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel personIcon = new JLabel("\uD83D\uDC64");
        personIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        doctorPanel.add(personIcon);
        
        JLabel doctorLabel = new JLabel("Staff Member");
        doctorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        doctorLabel.setForeground(TEXT_COLOR);
        doctorPanel.add(doctorLabel);
        
        detailsPanel.add(doctorPanel);
        card.add(detailsPanel);
        
        // Cancel button at bottom
        if (userReservation.getStatus().equals("WAITING")) {
            card.add(Box.createVerticalStrut(30));
            JButton cancelBtn = new JButton("Cancel Reservation");
            cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBackground(new Color(200, 80, 80));
            cancelBtn.setBorderPainted(false);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.setPreferredSize(new Dimension(200, 40));
            cancelBtn.setMaximumSize(new Dimension(200, 40));
            cancelBtn.addActionListener(e -> cancelUserReservation());
            card.add(cancelBtn);
        }
        
        return card;
    }
    
    private Reservation findUserReservation() {
        if (currentUserName == null || currentUserName.isEmpty()) {
            return null;
        }
        
        // Search through the queue for matching name (case-sensitive exact match)
        for (Reservation r : reservationQueue) {
            if (r != null && r.getName() != null && r.getName().trim().equals(currentUserName.trim())) {
                return r;
            }
        }
        return null;
    }
    
    private int getQueuePosition(Reservation reservation) {
        int position = 0;
        for (Reservation r : reservationQueue) {
            if (r.getQueueNumber() < reservation.getQueueNumber() && r.getStatus().equals("WAITING")) {
                position++;
            }
        }
        return position;
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
    
    
    private void cancelUserReservation() {
        Reservation userReservation = findUserReservation();
        if (userReservation == null) {
            JOptionPane.showMessageDialog(this, "You are not in the queue!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel your reservation?", 
            "Cancel Reservation", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            reservationQueue.remove(userReservation);
            addTransaction(currentUserName, "Cancelled reservation for " + userReservation.getRoom());
            addSystemLog("REMOVE: " + currentUserName + " cancelled their reservation (Q-" + userReservation.getQueueNumber() + ")");
            
        SwingUtilities.invokeLater(() -> {
            updateReservationQueuePanel();
            updateLogsPanel();
            updateNotificationBar(); // Update notification bar
            showQueueStatus(); // Refresh view
        });
            JOptionPane.showMessageDialog(this, "Reservation cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    private void showSearchReserve() {
        setNavButtonUnselected(userInfoBtn);
        setNavButtonUnselected(queueStatusBtn);
        setNavButtonSelected(searchReserveBtn);
        setNavButtonUnselected(staffBtn);
        isStaffMode = false;
        
        contentPanel.removeAll();
        
        searchReservePanel = new SearchReservePanel(
            PANEL_BG,
            INPUT_BG,
            ACCENT_BLUE,
            SELECTED_BLUE,
            TEXT_COLOR,
            TEXT_SECONDARY,
            this::joinQueue
        );
        searchReservePanel.setPreferredSize(new Dimension(1000, Integer.MAX_VALUE));
        
        contentPanel.add(searchReservePanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    public void joinQueue(String[] seatInfo) {
        String roomCode = seatInfo[0];
        String building = seatInfo[1];
        String timeSlot = seatInfo[2];
        if (currentUserName.isEmpty() || currentContactNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in your information first in the User Information section!", 
                "Information Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if user is already in queue
        for (Reservation r : reservationQueue) {
            if (r != null && r.getName() != null && currentUserName != null) {
                if (r.getName().trim().equals(currentUserName.trim()) && r.getStatus().equals("WAITING")) {
                    JOptionPane.showMessageDialog(this, 
                        "You are already in the queue! Check 'My Queue Status' for details.", 
                        "Already in Queue", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
        
        // Create new reservation and add to queue (FIFO)
        Reservation newReservation = new Reservation(
            currentUserName,
            currentContactNumber,
            currentAge,
            roomCode,
            timeSlot,
            nextQueueNumber++
        );
        
        reservationQueue.offer(newReservation);
        
        // Decrease available seats for the reserved room
        if (searchReservePanel != null) {
            searchReservePanel.decreaseSeatAvailability(roomCode, timeSlot);
        }
        
        // Verify reservation was added
        System.out.println("DEBUG: Added reservation for " + currentUserName + " (Q-" + newReservation.getQueueNumber() + ")");
        System.out.println("DEBUG: Queue size is now: " + reservationQueue.size());
        
        addTransaction(currentUserName, "Joined queue for " + roomCode + " (" + building + ") at " + timeSlot);
        addSystemLog("ENQUEUE: " + currentUserName + " joined the queue (Q-" + newReservation.getQueueNumber() + ") for " + roomCode + " - Added to REAR");
        
        // Update logs immediately
        updateLogsPanel();
        
        // Show success message with option to view queue
        int option = JOptionPane.showOptionDialog(this, 
            "Successfully joined the queue!\n\nQueue Number: Q-" + newReservation.getQueueNumber() + 
            "\nRoom: " + roomCode + "\nTime: " + timeSlot + 
            "\n\nWould you like to view your reservation now?", 
            "Queue Joined", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"View Queue", "OK"},
            "View Queue");
        
        // If user wants to view, navigate to User Information to see the queue
        if (option == 0) {
            // Navigate to User Information which will show the queue
            showUserInformation();
        } else {
            // Still try to update the queue panel if it exists
        SwingUtilities.invokeLater(() -> {
            if (reservationQueuePanel != null) {
                populateReservationQueuePanel();
            }
            updateNotificationBar(); // Update notification bar when queue changes
        });
        }
    }
    
    private void toggleStaffMode() {
        if (!isStaffLoggedIn) {
            // Show login dialog
            if (showStaffLoginDialog()) {
                isStaffLoggedIn = true;
                isStaffMode = true;
                showStaffPanel();
                addSystemLog("Staff logged in");
                updateLogsPanel(); // Refresh logs panel to show content now that staff is logged in
                // If currently viewing system logs, refresh the view
                if (bottomPanel.getComponentCount() > 1) {
                    Component currentView = bottomPanel.getComponent(1);
                    if (currentView == logsPanel) {
                        showSystemLogs(); // Refresh the system logs view
                    }
                }
            } else {
                // Login failed or cancelled, don't switch to staff mode
                return;
            }
        } else {
            // Already logged in, toggle between staff panel and user view
            isStaffMode = !isStaffMode;
            if (isStaffMode) {
                showStaffPanel();
            } else {
                showUserInformation();
            }
        }
    }
    
    private boolean showStaffLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Staff Login", true);
        loginDialog.setSize(450, 350);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // Title
        JLabel titleLabel = new JLabel("Staff Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PANEL_BG);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setPreferredSize(new Dimension(350, 200));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(8));
        
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setBackground(INPUT_BG);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        usernameField.setCaretColor(TEXT_COLOR);
        usernameField.setPreferredSize(new Dimension(350, 35));
        usernameField.setMaximumSize(new Dimension(350, 35));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(8));
        
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setBackground(INPUT_BG);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        passwordField.setCaretColor(TEXT_COLOR);
        passwordField.setPreferredSize(new Dimension(350, 35));
        passwordField.setMaximumSize(new Dimension(350, 35));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(ACCENT_BLUE);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(100, 35));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setForeground(TEXT_COLOR);
        cancelBtn.setBackground(INPUT_BG);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final boolean[] loginResult = {false};
        
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Please enter both username and password!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Simple authentication (in real app, this would check against a database)
            if (password.equals(STAFF_PASSWORD)) {
                loginResult[0] = true;
                loginDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Invalid username or password!", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });
        
        cancelBtn.addActionListener(e -> {
            loginDialog.dispose();
        });
        
        // Allow Enter key to submit
        passwordField.addActionListener(e -> loginBtn.doClick());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);
        
        formPanel.add(buttonPanel);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Hint label
        JLabel hintLabel = new JLabel("<html><div style='text-align: center; color: #B4B4B4;'>Default password: staff123</div></html>");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        mainPanel.add(hintLabel, BorderLayout.SOUTH);
        
        loginDialog.add(mainPanel);
        loginDialog.setVisible(true);
        
        // Focus on username field when dialog opens
        SwingUtilities.invokeLater(() -> {
            usernameField.requestFocus();
        });
        
        return loginResult[0];
    }
    
    private void logoutStaff() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            isStaffLoggedIn = false;
            isStaffMode = false;
            addSystemLog("Staff logged out");
            updateLogsPanel(); // Refresh logs panel to show access denied message
            showUserInformation();
            // If currently viewing system logs, refresh the view to show access denied
            if (bottomPanel.getComponentCount() > 1) {
                Component currentView = bottomPanel.getComponent(1);
                if (currentView == logsPanel) {
                    showSystemLogs(); // Refresh the system logs view
                }
            }
            JOptionPane.showMessageDialog(this, 
                "Logged out successfully", 
                "Logout", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showStaffPanel() {
        setNavButtonUnselected(userInfoBtn);
        setNavButtonUnselected(queueStatusBtn);
        setNavButtonUnselected(searchReserveBtn);
        setNavButtonSelected(staffBtn);
        
        contentPanel.removeAll();
        
        JPanel staffPanel = createStaffPanel();
        staffPanel.setPreferredSize(new Dimension(1000, Integer.MAX_VALUE));
        
        contentPanel.add(staffPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel with logout button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("Staff Panel - Queue Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(200, 80, 80));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(80, 30));
        logoutBtn.addActionListener(e -> logoutStaff());
        titlePanel.add(logoutBtn, BorderLayout.EAST);
        
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Search section
        JPanel searchSection = new JPanel(new BorderLayout(10, 10));
        searchSection.setBackground(PANEL_BG);
        searchSection.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel searchLabel = new JLabel("Search Queue Position or Customer:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(TEXT_COLOR);
        searchLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        searchSection.add(searchLabel, BorderLayout.NORTH);
        
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setBackground(PANEL_BG);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_COLOR);
        searchField.setBackground(INPUT_BG);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        searchField.setCaretColor(TEXT_COLOR);
        searchField.setToolTipText("Search by name, contact, queue number, or room...");
        
        // Real-time search filtering
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterStaffQueue(searchField.getText().trim());
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterStaffQueue(searchField.getText().trim());
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterStaffQueue(searchField.getText().trim());
            }
        });
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBackground(PANEL_BG);
        clearBtn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterStaffQueue("");
        });
        
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(clearBtn, BorderLayout.EAST);
        searchSection.add(searchRow, BorderLayout.CENTER);
        
        panel.add(searchSection, BorderLayout.NORTH);
        
        // Queue management section
        JPanel queueManagementPanel = new JPanel();
        queueManagementPanel.setLayout(new BoxLayout(queueManagementPanel, BoxLayout.Y_AXIS));
        queueManagementPanel.setBackground(PANEL_BG);
        
        updateStaffQueuePanel(queueManagementPanel, "");
        
        JScrollPane scrollPane = new JScrollPane(queueManagementPanel);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateStaffQueuePanel(JPanel panel, String searchQuery) {
        panel.removeAll();
        
        if (reservationQueue.isEmpty()) {
            JLabel emptyLabel = new JLabel("Queue is empty");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(emptyLabel);
        } else {
            List<Reservation> queueList = new ArrayList<>(reservationQueue);
            String queryLower = searchQuery.toLowerCase().trim();
            boolean hasFilter = !queryLower.isEmpty();
            int matchCount = 0;
            
            for (Reservation reservation : queueList) {
                boolean matches = !hasFilter || 
                    reservation.getName().toLowerCase().contains(queryLower) ||
                    reservation.getContactNumber().contains(searchQuery) ||
                    String.valueOf(reservation.getQueueNumber()).contains(searchQuery) ||
                    reservation.getRoom().toLowerCase().contains(queryLower);
                
                if (matches) {
                    panel.add(createStaffReservationEntry(reservation, hasFilter));
                    panel.add(Box.createVerticalStrut(10));
                    matchCount++;
                }
            }
            
            if (hasFilter && matchCount == 0) {
                JLabel noResultsLabel = new JLabel("No matching results found");
                noResultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                noResultsLabel.setForeground(TEXT_SECONDARY);
                noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(noResultsLabel);
            } else if (hasFilter) {
                JLabel resultsLabel = new JLabel("Found " + matchCount + " result(s)");
                resultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                resultsLabel.setForeground(TEXT_SECONDARY);
                resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
                panel.add(resultsLabel);
                panel.add(Box.createVerticalStrut(5));
            }
        }
        
        panel.revalidate();
        panel.repaint();
    }
    
    private void filterStaffQueue(String query) {
        // Find the queue management panel in the staff panel and update it
        if (contentPanel.getComponentCount() > 0) {
            Component mainComp = contentPanel.getComponent(0);
            if (mainComp instanceof JPanel) {
                JPanel staffPanel = (JPanel) mainComp;
                // Find the scroll pane
                for (Component comp : staffPanel.getComponents()) {
                    if (comp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) comp;
                        Component viewport = scrollPane.getViewport().getView();
                        if (viewport instanceof JPanel) {
                            updateStaffQueuePanel((JPanel) viewport, query);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private JPanel createStaffReservationEntry(Reservation reservation, boolean isHighlighted) {
        JPanel entry = new JPanel(new BorderLayout(15, 0));
        Color bgColor = isHighlighted ? new Color(INPUT_BG.getRed() + 10, INPUT_BG.getGreen() + 10, INPUT_BG.getBlue() + 15) : INPUT_BG;
        entry.setBackground(bgColor);
        Color borderColor = isHighlighted ? ACCENT_BLUE : new Color(50, 80, 120);
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, isHighlighted ? 2 : 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Left side - Customer info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(INPUT_BG);
        
        JLabel nameLabel = new JLabel("Name: " + reservation.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        infoPanel.add(nameLabel);
        
        JLabel contactLabel = new JLabel("Contact: " + reservation.getContactNumber() + " | Age: " + reservation.getAge());
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactLabel.setForeground(TEXT_SECONDARY);
        infoPanel.add(contactLabel);
        
        JLabel roomLabel = new JLabel("Room: " + reservation.getRoom() + " | Time: " + reservation.getTimeSlot());
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomLabel.setForeground(TEXT_SECONDARY);
        infoPanel.add(roomLabel);
        
        JLabel queueLabel = new JLabel("Queue: Q-" + reservation.getQueueNumber() + " | Status: " + reservation.getStatus());
        queueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        queueLabel.setForeground(reservation.getStatus().equals("APPROVED") ? 
            new Color(120, 200, 140) : TEXT_SECONDARY);
        infoPanel.add(queueLabel);
        
        entry.add(infoPanel, BorderLayout.CENTER);
        
        // Right side - Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(INPUT_BG);
        
        if (reservation.getStatus().equals("WAITING")) {
            JButton approveBtn = new JButton("Approve");
            approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            approveBtn.setForeground(Color.WHITE);
            approveBtn.setBackground(new Color(120, 200, 140));
            approveBtn.setBorderPainted(false);
            approveBtn.setFocusPainted(false);
            approveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            approveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            approveBtn.addActionListener(e -> approveReservation(reservation));
            buttonPanel.add(approveBtn);
            buttonPanel.add(Box.createVerticalStrut(5));
        }
        
        JButton removeBtn = new JButton("Remove");
        removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setBackground(new Color(200, 80, 80));
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.addActionListener(e -> removeFromQueue(reservation));
        buttonPanel.add(removeBtn);
        
        entry.add(buttonPanel, BorderLayout.EAST);
        
        return entry;
    }
    
    private void approveReservation(Reservation reservation) {
        reservation.setStatus("APPROVED");
        approvedReservations.add(reservation);
        reservationQueue.remove(reservation);
        
        addTransaction(reservation.getName(), "Reservation approved for " + reservation.getRoom() + " at " + reservation.getTimeSlot());
        addSystemLog("DEQUEUE: Staff approved reservation Q-" + reservation.getQueueNumber() + " for " + reservation.getName() + " - Removed from FRONT");
        
        SwingUtilities.invokeLater(() -> {
            updateReservationQueuePanel();
            updateLogsPanel();
            updateNotificationBar(); // Update notification bar when reservation is approved
            // Refresh staff panel by finding and updating the queue panel
            filterStaffQueue(""); // Reset filter to show all
        });
        
        JOptionPane.showMessageDialog(this, 
            "Reservation approved for " + reservation.getName() + " (Q-" + reservation.getQueueNumber() + ")", 
            "Approved", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void removeFromQueue(Reservation reservation) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove " + reservation.getName() + " (Q-" + reservation.getQueueNumber() + ") from queue?", 
            "Remove from Queue", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            reservationQueue.remove(reservation);
            addTransaction(reservation.getName(), "Removed from queue by staff");
            addSystemLog("REMOVE: Staff removed " + reservation.getName() + " (Q-" + reservation.getQueueNumber() + ") from queue");
            
            SwingUtilities.invokeLater(() -> {
                updateReservationQueuePanel();
                updateLogsPanel();
                updateNotificationBar(); // Update notification bar when reservation is removed
                // Refresh staff panel by finding and updating the queue panel
                filterStaffQueue(""); // Reset filter to show all
            });
            
            JOptionPane.showMessageDialog(this, 
                "Customer removed from queue successfully", 
                "Removed", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    private JPanel createUserInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
        panel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        panel.setMinimumSize(new Dimension(500, Integer.MAX_VALUE));
        
        // Title
        JLabel titleLabel = new JLabel("User Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PANEL_BG);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Full Name field
        JPanel namePanel = createFormField("Full Name", "Enter your name", "");
        fullNameField = extractTextField(namePanel);
        fullNameField.setText("Ian Sia");
        namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Contact Number field
        JPanel contactPanel = createFormField("Contact Number", "Enter your contact number", "");
        contactNumberField = extractTextField(contactPanel);
        // No default value - user must enter their contact
        contactPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(contactPanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Age field
        JPanel agePanel = createFormField("Age", "Enter your age", "");
        ageField = extractTextField(agePanel);
        // No default value - user must enter their age
        agePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(agePanel);
        
        // Instruction text
        JLabel instructionLabel = new JLabel("<html><div style='text-align: center;'>Please fill in your information first, then search for available rooms to make a reservation.</div></html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructionLabel.setForeground(TEXT_SECONDARY);
        instructionLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(instructionLabel);
        
        // Save button
        JButton saveButton = new JButton("Save Information");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(ACCENT_BLUE);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(400, 40));
        saveButton.setMaximumSize(new Dimension(400, 40));
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            String name = fullNameField.getText().trim();
            String contact = contactNumberField.getText().trim();
            String ageStr = ageField.getText().trim();
            
            if (name.isEmpty() || contact.isEmpty() || ageStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 150) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid age!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                currentUserName = name;
                currentContactNumber = contact;
                currentAge = age;
                
                addTransaction(name, "Updated user information");
                addSystemLog("User information updated: " + name);
                
                // Update the reservation queue panel to show user info if they're in queue
        SwingUtilities.invokeLater(() -> {
            updateReservationQueuePanel();
            updateLogsPanel();
            updateNotificationBar(); // Update notification bar when user info changes
        });
        
        JOptionPane.showMessageDialog(this, 
            "Information saved successfully!\n\nName: " + name + 
            "\nContact: " + contact + 
            "\nAge: " + age, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        formPanel.add(saveButton);
        
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(PANEL_BG);
        centerWrapper.add(formPanel);
        panel.add(centerWrapper, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormField(String labelText, String placeholder, String icon) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(PANEL_BG);
        fieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Label - centered
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));
        
        // Input field - centered, no icon
        JTextField textField = new JTextField(placeholder);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setForeground(TEXT_COLOR);
        textField.setBackground(INPUT_BG);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setCaretColor(TEXT_COLOR);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setMaximumSize(new Dimension(400, textField.getPreferredSize().height));
        textField.setHorizontalAlignment(JTextField.CENTER);
        
        fieldPanel.add(textField);
        
        return fieldPanel;
    }
    
    private JTextField extractTextField(JPanel fieldPanel) {
        Component[] components = fieldPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        return null;
    }
    
    private JPanel createReservationQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(500, Integer.MAX_VALUE));
        panel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        panel.setMinimumSize(new Dimension(500, Integer.MAX_VALUE));
        
        // Title panel with refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("Reservation Queue");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        refreshBtn.setForeground(TEXT_COLOR);
        refreshBtn.setBackground(INPUT_BG);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(80, 25));
        refreshBtn.addActionListener(e -> {
            populateReservationQueuePanel();
        });
        titlePanel.add(refreshBtn, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Scrollable queue list
        reservationQueuePanel = new JPanel();
        reservationQueuePanel.setLayout(new BoxLayout(reservationQueuePanel, BoxLayout.Y_AXIS));
        reservationQueuePanel.setBackground(PANEL_BG);
        
        // Add reservation entries from queue (FIFO order) - populate immediately
        populateReservationQueuePanel();
        
        JScrollPane scrollPane = new JScrollPane(reservationQueuePanel);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 80, 120);
                this.trackColor = PANEL_BG;
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void populateReservationQueuePanel() {
        if (reservationQueuePanel == null) {
            return;
        }
        
        reservationQueuePanel.removeAll();
        
        // Convert queue to list for display (maintaining FIFO order)
        List<Reservation> queueList = new ArrayList<>(reservationQueue);
        
        if (queueList.isEmpty()) {
            JLabel emptyLabel = new JLabel("Queue is empty");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            reservationQueuePanel.add(emptyLabel);
        } else {
            for (Reservation reservation : queueList) {
                if (reservation != null) {
                    reservationQueuePanel.add(createReservationEntry(reservation));
                    reservationQueuePanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        
        reservationQueuePanel.revalidate();
        reservationQueuePanel.repaint();
        
        // Force parent to update
        Container parent = reservationQueuePanel.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
    
    private void updateReservationQueuePanel() {
        // Always try to find the panel first
        if (reservationQueuePanel == null) {
            findReservationQueuePanelInView();
        }
        
        if (reservationQueuePanel == null) {
            // Panel doesn't exist yet - this is OK, it will be created when User Information view is shown
            return;
        }
        
        // Use the populate method to update
        populateReservationQueuePanel();
        
        // Also update the parent scroll pane if it exists
        if (reservationQueuePanel.getParent() != null) {
            reservationQueuePanel.getParent().revalidate();
            reservationQueuePanel.getParent().repaint();
        }
    }
    
    private void findReservationQueuePanelInView() {
        if (contentPanel == null) return;
        
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                findReservationQueuePanel((JPanel) comp);
                if (reservationQueuePanel != null) return;
            }
        }
    }
    
    private void findReservationQueuePanel(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component viewport = scrollPane.getViewport().getView();
                if (viewport instanceof JPanel) {
                    JPanel innerPanel = (JPanel) viewport;
                    if (innerPanel.getLayout() instanceof BoxLayout) {
                        reservationQueuePanel = innerPanel;
                        return;
                    }
                }
            } else if (comp instanceof JPanel) {
                findReservationQueuePanel((JPanel) comp);
                if (reservationQueuePanel != null) return;
            }
        }
    }
    
    private JPanel createReservationEntry(Reservation reservation) {
        JPanel entry = new JPanel(new BorderLayout(15, 0));
        entry.setBackground(INPUT_BG);
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Left side - Icon and info
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setBackground(INPUT_BG);
        
        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        leftPanel.add(iconLabel, BorderLayout.WEST);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(INPUT_BG);
        
        JLabel nameLabel = new JLabel(reservation.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        infoPanel.add(nameLabel);
        
        JLabel roomLabel = new JLabel(reservation.getRoom() + " - " + reservation.getTimeSlot());
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roomLabel.setForeground(TEXT_SECONDARY);
        infoPanel.add(roomLabel);
        
        JLabel statusLabel = new JLabel("Status: " + reservation.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(reservation.getStatus().equals("APPROVED") ? 
            new Color(120, 200, 140) : TEXT_SECONDARY);
        infoPanel.add(statusLabel);
        
        leftPanel.add(infoPanel, BorderLayout.CENTER);
        entry.add(leftPanel, BorderLayout.CENTER);
        
        // Right side - Queue number
        JLabel queueLabel = new JLabel(String.valueOf(reservation.getQueueNumber()));
        queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        queueLabel.setForeground(ACCENT_BLUE);
        queueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(queueLabel, BorderLayout.EAST);
        
        return entry;
    }
    
    
    private void createBottomPanel() {
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(DARK_BG);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        bottomPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        bottomPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Tab buttons
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setBackground(DARK_BG);
        
        recentTransactionsBtn = createTabButton("Recent Transactions");
        systemLogsBtn = createTabButton("System Logs");
        
        recentTransactionsBtn.addActionListener(e -> showRecentTransactions());
        systemLogsBtn.addActionListener(e -> showSystemLogs());
        
        tabPanel.add(recentTransactionsBtn);
        tabPanel.add(systemLogsBtn);
        
        bottomPanel.add(tabPanel, BorderLayout.NORTH);
        
        // Content area
        transactionsPanel = createTransactionsPanel();
        logsPanel = createLogsPanel();
        
        bottomPanel.add(transactionsPanel, BorderLayout.CENTER);
        
        // Set Recent Transactions as default
        setTabButtonSelected(recentTransactionsBtn);
        setTabButtonUnselected(systemLogsBtn);
    }
    
    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(PANEL_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.getBackground() != SELECTED_BLUE) {
                    btn.setBackground(new Color(PANEL_BG.getRed() + 15, PANEL_BG.getGreen() + 15, PANEL_BG.getBlue() + 20));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.getBackground() != SELECTED_BLUE) {
                    btn.setBackground(PANEL_BG);
                }
            }
        });
        
        return btn;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(PANEL_BG);
        
        int index = 1;
        for (Transaction transaction : transactions) {
            listPanel.add(createTransactionEntry(transaction, index++));
            listPanel.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 80, 120);
                this.trackColor = PANEL_BG;
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTransactionEntry(Transaction transaction, int index) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
        entry.setBackground(INPUT_BG);
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        // First line: Index. Transaction ID | User
        JPanel firstLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        firstLine.setBackground(INPUT_BG);
        
        JLabel indexLabel = new JLabel(index + ". ");
        indexLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        indexLabel.setForeground(TEXT_COLOR);
        firstLine.add(indexLabel);
        
        JLabel idLabel = new JLabel("Transaction ID: " + transaction.getId() + " | User: " + transaction.getUserName());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idLabel.setForeground(TEXT_COLOR);
        firstLine.add(idLabel);
        
        entry.add(firstLine);
        entry.add(Box.createVerticalStrut(5));
        
        // Second line: Description
        JLabel descLabel = new JLabel(transaction.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        entry.add(descLabel);
        entry.add(Box.createVerticalStrut(5));
        
        // Third line: Date and Time
        JLabel dateLabel = new JLabel("Date: " + transaction.getDate() + " | Time: " + transaction.getTime());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_SECONDARY);
        entry.add(dateLabel);
        
        return entry;
    }
    
    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("System Logs" + (isStaffLoggedIn ? "" : " (Staff Only)"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel logsListPanel = new JPanel();
        logsListPanel.setLayout(new BoxLayout(logsListPanel, BoxLayout.Y_AXIS));
        logsListPanel.setBackground(PANEL_BG);
        
        updateLogsPanelContent(logsListPanel);
        
        JScrollPane scrollPane = new JScrollPane(logsListPanel);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PANEL_BG);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(50, 80, 120);
                this.trackColor = PANEL_BG;
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateLogsPanel() {
        if (logsPanel != null) {
            logsPanel.removeAll();
            JLabel titleLabel = new JLabel("System Logs" + (isStaffLoggedIn ? "" : " (Staff Only)"));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
            logsPanel.add(titleLabel, BorderLayout.NORTH);
            
            JPanel logsListPanel = new JPanel();
            logsListPanel.setLayout(new BoxLayout(logsListPanel, BoxLayout.Y_AXIS));
            logsListPanel.setBackground(PANEL_BG);
            
            updateLogsPanelContent(logsListPanel);
            
            JScrollPane scrollPane = new JScrollPane(logsListPanel);
            scrollPane.setBackground(PANEL_BG);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(PANEL_BG);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setBackground(PANEL_BG);
            scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new Color(50, 80, 100);
                    this.trackColor = PANEL_BG;
                }
            });
            
            logsPanel.add(scrollPane, BorderLayout.CENTER);
            logsPanel.revalidate();
            logsPanel.repaint();
        }
    }
    
    private void updateLogsPanelContent(JPanel logsListPanel) {
        logsListPanel.removeAll();
        
        // Only show logs if staff is logged in
        if (!isStaffLoggedIn) {
            JLabel accessDeniedLabel = new JLabel("Access Restricted: Please log in as staff to view system logs.");
            accessDeniedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            accessDeniedLabel.setForeground(new Color(200, 100, 100));
            accessDeniedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            logsListPanel.add(accessDeniedLabel);
            
            JLabel hintLabel = new JLabel("Click 'Staff Panel' in the navigation bar to log in.");
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hintLabel.setForeground(TEXT_SECONDARY);
            hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            hintLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            logsListPanel.add(hintLabel);
        } else if (systemLogs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No system logs yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            logsListPanel.add(emptyLabel);
        } else {
            for (String log : systemLogs) {
                JLabel logLabel = new JLabel(log);
                logLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                logLabel.setForeground(TEXT_SECONDARY);
                logLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                logLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
                logsListPanel.add(logLabel);
            }
        }
        
        logsListPanel.revalidate();
        logsListPanel.repaint();
    }
    
    private void showRecentTransactions() {
        setTabButtonSelected(recentTransactionsBtn);
        setTabButtonUnselected(systemLogsBtn);
        bottomPanel.remove(1);
        bottomPanel.add(transactionsPanel, BorderLayout.CENTER);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }
    
    private void showSystemLogs() {
        setTabButtonUnselected(recentTransactionsBtn);
        setTabButtonSelected(systemLogsBtn);
        bottomPanel.remove(1);
        
        // Check if staff is logged in
        if (!isStaffLoggedIn) {
            // Show access denied message
            JPanel accessDeniedPanel = new JPanel(new BorderLayout());
            accessDeniedPanel.setBackground(PANEL_BG);
            accessDeniedPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            JLabel accessDeniedLabel = new JLabel("Access Restricted: Please log in as staff to view system logs.");
            accessDeniedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            accessDeniedLabel.setForeground(new Color(200, 100, 100));
            accessDeniedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            accessDeniedPanel.add(accessDeniedLabel, BorderLayout.CENTER);
            
            JLabel hintLabel = new JLabel("Click 'Staff Panel' in the navigation bar to log in.");
            hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hintLabel.setForeground(TEXT_SECONDARY);
            hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
            hintLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            accessDeniedPanel.add(hintLabel, BorderLayout.SOUTH);
            
            bottomPanel.add(accessDeniedPanel, BorderLayout.CENTER);
        } else {
            bottomPanel.add(logsPanel, BorderLayout.CENTER);
        }
        
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }
    
    private void setNavButtonSelected(JButton btn) {
        btn.setBackground(SELECTED_BLUE);
        btn.setForeground(Color.WHITE);
    }
    
    private void setNavButtonUnselected(JButton btn) {
        btn.setBackground(PANEL_BG);
        btn.setForeground(TEXT_COLOR);
    }
    
    private void setTabButtonSelected(JButton btn) {
        btn.setBackground(SELECTED_BLUE);
        btn.setForeground(Color.WHITE);
    }
    
    private void setTabButtonUnselected(JButton btn) {
        btn.setBackground(PANEL_BG);
        btn.setForeground(TEXT_COLOR);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            FlowDesk app = new FlowDesk();
            app.setVisible(true);
        });
    }
}