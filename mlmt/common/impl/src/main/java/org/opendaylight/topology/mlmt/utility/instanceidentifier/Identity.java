/*
 * Copyright (c) 2016 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.topology.mlmt.utility.instanceidentifier;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class Identity implements Transformer<InstanceIdentifier.PathArgument, InstanceIdentifier.PathArgument> {

    @Override
    public PathArgument transform(PathArgument item) {
        return item;
    }
}