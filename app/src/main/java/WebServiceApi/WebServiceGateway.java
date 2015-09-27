package WebServiceApi;

import java.util.HashMap;
import Domain.Item;
import Domain.TransactionItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicolasdubus on 15-09-26.
 */
public class WebServiceGateway {

    private HashMap<String, Item> itemsMap = new HashMap<>();
//    Firebase transactionRef = new Firebase("https://glacial-bastion-6427.herokuapp.com/charge_card");

    // NOTE: WILL NEED TO MAKE API CALLS. CURRENTLY SET UP FOR TESTING REST OF SYSTEM.
    public WebServiceGateway() {
        itemsMap.put("667888093731", new Item("667888093731", 1.99, "3 Subject Notebook 250", "3 Subject Notebook"));
        itemsMap.put("180854000101", new Item("180854000101", 2.99, "Red Bull 250ml reg", "Red Bull 250ml Regular"));
        itemsMap.put("067311057895", new Item("067311057895", 1.99, "Oasis Trop Mang Smoothie 1.75L", "Oasis Trop Mang Smoothie 1.75L"));
        itemsMap.put("770981093178", new Item("770981093178", 3.49, "Coconut Macaroons 200g", "Oasis Trop Mang Smoothie 1.75L"));
    }

    public HashMap getItemsMap() {
        return itemsMap;
    }

    public void addTransactionElement(String transactionId, Item item) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("transaction", new TransactionItem(transactionId, item));

        } catch (JSONException e) {}
    }

    public void removeTransactionElement(String transactionId, Item item) {
        // push - minus one
    }
}
