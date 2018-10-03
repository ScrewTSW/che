/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.utils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Screenshooter {

  private static final Logger LOG = LoggerFactory.getLogger(Screenshooter.class);

  @Inject
  @Named("tests.screenshots_dir")
  private String screenshotsDirectory;

  private static SeleniumWebDriver webDriver;

  @Inject
  public Screenshooter(SeleniumWebDriver webDriver) {
    Screenshooter.webDriver = webDriver;
  }

  public void takeScreenshot(Class testClass) {
    Path screenshotPath =
        Paths.get(
            screenshotsDirectory,
            testClass.getName()
                + " - "
                + new Timestamp(System.currentTimeMillis()).toGMTString()
                + ".png");
    try {
      byte[] screenshot = webDriver.getScreenshotAs(OutputType.BYTES);
      Files.createDirectories(screenshotPath.getParent());
      Files.copy(new ByteArrayInputStream(screenshot), screenshotPath);
    } catch (WebDriverException | IOException e) {
      LOG.error("Failed to capture screenshot " + screenshotPath.toString(), e);
    }
  }
}
