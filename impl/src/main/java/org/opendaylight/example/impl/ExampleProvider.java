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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.ExampleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.GetNameOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.HelloWorldOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.example.container.ExampleList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.example.container.ExampleListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.example.rev180517.example.container.ExampleListKey;

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

    @Override
    public ListenableFuture<RpcResult<AddNameOutput>> addName(AddNameInput input) {
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        InstanceIdentifier<ExampleList> iid = InstanceIdentifier.builder(ExampleContainer.class)
                .child(ExampleList.class, new ExampleListKey(input.getName())).build();

        LOG.info("InstanceIdentifier for addName: {}", iid);
        LOG.info("WriteTransaction for addName: {}", transaction);

        ExampleList exampleList = new ExampleListBuilder().setName(input.getName()).setValue(input.getValue()).build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, iid, exampleList);
        FluentFuture<? extends CommitInfo> commitFuture = transaction.commit();
        return Futures.transform(commitFuture, commitInfo -> {
            AddNameOutputBuilder outputBuilder = new AddNameOutputBuilder();
            outputBuilder.setResult("Name added successfully");
            return RpcResultBuilder.success(outputBuilder.build()).build();
        }, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<RpcResult<GetNameOutput>> getName(GetNameInput input) {
        ReadTransaction transaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ExampleList> iid = InstanceIdentifier.builder(ExampleContainer.class)
                .child(ExampleList.class, new ExampleListKey(input.getName())).build();

        LOG.info("InstanceIdentifier for getName: {}", iid);
        LOG.info("WriteTransaction for getName: {}", transaction);

        ListenableFuture<Optional<ExampleList>> future = transaction.read(LogicalDatastoreType.CONFIGURATION, iid);

        Futures.addCallback(future, new FutureCallback<Optional<ExampleList>>() {
            @Override
            public void onSuccess(Optional<ExampleList> result) {
                if (result.isPresent()) {
                    LOG.info("Command executed successfully, result: {}", result.get());
                } else {
                    LOG.info("Command executed successfully, but no result found");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Command execution failed", throwable);
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

    public ListenableFuture<Optional<ExampleContainer>> getExample() {
        ReadTransaction transaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<ExampleContainer> iid = InstanceIdentifier.builder(ExampleContainer.class).build();
        LOG.info("InstanceIdentifier for getExample: {}", iid);
        LOG.info("WriteTransaction for getExample: {}", transaction);
        return transaction.read(LogicalDatastoreType.CONFIGURATION, iid);
    }

    public ListenableFuture<RpcResult<Void>> addExample(String name, String value) {
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        InstanceIdentifier<ExampleList> iid = InstanceIdentifier.builder(ExampleContainer.class)
                .child(ExampleList.class, new ExampleListKey(name)).build();

        LOG.info("InstanceIdentifier for getName: {}", iid);
        LOG.info("WriteTransaction for getName: {}", transaction);
        ExampleList exampleList = new ExampleListBuilder().setName(name).setValue(value).build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, iid, exampleList);
        FluentFuture<? extends CommitInfo> commitFuture = transaction.commit();
        return Futures.transform(commitFuture, commitInfo -> RpcResultBuilder.<Void>success().build(),
                MoreExecutors.directExecutor());
    }
}