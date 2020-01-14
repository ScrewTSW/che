/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, Key, WebElement, error } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';
import { Logger } from '../../utils/Logger';

@injectable()
export class Notification {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

}