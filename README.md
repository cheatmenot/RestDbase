    <!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->
# Apache Karaf REST Example

## Abstract

This example shows how to use JAX-RS to implement a REST service.

This example uses blueprint to deal with the jaxrs-server and Apache CXF as the implementation of the JAXRS specification.

It implements a `BookingService` with a REST implementation. 

The "client" bundle uses the `BookingService` with a REST client stub.

## Artifacts

* **karaf-rest-example-api** is a common bundle containing the `Booking` POJO and the `BookingService` interface.   
* **karaf-rest-example-websvc** is a blueprint bundle providing the `BookingServiceRest` implementation of the `BookingService` interface. It uses JAX-RS.
* **karaf-rest-example-dbase-connector** is a regular Blueprint bundle using the `BookingService`. It uses pax-jdbc-derby as driver.
* **karaf-rest-example-client-jersey** is a regular Blueprint REST client bundle using Jersey. It 
* **karaf-rest-example-features** provides a Karaf features repository used for the deployment.

## Build

The build uses Apache Maven. Simply use:

```
mvn clean install
```

## Feature and Deployment

On a running Karaf instance, register the features repository using:

```
karaf@root()> feature:repo-add mvn:org.apache.karaf.examples/karaf-rest-example-features/4.2.11/xml
```

Then, you can install the application:

```
karaf@root()> feature:install karaf-rest-example-app
```

## Usage

Once you have installed the application, you can use `booking:add` and `booking:list` commands to interact with the REST
service.

```
karaf@root()> booking:add 1 "John Doe" AF520
karaf@root()> booking:list
```