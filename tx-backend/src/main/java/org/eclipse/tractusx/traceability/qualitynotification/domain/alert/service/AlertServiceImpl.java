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

package org.eclipse.tractusx.traceability.qualitynotification.domain.alert.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.traceability.common.model.PageResult;
import org.eclipse.tractusx.traceability.qualitynotification.application.alert.service.AlertService;
import org.eclipse.tractusx.traceability.qualitynotification.domain.alert.model.exception.AlertNotFoundException;
import org.eclipse.tractusx.traceability.qualitynotification.domain.alert.repository.AlertRepository;
import org.eclipse.tractusx.traceability.qualitynotification.domain.model.QualityNotification;
import org.eclipse.tractusx.traceability.qualitynotification.domain.model.QualityNotificationId;
import org.eclipse.tractusx.traceability.qualitynotification.domain.model.QualityNotificationSeverity;
import org.eclipse.tractusx.traceability.qualitynotification.domain.model.QualityNotificationSide;
import org.eclipse.tractusx.traceability.qualitynotification.domain.model.QualityNotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertsPublisherService alertsPublisherService;

    private final AlertRepository alertRepository;

    @Override
    public QualityNotificationId start(List<String> partIds, String description, Instant targetDate, QualityNotificationSeverity severity) {
        return alertsPublisherService.startAlert(partIds, description, targetDate, severity);
    }

    @Override
    public PageResult<QualityNotification> getCreated(Pageable pageable) {
        return getAlertsPageResult(pageable, QualityNotificationSide.SENDER);
    }

    @Override
    public PageResult<QualityNotification> getReceived(Pageable pageable) {
        return getAlertsPageResult(pageable, QualityNotificationSide.RECEIVER);
    }

    @Override
    public QualityNotification find(Long notificationId) {
        QualityNotificationId alertId = new QualityNotificationId(notificationId);
        return loadOrNotFoundException(alertId);
    }

    @Override
    public QualityNotification loadOrNotFoundException(QualityNotificationId notificationId) {
        return alertRepository.findOptionalQualityNotificationById(notificationId)
                .orElseThrow(() -> new AlertNotFoundException(notificationId));
    }

    @Override
    public QualityNotification loadByEdcNotificationIdOrNotFoundException(String edcNotificationId) {
        return alertRepository.findByEdcNotificationId(edcNotificationId)
                .orElseThrow(() -> new AlertNotFoundException(edcNotificationId));
    }

    @Override
    public void approve(Long notificationId) {
        QualityNotification investigation = loadOrNotFoundException(new QualityNotificationId(notificationId));
        alertsPublisherService.approveAlert(investigation);
    }

    @Override
    public void cancel(Long notificationId) {
        QualityNotification alert = loadOrNotFoundException(new QualityNotificationId(notificationId));
        alertsPublisherService.cancelAlert(alert);
    }

    @Override
    public void update(Long notificationId, QualityNotificationStatus notificationStatus, String reason) {
        QualityNotification alert = loadOrNotFoundException(new QualityNotificationId(notificationId));
        alertsPublisherService.updateAlertPublisher(alert, notificationStatus, reason);
    }

    private PageResult<QualityNotification> getAlertsPageResult(Pageable pageable, QualityNotificationSide alertSide) {
        List<QualityNotification> alertData = alertRepository.findQualityNotificationsBySide(alertSide, pageable)
                .content()
                .stream()
                .sorted(QualityNotification.COMPARE_BY_NEWEST_QUALITY_NOTIFICATION_CREATION_TIME)
                .toList();
        Page<QualityNotification> alertDataPage = new PageImpl<>(alertData, pageable, alertRepository.countQualityNotificationEntitiesBySide(alertSide));
        return new PageResult<>(alertDataPage);
    }
}
