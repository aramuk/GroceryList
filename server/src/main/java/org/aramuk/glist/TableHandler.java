package org.aramuk.glist;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;

/**
 * Creates AWS DynamoDB table to maintain items in a hierarchy. Overall JSON representation
 * is as follows.
 * <code>
 *     "items": [
 *         {
 *             "device_id": "12345678", // Uniquely identifies one instance of the application
 *             "parent_item_id": "0", // Top level items are always stored with zero id
 *             "item_info": [
 *                 {"item_id": "123", "value": "Trader Joe's"},
 *                 {"item_id": "345", "value": "Costco"},
 *                 {"item_id": "567", "value": "Whole Foods"}
 *             ]
 *          },
 *          {
 *              "device_id": "12345678",
 *              "parent_item_id": "123",
 *              "item_info": [
 *                  {"item_id": "901", "value": "Balsamic Vinegar"},
 *                  {"item_id": "902", "value": "Waffles"}
 *              ]
 *          },
 *          {
 *              "device_id": "87654321",
 *              "parent_item_id": "0",
 *              "item_info": [
 *                  {"item_id": "ABC", "value": "Rock Climbing"},
 *                  {"item_id": "DEF", "value": "Skiing"}
 *              ]
 *          }
 *      ]
 * </code>
 */
public class TableHandler {

    public static final String DEFAULT_TABLE_NAME = "items";
    public static final String ATTR_DEVICE_ID = "device_id";
    public static final String ATTR_PARENT_ID = "parent_item_id";
    public static final String ATTR_ITEM_INFO = "item_info";
    public static final String ATTR_ITEM_ID = "item_id";
    public static final String ATTR_ITEM_VALUE = "value";

    private AmazonDynamoDB dynamoDBClient;
    private String tableName;

    public static void main(String[] args) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("init")) {
                TableHandler handler = new TableHandler();
                if (handler.checkTableExists()) {
                    handler.deleteTable();
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
    }

    private DynamoDB getDynamoDB() {
        if (dynamoDBClient != null) {
            return new DynamoDB(dynamoDBClient);
        }
        throw new IllegalStateException("DynamoDBClient not initialized. Using object after releasing resources?");
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

    public void deleteTable() {
        DynamoDB dynamoDB = getDynamoDB();
        Table table = dynamoDB.getTable(tableName);
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
     * @param values List containing Map of entries
     */
    public void addItem(String deviceId, String parentId, List values) {
        DynamoDB dynamoDB = getDynamoDB();
        Table table = dynamoDB.getTable(tableName);
        Item item = new Item()
                .withPrimaryKey(ATTR_DEVICE_ID, deviceId, ATTR_PARENT_ID, parentId)
                .withList(ATTR_ITEM_INFO, values);
        PutItemOutcome outcome = table.putItem(item);
        System.out.println("AddItemDone with HTTP Status code: "
                + outcome.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode());
    }
}
