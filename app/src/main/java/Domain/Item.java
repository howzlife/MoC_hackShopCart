package Domain;


/**
 * Created by nicolasdubus on 15-09-26.
 */
public class Item {

    private String upcCode;

    private double price;
    private String name;
    private String description;

    public int quantity;

    public Item (String upcCode, double price, String name, String description) {
        this.upcCode = upcCode;
        this.price = price;
        this.name = name;
        this.description = description;
        this.quantity = 0;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addOne() {
        ++quantity;
    }

    public void removeOne() {
        if (quantity > 0) {
            quantity--;
        }
    }

    public String getUpcCode() {
        return upcCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
}
