/*
 * Copyright © 2018 Copyright (c) 2021 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.example.impl;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Optional;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.AddNameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.AddNameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.AddNameOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameOutputBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldOutputBuilder;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleProvider implements ExampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);

    private final DataBroker dataBroker;

    public ExampleProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public ListenableFuture<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
        HelloWorldOutputBuilder helloBuilder = new HelloWorldOutputBuilder();
        helloBuilder.setGreeting("Hello " + input.getName());
        return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ExampleProvider Session Initiated");
        LOG.info("linhpt21 test change");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ExampleProvider Closed");
    }

    /**
     * Method to add a property to the data store.
     */
    public void addProperty(String name, String value) {
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        ExampleContainer exampleContainer = new ExampleContainerBuilder()
                .setName(name)
                .setValue(value)
                .build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, iid, exampleContainer);
        transaction.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo result) {
                LOG.info("Property added successfully");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Failed to add property", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<RpcResult<AddNameOutput>> addName(AddNameInput input) {
        addProperty(input.getName(), input.getValue());
        AddNameOutputBuilder outputBuilder = new AddNameOutputBuilder();
        outputBuilder.setResult("Name added successfully");
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetNameOutput>> getName(GetNameInput input) {
        ReadTransaction transaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        ListenableFuture<Optional<ExampleContainer>> future = transaction.read(LogicalDatastoreType.CONFIGURATION, iid);

        // Add a callback to print the value of the future
        Futures.addCallback(future, new FutureCallback<Optional<ExampleContainer>>() {
            @Override
            public void onSuccess(Optional<ExampleContainer> result) {
                if (result.isPresent()) {
                    LOG.info("Future result: {}", result.get());
                } else {
                    LOG.info("Future result: empty");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Failed to get future result", throwable);
            }
        }, MoreExecutors.directExecutor());

        return Futures.transform(future, optional -> {
            if (optional.isPresent() && optional.get().getName().equals(input.getName())) {
                GetNameOutputBuilder outputBuilder = new GetNameOutputBuilder();
                outputBuilder.setValue(optional.get().getValue());
                return RpcResultBuilder.success(outputBuilder.build()).build();
            } else {
                return RpcResultBuilder.<GetNameOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "Name not found")
                        .build();
            }
        }, MoreExecutors.directExecutor());
    }

    public ListenableFuture<Optional<ExampleContainer>> getExample() {
        ReadTransaction transaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        return transaction.read(LogicalDatastoreType.CONFIGURATION, iid);
    }

    public ListenableFuture<RpcResult<Void>> addExample(String name, String value) {
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        ExampleContainer exampleContainer = new ExampleContainerBuilder().setName(name).setValue(value).build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, iid, exampleContainer);
        FluentFuture<? extends CommitInfo> commitFuture = transaction.commit();
        return Futures.transform(commitFuture, commitInfo -> RpcResultBuilder.<Void>success().build(),
                MoreExecutors.directExecutor());
    }

}