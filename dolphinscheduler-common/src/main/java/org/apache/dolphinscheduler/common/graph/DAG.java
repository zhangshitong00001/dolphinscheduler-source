/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.graph;

import org.apache.commons.collections4.CollectionUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 有向无环图（DAG）数据结构，支持拓扑排序和环检测。
 * 提供节点和边的添加、删除、查询功能，以及入度计算、前驱/后继节点查找等图分析操作。
 * 使用读写锁保证线程安全。
 *
 * @param <Node> 节点类型
 * @param <NodeInfo> 节点描述信息类型
 * @param <EdgeInfo> 边描述信息类型
 */
public class DAG<Node, NodeInfo, EdgeInfo> {
    private static final Logger logger = LoggerFactory.getLogger(DAG.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * node map, key is node, value is node information
     */
    private final Map<Node, NodeInfo> nodesMap;

    /**
     * edge map. key is node of origin;value is Map with key for destination node and value for edge
     */
    private final Map<Node, Map<Node, EdgeInfo>> edgesMap;

    /**
     * reversed edge set，key is node of destination, value is Map with key for origin node and value for edge
     */
    private final Map<Node, Map<Node, EdgeInfo>> reverseEdgesMap;

    public DAG() {
        nodesMap = new HashMap<>();
        edgesMap = new HashMap<>();
        reverseEdgesMap = new HashMap<>();
    }

    /**
     * 向DAG中添加一个节点及其描述信息。
     *
     * @param node 节点
     * @param nodeInfo 节点描述信息
     */
    public void addNode(Node node, NodeInfo nodeInfo) {
        lock.writeLock().lock();

        try {
            nodesMap.put(node, nodeInfo);
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * 添加一条从fromNode到toNode的有向边。
     *
     * @param fromNode 起始节点
     * @param toNode 目标节点
     * @return 添加成功返回true，如果会形成环则返回false
     */
    public boolean addEdge(Node fromNode, Node toNode) {
        return addEdge(fromNode, toNode, false);
    }

    /**
     * add edge
     *
     * @param fromNode node of origin
     * @param toNode node of destination
     * @param createNode whether the node needs to be created if it does not exist
     * @return The result of adding an edge. returns false if the DAG result is a ring result
     */
    private boolean addEdge(Node fromNode, Node toNode, boolean createNode) {
        return addEdge(fromNode, toNode, null, createNode);
    }

    /**
     * 添加一条带描述信息的有向边，可选择在节点不存在时自动创建。
     *
     * @param fromNode 起始节点
     * @param toNode 目标节点
     * @param edge 边描述信息
     * @param createNode 节点不存在时是否自动创建
     * @return 添加成功返回true，如果会形成环则返回false
     */
    public boolean addEdge(Node fromNode, Node toNode, EdgeInfo edge, boolean createNode) {
        lock.writeLock().lock();

        try {
            // Whether an edge can be successfully added(fromNode -> toNode)
            if (!isLegalAddEdge(fromNode, toNode, createNode)) {
                logger.error("serious error: add edge({} -> {}) is invalid, cause cycle！", fromNode, toNode);
                return false;
            }

            addNodeIfAbsent(fromNode, null);
            addNodeIfAbsent(toNode, null);

            addEdge(fromNode, toNode, edge, edgesMap);
            addEdge(toNode, fromNode, edge, reverseEdgesMap);

            return true;
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * 判断图中是否包含指定的节点。
     *
     * @param node 节点
     * @return 如果包含返回true
     */
    public boolean containsNode(Node node) {
        lock.readLock().lock();

        try {
            return nodesMap.containsKey(node);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 判断图中是否包含指定从fromNode到toNode的边。
     *
     * @param fromNode 起始节点
     * @param toNode 目标节点
     * @return 如果包含该边返回true
     */
    public boolean containsEdge(Node fromNode, Node toNode) {
        lock.readLock().lock();
        try {
            Map<Node, EdgeInfo> endEdges = edgesMap.get(fromNode);
            if (endEdges == null) {
                return false;
            }

            return endEdges.containsKey(toNode);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定节点的描述信息。
     *
     * @param node 节点
     * @return 节点描述信息，如果不存在则返回null
     */
    public NodeInfo getNode(Node node) {
        lock.readLock().lock();

        try {
            return nodesMap.get(node);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取图中节点的总数。
     *
     * @return 节点数量
     */
    public int getNodesCount() {
        lock.readLock().lock();

        try {
            return nodesMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取图中边的总数。
     *
     * @return 边的数量
     */
    public int getEdgesCount() {
        lock.readLock().lock();
        try {
            int count = 0;

            for (Map.Entry<Node, Map<Node, EdgeInfo>> entry : edgesMap.entrySet()) {
                count += entry.getValue().size();
            }

            return count;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取DAG的所有起始节点（入度为0的节点）。
     *
     * @return 起始节点集合
     */
    public Collection<Node> getBeginNode() {
        lock.readLock().lock();

        try {
            return CollectionUtils.subtract(nodesMap.keySet(), reverseEdgesMap.keySet());
        } finally {
            lock.readLock().unlock();
        }

    }

    /**
     * 获取DAG的所有终止节点（出度为0的节点）。
     *
     * @return 终止节点集合
     */
    public Collection<Node> getEndNode() {
        lock.readLock().lock();

        try {
            return CollectionUtils.subtract(nodesMap.keySet(), edgesMap.keySet());
        } finally {
            lock.readLock().unlock();
        }

    }

    /**
     * 获取指定节点的所有前驱节点。
     *
     * @param node 目标节点
     * @return 所有前驱节点的集合
     */
    public Set<Node> getPreviousNodes(Node node) {
        lock.readLock().lock();

        try {
            return getNeighborNodes(node, reverseEdgesMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定节点的所有后继节点。
     *
     * @param node 目标节点
     * @return 所有后继节点的集合
     */
    public Set<Node> getSubsequentNodes(Node node) {
        lock.readLock().lock();

        try {
            return getNeighborNodes(node, edgesMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定节点的入度。
     *
     * @param node 目标节点
     * @return 入度值
     */
    public int getIndegree(Node node) {
        lock.readLock().lock();

        try {
            return getPreviousNodes(node).size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 判断图中是否存在环。
     *
     * @return 如果存在环返回true，否则返回false
     */
    public boolean hasCycle() {
        lock.readLock().lock();
        try {
            return !topologicalSortImpl().getKey();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 对DAG执行拓扑排序，仅当图中无环时才能成功排序。
     *
     * @return 拓扑排序后的节点列表
     * @throws Exception 如果图中存在环则抛出异常
     */
    public List<Node> topologicalSort() throws Exception {
        lock.readLock().lock();

        try {
            Map.Entry<Boolean, List<Node>> entry = topologicalSortImpl();

            if (entry.getKey()) {
                return entry.getValue();
            }

            throw new Exception("serious error: graph has cycle ! ");
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * if tho node does not exist,add this node
     *
     * @param node node
     * @param nodeInfo node information
     */
    private void addNodeIfAbsent(Node node, NodeInfo nodeInfo) {
        if (!containsNode(node)) {
            addNode(node, nodeInfo);
        }
    }

    /**
     * add edge
     *
     * @param fromNode node of origin
     * @param toNode node of destination
     * @param edge edge description
     * @param edges edge set
     */
    private void addEdge(Node fromNode, Node toNode, EdgeInfo edge, Map<Node, Map<Node, EdgeInfo>> edges) {
        edges.putIfAbsent(fromNode, new HashMap<>());
        Map<Node, EdgeInfo> toNodeEdges = edges.get(fromNode);
        toNodeEdges.put(toNode, edge);
    }

    /**
     * Whether an edge can be successfully added(fromNode -> toNode)
     * need to determine whether the DAG has cycle
     *
     * @param fromNode node of origin
     * @param toNode node of destination
     * @param createNode whether to create a node
     * @return true if added
     */
    private boolean isLegalAddEdge(Node fromNode, Node toNode, boolean createNode) {
        if (fromNode.equals(toNode)) {
            logger.error("edge fromNode({}) can't equals toNode({})", fromNode, toNode);
            return false;
        }

        if (!createNode) {
            if (!containsNode(fromNode) || !containsNode(toNode)) {
                logger.error("edge fromNode({}) or toNode({}) is not in vertices map", fromNode, toNode);
                return false;
            }
        }

        // Whether an edge can be successfully added(fromNode -> toNode),need to determine whether the DAG has cycle!
        int verticesCount = getNodesCount();

        Queue<Node> queue = new LinkedList<>();

        queue.add(toNode);

        // if DAG doesn't find fromNode, it's not has cycle!
        while (!queue.isEmpty() && (--verticesCount > 0)) {
            Node key = queue.poll();

            for (Node subsequentNode : getSubsequentNodes(key)) {
                if (subsequentNode.equals(fromNode)) {
                    return false;
                }

                queue.add(subsequentNode);
            }
        }

        return true;
    }

    /**
     * Get all neighbor nodes of the node
     *
     * @param node Node id to be calculated
     * @param edges neighbor edge information
     * @return all neighbor nodes of the node
     */
    private Set<Node> getNeighborNodes(Node node, final Map<Node, Map<Node, EdgeInfo>> edges) {
        final Map<Node, EdgeInfo> neighborEdges = edges.get(node);
        if (neighborEdges == null) {
            return Collections.emptySet();
        }
        return neighborEdges.keySet();
    }

    /**
     * Determine whether there are ring and topological sorting results
     * <p>
     * Directed acyclic graph (DAG) has topological ordering
     * Breadth First Search：
     * 1、Traversal of all the vertices in the graph, the degree of entry is 0 vertex into the queue
     * 2、Poll a vertex in the queue to update its adjacency (minus 1) and queue the adjacency if it is 0 after minus 1
     * 3、Do step 2 until the queue is empty
     * If you cannot traverse all the nodes, it means that the current graph is not a directed acyclic graph.
     * There is no topological sort.
     *
     * @return key Returns the state
     * if success (acyclic) is true, failure (acyclic) is looped,
     * and value (possibly one of the topological sort results)
     */
    private Map.Entry<Boolean, List<Node>> topologicalSortImpl() {
        // node queue with degree of entry 0
        Queue<Node> zeroIndegreeNodeQueue = new LinkedList<>();
        // save result
        List<Node> topoResultList = new ArrayList<>();
        // save the node whose degree is not 0
        Map<Node, Integer> notZeroIndegreeNodeMap = new HashMap<>();

        // Scan all the vertices and push vertexs with an entry degree of 0 to queue
        for (Map.Entry<Node, NodeInfo> vertices : nodesMap.entrySet()) {
            Node node = vertices.getKey();
            int inDegree = getIndegree(node);

            if (inDegree == 0) {
                zeroIndegreeNodeQueue.add(node);
                topoResultList.add(node);
            } else {
                notZeroIndegreeNodeMap.put(node, inDegree);
            }
        }

        /*
         * After scanning, there is no node with 0 degree of entry,
         * indicating that there is a ring, and return directly
         */
        if (zeroIndegreeNodeQueue.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(false, topoResultList);
        }

        // The topology algorithm is used to delete nodes with 0 degree of entry and its associated edges
        while (!zeroIndegreeNodeQueue.isEmpty()) {
            Node v = zeroIndegreeNodeQueue.poll();
            // Get the neighbor node
            Set<Node> subsequentNodes = getSubsequentNodes(v);

            for (Node subsequentNode : subsequentNodes) {

                Integer degree = notZeroIndegreeNodeMap.get(subsequentNode);

                if (--degree == 0) {
                    topoResultList.add(subsequentNode);
                    zeroIndegreeNodeQueue.add(subsequentNode);
                    notZeroIndegreeNodeMap.remove(subsequentNode);
                } else {
                    notZeroIndegreeNodeMap.put(subsequentNode, degree);
                }

            }
        }

        // if notZeroIndegreeNodeMap is empty,there is no ring!
        return new AbstractMap.SimpleEntry<>(notZeroIndegreeNodeMap.size() == 0, topoResultList);
    }

    @Override
    public String toString() {
        return "DAG{"
            + "nodesMap="
            + nodesMap
            + ", edgesMap="
            + edgesMap
            + ", reverseEdgesMap="
            + reverseEdgesMap
            + '}';
    }
}

