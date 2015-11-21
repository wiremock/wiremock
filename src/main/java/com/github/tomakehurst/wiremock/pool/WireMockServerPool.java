/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.pool;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.google.common.base.Preconditions;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class WireMockServerPool {

    private static int max = 10;
    private static GenericKeyedObjectPool<Options, WireMockServer> serverPool;

    static {
        initialise(20);
    }

    public static void initialise(int maxServers) {
        Preconditions.checkArgument(maxServers > 0, "Pool must be limited to at least 1 server");
        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setMaxTotal(max);
        poolConfig.setMaxTotalPerKey(max);

        serverPool = new GenericKeyedObjectPool<Options, WireMockServer>(new PooledWireMockServerFactory(), poolConfig);
    }

    public static WireMockServer checkOut(Options options) {
        try {
            WireMockServer server = serverPool.borrowObject(options);
            System.out.printf("Checked a server out. Total created: %d, checked out: %d \n",
                    serverPool.getCreatedCount(),
                    serverPool.getBorrowedCount());
            return server;
        } catch (Exception e) {
            return throwUnchecked(e, WireMockServer.class);
        }
    }

    public static void checkIn(WireMockServer server) {
        serverPool.returnObject(server.getOptions(), server);
        System.out.printf("Checked a server in. Total created: %d, checked out: %d \n",
                serverPool.getCreatedCount(),
                serverPool.getBorrowedCount());
    }

    public static void reset() {
        serverPool.clear();
    }
}
