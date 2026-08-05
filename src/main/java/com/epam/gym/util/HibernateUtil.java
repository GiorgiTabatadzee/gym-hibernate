package com.epam.gym.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a single, application-wide SessionFactory from hibernate.cfg.xml.
 * A dedicated cfg file/resource name can be supplied (used by tests to point
 * at an isolated in-memory H2 instance instead of the file-based dev database).
 */
public final class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory("hibernate.cfg.xml");
        }
        return sessionFactory;
    }

    /** Rebuilds the factory from an alternate config resource (e.g. for tests). Not thread-safe by design. */
    public static synchronized SessionFactory rebuild(String cfgResourceName) {
        shutdown();
        sessionFactory = buildSessionFactory(cfgResourceName);
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory(String cfgResourceName) {
        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure(cfgResourceName)
                    .build();
            return new Configuration().configure(cfgResourceName)
                    .buildSessionFactory(registry);
        } catch (Exception e) {
            log.error("Hibernate SessionFactory creation failed", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
