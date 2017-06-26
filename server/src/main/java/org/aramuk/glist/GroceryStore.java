package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@XmlRootElement
public class GroceryStore {

    private String storeId;
    private String storeName;

    private GroceryStore() {
    }

    public GroceryStore(String sid, String name) {
        setStoreId(sid);
        this.setStoreName(name);
    }

    public static GroceryStore[] findAll(String deviceId) {
        TableHandler th = new TableHandler();
        try {
            Item item = th.getItem(deviceId, "0");
            if (item == null) {
                return null;
            }
            List rawStores = item.getList(TableHandler.ATTR_ITEM_INFO);
            if (rawStores == null) {
                return null;
            }
            int numStores = rawStores.size();
            if (numStores == 0) {
                return null;
            }
            GroceryStore[] stores = new GroceryStore[numStores];
            for (int i = 0; i < numStores; i++) {
                Object obj = rawStores.get(i);
                if (!(obj instanceof Map)) {
                    System.err.println("Unexpected data type, expecting Map got " + obj != null ? obj.getClass() : "null");
                    throw new RuntimeException("Internal Error!");
                }
                stores[i] = GroceryStore.createFromMap((Map) obj);
            }
            return stores;
        } finally {
            th.releaseResources();
        }
    }

    public static GroceryStore createFromMap(Map map) {
        return new GroceryStore((String)map.get(TableHandler.ATTR_ITEM_ID),
                (String)map.get(TableHandler.ATTR_ITEM_NAME));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroceryStore)) {
            return false;
        }
        GroceryStore that = (GroceryStore)o;
        return Objects.equals(getStoreId(), that.getStoreId()) &&
                Objects.equals(getStoreName(), that.getStoreName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStoreId(), getStoreName());
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
