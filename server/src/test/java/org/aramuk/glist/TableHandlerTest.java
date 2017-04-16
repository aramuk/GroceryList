package org.aramuk.glist;

import com.amazonaws.services.dynamodbv2.document.Item;
import org.junit.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for TableHandler
 */
public class TableHandlerTest {

    private final String DEFAULT_DEVICE_ID = "12345678";
    private final String ROOT_PARENT_ITEM_ID = "0";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        // Check that local DynamoDB is running
        if (!isLocalDynamoDbRunning()) {
            throw new Exception("DynamoDB is not running and must be started.");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testDropNonExistingTable() {
        // Todo: Make it really random
        TableHandler th = new TableHandler("UnknownRandomTableName");
        try {
            th.dropTable();
        } finally {
            th.releaseResources();
        }
    }

    @Test
    public void testAddItemNormal() {
        System.out.println("Testing add item...");
        List<Map<String, String>> values = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put(TableHandler.ATTR_ITEM_ID, "123");
        dataMap.put(TableHandler.ATTR_ITEM_VALUE, "Trader Joe's");
        values.add(dataMap);
        dataMap = new HashMap<>();
        dataMap.put(TableHandler.ATTR_ITEM_ID, "345");
        dataMap.put(TableHandler.ATTR_ITEM_VALUE, "Costco");
        values.add(dataMap);
        dataMap = new HashMap<>();
        dataMap.put(TableHandler.ATTR_ITEM_ID, "567");
        dataMap.put(TableHandler.ATTR_ITEM_VALUE, "Whole Foods");
        values.add(dataMap);
        TableHandler th = new TableHandler();
        try {
            th.addItem(DEFAULT_DEVICE_ID, ROOT_PARENT_ITEM_ID, values);
            Item addedItem = th.getItem(DEFAULT_DEVICE_ID, ROOT_PARENT_ITEM_ID);
            Assert.assertNotNull("Added item should not be null", addedItem);
            System.out.println("Item added: " + addedItem.toJSONPretty());
        } finally {
            try {
                th.deleteItem(DEFAULT_DEVICE_ID, ROOT_PARENT_ITEM_ID);
            } catch (Exception ee) {
                System.out.println("Cleanup of added item failed. " + ee.getMessage());
                ee.printStackTrace();
            }
            th.releaseResources();
        }
    }

    private static boolean isLocalDynamoDbRunning() {
        Socket s = null;
        try {
            String portString = System.getenv("DYNAMODB_PORT");
            if (portString == null) {
                portString = "8000";
            }
            int port = Integer.valueOf(portString);
            System.out.println("Checking if dynamoDB is running on port " + portString);
            s = new Socket("localhost", port);
            return true;
        }  catch (Exception e) {
            return false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch(Exception e){
                    // Ignored intentionally
                }
            }
        }
    }

}
