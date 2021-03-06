/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.kafka;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sitewhere.event.DeviceEventManagementDecorator;
import com.sitewhere.event.spi.microservice.IEventManagementMicroservice;
import com.sitewhere.event.spi.microservice.IEventManagementTenantEngine;
import com.sitewhere.grpc.kafka.model.KafkaModel.GPersistedEventPayload;
import com.sitewhere.grpc.model.converter.KafkaModelConverter;
import com.sitewhere.grpc.model.marshaler.KafkaModelMarshaler;
import com.sitewhere.rest.model.microservice.kafka.payload.PersistedEventPayload;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceEventManagement;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurements;
import com.sitewhere.spi.device.event.IDeviceStateChange;
import com.sitewhere.spi.device.event.IDeviceStreamData;
import com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStreamDataCreateRequest;
import com.sitewhere.spi.device.streaming.IDeviceStream;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;

/**
 * Adds triggers to event persistence methods to push the new events into a
 * Kafka topic.
 * 
 * @author Derek
 */
public class KafkaEventPersistenceTriggers extends DeviceEventManagementDecorator {

    /** Static logger instance */
    @SuppressWarnings("unused")
    private static Log LOGGER = LogFactory.getLog(KafkaEventPersistenceTriggers.class);

    /** Parent tenant engine */
    private IEventManagementTenantEngine tenantEngine;

    public KafkaEventPersistenceTriggers(IEventManagementTenantEngine tenantEngine, IDeviceEventManagement delegate) {
	super(delegate);
	this.tenantEngine = tenantEngine;
    }

    /**
     * Forward the given event to the Kafka persisted events topic.
     * 
     * @param deviceAssignmentId
     * @param event
     * @return
     * @throws SiteWhereException
     */
    protected <T extends IDeviceEvent> T forwardEvent(UUID deviceAssignmentId, T event) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	PersistedEventPayload api = new PersistedEventPayload();
	api.setDeviceId(assignment.getDeviceId());
	api.setEvent(event);
	GPersistedEventPayload payload = KafkaModelConverter.asGrpcPersistedEventPayload(api);

	getTenantEngine().getInboundPersistedEventsProducer().send(assignment.getId().toString(),
		KafkaModelMarshaler.buildPersistedEventPayloadMessage(payload));
	return event;
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceMeasurements(java
     * .util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceMeasurementsCreateRequest)
     */
    @Override
    public IDeviceMeasurements addDeviceMeasurements(UUID deviceAssignmentId,
	    IDeviceMeasurementsCreateRequest measurements) throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceMeasurements(deviceAssignmentId, measurements));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceLocation(java.
     * util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest)
     */
    @Override
    public IDeviceLocation addDeviceLocation(UUID deviceAssignmentId, IDeviceLocationCreateRequest request)
	    throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceLocation(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceAlert(java.util.
     * UUID, com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest)
     */
    @Override
    public IDeviceAlert addDeviceAlert(UUID deviceAssignmentId, IDeviceAlertCreateRequest request)
	    throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceAlert(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceStreamData(java.
     * util.UUID, com.sitewhere.spi.device.streaming.IDeviceStream,
     * com.sitewhere.spi.device.event.request.IDeviceStreamDataCreateRequest)
     */
    @Override
    public IDeviceStreamData addDeviceStreamData(UUID deviceAssignmentId, IDeviceStream stream,
	    IDeviceStreamDataCreateRequest request) throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceStreamData(deviceAssignmentId, stream, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceCommandInvocation
     * (java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest)
     */
    @Override
    public IDeviceCommandInvocation addDeviceCommandInvocation(UUID deviceAssignmentId,
	    IDeviceCommandInvocationCreateRequest request) throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceCommandInvocation(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceCommandResponse(
     * java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest)
     */
    @Override
    public IDeviceCommandResponse addDeviceCommandResponse(UUID deviceAssignmentId,
	    IDeviceCommandResponseCreateRequest request) throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceCommandResponse(deviceAssignmentId, request));
    }

    /*
     * @see
     * com.sitewhere.event.DeviceEventManagementDecorator#addDeviceStateChange(java.
     * util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest)
     */
    @Override
    public IDeviceStateChange addDeviceStateChange(UUID deviceAssignmentId, IDeviceStateChangeCreateRequest request)
	    throws SiteWhereException {
	return forwardEvent(deviceAssignmentId, super.addDeviceStateChange(deviceAssignmentId, request));
    }

    /**
     * Assert that a device assignment exists and throw an exception if not.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assertDeviceAssignmentById(UUID id) throws SiteWhereException {
	IDeviceAssignment assignment = getDeviceManagement().getDeviceAssignment(id);
	if (assignment == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentId, ErrorLevel.ERROR);
	}
	return assignment;
    }

    protected IDeviceManagement getDeviceManagement() {
	return ((IEventManagementMicroservice) getTenantEngine().getMicroservice()).getDeviceManagementApiDemux()
		.getApiChannel();
    }

    public IEventManagementTenantEngine getTenantEngine() {
	return tenantEngine;
    }

    public void setTenantEngine(IEventManagementTenantEngine tenantEngine) {
	this.tenantEngine = tenantEngine;
    }
}