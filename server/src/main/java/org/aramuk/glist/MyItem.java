package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an Item for my application
 */
public class MyItem {
    private String deviceId;
    private String parentItemId;
    private List<Map<String, String>> itemValues;

    public MyItem(String deviceId, String parentItemId) {
        this.deviceId = deviceId;
        this.parentItemId = parentItemId;
        itemValues = new ArrayList<>();
    }

    private MyItem(String deviceId, String parentItemId, List<Map<String, String>> itemValues) {
        this.deviceId = deviceId;
        this.parentItemId = parentItemId;
        this.itemValues = itemValues;
    }

    public void addItemValue(Map<String, String> itemMap) {
        itemValues.add(itemMap);
    }

    public static MyItem fromJSON(String json) {
        Item item = Item.fromJSON(json);
        String d = item.getString(TableHandler.ATTR_DEVICE_ID);
        String p = item.getString(TableHandler.ATTR_PARENT_ID);
        List<Map<String, String>> l = item.getList(TableHandler.ATTR_ITEM_INFO);
        return new MyItem(d, p, l);
    }

    public String toJSON() {
        Item item = new Item()
                .withPrimaryKey(TableHandler.ATTR_DEVICE_ID, deviceId, TableHandler.ATTR_PARENT_ID, parentItemId)
                .withList(TableHandler.ATTR_ITEM_INFO, itemValues);
        return item.toJSON();
    }
}
