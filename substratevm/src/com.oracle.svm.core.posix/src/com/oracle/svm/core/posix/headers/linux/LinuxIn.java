/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.posix.headers.linux;

/* Allow underscores in names: Checkstyle: stop. */

import com.oracle.svm.core.posix.headers.NetinetIn;
import com.oracle.svm.core.posix.headers.PosixDirectives;

import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CFieldAddress;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.impl.DeprecatedPlatform;
import org.graalvm.word.PointerBase;

@CContext(PosixDirectives.class)
@Platforms(DeprecatedPlatform.LINUX_SUBSTITUTION.class)
public class LinuxIn {
    // @formatter:off
    // struct ip_mreqn {
    //        struct in_addr  imr_multiaddr;          /* IP multicast address of group */
    //        struct in_addr  imr_address;            /* local IP address of interface */
    //        int             imr_ifindex;            /* Interface index */
    // };
    // @formatter:on
    @CStruct(addStructKeyword = true)
    public interface ip_mreqn extends PointerBase {

        @CFieldAddress
        NetinetIn.in_addr imr_multiaddr();

        @CFieldAddress
        NetinetIn.in_addr imr_address();

        @CField
        int imr_ifindex();

        @CField
        void set_imr_ifindex(int value);
    }
}
