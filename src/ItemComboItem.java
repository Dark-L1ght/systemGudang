// Helper class for item dropdown
class ItemComboItem {
    private String code;
    private String name;

    public ItemComboItem(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
