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

import { axios } from '@/service/service'
import type {
  ListReq,
  TaskNotificationReq,
  TaskNotificationResp,
  IdReq,
} from './types'

export function queryTaskNotificationListPaging(params: ListReq): Promise<TaskNotificationResp> {
  return axios({
    url: '/task-notifications',
    method: 'get',
    params,
  })
}

export function queryTaskNotification(id: IdReq): Promise<TaskNotificationResp> {
  return axios({
    url: `/task-notifications/${id}`,
    method: 'get',
  })
}

export function createTaskNotification(data: TaskNotificationReq): Promise<any> {
  return axios({
    url: '/task-notifications',
    method: 'post',
    data,
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    transformRequest: (params: any) => JSON.stringify(params),
  })
}

export function updateTaskNotification(id: IdReq, data: TaskNotificationReq): Promise<any> {
  return axios({
    url: `/task-notifications/${id}`,
    method: 'put',
    data,
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    transformRequest: (params: any) => JSON.stringify(params),
  })
}

export function deleteTaskNotification(id: IdReq): Promise<any> {
  return axios({
    url: `/task-notifications/${id}`,
    method: 'delete',
  })
}
