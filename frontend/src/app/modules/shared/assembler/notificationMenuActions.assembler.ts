/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
import { NotificationActionHelperService } from '@shared/assembler/notification-action-helper.service';
import { NotificationCommonModalComponent } from '@shared/components/notification-common-modal/notification-common-modal.component';
import { MenuActionConfig } from '@shared/components/table/table.model';
import { Notification, NotificationStatus } from '@shared/model/notification.model';
import { NotificationAction } from '@shared/modules/notification/notification-action.enum';
import { NotificationProcessingService } from '@shared/service/notification-processing.service';

export class NotificationMenuActionsAssembler {
  public static getMenuActions(helperService: NotificationActionHelperService, modal: NotificationCommonModalComponent, notificationProcessingService: NotificationProcessingService): MenuActionConfig<Notification>[] {
    const notInLoadingState = (id: string) => {
      console.log(id);
      return !notificationProcessingService.notificationIdsInLoadingState.has(id);
    };
    return [
      {
        label: 'actions.close',
        icon: 'close',
        action: data => modal.show(NotificationStatus.CLOSED, data),
        condition: data => helperService.showCloseButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.CLOSE) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
      {
        label: 'actions.approve',
        icon: 'share',
        action: data => modal.show(NotificationStatus.APPROVED, data),
        condition: data => helperService.showApproveButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.APPROVE) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
      {
        label: 'actions.cancel',
        icon: 'cancel',
        action: data => modal.show(NotificationStatus.CANCELED, data),
        condition: data => helperService.showCancelButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.CANCEL) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
      {
        label: 'actions.acknowledge',
        icon: 'work',
        action: data => modal.show(NotificationStatus.ACKNOWLEDGED, data),
        condition: data => helperService.showAcknowledgeButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.ACKNOWLEDGE) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
      {
        label: 'actions.accept',
        icon: 'assignment_turned_in',
        action: data => modal.show(NotificationStatus.ACCEPTED, data),
        condition: data => helperService.showAcceptButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.ACCEPT) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
      {
        label: 'actions.decline',
        icon: 'assignment_late',
        action: data => modal.show(NotificationStatus.DECLINED, data),
        condition: data => helperService.showDeclineButton(data),
        isAuthorized: data => helperService.isAuthorizedForButton(NotificationAction.DECLINE) && !notificationProcessingService.notificationIdsInLoadingState.has(data?.id),
      },
    ];
  }
}
