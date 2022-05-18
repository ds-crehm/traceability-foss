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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SvgIconsModule } from '@ngneat/svg-icon';
import { icons } from '../../shared/shared-icons.module';
import { SharedModule } from '../../shared/shared.module';
import { TemplateModule } from '../../shared/template.module';
import { FooterComponent } from './footer/footer.component';
import { LayoutRoutingModule } from './layout.routing';
import { NavBarComponent } from './nav-bar/nav-bar.component';
import { PrivateLayoutComponent } from './private-layout/private-layout.component';
import { ResizerComponent } from './resizer/resizer.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { SpinnerOverlayComponent } from './spinner-overlay/spinner-overlay.component';
import { SidebarSectionComponent } from './sidebar/sidebar-section/sidebar-section.component';

@NgModule({
  declarations: [
    PrivateLayoutComponent,
    NavBarComponent,
    ResizerComponent,
    SidebarComponent,
    FooterComponent,
    SpinnerOverlayComponent,
    SidebarSectionComponent,
  ],
  imports: [CommonModule, LayoutRoutingModule, TemplateModule, SharedModule, SvgIconsModule.forChild(icons)],
})
export class LayoutModule {}