package com.epam.gym.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-environment Hibernate behavior (task: "each environment - different db properties"),
 * bound from {@code app.hibernate.*} in the active profile's application-{profile}.yml —
 * separate from {@code spring.datasource.*}, which supplies the connection itself.
 */
@ConfigurationProperties(prefix = "app.hibernate")
public class HibernateProperties {

    /** e.g. org.hibernate.dialect.H2Dialect, org.hibernate.dialect.PostgreSQLDialect */
    private String dialect;

    /** hibernate.hbm2ddl.auto value: "update" for local/dev, "validate" for stg/prod. */
    private String ddlAuto = "validate";

    private boolean showSql = false;

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public void setDdlAuto(String ddlAuto) {
        this.ddlAuto = ddlAuto;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }
}
