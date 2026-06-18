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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessDag;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAG（有向无环图）辅助工具类，提供流程DAG的构建、节点查找、依赖分析、条件/分支任务解析等功能。
 * 用于工作流调度引擎中图结构的管理和遍历。
 */
public class DagHelper {

    private static final Logger logger = LoggerFactory.getLogger(DagHelper.class);

    /**
     * 根据任务节点列表生成节点关系列表。不在任务节点列表中的边将不会被添加到结果中。
     *
     * @param taskNodeList list of task nodes
     * @return list of task node relations
     */
    public static List<TaskNodeRelation> generateRelationListByFlowNodes(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> nodeRelationList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTaskList = JSONUtils.toList(preTasks, String.class);
            if (preTaskList != null) {
                for (String depNodeCode : preTaskList) {
                    if (null != findNodeByCode(taskNodeList, depNodeCode)) {
                        nodeRelationList.add(new TaskNodeRelation(depNodeCode, Long.toString(taskNode.getCode())));
                    }
                }
            }
        }
        return nodeRelationList;
    }

    /**
     * 根据起始节点和恢复节点生成DAG需要的任务节点列表。支持按前后依赖关系（TASK_PRE/TASK_POST）两种模式进行遍历。
     *
     * @param taskNodeList full list of task nodes
     * @param startNodeNameList list of start node codes
     * @param recoveryNodeCodeList list of recovery node codes
     * @param taskDependType the task dependency type
     * @return filtered list of task nodes needed for the DAG
     */
    public static List<TaskNode> generateFlowNodeListByStartNode(List<TaskNode> taskNodeList,
                                                                 List<String> startNodeNameList,
                                                                 List<String> recoveryNodeCodeList,
                                                                 TaskDependType taskDependType) {
        List<TaskNode> destFlowNodeList = new ArrayList<>();
        List<String> startNodeList = startNodeNameList;

        if (taskDependType != TaskDependType.TASK_POST && CollectionUtils.isEmpty(startNodeList)) {
            logger.error("start node list is empty! cannot continue run the process ");
            return destFlowNodeList;
        }

        List<TaskNode> destTaskNodeList = new ArrayList<>();
        List<TaskNode> tmpTaskNodeList = new ArrayList<>();

        if (taskDependType == TaskDependType.TASK_POST
                && CollectionUtils.isNotEmpty(recoveryNodeCodeList)) {
            startNodeList = recoveryNodeCodeList;
        }
        if (CollectionUtils.isEmpty(startNodeList)) {
            // no special designation start nodes
            tmpTaskNodeList = taskNodeList;
        } else {
            // specified start nodes or resume execution
            for (String startNodeCode : startNodeList) {
                TaskNode startNode = findNodeByCode(taskNodeList, startNodeCode);
                List<TaskNode> childNodeList = new ArrayList<>();
                if (startNode == null) {
                    logger.error("start node name [{}] is not in task node list [{}] ",
                            startNodeCode,
                            taskNodeList);
                    continue;
                } else if (TaskDependType.TASK_POST == taskDependType) {
                    List<String> visitedNodeCodeList = new ArrayList<>();
                    childNodeList = getFlowNodeListPost(startNode, taskNodeList, visitedNodeCodeList);
                } else if (TaskDependType.TASK_PRE == taskDependType) {
                    List<String> visitedNodeCodeList = new ArrayList<>();
                    childNodeList =
                            getFlowNodeListPre(startNode, recoveryNodeCodeList, taskNodeList, visitedNodeCodeList);
                } else {
                    childNodeList.add(startNode);
                }
                tmpTaskNodeList.addAll(childNodeList);
            }
        }

        for (TaskNode taskNode : tmpTaskNodeList) {
            if (null == findNodeByCode(destTaskNodeList, Long.toString(taskNode.getCode()))) {
                destTaskNodeList.add(taskNode);
            }
        }
        return destTaskNodeList;
    }

    /**
     * 查找所有依赖起始节点的后续节点（后序遍历方式）。
     *
     * @param startNode the start node
     * @param taskNodeList full list of task nodes
     * @param visitedNodeCodeList list of visited node codes
     * @return list of nodes that depend on the start node
     */
    private static List<TaskNode> getFlowNodeListPost(TaskNode startNode, List<TaskNode> taskNodeList,
                                                      List<String> visitedNodeCodeList) {
        List<TaskNode> resultList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            List<String> depList = taskNode.getDepList();
            if (null != depList && null != startNode && depList.contains(Long.toString(startNode.getCode()))
                    && !visitedNodeCodeList.contains(Long.toString(taskNode.getCode()))) {
                resultList.addAll(getFlowNodeListPost(taskNode, taskNodeList, visitedNodeCodeList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeCodeList.add(Long.toString(startNode.getCode()));
        }

        resultList.add(startNode);
        return resultList;
    }

    /**
     * 查找起始节点所依赖的所有前置节点（前序遍历方式）。
     *
     * @param startNode the start node
     * @param recoveryNodeCodeList list of recovery node codes
     * @param taskNodeList full list of task nodes
     * @param visitedNodeCodeList list of visited node codes
     * @return list of predecessor nodes
     */
    private static List<TaskNode> getFlowNodeListPre(TaskNode startNode, List<String> recoveryNodeCodeList,
                                                     List<TaskNode> taskNodeList, List<String> visitedNodeCodeList) {

        List<TaskNode> resultList = new ArrayList<>();

        List<String> depList = new ArrayList<>();
        if (null != startNode) {
            depList = startNode.getDepList();
            resultList.add(startNode);
        }
        if (CollectionUtils.isEmpty(depList)) {
            return resultList;
        }
        for (String depNodeCode : depList) {
            TaskNode start = findNodeByCode(taskNodeList, depNodeCode);
            if (recoveryNodeCodeList.contains(depNodeCode)) {
                resultList.add(start);
            } else if (!visitedNodeCodeList.contains(depNodeCode)) {
                resultList.addAll(getFlowNodeListPre(start, recoveryNodeCodeList, taskNodeList, visitedNodeCodeList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeCodeList.add(Long.toString(startNode.getCode()));
        }
        return resultList;
    }

    /**
     * 根据起始节点和恢复节点生成流程DAG图。
     *
     * @param totalTaskNodeList full list of task nodes
     * @param startNodeNameList list of start node codes
     * @param recoveryNodeCodeList list of recovery node codes
     * @param depNodeType the dependency node type
     * @return the generated ProcessDag, or null if no nodes
     * @throws Exception if an error occurs
     */
    public static ProcessDag generateFlowDag(List<TaskNode> totalTaskNodeList,
                                             List<String> startNodeNameList,
                                             List<String> recoveryNodeCodeList,
                                             TaskDependType depNodeType) throws Exception {

        List<TaskNode> destTaskNodeList = generateFlowNodeListByStartNode(totalTaskNodeList, startNodeNameList,
                recoveryNodeCodeList, depNodeType);
        if (destTaskNodeList.isEmpty()) {
            return null;
        }
        List<TaskNodeRelation> taskNodeRelations = generateRelationListByFlowNodes(destTaskNodeList);
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(destTaskNodeList);
        return processDag;
    }

    /**
     * 根据节点名称查找任务节点。
     *
     * @param nodeDetails list of task nodes to search
     * @param nodeName the name of the node to find
     * @return the found TaskNode, or null if not found
     */
    public static TaskNode findNodeByName(List<TaskNode> nodeDetails, String nodeName) {
        for (TaskNode taskNode : nodeDetails) {
            if (taskNode.getName().equals(nodeName)) {
                return taskNode;
            }
        }
        return null;
    }

    /**
     * 根据节点编码查找任务节点。
     *
     * @param nodeDetails list of task nodes to search
     * @param nodeCode the code of the node to find
     * @return the found TaskNode, or null if not found
     */
    public static TaskNode findNodeByCode(List<TaskNode> nodeDetails, String nodeCode) {
        for (TaskNode taskNode : nodeDetails) {
            if (Long.toString(taskNode.getCode()).equals(nodeCode)) {
                return taskNode;
            }
        }
        return null;
    }

    /**
     * 判断任务是否满足提交条件：所有依赖节点都已被禁止、已完成或在跳过列表中。
     *
     * @param taskNode the task node to check
     * @param dag the DAG graph
     * @param skipTaskNodeList map of skipped task nodes
     * @param completeTaskList map of completed task instances
     * @return true if all depends are forbidden or completed
     */
    public static boolean allDependsForbiddenOrEnd(TaskNode taskNode,
                                                   DAG<String, TaskNode, TaskNodeRelation> dag,
                                                   Map<String, TaskNode> skipTaskNodeList,
                                                   Map<String, TaskInstance> completeTaskList) {
        List<String> dependList = taskNode.getDepList();
        if (dependList == null) {
            return true;
        }
        for (String dependNodeCode : dependList) {
            TaskNode dependNode = dag.getNode(dependNodeCode);
            if (dependNode == null || completeTaskList.containsKey(dependNodeCode)
                    || dependNode.isForbidden()
                    || skipTaskNodeList.containsKey(dependNodeCode)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 解析前置节点的后继节点集合。此方法会处理条件节点的分支选择逻辑，
     * 同时检查所有依赖节点是否已禁止或完成，跳过不需要执行的节点。
     *
     * @param preNodeCode the code of the predecessor node
     * @param skipTaskNodeList map of skipped task nodes
     * @param dag the DAG graph
     * @param completeTaskList map of completed task instances
     * @return set of successor node codes
     */
    public static Set<String> parsePostNodes(String preNodeCode,
                                             Map<String, TaskNode> skipTaskNodeList,
                                             DAG<String, TaskNode, TaskNodeRelation> dag,
                                             Map<String, TaskInstance> completeTaskList) {
        Set<String> postNodeList = new HashSet<>();
        Collection<String> startVertexes = new ArrayList<>();

        if (preNodeCode == null) {
            startVertexes = dag.getBeginNode();
        } else if (dag.getNode(preNodeCode).isConditionsTask()) {
            List<String> conditionTaskList = parseConditionTask(preNodeCode, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else if (dag.getNode(preNodeCode).isSwitchTask()) {
            List<String> conditionTaskList = parseSwitchTask(preNodeCode, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else {
            startVertexes = dag.getSubsequentNodes(preNodeCode);
        }
        for (String subsequent : startVertexes) {
            TaskNode taskNode = dag.getNode(subsequent);
            if (taskNode == null) {
                logger.error("taskNode {} is null, please check dag", subsequent);
                continue;
            }
            if (isTaskNodeNeedSkip(taskNode, skipTaskNodeList)) {
                setTaskNodeSkip(subsequent, dag, completeTaskList, skipTaskNodeList);
                continue;
            }
            if (!DagHelper.allDependsForbiddenOrEnd(taskNode, dag, skipTaskNodeList, completeTaskList)) {
                continue;
            }
            if (taskNode.isForbidden() || completeTaskList.containsKey(subsequent)) {
                postNodeList.addAll(parsePostNodes(subsequent, skipTaskNodeList, dag, completeTaskList));
                continue;
            }
            postNodeList.add(subsequent);
        }
        return postNodeList;
    }

    /**
     * if all of the task dependence are skipped, skip it too.
     */
    private static boolean isTaskNodeNeedSkip(TaskNode taskNode,
                                              Map<String, TaskNode> skipTaskNodeList) {
        if (CollectionUtils.isEmpty(taskNode.getDepList())) {
            return false;
        }
        for (String depNode : taskNode.getDepList()) {
            if (!skipTaskNodeList.containsKey(depNode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解析条件任务，根据条件任务的执行结果（成功或失败）选择对应的后续分支路径，
     * 并将另一个未选择的分支节点标记为跳过。
     *
     * @param nodeCode the condition task node code
     * @param skipTaskNodeList map of skipped task nodes
     * @param dag the DAG graph
     * @param completeTaskList map of completed task instances
     * @return list of selected branch node codes
     */
    public static List<String> parseConditionTask(String nodeCode,
                                                  Map<String, TaskNode> skipTaskNodeList,
                                                  DAG<String, TaskNode, TaskNodeRelation> dag,
                                                  Map<String, TaskInstance> completeTaskList) {
        List<String> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeCode);
        if (!taskNode.isConditionsTask()) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeCode)) {
            return conditionTaskList;
        }
        TaskInstance taskInstance = completeTaskList.get(nodeCode);
        ConditionsParameters conditionsParameters =
                JSONUtils.parseObject(taskNode.getConditionResult(), ConditionsParameters.class);
        List<String> skipNodeList = new ArrayList<>();
        if (taskInstance.getState().isSuccess()) {
            conditionTaskList = conditionsParameters.getSuccessNode();
            skipNodeList = conditionsParameters.getFailedNode();
        } else if (taskInstance.getState().isFailure()) {
            conditionTaskList = conditionsParameters.getFailedNode();
            skipNodeList = conditionsParameters.getSuccessNode();
        } else {
            conditionTaskList.add(nodeCode);
        }
        // the skipNodeList maybe null if no next task
        skipNodeList = Optional.ofNullable(skipNodeList).orElse(new ArrayList<>());
        for (String failedNode : skipNodeList) {
            setTaskNodeSkip(failedNode, dag, completeTaskList, skipTaskNodeList);
        }
        // the conditionTaskList maybe null if no next task
        conditionTaskList = Optional.ofNullable(conditionTaskList).orElse(new ArrayList<>());
        return conditionTaskList;
    }

    /**
     * 解析分支(Switch)任务，根据Switch任务的执行结果选择对应的后续分支路径，
     * 并将其他未选中的分支节点标记为跳过。
     *
     * @param nodeCode the switch task node code
     * @param skipTaskNodeList map of skipped task nodes
     * @param dag the DAG graph
     * @param completeTaskList map of completed task instances
     * @return list of selected branch node codes
     */
    public static List<String> parseSwitchTask(String nodeCode,
                                               Map<String, TaskNode> skipTaskNodeList,
                                               DAG<String, TaskNode, TaskNodeRelation> dag,
                                               Map<String, TaskInstance> completeTaskList) {
        List<String> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeCode);
        if (!taskNode.isSwitchTask()) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeCode)) {
            return conditionTaskList;
        }
        conditionTaskList = skipTaskNode4Switch(taskNode, skipTaskNodeList, completeTaskList, dag);
        return conditionTaskList;
    }


    public static List<String> skipTaskNode4Switch(TaskNode taskNode,
                                                 Map<String, TaskNode> skipTaskNodeList,
                                                 Map<String, TaskInstance> completeTaskList,
                                                 DAG<String, TaskNode, TaskNodeRelation> dag) {

        SwitchParameters switchParameters =
                completeTaskList.get(Long.toString(taskNode.getCode())).getSwitchDependency();
        int resultConditionLocation = switchParameters.getResultConditionLocation();
        List<SwitchResultVo> conditionResultVoList = switchParameters.getDependTaskList();

        List<String> switchTaskList = conditionResultVoList.get(resultConditionLocation).getNextNode();
        Set<String> switchNeedWorkCodes = new HashSet<>();
        if (CollectionUtils.isEmpty(switchTaskList)) {
            return new ArrayList<>();
        }
        // get all downstream nodes of the branch that the switch node needs to execute
        for (String switchTaskCode : switchTaskList) {
            getSwitchNeedWorkCodes(switchTaskCode, dag, switchNeedWorkCodes);
        }
        conditionResultVoList.remove(resultConditionLocation);
        for (SwitchResultVo info : conditionResultVoList) {
            if (CollectionUtils.isEmpty(info.getNextNode())) {
                continue;
            }
            for (String nextNode : info.getNextNode()) {
                setSwitchTaskNodeSkip(nextNode, dag, completeTaskList, skipTaskNodeList,
                        switchNeedWorkCodes);
            }
        }
        return switchTaskList;
    }

    /**
     * 递归获取Switch任务需要执行的分支的所有下游节点编码。
     *
     * @param taskCode the current task code
     * @param dag the DAG graph
     * @param switchNeedWorkCodes set to collect all downstream node codes
     */
    public static void getSwitchNeedWorkCodes(String taskCode, DAG<String, TaskNode, TaskNodeRelation> dag,
                                              Set<String> switchNeedWorkCodes) {
        switchNeedWorkCodes.add(taskCode);
        Set<String> subsequentNodes = dag.getSubsequentNodes(taskCode);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(subsequentNodes)) {
            for (String subCode : subsequentNodes) {
                getSwitchNeedWorkCodes(subCode, dag, switchNeedWorkCodes);
            }
        }
    }

    private static void setSwitchTaskNodeSkip(String skipNodeCode,
                                              DAG<String, TaskNode, TaskNodeRelation> dag,
                                              Map<String, TaskInstance> completeTaskList,
                                              Map<String, TaskNode> skipTaskNodeList,
                                              Set<String> switchNeedWorkCodes) {
        // ignore when the node that needs to be skipped exists on the branch that the switch type node needs to execute
        if (!dag.containsNode(skipNodeCode) || switchNeedWorkCodes.contains(skipNodeCode)) {
            return;
        }
        skipTaskNodeList.putIfAbsent(skipNodeCode, dag.getNode(skipNodeCode));
        Collection<String> postNodeList = dag.getSubsequentNodes(skipNodeCode);
        for (String post : postNodeList) {
            TaskNode postNode = dag.getNode(post);
            if (isTaskNodeNeedSkip(postNode, skipTaskNodeList)) {
                setTaskNodeSkip(post, dag, completeTaskList, skipTaskNodeList);
            }
        }
    }
    /**
     * set task node and the post nodes skip flag
     */
    private static void setTaskNodeSkip(String skipNodeCode,
                                        DAG<String, TaskNode, TaskNodeRelation> dag,
                                        Map<String, TaskInstance> completeTaskList,
                                        Map<String, TaskNode> skipTaskNodeList) {
        if (!dag.containsNode(skipNodeCode)) {
            return;
        }
        skipTaskNodeList.putIfAbsent(skipNodeCode, dag.getNode(skipNodeCode));
        Collection<String> postNodeList = dag.getSubsequentNodes(skipNodeCode);
        for (String post : postNodeList) {
            TaskNode postNode = dag.getNode(post);
            if (isTaskNodeNeedSkip(postNode, skipTaskNodeList)) {
                setTaskNodeSkip(post, dag, completeTaskList, skipTaskNodeList);
            }
        }
    }

    /**
     * 根据 ProcessDag 构建 DAG 图结构，添加节点和边。
     *
     * @param processDag the process DAG model
     * @return the constructed DAG graph
     */
    public static DAG<String, TaskNode, TaskNodeRelation> buildDagGraph(ProcessDag processDag) {

        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        // add vertex
        if (CollectionUtils.isNotEmpty(processDag.getNodes())) {
            for (TaskNode node : processDag.getNodes()) {
                dag.addNode(Long.toString(node.getCode()), node);
            }
        }

        // add edge
        if (CollectionUtils.isNotEmpty(processDag.getEdges())) {
            for (TaskNodeRelation edge : processDag.getEdges()) {
                dag.addEdge(edge.getStartNode(), edge.getEndNode());
            }
        }
        return dag;
    }

    /**
     * 根据任务节点列表构建流程DAG（有向无环图）对象。
     *
     * @param taskNodeList list of task nodes
     * @return the constructed ProcessDag
     */
    public static ProcessDag getProcessDag(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();

        // Traverse node information and build relationships
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTasksList = JSONUtils.toList(preTasks, String.class);

            // If the dependency is not empty
            if (preTasksList != null) {
                for (String depNode : preTasksList) {
                    taskNodeRelations.add(new TaskNodeRelation(depNode, Long.toString(taskNode.getCode())));
                }
            }
        }

        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodeList);
        return processDag;
    }

    /**
     * 根据任务节点列表和任务关系列表构建流程DAG对象。
     *
     * @param taskNodeList list of task nodes
     * @param processTaskRelations list of process task relations
     * @return the constructed ProcessDag
     */
    public static ProcessDag getProcessDag(List<TaskNode> taskNodeList,
                                           List<ProcessTaskRelation> processTaskRelations) {
        Map<Long, TaskNode> taskNodeMap = new HashMap<>();

        taskNodeList.forEach(taskNode -> {
            taskNodeMap.putIfAbsent(taskNode.getCode(), taskNode);
        });

        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            long preTaskCode = processTaskRelation.getPreTaskCode();
            long postTaskCode = processTaskRelation.getPostTaskCode();

            if (processTaskRelation.getPreTaskCode() != 0
                    && taskNodeMap.containsKey(preTaskCode) && taskNodeMap.containsKey(postTaskCode)) {
                TaskNode preNode = taskNodeMap.get(preTaskCode);
                TaskNode postNode = taskNodeMap.get(postTaskCode);
                taskNodeRelations
                        .add(new TaskNodeRelation(Long.toString(preNode.getCode()), Long.toString(postNode.getCode())));
            }
        }
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodeList);
        return processDag;
    }

    /**
     * 判断指定父节点之后是否存在条件任务类型的后继节点。
     *
     * @param parentNodeCode the parent node code
     * @param dag the DAG graph
     * @return true if there are conditions tasks after the parent node
     */
    public static boolean haveConditionsAfterNode(String parentNodeCode,
                                                  DAG<String, TaskNode, TaskNodeRelation> dag) {
        return haveSubAfterNode(parentNodeCode, dag, TaskConstants.TASK_TYPE_CONDITIONS);
    }

    /**
     * 判断在任务节点列表中，指定父节点之后是否存在条件任务类型的后继节点。
     *
     * @param parentNodeCode the parent node code
     * @param taskNodes list of task nodes
     * @return true if there are conditions tasks after the parent node
     */
    public static boolean haveConditionsAfterNode(String parentNodeCode, List<TaskNode> taskNodes) {
        if (CollectionUtils.isEmpty(taskNodes)) {
            return false;
        }
        for (TaskNode taskNode : taskNodes) {
            List<String> preTasksList = JSONUtils.toList(taskNode.getPreTasks(), String.class);
            if (preTasksList.contains(parentNodeCode) && taskNode.isConditionsTask()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定父节点之后是否存在阻塞任务类型的后继节点。
     *
     * @param parentNodeCode the parent node code
     * @param dag the DAG graph
     * @return true if there are blocking tasks after the parent node
     */
    public static boolean haveBlockingAfterNode(String parentNodeCode,
                                                DAG<String, TaskNode, TaskNodeRelation> dag) {
        return haveSubAfterNode(parentNodeCode, dag, TaskConstants.TASK_TYPE_BLOCKING);
    }

    /**
     * 判断指定父节点之后是否存在任意类型的后继节点。
     *
     * @param parentNodeCode the parent node code
     * @param dag the DAG graph
     * @return true if there are any successor nodes
     */
    public static boolean haveAllNodeAfterNode(String parentNodeCode,
                                               DAG<String, TaskNode, TaskNodeRelation> dag) {
        return haveSubAfterNode(parentNodeCode, dag, null);
    }

    /**
     * 判断父节点之后是否存在指定类型的子节点。如果 filterNodeType 为空则判断是否存在任意子节点。
     *
     * @param parentNodeCode the parent node code
     * @param dag the DAG graph
     * @param filterNodeType the node type to filter, or null for any type
     * @return true if there is a matching successor node
     */
    public static boolean haveSubAfterNode(String parentNodeCode,
                                           DAG<String, TaskNode, TaskNodeRelation> dag, String filterNodeType) {
        Set<String> subsequentNodes = dag.getSubsequentNodes(parentNodeCode);
        if (CollectionUtils.isEmpty(subsequentNodes)) {
            return false;
        }
        if (StringUtils.isBlank(filterNodeType)) {
            return true;
        }
        for (String nodeName : subsequentNodes) {
            TaskNode taskNode = dag.getNode(nodeName);
            if (taskNode.getType().equalsIgnoreCase(filterNodeType)) {
                return true;
            }
        }
        return false;
    }
}
