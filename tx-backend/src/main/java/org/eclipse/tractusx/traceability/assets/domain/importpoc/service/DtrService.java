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

package org.eclipse.tractusx.traceability.assets.domain.importpoc.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SecurityAttribute;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.registryclient.decentral.DigitalTwinRegistryCreateShellService;
import org.eclipse.tractusx.irs.registryclient.decentral.exception.CreateDtrShellException;
import org.eclipse.tractusx.traceability.assets.domain.base.model.AssetBase;
import org.eclipse.tractusx.traceability.assets.domain.importpoc.repository.SubmodelPayloadRepository;
import org.eclipse.tractusx.traceability.common.properties.EdcProperties;
import org.eclipse.tractusx.traceability.common.properties.TraceabilityProperties;
import org.eclipse.tractusx.traceability.submodel.domain.repository.SubmodelServerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DtrService {
    private static final String GLOBAL_REFERENCE = "GlobalReference";
    private static final String EXTERNAL_REFERENCE = "ExternalReference";

    private final DigitalTwinRegistryCreateShellService dtrCreateShellService;
    private final SubmodelPayloadRepository submodelPayloadRepository;
    private final SubmodelServerRepository submodelServerRepository;
    private final EdcProperties edcProperties;
    private final TraceabilityProperties traceabilityProperties;


    public void createShellInDtr(final AssetBase assetBase) throws CreateDtrShellException {
        Map<String, String> payloadByAspectType = submodelPayloadRepository.getTypesAndPayloadsByAssetId(assetBase.getId());
        Map<String, UUID> createdSubmodelIdByAspectType = payloadByAspectType.entrySet().stream()
                .map(this::createSubmodel)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<SubmodelDescriptor> descriptors = toSubmodelDescriptors(createdSubmodelIdByAspectType);

        dtrCreateShellService.createShell(aasFrom(assetBase, descriptors));
    }

    private List<SubmodelDescriptor> toSubmodelDescriptors(Map<String, UUID> createdSubmodelIdByAspectType) {
        return createdSubmodelIdByAspectType.entrySet()
                .stream().map(entry ->
                        toSubmodelDescriptor(entry.getKey(), entry.getValue())

                ).toList();
    }

    private SubmodelDescriptor toSubmodelDescriptor(String aspectType, UUID submodelServerIdReference) {
        return SubmodelDescriptor.builder()
                .description(List.of())
                .idShort(aspectTypeToSimpleSubmodelName(aspectType)) // example SingleLevelUsageAsBuilt
                .id(submodelServerIdReference.toString())
                .semanticId(
                        Reference.builder()
                                .type(EXTERNAL_REFERENCE)
                                .keys(
                                        List.of(SemanticId.builder()
                                                .type(GLOBAL_REFERENCE)
                                                .value(aspectType).build())
                                ).build()
                )
                .endpoints(
                        List.of(
                                Endpoint.builder()
                                        .interfaceInformation("SUBMODEL-3.0")
                                        .protocolInformation(
                                                ProtocolInformation.builder()
                                                        .href(edcProperties.getPartsProviderEdcDataplaneUrl() + "/api/public/data/" + submodelServerIdReference)
                                                        .endpointProtocol("HTTP")
                                                        .endpointProtocolVersion(List.of("1.1"))
                                                        .subprotocol("DSP")
                                                        .subprotocolBodyEncoding("plain")
                                                        .subprotocolBody(getSubProtocol())
                                                        .securityAttributes(
                                                                List.of(SecurityAttribute.none())
                                                        ).build()
                                        ).build()
                        )
                ).build();
    }

    private String getSubProtocol() {
        final String submodelServerAssetId = "submodel-asset";
        final String edcProviderControlplaneUrl = edcProperties.getPartsProviderEdcControlplaneUrl();  // "https://trace-x-test-edc.dev.demo.catena-x.net"
        return "id=%s;dspEndpoint=%s".formatted(submodelServerAssetId, edcProviderControlplaneUrl);
    }

    private String aspectTypeToSimpleSubmodelName(String aspectType) {
        String[] split = aspectType.split("#");
        return split[1];
    }

    private Map.Entry<String, UUID> createSubmodel(Map.Entry<String, String> payloadByAspectType) {
        UUID submodelId = UUID.randomUUID();
        submodelServerRepository.saveSubmodel(submodelId.toString(), payloadByAspectType.getValue());
        return Map.entry(payloadByAspectType.getKey(), submodelId);
    }

    // Do we have partInstanceId ? python script is generating specificAssetId with part instance id in it
    private AssetAdministrationShellDescriptor aasFrom(AssetBase assetBase, List<SubmodelDescriptor> descriptors) {
        return AssetAdministrationShellDescriptor.builder()
                .globalAssetId(assetBase.getId())
                .idShort(assetBase.getIdShort())
                .id(UUID.randomUUID().toString()) // TODO: generated ? upon creation do we need it later on ?
                .specificAssetIds(aasIdentifiersFromAsset(assetBase))
                .submodelDescriptors(descriptors)
                .build();
    }

    List<IdentifierKeyValuePair> aasIdentifiersFromAsset(AssetBase assetBase) {
        return List.of(
                IdentifierKeyValuePair.builder()
                        .name("manufacturerId")
                        .value(assetBase.getManufacturerId())
                        .externalSubjectId(
                                Reference.builder()
                                        .type(EXTERNAL_REFERENCE)
                                        .keys(getExternalSubjectIds())
                                        .build())
                        .build(),
                IdentifierKeyValuePair.builder()
                        .name("manufacturerPartId")
                        .value(assetBase.getManufacturerPartId())
                        .externalSubjectId(
                                Reference.builder()
                                        .type(EXTERNAL_REFERENCE)
                                        .keys(getExternalSubjectIds())
                                        .build())
                        .build()
                // Python script creates also partInstanceId like below  where do I get partInstanceId information ??
//                ,IdentifierKeyValuePair.builder()
//                        .name("partInstanceId")
//                        .value(assetBase.getManufacturerPartId())
//                        .subjectId(// TODO: for now IRS lib does not support exterrnalSubjectId needs to be implemented
//                                Reference.builder()
//                                        .type("ExternalReference")
//                                        .keys(getExternalSubjectIds())
//                                        .build())
//                        .build()
        );
    }

    private List<SemanticId> getExternalSubjectIds() {
        return List.of(
                SemanticId.builder()
                        .type(GLOBAL_REFERENCE)
                        .value("PUBLIC_READABLE")
                        .build(),
                SemanticId.builder()
                        .type(GLOBAL_REFERENCE)
                        .value(traceabilityProperties.getBpn().toString())
                        .build()
                // TODO: Test if it works python script creates GlobalReferences for both instances BPN
                // TODO: Last time we used python script with only own bpn in global references other instance could not retrieve assets
                // our usage of python script generates following
//        {
//            "type": "GlobalReference",
//                "value": "PUBLIC_READABLE"
//        },
//        {
//            "type": "GlobalReference",
//                "value": "BPNL00000003CML1"
//        },
//        {
//            "type": "GlobalReference",
//                "value": "BPNL00000003CNKC"
//        }
        );
    }
}
