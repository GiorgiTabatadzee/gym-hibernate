package com.epam.gym.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HibernatePropertiesTest {

    @Test
    void gettersReflectSetValues() {
        HibernateProperties properties = new HibernateProperties();
        properties.setDialect("org.hibernate.dialect.PostgreSQLDialect");
        properties.setDdlAuto("validate");
        properties.setShowSql(true);

        assertEquals("org.hibernate.dialect.PostgreSQLDialect", properties.getDialect());
        assertEquals("validate", properties.getDdlAuto());
        assertTrue(properties.isShowSql());
    }

    @Test
    void ddlAuto_defaultsToValidate_andShowSqlDefaultsToFalse() {
        HibernateProperties properties = new HibernateProperties();

        assertEquals("validate", properties.getDdlAuto());
        assertFalse(properties.isShowSql());
    }
}
