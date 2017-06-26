package org.aramuk.glist;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.regions.Regions;

/**
 * Creates AWS DynamoDB table to maintain items in a hierarchy. Overall JSON representation
 * is as follows.
 * <code>
 *     "items": [
 *         {
 *             "device_id": "12345678", // Uniquely identifies one instance of the application
 *             "parent_item_id": "0", // Top level items are always stored with zero id
 *             "item_info": [
 *                 {"item_id": "123", "name": "Trader Joe's"},
 *                 {"item_id": "345", "name": "Costco"},
 *                 {"item_id": "567", "name": "Whole Foods"}
 *             ]
 *          },
 *          {
 *              "device_id": "12345678",
 *              "parent_item_id": "123",
 *              "item_info": [
 *                  {"item_id": "901", "name": "Balsamic Vinegar"},
 *                  {"item_id": "902", "name": "Waffles"}
 *              ]
 *          },
 *          {
 *              "device_id": "87654321",
 *              "parent_item_id": "0",
 *              "item_info": [
 *                  {"item_id": "ABC", "name": "Rock Climbing"},
 *                  {"item_id": "DEF", "name": "Skiing"}
 *              ]
 *          }
 *      ]
 * </code>
 */
public class TableHandler {

    static final String DEFAULT_TABLE_NAME = "items";
    static final String ATTR_DEVICE_ID = "device_id";
    static final String ATTR_PARENT_ID = "parent_item_id";
    static final String ATTR_ITEM_INFO = "item_info";
    static final String ATTR_ITEM_ID = "item_id";
    static final String ATTR_ITEM_NAME = "name";

    private AmazonDynamoDB dynamoDBClient;
    private String tableName;

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("init")) {
                TableHandler handler = new TableHandler();
                if (handler.checkTableExists()) {
                    handler.dropTableInternal();
                }
                handler.createTable();
                handler.releaseResources();
            }
        }
    }

    public TableHandler() {
        this(DEFAULT_TABLE_NAME);
    }

    public TableHandler(String tableName) {
        this.tableName = tableName;
        createDynamoDBClient();
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Release resources being used by this instance. The instance should not be used after releasing resources
     */
    public void releaseResources() {
        dynamoDBClient.shutdown();
        dynamoDBClient = null;
    }

    private void createDynamoDBClient() {
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();
        /*dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();*/
    }

    private DynamoDB getDynamoDB() {
        if (dynamoDBClient != null) {
            return new DynamoDB(dynamoDBClient);
        }
        throw new IllegalStateException("DynamoDBClient not initialized. Using object after releasing resources?");
    }

    private Table getTable() {
        DynamoDB dynamoDB = getDynamoDB();
        return dynamoDB.getTable(tableName);
    }

    /**
     * Check if the table handled by this instance exists in dynamoDB.
     * @return true if the table exists, false otherwise
     */
    public boolean checkTableExists() {
        boolean found = false;
        DynamoDB dynamoDB = getDynamoDB();
        TableCollection<ListTablesResult> tt = dynamoDB.listTables();
        for (Table table : tt) {
            if (tableName.equals(table.getTableName())) {
                found = true;
                break;
            }
        }
        return found;
    }

    public void createTable() {

        DynamoDB dynamoDB = getDynamoDB();

        try {
            System.out.println("Attempting to create table; " + tableName + " please wait...");
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                            new KeySchemaElement(ATTR_DEVICE_ID, KeyType.HASH), //Partition key
                            new KeySchemaElement(ATTR_PARENT_ID, KeyType.RANGE)), //Sort key
                    Arrays.asList(
                            new AttributeDefinition(ATTR_DEVICE_ID, ScalarAttributeType.S),
                            new AttributeDefinition(ATTR_PARENT_ID, ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            table.waitForActive();
            System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create table: " + tableName);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void dropTable() {
        if (tableName.equals(DEFAULT_TABLE_NAME)) {
            throw new IllegalStateException("Can not drop default table");
        }
        dropTableInternal();
    }

    private void dropTableInternal() {
        if (!checkTableExists()) {
            throw new IllegalStateException("Table " + tableName + " does not exist");
        }
        Table table = getTable();
        try {
            System.out.println("Issuing DeleteTable request for " + tableName);
            table.delete();
            System.out.println("Waiting for " + tableName
                    + " to be deleted...this may take a while...");
            table.waitForDelete();
        } catch (Exception e) {
            System.err.println("DeleteTable request failed for " + tableName);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds a new item to dynamo DB table if deviceId, parentId is not present
     * @param deviceId identifier of the device
     * @param parentId identifier of the parent item, zero for top level item
     * @param values List containing Map of entries to be added. Every item in the list (every map) must contain
     *               an entry with key ATTR_ITEM_ID.
     */
    public void addItem(String deviceId, String parentId, List<Map<String, String>> values) {
        // Ensure that item_id is present in every list item
        for (Map kvPairs : values) {
            if (kvPairs.get(ATTR_ITEM_ID) == null) {
                throw new IllegalArgumentException("Required attribute " + ATTR_ITEM_ID +
                        " is missing in the specified list of values");
            }
        }
        // ToDo: Add code to not replace existing entries
        Table table = getTable();
        Item item = new Item()
                .withPrimaryKey(ATTR_DEVICE_ID, deviceId, ATTR_PARENT_ID, parentId)
                .withList(ATTR_ITEM_INFO, values);
        PutItemOutcome outcome = table.putItem(item);
        int httpStatusCode = outcome.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode();
        if (httpStatusCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("DynamoDB addItem failed with http status code " + httpStatusCode);
        }
    }

    public Item getItem(String deviceId, String parentId) {
        Table table = getTable();
        return table.getItem(ATTR_DEVICE_ID, deviceId, ATTR_PARENT_ID, parentId);
    }

    public void deleteItem(String deviceId, String parentId) {
        // ToDo: Add code to check that there are no child items
        Table table = getTable();
        DeleteItemOutcome outcome = table.deleteItem(ATTR_DEVICE_ID, deviceId, ATTR_PARENT_ID, parentId);
        int httpStatusCode = outcome.getDeleteItemResult().getSdkHttpMetadata().getHttpStatusCode();
        if (httpStatusCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("DynamoDB deleteItem failed with http status code " + httpStatusCode);
        }
    }
}
