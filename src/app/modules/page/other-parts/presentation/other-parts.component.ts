/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Component, OnInit } from '@angular/core';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { Pagination } from '@core/model/pagination.model';
import { OtherPartsFacade } from '@page/other-parts/core/other-parts.facade';
import { Part } from '@page/parts/model/parts.model';
import { StaticIdService } from '@shared/service/staticId.service';
import { TableConfig, TableEventConfig } from '@shared/components/table/table.model';
import { View } from '@shared/model/view.model';
import { PartDetailsFacade } from '@shared/modules/part-details/core/partDetails.facade';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Component({
  selector: 'app-other-parts',
  templateUrl: './other-parts.component.html',
  styleUrls: ['./other-parts.component.scss'],
})
export class OtherPartsComponent implements OnInit {
  public readonly displayedColumns: string[] = [
    'select',
    'name',
    'manufacturer',
    'partNumber',
    'serialNumber',
    'productionDate',
  ];

  public readonly sortableColumns: Record<string, boolean> = {
    name: true,
    manufacturer: true,
    partNumber: true,
    serialNumber: true,
    productionDate: true,
  };

  public readonly tableConfig: TableConfig = {
    displayedColumns: this.displayedColumns,
    header: this.displayedColumns.map(column => `pageParts.column.${column}`),
    sortableColumns: this.sortableColumns,
  };

  public readonly customerParts$: Observable<View<Pagination<Part>>>;
  public readonly supplierParts$: Observable<View<Pagination<Part>>>;

  public readonly deselectPartTrigger$: Subject<Part[]> = new Subject();
  public addPartTrigger$: Subject<Part> = new Subject();

  public readonly isInvestigationOpen$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public readonly selectedItems: Array<Array<Part>> = [];

  public selectedTab = 0;

  public readonly supplierTabLabelId = this.staticIdService.generateId('OtherParts.supplierTabLabel');
  public readonly customerTabLabelId = this.staticIdService.generateId('OtherParts.customerTabLabel');

  constructor(
    private readonly otherPartsFacade: OtherPartsFacade,
    private readonly partDetailsFacade: PartDetailsFacade,
    private readonly staticIdService: StaticIdService,
  ) {
    this.customerParts$ = this.otherPartsFacade.customerParts$;
    this.supplierParts$ = this.otherPartsFacade.supplierParts$;
  }

  public get currentSelectedItems(): Part[] {
    return this.selectedItems[this.selectedTab] || [];
  }

  public set currentSelectedItems(parts: Part[]) {
    this.selectedItems[this.selectedTab] = parts;
  }

  public ngOnInit(): void {
    this.otherPartsFacade.setCustomerParts();
    this.otherPartsFacade.setSupplierParts();
  }

  public onSelectItem(event: Record<string, unknown>): void {
    this.partDetailsFacade.selectedPart = event as unknown as Part;
  }

  public onTableConfigChange({ page, pageSize, sorting }: TableEventConfig): void {
    if (this.selectedTab === 0) {
      this.otherPartsFacade.setSupplierParts(page, pageSize, sorting);
    } else {
      this.otherPartsFacade.setCustomerParts(page, pageSize, sorting);
    }
  }

  public onMultiSelect(event: unknown[]): void {
    this.currentSelectedItems = event as Part[];
  }

  public removeItemFromSelection(part: Part): void {
    this.deselectPartTrigger$.next([part]);
    this.currentSelectedItems = this.currentSelectedItems.filter(({ id }) => id !== part.id);
  }

  public onTabChange({ index }: MatTabChangeEvent): void {
    this.selectedTab = index;
    this.partDetailsFacade.selectedPart = null;
  }

  public clearSelected(): void {
    this.deselectPartTrigger$.next(this.currentSelectedItems);
    this.currentSelectedItems = [];
  }

  public addItemToSelection(part: Part): void {
    this.addPartTrigger$.next(part);
    this.currentSelectedItems = [...this.currentSelectedItems, part];
  }
}
