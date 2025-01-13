import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import javax.swing.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.*;

class DataManager {
    private static DataManager instance;
    private List<Item> items;
    private List<Supplier> suppliers;
    private List<Transaction> transactions;
    private static final String ITEMS_FILE = "items.csv";
    private static final String SUPPLIERS_FILE = "suppliers.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";


    private DataManager() {
        items = new ArrayList<>();
        suppliers = new ArrayList<>();
        transactions = new ArrayList<>();
        loadData();
    }

    public void createCsvFiles() {
        try {
            // Create files with headers if they don't exist
            File itemsFile = new File(ITEMS_FILE);
            if (!itemsFile.exists()) {
                try (PrintWriter writer = new PrintWriter(itemsFile)) {
                    writer.println("itemCode,itemName,category,stock,price,supplierID");
                }
            }

            File suppliersFile = new File(SUPPLIERS_FILE);
            if (!suppliersFile.exists()) {
                try (PrintWriter writer = new PrintWriter(suppliersFile)) {
                    writer.println("supplierID,supplierName,contact");
                }
            }

            File transactionsFile = new File(TRANSACTIONS_FILE);
            if (!transactionsFile.exists()) {
                try (PrintWriter writer = new PrintWriter(transactionsFile)) {
                    writer.println("transactionID,date,type,customerSupplier,itemCode,quantity");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a new supplier and save immediately to CSV
    public void addSupplier(Supplier supplier) {
        suppliers.add(supplier);
        try (FileWriter fw = new FileWriter(SUPPLIERS_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%s,%s,%s\n",
                    supplier.getSupplierID(),
                    supplier.getName(),
                    supplier.getContact()
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving supplier to CSV: " + e.getMessage());
        }
    }

    // Add a new item and save immediately to CSV
    public void addItem(Item item) {
        items.add(item);
        try (FileWriter fw = new FileWriter(ITEMS_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("%s,%s,%s,%d,%d,%s\n",
                    item.getItemCode(),
                    item.getItemName(),
                    item.getCategory(),
                    item.getStock(),
                    item.getPrice(),
                    item.getSupplier().getSupplierID()
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving item to CSV: " + e.getMessage());
        }
    }

    // Add a new transaction and save immediately to CSV
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        try (FileWriter fw = new FileWriter(TRANSACTIONS_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // For each item in the transaction, create a record
            for (Map.Entry<Item, Integer> entry : transaction.getItems().entrySet()) {
                out.printf("%s,%s,%s,%s,%s,%d\n",
                        transaction.getTransactionID(),
                        dateFormat.format(transaction.getDate()),
                        transaction instanceof GoodsInTransaction ? "Goods In" : "Goods Out",
                        transaction instanceof GoodsInTransaction ?
                                ((GoodsInTransaction) transaction).getSupplier().getSupplierID() :
                                ((GoodsOutTransaction) transaction).getCustomerName(),
                        entry.getKey().getItemCode(),
                        entry.getValue()
                );
            }

            // Update item stock in items.csv
            updateItemStock(transaction);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving transaction to CSV: " + e.getMessage());
        }
    }

    // Update item stock in CSV after a transaction
    private void updateItemStock(Transaction transaction) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ITEMS_FILE))) {
            // Read header
            String header = reader.readLine();
            lines.add(header);

            // Read all lines
            String line;
            while ((line = reader.readLine()) != null) {
                boolean lineUpdated = false;
                String[] values = line.split(",");
                String itemCode = values[0];

                for (Map.Entry<Item, Integer> entry : transaction.getItems().entrySet()) {
                    if (entry.getKey().getItemCode().equals(itemCode)) {
                        // Update stock value
                        int newStock = Integer.parseInt(values[3]);
                        if (transaction instanceof GoodsInTransaction) {
                            newStock += entry.getValue();
                        } else {
                            newStock -= entry.getValue();
                        }
                        values[3] = String.valueOf(newStock);
                        lineUpdated = true;
                        break;
                    }
                }

                // Add the line (updated or original)
                lines.add(String.join(",", values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write back to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(ITEMS_FILE))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load data from CSV files
    private void loadData() {
        loadSuppliers();
        loadItems();
        loadTransactions();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void loadSuppliers() {
        try (BufferedReader br = new BufferedReader(new FileReader(SUPPLIERS_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                suppliers.add(new Supplier(values[0], values[1], values[2]));
            }
        } catch (IOException e) {
            System.out.println("No existing suppliers file found. Starting fresh.");
        }
    }

    private void loadItems() {
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                Supplier supplier = findSupplierById(values[5]);
                if (supplier != null) {
                    items.add(new Item(
                            values[0],
                            values[1],
                            values[2],
                            Integer.parseInt(values[3]),
                            Integer.parseInt(values[4]),
                            supplier
                    ));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing items file found. Starting fresh.");
        }
    }

    private void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(values[1]);
                Transaction transaction;

                if (values[3].equals("Goods In")) {
                    Supplier supplier = findSupplierById(values[4]);
                    transaction = new GoodsInTransaction(values[0], date, supplier);
                } else {
                    transaction = new GoodsOutTransaction(values[0], date, values[4]);
                }

                Item item = findItemByCode(values[5]);
                if (item != null) {
                    transaction.addItem(item, Integer.parseInt(values[6]));
                }
                transactions.add(transaction);
            }
        } catch (IOException | ParseException e) {
            System.out.println("No existing transactions file found. Starting fresh.");
        }
    }

    private void saveSuppliers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SUPPLIERS_FILE))) {
            writer.println("supplierName,contact,supplierID");
            for (Supplier supplier : suppliers) {
                writer.println(String.format("%s,%s,%s",
                        supplier.getName(),
                        supplier.getContact(),
                        supplier.getSupplierID()
                ));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving suppliers: " + e.getMessage());
        }
    }

    private void saveItems() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ITEMS_FILE))) {
            writer.println("itemCode,itemName,category,stock,price,supplierID");
            for (Item item : items) {
                writer.println(String.format("%s,%s,%s,%d,%d,%s",
                        item.getItemCode(),
                        item.getItemName(),
                        item.getCategory(),
                        item.getStock(),
                        item.getPrice(),
                        item.getSupplier().getSupplierID()
                ));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving items: " + e.getMessage());
        }
    }

    private void saveTransactions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            writer.println("transactionID,date,type,customerSupplier,itemCode,quantity");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Transaction transaction : transactions) {
                for (Map.Entry<Item, Integer> entry : transaction.getItems().entrySet()) {
                    writer.println(String.format("%s,%s,%s,%s,%s,%d",
                            transaction.getTransactionID(),
                            dateFormat.format(transaction.getDate()),
                            transaction instanceof GoodsInTransaction ? "Goods In" : "Goods Out",
                            transaction instanceof GoodsInTransaction ?
                                    ((GoodsInTransaction) transaction).getSupplier().getSupplierID() :
                                    ((GoodsOutTransaction) transaction).getCustomerName(),
                            entry.getKey().getItemCode(),
                            entry.getValue()
                    ));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving transactions: " + e.getMessage());
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Item findItemByCode(String itemCode) {
        try {
            // Read from items.csv
            List<String> lines = Files.readAllLines(Paths.get(ITEMS_FILE));

            // Skip header and search for item
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                if (values.length >= 6 && values[0].equals(itemCode)) {
                    // Find the supplier first since we need it to construct the Item
                    Supplier supplier = findSupplierById(values[5]);
                    if (supplier != null) {
                        return new Item(
                                values[0],                           // itemCode
                                values[1],                           // itemName
                                values[2],                           // category
                                Integer.parseInt(values[3]),         // stock
                                Integer.parseInt(values[4]),       // price
                                supplier                            // supplier
                        );
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading items file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values: " + e.getMessage());
        }

        return null; // Item not found or error occurred
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

    // Utility method to ensure CSV files exist
    private void ensureFilesExist() {
        try {
            // Check and create items.csv if it doesn't exist
            File itemsFile = new File(ITEMS_FILE);
            if (!itemsFile.exists()) {
                Files.write(itemsFile.toPath(),
                        Collections.singletonList("itemCode,itemName,category,stock,price,supplierID"),
                        StandardOpenOption.CREATE);
            }

            // Check and create suppliers.csv if it doesn't exist
            File suppliersFile = new File(SUPPLIERS_FILE);
            if (!suppliersFile.exists()) {
                Files.write(suppliersFile.toPath(),
                        Collections.singletonList("supplierID,name,contact"),
                        StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            System.err.println("Error creating CSV files: " + e.getMessage());
        }
    }
}

