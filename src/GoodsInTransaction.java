import java.util.*;

class GoodsInTransaction extends Transaction {
    private Supplier supplier;

    public GoodsInTransaction(String transactionID, Date date, Supplier supplier) {
        super(transactionID, date);
        this.supplier = supplier;
    }

    @Override
    public void processTransaction() {
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            int quantity = entry.getValue();
            item.updateStock(quantity);
            System.out.println("Added " + quantity + " units of " + item.getItemCode());
        }
    }

    public Supplier getSupplier() {
        return supplier;
    }
}