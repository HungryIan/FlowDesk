package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import model.Transaction;

/**
 * Transactions list view-only panel.
 */
public class TransactionsPanelView extends JPanel {

    private final Color panelBg;
    private final Color inputBg;
    private final Color textColor;
    private final Color textSecondary;

    private JPanel listPanel;

    public TransactionsPanelView(Color panelBg, Color inputBg, Color textColor, Color textSecondary, List<Transaction> transactions) {
        this.panelBg = panelBg;
        this.inputBg = inputBg;
        this.textColor = textColor;
        this.textSecondary = textSecondary;

        setLayout(new BorderLayout());
        setBackground(panelBg);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(panelBg);

        JScrollPane scrollPane = new JScrollPane(listPanel);
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

        setTransactions(transactions);
    }

    public void setTransactions(List<Transaction> transactions) {
        listPanel.removeAll();
        List<Transaction> list = transactions == null ? new ArrayList<>() : transactions;
        int index = 1;
        for (Transaction transaction : list) {
            listPanel.add(createTransactionEntry(transaction, index++));
            listPanel.add(Box.createVerticalStrut(10));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createTransactionEntry(Transaction transaction, int index) {
        JPanel entry = new JPanel();
        entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
        entry.setBackground(inputBg);
        entry.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 80, 120), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel firstLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        firstLine.setBackground(inputBg);

        JLabel indexLabel = new JLabel(index + ". ");
        indexLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        indexLabel.setForeground(textColor);
        firstLine.add(indexLabel);

        JLabel idLabel = new JLabel("Transaction ID: " + transaction.getId() + " | User: " + transaction.getUserName());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idLabel.setForeground(textColor);
        firstLine.add(idLabel);

        entry.add(firstLine);
        entry.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel(transaction.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(textSecondary);
        entry.add(descLabel);
        entry.add(Box.createVerticalStrut(5));

        JLabel dateLabel = new JLabel("Date: " + transaction.getDate() + " | Time: " + transaction.getTime());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(textSecondary);
        entry.add(dateLabel);

        return entry;
    }
}

