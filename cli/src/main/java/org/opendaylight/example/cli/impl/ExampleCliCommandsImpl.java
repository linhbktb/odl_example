/*
 * Copyright © 2018 Copyright (c) 2021 Yoyodyne, Inc/. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.example.cli.impl;

/*import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;*/

//import java.util.Optional;

import org.opendaylight.example.cli.api.ExampleCliCommands;

import org.opendaylight.mdsal.binding.api.DataBroker;
/*import org.opendaylight.mdsal.binding.api.ReadTransaction;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleContainer;*/
//import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

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
        return "testCommand called with argument: " + nameObject;
    }
}
