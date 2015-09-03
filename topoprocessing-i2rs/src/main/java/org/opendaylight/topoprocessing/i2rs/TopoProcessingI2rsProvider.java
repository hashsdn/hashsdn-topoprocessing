/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.topoprocessing.i2rs;

import org.opendaylight.topoprocessing.spi.provider.TopoProcessingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopoProcessingI2rsProvider implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TopoProcessingI2rsProvider.class);

    @Override
    public void close() throws Exception {
        LOGGER.info("TopoprocessingI2rsProvider close");
    }

    public void startup(TopoProcessingProvider topoProvider) {
        LOGGER.info("TopoprocessingI2rsProvider startup");
    }

}