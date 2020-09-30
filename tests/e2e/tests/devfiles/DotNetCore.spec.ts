/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { WorkspaceNameHandler, Editor, CLASSES } from '../..';
import { Key } from 'selenium-webdriver';
import { e2eContainer } from '../../inversify.config';
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import * as commonLsTests from '../../testsLibrary/LsTests';
import * as workspaceHandling from '../../testsLibrary/WorksapceHandlingTests';
import * as codeExecutionTests from '../../testsLibrary/CodeExecutionTests';

const editor: Editor = e2eContainer.get(CLASSES.Editor);

const workspaceSampleName: string = 'dotnet-web-simple';
const fileFolderPath: string = `${workspaceSampleName}`;
const tabTitle: string = 'Program.cs';
// const codeNavigationClassName: string = '[metadata] Console.cs';
const stack : string = '.NET Core';
const codeNavigationClassName : string = 'Console.cs';
const updateDependenciesTaskName: string = 'update dependencies';
const buildTaskName: string = 'build';
const runTaskName: string = 'run';
const runTaskNameExpectedString: string = 'A process is now listening on port 5000.';

suite(`${stack} test`, async () => {
    suite (`Create ${stack} workspace`, async () => {
        workspaceHandling.createAndOpenWorkspace(stack);
        projectAndFileTests.waitWorkspaceReadinessNoSubfolder(workspaceSampleName);
    });

    suite('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
        prepareEditorForLSTests();
    });

    suite('Validation of workspace build and run', async () => {
        codeExecutionTests.runTask(updateDependenciesTaskName, 120_000);
        codeExecutionTests.closeTerminal(updateDependenciesTaskName);
        codeExecutionTests.runTask(buildTaskName, 30_000);
        codeExecutionTests.closeTerminal(buildTaskName);
        codeExecutionTests.runTaskWithDialogShellAndOpenLink(runTaskName, runTaskNameExpectedString , 30_000);
    });

    suite('Language server validation', async () => {
        commonLsTests.suggestionInvoking(tabTitle, 22, 33, 'test');
        commonLsTests.errorHighlighting(tabTitle, 'error_text;', 23);
        commonLsTests.autocomplete(tabTitle, 22, 27, 'WriteLine');
        commonLsTests.codeNavigationGoTo(tabTitle, 22, 27, codeNavigationClassName, Key.chord(Key.CONTROL, Key.F11));
    });

    suite ('Stopping and deleting the workspace', async () => {
        let workspaceName = 'not defined';
        suiteSetup( async () => {
            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
        test (`Stop worksapce`, async () => {
            await workspaceHandling.stopWorkspace(workspaceName);
        });
        test (`Remove workspace`, async () => {
            await workspaceHandling.removeWorkspace(workspaceName);
        });
    });
});

export function prepareEditorForLSTests() {
    test(`Prepare file for LS tests`, async () => {
        await editor.moveCursorToLineAndChar(tabTitle, 18, 6);
        await editor.performKeyCombination(tabTitle, '\nprivate static String test = "test";');
        await editor.moveCursorToLineAndChar(tabTitle, 21, 10);
        await editor.performKeyCombination(tabTitle, '\nConsole.WriteLine(test);\n');
    });
}
