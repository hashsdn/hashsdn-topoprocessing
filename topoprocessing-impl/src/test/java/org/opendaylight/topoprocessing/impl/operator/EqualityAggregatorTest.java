/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.topoprocessing.impl.operator;

import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.topoprocessing.impl.structure.LogicalNode;
import org.opendaylight.topoprocessing.impl.structure.PhysicalNode;
import org.opendaylight.topoprocessing.impl.structure.TopologyStore;
import org.opendaylight.topoprocessing.impl.testUtilities.TestNodeCreator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

/**
 * @author matus.marko
 */
@RunWith(MockitoJUnitRunner.class)
public class EqualityAggregatorTest {

    private static final QName ROOT_QNAME = QName.create("foo", "2014-03-13", "bar");
    private static final QName LIST_IP_QNAME = QName.create(ROOT_QNAME, "ip");
    private static final QName LEAF_IP_QNAME = QName.create(ROOT_QNAME, "ip-id");
    private static final QName QNAME_LEAF_IP = QName.create(ROOT_QNAME, "ip");

    private static final String TOPO1 = "topo1";
    private static final String TOPO2 = "topo2";
    private static final String TOPO3 = "topo3";
    private static final String TOPO4 = "topo4";

    private TopologyAggregator aggregator;
    private YangInstanceIdentifier leafYiid11, leafYiid12, leafYiid21, leafYiid22, leafYiid23;
    private LeafNode<String> leafNode11;
    private TestNodeCreator testNodeCreator;

    @Mock private NormalizedNode<?,?> mockNormalizedNode1, mockNormalizedNode2;
    @Mock private TopologyManager mockManager;

    /**
     * Setup schema
     *
     * <pre>
     * TOPO1 {
     *     node11: 192.168.1.1;
     * }
     *
     * TOPO2 {
     *     node12: 192.168.1.2;
     * }
     * </pre>
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // initialize and set up topology stores
        aggregator = new EqualityAggregator();
        aggregator.initializeStore(TOPO1, false);
        aggregator.initializeStore(TOPO2, false);
        aggregator.initializeStore(TOPO3, false);
        aggregator.initializeStore(TOPO4, false);

        TopologyStore topo1 = aggregator.getTopologyStore(TOPO1);
        TopologyStore topo2 = aggregator.getTopologyStore(TOPO2);

        // fill topology stores
        testNodeCreator = new TestNodeCreator();
        leafYiid11 = testNodeCreator.createNodeIdYiid("11");
        leafNode11 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.1");
        leafYiid12 = testNodeCreator.createNodeIdYiid("12");
        LeafNode<String> leafNode12 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.2");
        PhysicalNode physicalNode1 = new PhysicalNode(mockNormalizedNode1, leafNode11, TOPO1, "11");
        PhysicalNode physicalNode2 = new PhysicalNode(mockNormalizedNode1, leafNode12, TOPO2, "12");
        topo1.getPhysicalNodes().put(leafYiid11, physicalNode1);
        topo2.getPhysicalNodes().put(leafYiid12, physicalNode2);

        aggregator.setTopologyManager(mockManager);
    }

    /**
     * Create changes schema
     *
     * Change 1 - add:
     * <pre>
     *     TOPO2 {
     *         node21: 192.168.1.1;
     *         node22: 192.168.1.3;
     *     }
     * </pre>
     *
     * Change 2 - add:
     * <pre>
     *     TOPO3 {
     *         node23: 192.168.1.1;
     *     }
     * </pre>
     *
     * Result:
     * <pre>
     *     LOGICAL1 {
     *          node11 (192.168.1.1, TOPO1);
     *          node21 (192.168.1.1, TOPO2);
     *          node23 (192.168.1.1, TOPO3);
     *     }
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testProcessCreatedChanges() throws Exception {
        // change 1
        leafYiid21 = testNodeCreator.createNodeIdYiid("21");
        LeafNode<String> leafNode21 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.1");
        leafYiid22 = testNodeCreator.createNodeIdYiid("22");
        LeafNode<String> leafNode22 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.3");
        PhysicalNode physicalNode1 = new PhysicalNode(mockNormalizedNode1, leafNode21, TOPO2, "21");
        PhysicalNode physicalNode2 = new PhysicalNode(mockNormalizedNode1, leafNode22, TOPO2, "22");
        Map<YangInstanceIdentifier, PhysicalNode> physicalNodes1 = new HashMap<>();
        physicalNodes1.put(leafYiid21, physicalNode1);
        physicalNodes1.put(leafYiid22, physicalNode2);

        aggregator.processCreatedChanges(physicalNodes1, TOPO2);

        Mockito.verify(mockManager, Mockito.times(1)).addLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).removeLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).updateLogicalNode((LogicalNode) any());

        // change 2
        leafYiid23 = testNodeCreator.createNodeIdYiid("23");
        LeafNode<String> leafNode23 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.1");
        PhysicalNode physicalNode3 = new PhysicalNode(mockNormalizedNode1, leafNode23, TOPO3, "23");
        Map<YangInstanceIdentifier, PhysicalNode> physicalNodes2 = new HashMap<>();
        physicalNodes2.put(leafYiid23, physicalNode3);

        aggregator.processCreatedChanges(physicalNodes2, TOPO3);

        // one physical node in topology store TOPO1
        Assert.assertEquals(1, aggregator.getTopologyStore(TOPO1).getPhysicalNodes().size());
        // three physical nodes in topology store TOPO2
        Assert.assertEquals(3, aggregator.getTopologyStore(TOPO2).getPhysicalNodes().size());
        // one physical node in topology store TOPO3
        Assert.assertEquals(1, aggregator.getTopologyStore(TOPO3).getPhysicalNodes().size());
        // no physical node in topology store TOPO4
        Assert.assertEquals(0, aggregator.getTopologyStore(TOPO4).getPhysicalNodes().size());

        Mockito.verify(mockManager, Mockito.times(1)).addLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).removeLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(1)).updateLogicalNode((LogicalNode) any());
    }

    /**
     * Remove changes schema
     * input data form createChanges
     *
     * Change 1 - remove:
     * <pre>
     *     TOPO1 {
     *         node11: 192.168.1.1;
     *     }
     * </pre>
     *
     * * Change 2 - remove:
     * <pre>
     *     TOPO2 {
     *         node21: 192.168.1.1;
     *     }
     * </pre>
     *
     * Result: Logical node removed
     *
     * @throws Exception
     */
    @Test
    public void testProcessRemovedChanges() throws Exception {
        testProcessCreatedChanges();

        // case 1
        ArrayList<YangInstanceIdentifier> remove1 = new ArrayList<>();
        remove1.add(leafYiid11);
        aggregator.processRemovedChanges(remove1, TOPO1);

        // no physical nodes in topology store TOPO1
        Assert.assertEquals(0, aggregator.getTopologyStore(TOPO1).getPhysicalNodes().size());

        Mockito.verify(mockManager, Mockito.times(1)).addLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).removeLogicalNode((LogicalNode) any());
        // one logical node was updated
        Mockito.verify(mockManager, Mockito.times(2)).updateLogicalNode((LogicalNode) any());

        // case 2
        ArrayList<YangInstanceIdentifier> remove2 = new ArrayList<>();
        remove2.add(leafYiid21);
        aggregator.processRemovedChanges(remove2, TOPO2);

        // two physical nodes in topology store TOPO2
        Assert.assertEquals(2, aggregator.getTopologyStore(TOPO2).getPhysicalNodes().size());

        Mockito.verify(mockManager, Mockito.times(1)).addLogicalNode((LogicalNode) any());
        // one logical node has been removed
        Mockito.verify(mockManager, Mockito.times(1)).removeLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(2)).updateLogicalNode((LogicalNode) any());
    }

    /**
     * Update changes 1 schema
     * input data form createChanges
     *
     * Change 1 - update:
     * <pre>
     *     TOPO1 {
     *         node11: 192.168.1.1 -> 192.168.1.2;
     *     }
     * </pre>
     *
     * Result:
     * <pre>
     *     LOGICAL1 {
     *          node21 (192.168.1.1, TOPO2);
     *          node23 (192.168.1.1, TOPO3);
     *     }
     *     LOGICAL2 {
     *          node11 (192.168.1.2, TOPO1);
     *          node12 (192.168.1.2, TOPO2);
     *     }
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testProcessUpdatedChanges1() throws Exception {
        testProcessCreatedChanges();
        LeafNode<String> leafNode31 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.2");
        PhysicalNode physicalNode31 = new PhysicalNode(mockNormalizedNode1, leafNode31, TOPO1, "11");
        Map<YangInstanceIdentifier, PhysicalNode> update = new HashMap<>();
        update.put(leafYiid11, physicalNode31);
        aggregator.processUpdatedChanges(update, TOPO1);

        // one new logical node has been created
        Mockito.verify(mockManager, Mockito.times(2)).addLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).removeLogicalNode((LogicalNode) any());
        // two logical nodes have been updated
        Mockito.verify(mockManager, Mockito.times(2)).updateLogicalNode((LogicalNode) any());
    }

    /**
     * Update changes 2 schema
     * input data form updateChanges1
     *
     * Change 1 - update:
     * <pre>
     *     TOPO3 {
     *         node23: 192.168.1.1 -> 192.168.1.2;
     *     }
     * </pre>
     *
     * Result:
     * <pre>
     *     LOGICAL1 - removed
     *     LOGICAL2 {
     *          node11 (192.168.1.2, TOPO1);
     *          node12 (192.168.1.2, TOPO2);
     *          node23 (192.168.1.2, TOPO3);
     *     }
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testProcessUpdatedChanges2() throws Exception {
        testProcessUpdatedChanges1();

        LeafNode<String> leafNode32 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.2");
        PhysicalNode physicalNode = new PhysicalNode(mockNormalizedNode1, leafNode32, TOPO3, "23");
        Map<YangInstanceIdentifier, PhysicalNode> update = new HashMap<>();
        update.put(leafYiid23, physicalNode);
        aggregator.processUpdatedChanges(update, TOPO3);

        Mockito.verify(mockManager, Mockito.times(2)).addLogicalNode((LogicalNode) any());
        // one logical node has been removed
        Mockito.verify(mockManager, Mockito.times(1)).removeLogicalNode((LogicalNode) any());
        // three logical node have been updated
        Mockito.verify(mockManager, Mockito.times(3)).updateLogicalNode((LogicalNode) any());
    }

    /**
     * Update changes 3 schema
     * input data form createChanges
     *
     * Change 1 - update:
     * - modifying physical node not belonging to any logical node
     * <pre>
     *     TOPO2 {
     *         node22: 192.168.1.3 -> 192.168.1.2;
     *     }
     * </pre>
     *
     * Result:
     * <pre>
     *     LOGICAL1 {
     *          node11 (192.168.1.1, TOPO1);
     *          node21 (192.168.1.1, TOPO2);
     *          node23 (192.168.1.1, TOPO3);
     *     }
     *     physical not pertaining to any logical node
     *          node12 (192.168.1.2, TOPO2);
     *          node22 (192.168.1.2, TOPO2);
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testProcessUpdatedChanges3() throws Exception {
        testProcessCreatedChanges();
        LeafNode<String> leafNode31 = ImmutableNodes.leafNode(QNAME_LEAF_IP, "192.168.1.2");
        PhysicalNode physicalNode31 = new PhysicalNode(mockNormalizedNode1, leafNode31, TOPO2, "22");
        Map<YangInstanceIdentifier, PhysicalNode> update = new HashMap<>();
        update.put(leafYiid22, physicalNode31);
        aggregator.processUpdatedChanges(update, TOPO2);

        // Even though nodes node12 and node22 have now the same IP 192.168.1.2, they cannot create aggregated
        // node because they belong to the same topology TOPO2
        Mockito.verify(mockManager, Mockito.times(1)).addLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(0)).removeLogicalNode((LogicalNode) any());
        Mockito.verify(mockManager, Mockito.times(1)).updateLogicalNode((LogicalNode) any());
    }
}