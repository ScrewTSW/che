/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import java.security.PrivateKey;
import java.security.PublicKey;

/** @author Anton Korneta */
public class SignatureKeyPair {

  private final PublicKey publicKey;
  private final PrivateKey privateKey;

  public SignatureKeyPair(PrivateKey privateKey, PublicKey publicKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public PublicKey getPublic() {
    return publicKey;
  }

  public PrivateKey getPrivate() {
    return privateKey;
  }
}
