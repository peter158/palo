// Modifications copyright (C) 2017, Baidu.com, Inc.
// Copyright 2017 The Apache Software Foundation

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.baidu.palo.common.proc;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.baidu.palo.catalog.Catalog;
import com.baidu.palo.common.AnalysisException;
import com.baidu.palo.persist.EditLog;
import com.baidu.palo.system.Backend;
import com.google.common.collect.Lists;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("org.apache.log4j.*")
@PrepareForTest(Catalog.class)
public class BackendProcNodeTest {
    private Backend b1;
    private Catalog catalog;
    private EditLog editLog;

    @Before
    public void setUp() {
        editLog = EasyMock.createMock(EditLog.class);
        editLog.logAddBackend(EasyMock.anyObject(Backend.class));
        EasyMock.expectLastCall().anyTimes();
        editLog.logDropBackend(EasyMock.anyObject(Backend.class));
        EasyMock.expectLastCall().anyTimes();
        editLog.logBackendStateChange(EasyMock.anyObject(Backend.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(editLog);

        catalog = EasyMock.createMock(Catalog.class);
        EasyMock.expect(catalog.getNextId()).andReturn(10000L).anyTimes();
        EasyMock.expect(catalog.getEditLog()).andReturn(editLog).anyTimes();
        catalog.clear();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(catalog);

        PowerMock.mockStatic(Catalog.class);
        EasyMock.expect(Catalog.getInstance()).andReturn(catalog).anyTimes();
        PowerMock.replay(Catalog.class);

        b1 = new Backend(1000, "host1", 10000);
        b1.updateOnce(10001, 10003, 10005);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testResultNormal() throws AnalysisException {
        BackendProcNode node = new BackendProcNode(b1);
        ProcResult result;

        // fetch result
        result = node.fetchResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof BaseProcResult);

        Assert.assertTrue(result.getRows().size() >= 1);
        Assert.assertEquals(Lists.newArrayList("RootPath", "TotalCapacity", "AvailableCapacity", "State"),
                            result.getColumnNames());
    }

}
