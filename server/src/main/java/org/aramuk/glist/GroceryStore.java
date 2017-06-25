package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroceryStore {

    private String name;
    private String storeID;
    private Map<String, GroceryItem> groceryItems;

    private GroceryStore() {
    }

    public GroceryStore(String name, String sid) {
        this.name = name;
        storeID = sid;
    }

    public static GroceryStore findByName(String deviceId, String storeName) {
        GroceryStore store = new GroceryStore();
        store.load(deviceId, storeName);
        return store;
    }

    private void load(String deviceId, String storeName) {
        TableHandler th = new TableHandler();
        Item item = th.getItem(deviceId, "0");
    }

    private void save(String deviceID){
        TableHandler th = new TableHandler();
        List info = new ArrayList();
        for(String key:groceryItems.keySet()){
            GroceryItem item = groceryItems.get(key);
            info.add(item.toJSON());
        }
        th.addItem(deviceID, storeID, info);
    }
}
