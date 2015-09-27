package Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicolasdubus on 15-09-26.
 */
public class Cart {

    private List<Item> items;

    public Cart() {
        items = new ArrayList<>();
    }

    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        } else {
            items.get(items.indexOf(item)).addOne();
        }
    }

    public void removeOne(Item item) {
        if (items.contains(item)) {
            if (items.get(items.indexOf(item)).quantity > 1) {
                items.get(items.indexOf(item)).removeOne(); // if there is more than 1 item, reduct quantity
            } else {
                items.remove(item); // if there is one or less items left, remove from list
            }
        }
    }

    public double getTotal() {
        double price = 0;

        for (Item item: items) {
            price += (item.getQuantity() * item.getPrice());
        }

        return price;
    }

    public List<Item> getItemsList() {
        return items;
    }
}
