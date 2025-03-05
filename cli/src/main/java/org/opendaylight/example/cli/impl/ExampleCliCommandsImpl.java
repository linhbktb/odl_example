/*
 * Copyright © 2018 Copyright (c) 2021 Yoyodyne, Inc/. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.example.cli.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Optional;

import org.opendaylight.example.cli.api.ExampleCliCommands;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleCliCommandsImpl implements ExampleCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public ExampleCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("ExampleCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object nameObject) {
        LOG.info("testCommand called with argument: {}", nameObject);

        //Read the name from the data store
        ReadTransaction readTx = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        ListenableFuture<Optional<ExampleContainer>> future = readTx.read(LogicalDatastoreType.CONFIGURATION, iid);

        // Add a callback to handle the result
        Futures.addCallback(future, new FutureCallback<Optional<ExampleContainer>>() {
            @Override
            public void onSuccess(Optional<ExampleContainer> result) {
                if (result.isPresent()) {
                    String name = result.get().getName();
                    LOG.info("Name retrieved from data store: {}", name);
                } else {
                    LOG.info("No data found in the data store");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Failed to read from data store", throwable);
            }
        }, MoreExecutors.directExecutor());

        return Futures.transform(future, optional -> {
            if (optional.isPresent() && optional.get().getName().equals(nameObject.toString())) {
                String value = optional.get().getValue().toString();
                LOG.info("Value retrieved from data store: {}", value);
                return value;
            } else {
                return "can find value of "  + nameObject.toString();
            }
        }, MoreExecutors.directExecutor());
    }
}
