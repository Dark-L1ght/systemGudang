import javax.swing.*;
import java.awt.*;

class MainGUI {
    private JFrame frame;
    private DataManager dataManager;

    public MainGUI() {
        dataManager = DataManager.getInstance();
        dataManager.createCsvFiles();

        frame = new JFrame("Warehouse Inventory Management");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Warehouse Inventory Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton itemButton = new JButton("Manage Items");
        JButton supplierButton = new JButton("Manage Suppliers");
        JButton transactionButton = new JButton("Manage Transactions");
        JButton reportButton = new JButton("Generate Reports");

        itemButton.setFont(new Font("Arial", Font.PLAIN, 18));
        supplierButton.setFont(new Font("Arial", Font.PLAIN, 18));
        transactionButton.setFont(new Font("Arial", Font.PLAIN, 18));
        reportButton.setFont(new Font("Arial", Font.PLAIN, 18));

        buttonPanel.add(itemButton);
        buttonPanel.add(supplierButton);
        buttonPanel.add(transactionButton);
        buttonPanel.add(reportButton);

        // Add action listeners
        itemButton.addActionListener(e -> new ItemFormGUI());
        supplierButton.addActionListener(e -> new SupplierFormGUI());
        transactionButton.addActionListener(e -> new TransactionFormGUI());
        reportButton.addActionListener(e -> generateReports());

        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void generateReports() {
        InventoryReport report = new InventoryReport();
        report.generateInventoryList();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}

