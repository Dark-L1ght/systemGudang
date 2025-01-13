import java.util.*;

abstract class Transaction {
    protected String transactionID;
    protected Date date;
    protected Map<Item, Integer> items;

    public Transaction(String transactionID, Date date) {
        this.transactionID = transactionID;
        this.date = date;
        this.items = new HashMap<>();
    }

    public void addItem(Item item, int quantity) {
        items.put(item, quantity);
    }

    public String getTransactionID() {
        return transactionID;
    }

    public Date getDate() {
        return date;
    }

    public Map<Item, Integer> getItems() {
        return items;
    }

    public abstract void processTransaction();
}