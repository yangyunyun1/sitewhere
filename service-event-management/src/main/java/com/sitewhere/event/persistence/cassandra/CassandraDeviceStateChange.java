/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.persistence.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.sitewhere.cassandra.CassandraClient;
import com.sitewhere.rest.model.device.event.DeviceStateChange;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceStateChange;

/**
 * Handles mapping of device state change event fields to Cassandra records.
 * 
 * @author Derek
 */
public class CassandraDeviceStateChange {

    // State change field.
    public static final String FIELD_STATE_CHANGE = "state_change";

    // Category field.
    public static final String FIELD_CATEGORY = "category";

    // Type field.
    public static final String FIELD_TYPE = "type";

    // Previous state field.
    public static final String FIELD_PREVIOUS_STATE = "previous_state";

    // New state field.
    public static final String FIELD_NEW_STATE = "new_state";

    /**
     * Bind fields from a device state change to an existing {@link BoundStatement}.
     * 
     * @param client
     * @param bound
     * @param state
     * @throws SiteWhereException
     */
    public static void bindFields(CassandraClient client, BoundStatement bound, IDeviceStateChange state)
	    throws SiteWhereException {
	CassandraDeviceEvent.bindEventFields(bound, state);

	UDTValue udt = client.getStateChangeType().newValue();
	udt.setString(FIELD_CATEGORY, state.getCategory());
	udt.setString(FIELD_TYPE, state.getType());
	udt.setString(FIELD_PREVIOUS_STATE, state.getPreviousState());
	udt.setString(FIELD_NEW_STATE, state.getNewState());
	bound.setUDTValue(FIELD_STATE_CHANGE, udt);
    }

    /**
     * Load fields from a row into a device state change event.
     * 
     * @param client
     * @param state
     * @param row
     * @throws SiteWhereException
     */
    public static void loadFields(CassandraClient client, DeviceStateChange state, Row row) throws SiteWhereException {
	CassandraDeviceEvent.loadEventFields(state, row);

	UDTValue udt = row.getUDTValue(FIELD_STATE_CHANGE);
	state.setCategory(udt.getString(FIELD_CATEGORY));
	state.setType(udt.getString(FIELD_TYPE));
	state.setPreviousState(udt.getString(FIELD_PREVIOUS_STATE));
	state.setNewState(udt.getString(FIELD_NEW_STATE));
    }
}