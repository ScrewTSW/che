/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.utils.FileUtil.removeEmptyDirectory;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
public abstract class TestWorkspaceLogsGrabber {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  /**
   * Grabs logs from workspace.
   *
   * @param workspace workspace which logs should be grabbed.
   * @param pathToStore location of directory where logs should be stored.
   */
  public void grabLogs(TestWorkspace workspace, Path pathToStore) {
    if (!canLogsBeGrabbed()) {
      return;
    }

    final String workspaceId;
    try {
      workspaceId = workspace.getId();
    } catch (ExecutionException | InterruptedException e) {
      LOG.warn("It's impossible to get id of test workspace.", e);
      return;
    }

    // check if workspace is running
    try {
      WorkspaceStatus status = workspaceServiceClient.getStatus(workspaceId);
      if (status != RUNNING) {
        LOG.warn(
            "It's impossible to get logs of workspace with workspaceId={} because of improper status {}",
            workspaceId,
            status);
        return;
      }
    } catch (Exception e) {
      LOG.warn("It's impossible to get status of workspace with id={}", workspaceId, e);
      return;
    }

    getLogs().forEach(workspaceLog -> workspaceLog.grab(workspaceId, pathToStore));
  }

  /**
   * Returns bash command to grab logs from workspace by path to them inside workspace.
   *
   * @param workspaceId ID of workspace
   * @param testLogsDirectory location of directory to save the logs
   * @param logDestinationInsideWorkspace destination to logs inside workspace
   * @return command to grab log from workspace
   */
  abstract String getGrabLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logDestinationInsideWorkspace);

  /**
   * Gets list of logs inside workspace
   *
   * @return list of logs to grab
   */
  abstract List<WorkspaceLog> getLogs();

  /**
   * Checks if it is possible to grab logs from workspace.
   *
   * @return <b>true</b> if it is possible to grab logs from workspace, or <b>false</b> otherwise.
   */
  abstract boolean canLogsBeGrabbed();

  class WorkspaceLog {
    private final String name;
    private final Path destinationInsideWorkspace;

    WorkspaceLog(String name, Path destinationInsideWorkspace) {
      this.name = name;
      this.destinationInsideWorkspace = destinationInsideWorkspace;
    }

    String getName() {
      return name;
    }

    Path getDestinationInsideWorkspace() {
      return destinationInsideWorkspace;
    }

    private void grab(String workspaceId, Path pathToStore) {
      ListLineConsumer outputConsumer = new ListLineConsumer();
      Path testLogsDirectory = pathToStore.resolve(workspaceId).resolve(getName());

      try {
        Files.createDirectories(testLogsDirectory.getParent());

        // execute command to copy logs from workspace container to the workspaceLogsDir
        String[] commandLine = {
          "bash",
          "-c",
          getGrabLogsCommand(workspaceId, testLogsDirectory, getDestinationInsideWorkspace())
        };

        ProcessUtil.executeAndWait(commandLine, PREPARING_WS_TIMEOUT_SEC, SECONDS, outputConsumer);
      } catch (Exception e) {
        LOG.warn(
            "Can't obtain {} logs from workspace with id={} from directory {}. Error: {}",
            getName(),
            workspaceId,
            getDestinationInsideWorkspace(),
            outputConsumer.getText(),
            e);
      } finally {
        try {
          removeEmptyDirectory(testLogsDirectory);
        } catch (IOException e) {
          LOG.warn("Error of removal of empty log directory {}.", testLogsDirectory, e);
        }
      }
    }
  }
}
