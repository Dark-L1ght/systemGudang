import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

class InventoryReport implements Report {
    private DataManager dataManager;
    private static final String ITEMS_FILE = "items.csv";
    private static final String SUPPLIERS_FILE = "suppliers.csv";
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));


    public InventoryReport() {
        dataManager = DataManager.getInstance();
    }

    public void generateInventoryList() {
        JFrame frame = new JFrame("Inventory Report");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // Create report panel
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));
        reportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add title
        JLabel titleLabel = new JLabel("Current Inventory Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        reportPanel.add(titleLabel, BorderLayout.NORTH);

        // Create table model with columns
        String[] columns = {
                "Item Code", "Item Name", "Category", "Current Stock",
                "Price", "Total Value", "Supplier", "Status"
        };

        List<Object[]> data = new ArrayList<>();

        // Read data from items.csv and suppliers.csv
        try {
            List<String> itemLines = Files.readAllLines(Paths.get(ITEMS_FILE));
            List<String> supplierLines = Files.readAllLines(Paths.get(SUPPLIERS_FILE));

            // Create supplier map for quick lookup
            Map<String, String> supplierMap = new HashMap<>();
            for (int i = 1; i < supplierLines.size(); i++) {
                String[] supplierData = supplierLines.get(i).split(",");
                supplierMap.put(supplierData[0], supplierData[1]); // supplierID -> supplierName
            }

            // Process items and create table data
            for (int i = 1; i < itemLines.size(); i++) {
                String[] row = itemLines.get(i).split(",");
                if (row.length >= 6) {
                    int stock = Integer.parseInt(row[3]);
                    int price = Integer.parseInt(row[4]);
                    String formattedPrice = currencyFormat.format(price);
                    String formattedTotalValue = currencyFormat.format((long) stock * price);
                    String supplierName = supplierMap.getOrDefault(row[5], "Unknown Supplier");

                    Object[] tableRow = new Object[]{
                            row[0], // Item Code
                            row[1], // Item Name
                            row[2], // Category
                            stock, // Current Stock
                            formattedPrice, // Price
                            formattedTotalValue, // Total Value
                            supplierName, // Supplier Name
                            getStockStatus(stock) // Status
                    };
                    data.add(tableRow);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "Error reading inventory data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convert List<Object[]> to Object[][] for the table
        Object[][] dataArray = data.toArray(new Object[0][]);
        JTable table = new JTable(dataArray, columns);
        table.setAutoCreateRowSorter(true);

        // Custom renderer for status column
        table.getColumn("Status").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                String status = (String) value;
                switch (status) {
                    case "Out of Stock":
                        c.setForeground(Color.RED);
                        break;
                    case "Low Stock":
                        c.setForeground(Color.ORANGE);
                        break;
                    case "Good Stock":
                        c.setForeground(new Color(0, 128, 0)); // Dark Green
                        break;
                }
                return c;
            }
        });

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        reportPanel.add(scrollPane, BorderLayout.CENTER);

        // Add summary panel
        JPanel summaryPanel = createSummaryPanel(data);
        reportPanel.add(summaryPanel, BorderLayout.SOUTH);

        // Add export and print buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Export to CSV");
        JButton printButton = new JButton("Print Report");

        exportButton.addActionListener(e -> exportReport(data, columns));
        printButton.addActionListener(e -> printReport(table));

        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);

        frame.add(reportPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createSummaryPanel(List<Object[]> data) {
        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Inventory Summary"));

        // Calculate summary statistics
        int totalItems = data.size();
        int totalUnits = 0;
        int totalValue = 0;
        int lowStockItems = 0;
        int outOfStockItems = 0;

        for (Object[] row : data) {
            int stock = (int) row[3];
            String totalValueStr = (String) row[5];
            int value = Integer.parseInt(totalValueStr.replace("Rp.", "").replace(".","").replace(",00","").replace("Rp",""));

            totalUnits += stock;
            totalValue += value;

            if (stock == 0) outOfStockItems++;
            else if (stock < 10) lowStockItems++;
        }
        String formattedTotalValue = currencyFormat.format(totalValue);
        // Add summary labels
        summaryPanel.add(new JLabel("Total Items: " + totalItems));
        summaryPanel.add(new JLabel("Total Units: " + totalUnits));
        summaryPanel.add(new JLabel("Total Value: " + formattedTotalValue));
        summaryPanel.add(new JLabel("Low Stock Items: " + lowStockItems));
        summaryPanel.add(new JLabel("Out of Stock: " + outOfStockItems));

        return summaryPanel;
    }

    public String getStockStatus(int stock) {
        if (stock == 0) return "Out of Stock";
        if (stock < 10) return "Low Stock";
        if (stock < 20) return "Medium Stock";
        return "Good Stock";
    }

    private void exportReport(List<Object[]> data, String[] columns) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "inventory_report_" + dateFormat.format(new Date()) + ".csv";

            try (PrintWriter writer = new PrintWriter(new File(fileName))) {
                // Write headers
                writer.println(String.join(",", columns));

                // Write data
                for (Object[] row : data) {
                    writer.println(String.join(",", Arrays.stream(row)
                            .map(Object::toString)
                            .collect(Collectors.toList())));
                }
            }

            JOptionPane.showMessageDialog(null,
                    "Report exported successfully to " + fileName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error exporting report: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport(JTable table) {
        try {
            MessageFormat header = new MessageFormat("Inventory Report - {0}");
            MessageFormat footer = new MessageFormat("Page {0}");
            table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(null,
                    "Error printing report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}