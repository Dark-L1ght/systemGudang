import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

class ItemFormGUI {
    private DataManager dataManager;
    private JComboBox<SupplierComboItem> supplierDropdown;
    private static final String SUPPLIERS_FILE = "suppliers.csv";

    public ItemFormGUI() {
        dataManager = DataManager.getInstance();

        JFrame frame = new JFrame("Item Management");
        frame.setSize(400, 350);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add fields
        panel.add(new JLabel("Item Code:"));
        JTextField itemCodeField = new JTextField();
        panel.add(itemCodeField);

        panel.add(new JLabel("Item Name:"));
        JTextField itemNameField = new JTextField();
        panel.add(itemNameField);

        panel.add(new JLabel("Category:"));
        JTextField categoryField = new JTextField();
        panel.add(categoryField);

        panel.add(new JLabel("Stock:"));
        JTextField stockField = new JTextField();
        panel.add(stockField);

        panel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField();
        panel.add(priceField);

        panel.add(new JLabel("Supplier:"));
        supplierDropdown = new JComboBox<>();
        updateSupplierDropdown();
        panel.add(supplierDropdown);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        panel.add(saveButton);
        panel.add(cancelButton);

        saveButton.addActionListener(e -> {
            try {
                // Validate fields
                if (itemCodeField.getText().isEmpty() || itemNameField.getText().isEmpty() ||
                        categoryField.getText().isEmpty() || stockField.getText().isEmpty() ||
                        priceField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields are required");
                    return;
                }

                SupplierComboItem selectedSupplier = (SupplierComboItem) supplierDropdown.getSelectedItem();
                if (selectedSupplier == null) {
                    JOptionPane.showMessageDialog(frame, "Please select a valid supplier");
                    return;
                }

                Supplier supplier = dataManager.findSupplierById(selectedSupplier.getId());
                if (supplier == null) {
                    JOptionPane.showMessageDialog(frame, "Selected supplier not found");
                    return;
                }

                // Validate numeric inputs
                int stock = Integer.parseInt(stockField.getText());
                int price = Integer.parseInt(priceField.getText());

                if (stock < 0) {
                    JOptionPane.showMessageDialog(frame, "Stock cannot be negative");
                    return;
                }
                if (price < 0) {
                    JOptionPane.showMessageDialog(frame, "Price cannot be negative");
                    return;
                }

                Item item = new Item(
                        itemCodeField.getText(),
                        itemNameField.getText(),
                        categoryField.getText(),
                        stock,
                        price,
                        supplier
                );

                dataManager.addItem(item);
                JOptionPane.showMessageDialog(frame, "Item saved successfully!");
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers for stock and price");
            }
        });

        cancelButton.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updateSupplierDropdown() {
        supplierDropdown.removeAllItems();

        try {
            // Read suppliers from CSV
            List<String> lines = Files.readAllLines(Paths.get(SUPPLIERS_FILE));
            boolean firstLine = true;

            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] values = line.split(",");
                if (values.length >= 3) {
                    String supplierId = values[0];
                    String supplierName = values[1];
                    supplierDropdown.addItem(new SupplierComboItem(supplierId, supplierName));
                }
            }

            if (supplierDropdown.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null,
                        "No suppliers found. Please add suppliers first.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading suppliers: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
