package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * User information entry panel (view-only). Delegates save action via callback.
 */
public class UserInfoPanelView extends JPanel {

    private final Color panelBg;
    private final Color inputBg;
    private final Color accentBlue;
    private final Color textColor;
    private final Color textSecondary;

    private JTextField fullNameField;
    private JTextField contactNumberField;
    private JTextField ageField;

    public UserInfoPanelView(
            Color panelBg,
            Color inputBg,
            Color accentBlue,
            Color textColor,
            Color textSecondary,
            Consumer<UserInfo> onSave) {
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

        JLabel titleLabel = new JLabel("User Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textColor);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(panelBg);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel namePanel = createFormField("Full Name", "Enter your name");
        fullNameField = extractTextField(namePanel);
        namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(20));

        JPanel contactPanel = createFormField("Contact Number", "Enter your contact number");
        contactNumberField = extractTextField(contactPanel);
        contactPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(contactPanel);
        formPanel.add(Box.createVerticalStrut(20));

        JPanel agePanel = createFormField("Age", "Enter your age");
        ageField = extractTextField(agePanel);
        agePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(agePanel);

        JLabel instructionLabel = new JLabel("<html><div style='text-align: center;'>Please fill in your information first, then search for available rooms to make a reservation.</div></html>");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructionLabel.setForeground(textSecondary);
        instructionLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(instructionLabel);

        JButton saveButton = new JButton("Save Information");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(accentBlue);
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
            if (!contact.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Contact number must contain digits only.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ageStr.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Age must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 150) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid age!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (onSave != null) {
                    onSave.accept(new UserInfo(name, contact, age));
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(saveButton);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(panelBg);
        centerWrapper.add(formPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createFormField(String labelText, String placeholder) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(panelBg);
        fieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(textColor);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(5));

        JTextField textField = new JTextField(placeholder);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setForeground(textColor);
        textField.setBackground(inputBg);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setCaretColor(textColor);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setMaximumSize(new Dimension(400, textField.getPreferredSize().height));
        textField.setHorizontalAlignment(JTextField.CENTER);

        fieldPanel.add(textField);
        return fieldPanel;
    }

    private JTextField extractTextField(JPanel fieldPanel) {
        for (var comp : fieldPanel.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        return null;
    }

    public void setInitialValues(String name, String contact, int age) {
        if (fullNameField != null) fullNameField.setText(name == null ? "" : name);
        if (contactNumberField != null) contactNumberField.setText(contact == null ? "" : contact);
        if (ageField != null) ageField.setText(age > 0 ? String.valueOf(age) : "");
    }

    public record UserInfo(String name, String contact, int age) {}
}

