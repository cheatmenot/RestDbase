/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.karaf.examples.rest.websvc;

import org.apache.karaf.examples.rest.api.BookingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private ServiceTracker<BookingService, BookingService> bookingServiceTracker;
    private ServiceRegistration clientServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
    	System.out.println("REST WebSvc Bundle starting...");
        bookingServiceTracker = new ServiceTracker<BookingService, BookingService>(bundleContext, BookingService.class, null) {

            @Override
            public BookingService addingService(ServiceReference<BookingService> reference) {
                BookingService bookingService = bundleContext.getService(reference);
                System.out.println("Got a booking service!");
                ServiceCatcher.setBookingService(bookingService);

                return bookingService;
            }

            @Override
            public void removedService(ServiceReference<BookingService> reference, BookingService service) {
                clientServiceRegistration.unregister();
            }
        };
       
        bookingServiceTracker.open();
        System.out.println("REST WebSvc Bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        bookingServiceTracker.close();
    }
}
