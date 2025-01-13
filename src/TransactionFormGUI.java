import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


class TransactionFormGUI {
    private DataManager dataManager;
    private JComboBox<ItemComboItem> itemDropdown;
    private static final String ITEMS_FILE = "items.csv";
    private static final String SUPPLIERS_FILE = "suppliers.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";

    public TransactionFormGUI() {
        dataManager = DataManager.getInstance();

        JFrame frame = new JFrame("Transaction Management");
        frame.setSize(400, 350);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Transaction ID
        panel.add(new JLabel("Transaction ID:"));
        JTextField transactionIDField = new JTextField();
        panel.add(transactionIDField);

        // Transaction Type
        panel.add(new JLabel("Transaction Type:"));
        String[] types = {"Goods In", "Goods Out"};
        JComboBox<String> typeDropdown = new JComboBox<>(types);
        panel.add(typeDropdown);

        // Customer/Supplier field (label will change based on type)
        JLabel partyLabel = new JLabel("Supplier:");
        panel.add(partyLabel);
        JComboBox<SupplierComboItem> supplierDropdown = new JComboBox<>();
        JTextField customerField = new JTextField();
        JPanel partyPanel = new JPanel(new CardLayout());
        partyPanel.add(supplierDropdown, "Supplier");
        partyPanel.add(customerField, "Customer");
        panel.add(partyPanel);

        // Update party field based on transaction type
        typeDropdown.addActionListener(e -> {
            CardLayout cl = (CardLayout) partyPanel.getLayout();
            if (typeDropdown.getSelectedItem().equals("Goods In")) {
                partyLabel.setText("Supplier:");
                cl.show(partyPanel, "Supplier");
            } else {
                partyLabel.setText("Customer:");
                cl.show(partyPanel, "Customer");
            }
        });

        // Item selection
        panel.add(new JLabel("Item:"));
        itemDropdown = new JComboBox<>();
        panel.add(itemDropdown);

        // Quantity
        panel.add(new JLabel("Quantity:"));
        JTextField quantityField = new JTextField();
        panel.add(quantityField);

        // Date
        panel.add(new JLabel("Date:"));
        JTextField dateField = new JTextField();
        dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        panel.add(dateField);

        // Buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        panel.add(saveButton);
        panel.add(cancelButton);

        // Load suppliers and items
        updateSupplierDropdown(supplierDropdown);
        updateItemDropdown();

        // Save button action
        saveButton.addActionListener(e -> {
            try {
                // Validate fields
                if (transactionIDField.getText().isEmpty() ||
                        quantityField.getText().isEmpty() ||
                        dateField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields are required");
                    return;
                }

                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(frame, "Quantity must be positive");
                    return;
                }

                ItemComboItem selectedItem = (ItemComboItem) itemDropdown.getSelectedItem();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(frame, "Please select an item");
                    return;
                }

                // Process based on transaction type
                if (typeDropdown.getSelectedItem().equals("Goods In")) {
                    SupplierComboItem selectedSupplier = (SupplierComboItem) supplierDropdown.getSelectedItem();
                    if (selectedSupplier == null) {
                        JOptionPane.showMessageDialog(frame, "Please select a supplier");
                        return;
                    }
                    processGoodsIn(transactionIDField.getText(), selectedSupplier.getId(),
                            selectedItem.getCode(), quantity, dateField.getText());
                } else {
                    if (customerField.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please enter customer name");
                        return;
                    }
                    processGoodsOut(transactionIDField.getText(), customerField.getText(),
                            selectedItem.getCode(), quantity, dateField.getText());
                }

                JOptionPane.showMessageDialog(frame, "Transaction saved successfully!");
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number for quantity");
            }
        });

        cancelButton.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updateSupplierDropdown(JComboBox<SupplierComboItem> supplierDropdown) {
        supplierDropdown.removeAllItems();
        try {
            List<String> lines = Files.readAllLines(Paths.get(SUPPLIERS_FILE));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] values = lines.get(i).split(",");
                if (values.length >= 3) {
                    supplierDropdown.addItem(new SupplierComboItem(values[0], values[1]));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading suppliers: " + e.getMessage());
        }
    }

    private void updateItemDropdown() {
        itemDropdown.removeAllItems();
        try {
            List<String> lines = Files.readAllLines(Paths.get(ITEMS_FILE));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] values = lines.get(i).split(",");
                if (values.length >= 6) {
                    itemDropdown.addItem(new ItemComboItem(values[0], values[1]));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading items: " + e.getMessage());
        }
    }

    private void processGoodsIn(String transactionID, String supplierID, String itemCode,
                                int quantity, String date) {
        // Load the items from file or a data structure
        List<Item> items = loadItemsFromFile();

        // Find the item to update stock
        Item item = findItemByCode(items, itemCode);
        if (item != null && item.getSupplier().getSupplierID().equals(supplierID)) {
            // Update stock for Goods In
            item.updateStock(quantity);

            // Save the updated item list back to the file
            saveItemsToFile(items);

            // Log the transaction
            logTransaction(transactionID, date, "Goods In", supplierID, itemCode, quantity);
        } else {
            JOptionPane.showMessageDialog(null, "Item not found or supplier mismatch");
        }
    }


    private void processGoodsOut(String transactionID, String customerName, String itemCode,
                                 int quantity, String date) {
        // Load the items from file or a data structure
        List<Item> items = loadItemsFromFile();

        // Find the item to update stock
        Item item = findItemByCode(items, itemCode);
        if (item != null) {
            // Update stock for Goods Out
            if (item.getStock() >= quantity) {
                item.updateStock(-quantity);
                // Save the updated item list back to the file
                saveItemsToFile(items);

                // Log the transaction
                logTransaction(transactionID, date, "Goods Out", customerName, itemCode, quantity);
            } else {
                JOptionPane.showMessageDialog(null, "Not enough stock for this transaction");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Item not found");
        }
    }

    public Supplier findSupplierById(String supplierID) {
        try {
            // Read from suppliers.csv
            List<String> lines = Files.readAllLines(Paths.get(SUPPLIERS_FILE));

            // Skip header and search for supplier
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                if (values.length >= 3 && values[0].equals(supplierID)) {
                    return new Supplier(
                            values[0],    // supplierID
                            values[1],    // name
                            values[2]     // contact
                    );
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading suppliers file: " + e.getMessage());
        }

        return null; // Supplier not found or error occurred
    }

    private List<Item> loadItemsFromFile() {
        List<Item> items = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(ITEMS_FILE));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] values = lines.get(i).split(",");
                if (values.length >= 6) {
                    // Assuming Supplier is also loaded, you need to modify based on how you load suppliers
                    Supplier supplier = findSupplierById(values[5]); // Assuming supplier ID is at index 5
                    items.add(new Item(values[0], values[1], values[2], Integer.parseInt(values[3]),
                            Integer.parseInt(values[4]), supplier));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading items: " + e.getMessage());
        }
        return items;
    }

    private Item findItemByCode(List<Item> items, String itemCode) {
        for (Item item : items) {
            if (item.getItemCode().equals(itemCode)) {
                return item;
            }
        }
        return null; // Return null if item not found
    }

    public static void logTransaction(String transactionID, String date, String type,
                                      String customerSupplier, String itemCode, int quantity) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            String transactionDetails = String.format("%s,%s,%s,%s,%s,%d",
                    transactionID, date, type, customerSupplier, itemCode, quantity);
            writer.write(transactionDetails);
            writer.newLine(); // New line after each transaction
        } catch (IOException e) {
            System.err.println("Error writing transaction to CSV: " + e.getMessage());
        }
    }

    private void saveItemsToFile(List<Item> items) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ITEMS_FILE))) {
            writer.write("Item Code,Item Name,Category,Stock,Price,Supplier\n"); // Write header
            for (Item item : items) {
                writer.write(String.format("%s,%s,%s,%d,%d,%s\n",
                        item.getItemCode(), item.getItemName(), item.getCategory(),
                        item.getStock(), item.getPrice(), item.getSupplier().getSupplierID()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving items: " + e.getMessage());
        }
    }

}

