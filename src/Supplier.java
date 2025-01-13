import java.util.*;

class Supplier extends Person {
    private String supplierID;
    private List<Item> items = new ArrayList<>();

    public Supplier(String supplierID,String name, String contact) {
        super(name, contact);
        this.supplierID = supplierID;
    }

    public String getSupplierID() {
        return supplierID;
    }

    public void addItem(Item item) {
        items.add(item);
    }

}
