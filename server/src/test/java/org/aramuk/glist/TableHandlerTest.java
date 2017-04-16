package org.aramuk.glist;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for TableHandler
 */
public class TableHandlerTest {

    private TableHandler tableHandler;

    @Before
    public void setUp() throws Exception {
        // Check that local DynamoDB is running
        if (!isLocalDynamoDbRunning()) {
            throw new Exception("DynamoDB is not running and must be started.");
        }
        // Check that the table is present
        tableHandler = new TableHandler();
        if (!tableHandler.checkTableExists()) {
            throw new Exception("DynamoDB table " + tableHandler.getTableName() + " does not exist.");
        }
    }

    @Test
    public void testAddItem() {
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
        tableHandler.addItem("12345678", "0", values);
    }

    @After
    public void tearDown() throws Exception {
        if (tableHandler != null) {
            tableHandler.releaseResources();
        }
    }

    private boolean isLocalDynamoDbRunning() {
        Socket s = null;
        try {
            String portString = System.getenv("DYNAMODB_PORT");
            if (portString == null) {
                portString = new String("8000");
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
                } catch(Exception e){}
            }
        }
    }

}
