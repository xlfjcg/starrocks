// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.starrocks.lake;

import com.starrocks.common.jmockit.Deencapsulation;
import com.starrocks.proto.DeleteTabletRequest;
import com.starrocks.proto.DeleteTabletResponse;
import com.starrocks.rpc.BrpcProxy;
import com.starrocks.rpc.LakeService;
import com.starrocks.server.GlobalStateMgr;
import com.starrocks.system.Backend;
import com.starrocks.system.SystemInfoService;
import com.starrocks.thrift.TNetworkAddress;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ShardDeleterTest {

    private ShardDeleter shardDeleter = new ShardDeleter();

    @Mocked
    private GlobalStateMgr globalStateMgr;

    @Mocked
    private SystemInfoService systemInfoService;

    @Mocked
    private StarOSAgent starOSAgent;

    @Mocked
    private LakeService lakeService;

    @Mocked
    private Backend be;

    private Set<Long> ids = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        ids.add(1001L);
        ids.add(1002L);

        be = new Backend(100, "127.0.0.1", 8090);
        new MockUp<GlobalStateMgr>() {
            @Mock
            public SystemInfoService getCurrentSystemInfo() {
                return systemInfoService;
            }

            @Mock
            public StarOSAgent getStarOSAgent() {
                return starOSAgent;
            }
        };

        new MockUp<BrpcProxy>() {
            @Mock
            public LakeService getLakeService(TNetworkAddress address) {
                return lakeService;
            }
            @Mock
            public LakeService getLakeService(String host, int port) {
                return lakeService;
            }
        };

        new Expectations() {
            {
                starOSAgent.getPrimaryBackendIdByShard(anyLong);
                minTimes = 0;
                result = 1;

                systemInfoService.getBackend(1);
                minTimes = 0;
                result = be;
            }
        };

    }

    @Test
    public void testNormal() throws Exception {

        DeleteTabletResponse response = new DeleteTabletResponse();
        response.failedTablets = new ArrayList<>();

        new Expectations() {{
                lakeService.deleteTablet((DeleteTabletRequest) any);
                minTimes = 1;
                result = CompletableFuture.completedFuture(response);

                starOSAgent.deleteShards(ids);
                minTimes = 1;
                result = null;
            }};

        shardDeleter.addUnusedShardId(ids);
        shardDeleter.runAfterCatalogReady();

        Assert.assertEquals(Deencapsulation.getField(shardDeleter, "shardIds"), new HashSet<>());
    }
}
