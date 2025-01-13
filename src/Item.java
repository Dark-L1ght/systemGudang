class Item {
    private String itemCode;
    private String itemName;
    private String category;
    private int stock;
    private int price;
    private Supplier supplier;

    public Item(String itemCode, String itemName, String category, int stock, int price, Supplier supplier) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.category = category;
        this.stock = stock;
        this.price = price;
        this.supplier = supplier;
        supplier.addItem(this);
    }

    public String getItemCode() {
        return itemCode;
    }

    public void updateStock(int quantity) {
        stock += quantity;
    }

    public int getStock() {
        return stock;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }
}