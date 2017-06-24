package org.aramuk.glist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroceryStore {

    private String name;
    private final String storeID;
    private Map<String,GroceryItem> groceryItems;

    public GroceryStore(String sname, String sid){
        name = sname;
        storeID = sid;
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
