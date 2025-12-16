package controller;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Reservation;
import model.Transaction;
import view.LogsPanelView;
import view.QueueStatusView;
import view.ReservationQueuePanelView;
import view.SearchReservePanel;
import view.StaffPanelView;
import view.TransactionsPanelView;
import view.UserInfoPanelView;
import view.UserInfoPanelView.UserInfo;

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
    
    // Reservation Queue panel
    private ReservationQueuePanelView reservationQueuePanelView;
    private UserInfoPanelView userInfoPanelView;
    private SearchReservePanel searchReservePanel;
    
    // Notification bar
    private JPanel notificationBar;
    private JLabel notificationLabel;
    private javax.swing.Timer notificationTimer;
    
    // Bottom tabs
    private JButton recentTransactionsBtn;
    private JButton systemLogsBtn;
    private TransactionsPanelView transactionsPanelView;
    private LogsPanelView logsPanelView;
    
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
        String[][] demoReservations = {
            {"Crishine Bangay", "09171234567", "21", "A-201", "10:00 - 12:00"},
            {"Evangeline Herondio", "09183456721", "22", "A-102", "14:00 - 16:00"},
            {"Nudo Christine", "09224567891", "24", "B-101", "13:00 - 15:00"},
            {"Roldan Torrejas", "09335678912", "23", "B-202", "09:00 - 11:00"},
            {"Ryan Ligasan", "09451234567", "27", "C-105", "08:00 - 10:00"}
        };
        
        for (String[] demo : demoReservations) {
            Reservation reservation = new Reservation(
                demo[0],
                demo[1],
                Integer.parseInt(demo[2]),
                demo[3],
                demo[4],
                nextQueueNumber++
            );
            reservationQueue.offer(reservation);
            addTransaction(reservation.getName(), "Joined queue for " + reservation.getRoom() + " at " + reservation.getTimeSlot());
            addSystemLog("ENQUEUE: Demo user " + reservation.getName() + " added to queue (Q-" + reservation.getQueueNumber() + ") for " + reservation.getRoom());
        }
        
        addSystemLog("System initialized - Queue preloaded with demo reservations");
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
        
        userInfoPanelView = new UserInfoPanelView(
            PANEL_BG, INPUT_BG, ACCENT_BLUE, TEXT_COLOR, TEXT_SECONDARY,
            info -> saveUserInfo(info)
        );
        userInfoPanelView.setInitialValues(currentUserName, currentContactNumber, currentAge);

        reservationQueuePanelView = new ReservationQueuePanelView(
            PANEL_BG, INPUT_BG, ACCENT_BLUE, TEXT_COLOR, TEXT_SECONDARY,
            this::updateReservationQueuePanelView,
            new ArrayList<>(reservationQueue)
        );
        
        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setBackground(DARK_BG);
        centerPanel.add(userInfoPanelView, BorderLayout.WEST);
        centerPanel.add(reservationQueuePanelView, BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        SwingUtilities.invokeLater(this::updateReservationQueuePanelView);
    }
    
    private void showQueueStatus() {
        setNavButtonUnselected(userInfoBtn);
        setNavButtonSelected(queueStatusBtn);
        setNavButtonUnselected(searchReserveBtn);
        setNavButtonUnselected(staffBtn);
        isStaffMode = false;
        
        contentPanel.removeAll();
        Reservation userReservation = findUserReservation();
        int position = userReservation == null ? -1 : getQueuePosition(userReservation);

        QueueStatusView queueStatusView = new QueueStatusView(
            PANEL_BG, ACCENT_BLUE, TEXT_COLOR, TEXT_SECONDARY,
            userReservation, position, this::cancelUserReservation
        );
        queueStatusView.setPreferredSize(new Dimension(1000, Integer.MAX_VALUE));

        contentPanel.add(queueStatusView, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
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
            refreshLogsView();
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
        
        // Check if seats are available before decreasing
        boolean hasAvailableSeats = false;
        if (searchReservePanel != null) {
            hasAvailableSeats = searchReservePanel.hasAvailableSeats(roomCode, timeSlot);
            // Only decrease availability if seats are available
            if (hasAvailableSeats) {
                searchReservePanel.decreaseSeatAvailability(roomCode, timeSlot);
            }
        }
        
        // Verify reservation was added
        System.out.println("DEBUG: Added reservation for " + currentUserName + " (Q-" + newReservation.getQueueNumber() + ")");
        System.out.println("DEBUG: Queue size is now: " + reservationQueue.size());
        
        String transactionDesc = hasAvailableSeats 
            ? "Reserved seat for " + roomCode + " (" + building + ") at " + timeSlot
            : "Joined waiting queue for " + roomCode + " (" + building + ") at " + timeSlot + " (Room is full)";
        addTransaction(currentUserName, transactionDesc);
        addSystemLog("ENQUEUE: " + currentUserName + " joined the queue (Q-" + newReservation.getQueueNumber() + ") for " + roomCode + " - Added to REAR" + (hasAvailableSeats ? "" : " (Waiting - room full)"));
        
        // Update logs immediately
        refreshLogsView();
        
        // Show success message with option to view queue
        String message = hasAvailableSeats
            ? "Successfully reserved a seat!\n\nQueue Number: Q-" + newReservation.getQueueNumber() + 
              "\nRoom: " + roomCode + "\nTime: " + timeSlot + 
              "\n\nWould you like to view your reservation now?"
            : "Successfully joined the waiting queue!\n\nQueue Number: Q-" + newReservation.getQueueNumber() + 
              "\nRoom: " + roomCode + "\nTime: " + timeSlot + 
              "\n\nNote: This room is currently full. You will be notified when a seat becomes available." +
              "\n\nWould you like to view your reservation now?";
        
        int option = JOptionPane.showOptionDialog(this, 
            message, 
            hasAvailableSeats ? "Seat Reserved" : "Joined Waiting Queue", 
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
            updateReservationQueuePanelView();
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
            logsPanelView.setLogs(isStaffLoggedIn, systemLogs); // Refresh logs panel
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
        logsPanelView.setLogs(isStaffLoggedIn, systemLogs); // Refresh logs panel to show access denied message
            showUserInformation();
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
        
        StaffPanelView staffPanel = new StaffPanelView(
            PANEL_BG, INPUT_BG, ACCENT_BLUE, TEXT_COLOR, TEXT_SECONDARY,
            this::logoutStaff,
            this::filterStaffQueue,
            this::approveReservation,
            this::removeFromQueue
        );
        staffPanel.setPreferredSize(new Dimension(1000, Integer.MAX_VALUE));
        // initial render
        staffPanel.renderQueue(new ArrayList<>(reservationQueue), "");
        
        contentPanel.add(staffPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void filterStaffQueue(String query) {
            List<Reservation> queueList = new ArrayList<>(reservationQueue);
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Reservation> filtered = new ArrayList<>();
        for (Reservation r : queueList) {
            boolean matches = q.isEmpty() ||
                r.getName().toLowerCase().contains(q) ||
                r.getContactNumber().contains(query) ||
                String.valueOf(r.getQueueNumber()).contains(query) ||
                r.getRoom().toLowerCase().contains(q);
            if (matches) filtered.add(r);
        }

        if (contentPanel.getComponentCount() > 0) {
            Component mainComp = contentPanel.getComponent(0);
            if (mainComp instanceof StaffPanelView) {
                ((StaffPanelView) mainComp).renderQueue(filtered, query);
            }
        }
    }
    
    private void approveReservation(Reservation reservation) {
        reservation.setStatus("APPROVED");
        approvedReservations.add(reservation);
        reservationQueue.remove(reservation);
        
        addTransaction(reservation.getName(), "Reservation approved for " + reservation.getRoom() + " at " + reservation.getTimeSlot());
        addSystemLog("DEQUEUE: Staff approved reservation Q-" + reservation.getQueueNumber() + " for " + reservation.getName() + " - Removed from FRONT");
        
        SwingUtilities.invokeLater(() -> {
            updateReservationQueuePanel();
            refreshLogsView();
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
                refreshLogsView();
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
    
    
    private void updateReservationQueuePanel() {
        updateReservationQueuePanelView();
    }

    private void updateReservationQueuePanelView() {
        if (reservationQueuePanelView != null) {
            reservationQueuePanelView.updateQueue(new ArrayList<>(reservationQueue));
        }
    }

    private void refreshLogsView() {
        if (logsPanelView != null) {
            logsPanelView.setLogs(isStaffLoggedIn, systemLogs);
        }
    }

    private void saveUserInfo(UserInfo info) {
        if (info == null) return;
        String name = info.name();
        String contact = info.contact();
        int age = info.age();
                
                currentUserName = name;
                currentContactNumber = contact;
                currentAge = age;
                
                addTransaction(name, "Updated user information");
                addSystemLog("User information updated: " + name);
                
        SwingUtilities.invokeLater(() -> {
            updateReservationQueuePanelView();
            refreshLogsView();
            updateNotificationBar();
        });
        
        JOptionPane.showMessageDialog(this, 
            "Information saved successfully!\n\nName: " + name + 
            "\nContact: " + contact + 
            "\nAge: " + age, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
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
        
        // Content area using new view panels
        transactionsPanelView = new TransactionsPanelView(PANEL_BG, INPUT_BG, TEXT_COLOR, TEXT_SECONDARY, transactions);
        logsPanelView = new LogsPanelView(PANEL_BG, TEXT_COLOR, TEXT_SECONDARY, isStaffLoggedIn, systemLogs);
        bottomPanel.add(transactionsPanelView, BorderLayout.CENTER);
        
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
    
    
    private void showRecentTransactions() {
        setTabButtonSelected(recentTransactionsBtn);
        setTabButtonUnselected(systemLogsBtn);
        bottomPanel.remove(1);
        transactionsPanelView.setTransactions(transactions);
        bottomPanel.add(transactionsPanelView, BorderLayout.CENTER);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }
    
    private void showSystemLogs() {
        setTabButtonUnselected(recentTransactionsBtn);
        setTabButtonSelected(systemLogsBtn);
        bottomPanel.remove(1);
        logsPanelView.setLogs(isStaffLoggedIn, systemLogs);
        bottomPanel.add(logsPanelView, BorderLayout.CENTER);
        
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