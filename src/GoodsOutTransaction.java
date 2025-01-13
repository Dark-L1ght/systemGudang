import java.util.*;

class GoodsOutTransaction extends Transaction {
    private String customerName;

    public GoodsOutTransaction(String transactionID, Date date, String customerName) {
        super(transactionID, date);
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    @Override
    public void processTransaction() {
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            int quantity = entry.getValue();
            if (item.getStock() >= quantity) {
                item.updateStock(-quantity);
                System.out.println("Sold " + quantity + " units of " + item.getItemCode());
            } else {
                System.out.println("Insufficient stock for item: " + item.getItemCode());
            }
        }
    }
}
