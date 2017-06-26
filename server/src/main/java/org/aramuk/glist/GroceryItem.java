package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@XmlRootElement
public class GroceryItem {

    static final String ATTR_FREQUENCY = "frequency";
    private String itemId;
    private String itemName;
    private int frequency;

    public GroceryItem() {
    }

    public Map toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(TableHandler.ATTR_ITEM_ID, getItemId());
        map.put(TableHandler.ATTR_ITEM_NAME, getItemName());
        map.put(ATTR_FREQUENCY, String.valueOf(getFrequency()));
        return map;
    }

    public String toJSON(){
        Map<String, String> attrMap = new HashMap<String, String>();
        /* Item item = new Item()
                .withPrimaryKey(TableHandler.ATTR_DEVICE_ID, deviceID, TableHandler.ATTR_PARENT_ID, parentItemID)
                .withList(TableHandler.ATTR_ITEM_INFO, (List<?>) itemValues*);
        return item.toJSON();*/
        return null;
    }

    /*
    public static GroceryItem fromJSON(String json) {
        Item item = Item.fromJSON(json);
        String d = item.getString(TableHandler.ATTR_DEVICE_ID);
        String p = item.getString(TableHandler.ATTR_PARENT_ID);
        List<Map<String, String>> l = item.getList(TableHandler.ATTR_ITEM_INFO);
        // return new GroceryItem(d, p, l);
        return null;
    }
    */

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroceryItem)) {
            return false;
        }
        GroceryItem that = (GroceryItem)o;
        return Objects.equals(itemId, that.itemId) &&
                Objects.equals(itemName, that.itemName) &&
                frequency == that.frequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemName, frequency);
    }
}
