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
package org.apache.karaf.examples.jdbc.provider;

import org.apache.karaf.examples.rest.api.Booking;
import org.apache.karaf.examples.rest.api.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * JDBC-My implementation of a booking service.
 */
public class BookingServiceJdbcImpl implements BookingService {

    private final static Logger LOGGER = LoggerFactory.getLogger(BookingService.class);

    private final static String DATABASE_SCHEMA = "KARAF_EXAMPLE";

    private final static String[] createTableQueryDerbyTemplate = new String[] {
            "CREATE SCHEMA " + DATABASE_SCHEMA,

            "CREATE TABLE " + DATABASE_SCHEMA + ".BOOKING(id SMALLINT NOT NULL GENERATED BY DEFAULT AS IDENTITY "
                    + " CONSTRAINT BOOKING_PK PRIMARY KEY, customer VARCHAR(200) NOT NULL, flight VARCHAR(100))"};

    /** Select queries */
    private final static String selectBookingSql =
            "select id, customer, flight "
                    + "from " + DATABASE_SCHEMA + ".BOOKING";

    /** Insert queries */
    private final static String insertBookingSql =
            "insert into " + DATABASE_SCHEMA + ".BOOKING "
                    + "(customer, flight) "
                    + "values (?, ?)";

    /** Update queries */
    private final static String updateBookingSql =
            "update " + DATABASE_SCHEMA + ".BOOKING "
                    + "set customer = ?, flight = ?"
                    + "where id = ?";

    /** Delete queries */
    private final static String deleteBookingSql =
            "delete from " + DATABASE_SCHEMA + ".BOOKING "
                    + "where id = ?";

    private DataSource _dataSource = null; 

    public BookingServiceJdbcImpl(DataSource ds) {
		_dataSource = ds;
		LOGGER.debug("Datasource {} ", _dataSource);
		try (Connection connection = _dataSource.getConnection()) {
		    createTables(connection);
		} catch (Exception e) {
		    LOGGER.error("Error creating table ", e);
		}    	
    }

    private String getValue(Dictionary<String, Object> config, String key, String defaultValue) {
        String value = (String) config.get(key);
        return (value != null) ? value : defaultValue;
    }

    private void createTables(Connection connection) {

        DatabaseMetaData dbm;
        ResultSet tables;

        try {
            dbm = connection.getMetaData();

            tables = dbm.getTables(null, "KARAF_EXAMPLE", "BOOKING", null);
            if (!tables.next()) {
                LOGGER.info("Tables does not exist");
                // Tables does not exist so we create all the tables
                String[] createTemplate = createTemplate = createTableQueryDerbyTemplate;
                try (Statement createStatement = connection.createStatement()) {
                    for (int cpt = 0; cpt < createTemplate.length; cpt++) {
                        createStatement.addBatch(createTemplate[cpt]);
                    }
                    if (createStatement.executeBatch().length == 0) {
                        throw new SQLException("No table has been created !");
                    }
                    LOGGER.info("Schema and tables has been created");
                } catch (SQLException exception) {
                    LOGGER.error("Can't create tables", exception);
                }
            } else {
                LOGGER.info("Tables already exist");
            }
        } catch (SQLException exception) {
            LOGGER.error("Can't verify tables existence", exception);
        }
    }

    @Override
    public Collection<Booking> list() {
        Collection<Booking> bookings = new ArrayList<>();

        try (Connection connection = _dataSource.getConnection()) {

            try (PreparedStatement selectStatement = connection.prepareStatement(selectBookingSql)) {
                ResultSet rs = selectStatement.executeQuery();

                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setId(rs.getLong("id"));
                    booking.setCustomer(rs.getString("customer"));
                    booking.setFlight(rs.getString("flight"));
                    bookings.add(booking);
                }

            } catch (SQLException exception) {
                LOGGER.error("Can't retreive the bookings", exception);
            }

        } catch (Exception exception) {
            LOGGER.error("Error getting connection ", exception);
        }
        return bookings;
    }

    @Override
    public Booking get(Long id) {
        try (Connection connection = _dataSource.getConnection()) {

            String sqlQuery = selectBookingSql + " where id = ?";

            try (PreparedStatement selectStatement = connection.prepareStatement(sqlQuery)) {
                selectStatement.setLong(1, id);
                ResultSet rs = selectStatement.executeQuery();

                if (rs.next()) {
                    Booking booking = new Booking();
                    booking.setId(id);
                    booking.setCustomer(rs.getString("customer"));
                    booking.setFlight(rs.getString("flight"));
                    return booking;
                }

            } catch (SQLException exception) {
                LOGGER.error("Can't find booking with id {}", id, exception);
            }

        } catch (Exception exception) {
            LOGGER.error("Error getting connection ", exception);
        }
        return null;
    }

    @Override
    public void add(Booking booking) {
        try (Connection connection = _dataSource.getConnection()) {

            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertBookingSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                // set values
                insertStatement.setString(1, booking.getCustomer());
                insertStatement.setString(2, booking.getFlight());
                insertStatement.executeUpdate();

                int newId = 0;
                ResultSet rs = insertStatement.getGeneratedKeys();

                if (rs.next()) {
                    newId = rs.getInt(1);
                }

                connection.commit();

                booking.setId(Long.valueOf(newId));
                LOGGER.debug("Booking created with id = {}", newId);

            } catch (SQLException exception) {
                connection.rollback();
                LOGGER.error("Can't insert booking with customer {}", booking.getCustomer(), exception);
            }

        } catch (Exception exception) {
            LOGGER.error("Error getting connection ", exception);
        }
    }

    @Override
    public void remove(Long id) {
        try (Connection connection = _dataSource.getConnection()) {

            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }

            try (PreparedStatement deleteStatement =
                         connection.prepareStatement(deleteBookingSql)) {
                // where values
                deleteStatement.setInt(1, id.intValue());
                deleteStatement.executeUpdate();
                connection.commit();
                LOGGER.debug("Service deleted with id = {}", id);
            } catch (SQLException exception) {
                connection.rollback();
                LOGGER.error("Can't delete service with id {}", id, exception);
            }

        } catch (Exception exception) {
            LOGGER.error("Error getting connection ", exception);
        }
    }

	@Override
	public void update(Booking booking) {
		// TODO Auto-generated method stub
		
	}
}
