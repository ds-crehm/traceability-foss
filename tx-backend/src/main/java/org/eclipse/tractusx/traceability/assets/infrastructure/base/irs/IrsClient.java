/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.traceability.assets.infrastructure.base.irs;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.request.RegisterJobRequest;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.JobDetailResponse;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.PolicyResponse;
import org.eclipse.tractusx.traceability.common.properties.TraceabilityProperties;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class IrsClient {

    private final RestTemplate irsAdminTemplate;

    private final RestTemplate irsRegularTemplate;

    private final TraceabilityProperties traceabilityProperties;

    private static final String POLICY_PATH = "/irs/policies";

    public IrsClient(RestTemplate irsAdminTemplate,
                     RestTemplate irsRegularTemplate,
                     TraceabilityProperties traceabilityProperties) {
        this.irsAdminTemplate = irsAdminTemplate;
        this.irsRegularTemplate = irsRegularTemplate;
        this.traceabilityProperties = traceabilityProperties;
    }

    public List<PolicyResponse> getPolicies() {
        return irsAdminTemplate.exchange(POLICY_PATH, HttpMethod.GET, null, new ParameterizedTypeReference<List<PolicyResponse>>() {
        }).getBody();
    }

    public void deletePolicy() {
        irsAdminTemplate.exchange(POLICY_PATH + "/" + traceabilityProperties.getRightOperand(), HttpMethod.DELETE, null, new ParameterizedTypeReference<>() {
        });
    }

    public void registerPolicy() {
        Instant validUntil = traceabilityProperties.getValidUntil().toInstant();
        String leftOperandMembership = "Membership";
        String operatorEq = "odrl:" + traceabilityProperties.getOperatorType();
        String rightOperandActive = "active";
        String leftOperandPurpose = traceabilityProperties.getLeftOperand();
        String rightOperandPurpose = traceabilityProperties.getRightOperand();
        String uuid = UUID.randomUUID().toString();
        final String payload = """
                {
                    "validUntil": "%s",
                    "payload": {
                        "@context": {
                            "odrl": "http://www.w3.org/ns/odrl/2/"
                        },
                        "@id": "%s",
                        "policy": {
                            "odrl:permission": [
                                {
                                    "odrl:action": "USE",
                                    "odrl:constraint": {
                                        "odrl:and": [
                                            {
                                                "odrl:leftOperand": "%s",
                                                "odrl:operator": {
                                                    "@id": "%s"
                                                },
                                                "odrl:rightOperand": "%s"
                                            },
                                            {
                                                "odrl:leftOperand": "%s",
                                                "odrl:operator": {
                                                    "@id": "%s"
                                                },
                                                "odrl:rightOperand": "%s"
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                }
                """.formatted(validUntil, uuid, leftOperandMembership, operatorEq, rightOperandActive, leftOperandPurpose, operatorEq, rightOperandPurpose);
        irsAdminTemplate.exchange(POLICY_PATH, HttpMethod.POST, new HttpEntity<>(payload), Void.class);
    }

    public void registerJob(RegisterJobRequest registerJobRequest) {
        irsRegularTemplate.exchange("/irs/jobs", HttpMethod.POST, new HttpEntity<>(registerJobRequest), Void.class);
    }


    @Nullable
    public JobDetailResponse getJobDetailResponse(String jobId) {
        return irsRegularTemplate.exchange("/irs/jobs/" + jobId, HttpMethod.GET, null, new ParameterizedTypeReference<JobDetailResponse>() {
        }).getBody();
    }

}
