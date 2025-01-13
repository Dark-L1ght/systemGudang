import javax.swing.*;
import java.awt.*;

class SupplierFormGUI {
    private DataManager dataManager;

    public SupplierFormGUI() {
        dataManager = DataManager.getInstance();

        JFrame frame = new JFrame("Supplier Management");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Supplier ID:"));
        JTextField idField = new JTextField();
        panel.add(idField);

        panel.add(new JLabel("Supplier Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Contact:"));
        JTextField contactField = new JTextField();
        panel.add(contactField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        panel.add(saveButton);
        panel.add(cancelButton);

        saveButton.addActionListener(e -> {
            // Validate fields
            if (nameField.getText().isEmpty() || contactField.getText().isEmpty() || idField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required");
                return;
            }

            // Check if supplier ID already exists
            if (dataManager.findSupplierById(idField.getText()) != null) {
                JOptionPane.showMessageDialog(frame, "Supplier ID already exists");
                return;
            }

            Supplier supplier = new Supplier(
                    idField.getText(),
                    nameField.getText(),
                    contactField.getText()
            );

            dataManager.addSupplier(supplier);
            JOptionPane.showMessageDialog(frame, "Supplier saved successfully!");
            frame.dispose();
        });

        cancelButton.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

