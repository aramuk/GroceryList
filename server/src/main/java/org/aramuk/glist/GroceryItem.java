package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;

import java.util.List;
import java.util.Map;

public class GroceryItem {

    private String deviceID;
    private String parentItemID;
    private String name;
    private int frequency;

    public GroceryItem(){

    }

    public String toJSON(){
        Item item = new Item()
                .withPrimaryKey(TableHandler.ATTR_DEVICE_ID, deviceID, TableHandler.ATTR_PARENT_ID, parentItemID)
                .withList(TableHandler.ATTR_ITEM_INFO, itemValues);
        return item.toJSON();
    }

    public static GroceryItem fromJSON(String json) {
        Item item = Item.fromJSON(json);
        String d = item.getString(TableHandler.ATTR_DEVICE_ID);
        String p = item.getString(TableHandler.ATTR_PARENT_ID);
        List<Map<String, String>> l = item.getList(TableHandler.ATTR_ITEM_INFO);
        return new GroceryItem(d, p, l);
    }

}
