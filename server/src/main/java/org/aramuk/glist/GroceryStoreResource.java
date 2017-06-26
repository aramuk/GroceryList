package org.aramuk.glist;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Grocery Store resource responsible to list, add and remove grocery stores.
 */
@Path("glist")
public class GroceryStoreResource {

    /**
     * Get a list of all stores.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroceryStore[] list(@QueryParam("device-id") String deviceId) {
        if (deviceId == null) {
            throw new RuntimeException("Error. Require Device Id.");
        }
        return GroceryStore.findAll(deviceId);
    }
}
