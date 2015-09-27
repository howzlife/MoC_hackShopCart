package Domain;

/**
 * Created by nicolasdubus on 15-09-26.
 */
public class TransactionItem {

    private String transaction_ID;
    private Item item;

    public TransactionItem(String tId, Item item) {
        this.transaction_ID = tId;
        this.item = item;
    }
}
