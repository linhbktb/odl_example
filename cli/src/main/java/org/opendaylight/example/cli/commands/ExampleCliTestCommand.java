/*
 * Copyright © 2018 Copyright (c) 2021 Yoyodyne, Inc/. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.example.cli.commands;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

import org.opendaylight.example.cli.api.ExampleCliCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example class. The class name can be renamed to match the command
 * implementation that it will invoke.
 * Specify command details by updating the fields in the Command annotation
 * below.
 */
@Command(name = "test-command", scope = "add the scope of the command, usually project name")
public class ExampleCliTestCommand extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleCliTestCommand.class);
    protected final ExampleCliCommands service;

    public ExampleCliTestCommand(final ExampleCliCommands service) {
        this.service = service;
    }

    /**
     * Add the arguments required by the command.
     * Any number of arguments can be added using the Option annotation
     * The below argument is just an example and should be changed as per your
     * requirements
     */
    @Option(name = "-tA", aliases = {
            "--testArgument" }, description = "test command argument", required = true, multiValued = false)
    private Object testArgument;

    @Override
    protected Object doExecute() throws Exception {
        /**
         * Invoke commannd implementation here using the service instance.
         * Implement how you want the output of the command to be displayed.
         * Below is just an example.
         */
        // Gọi triển khai lệnh và lấy ListenableFuture
        ListenableFuture<String> future = (ListenableFuture<String>) service.testCommand(testArgument);

        // Thêm một callback để xử lý kết quả
        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LOG.info("Command executed successfully, result: {}", result);
                session.getConsole().println(result);
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Command execution failed", throwable);
                session.getConsole().println("Command execution failed: " + throwable.getMessage());
            }
        }, MoreExecutors.directExecutor());

        // Trả về một thông báo chỉ ra rằng lệnh đang được thực thi bất đồng bộ
        return "Command is being executed asynchronously. Check logs for the result.";
    }
}
