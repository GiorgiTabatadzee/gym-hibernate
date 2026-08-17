package com.epam.gym.config;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TraineeDaoImpl;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainerDaoImpl;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingDaoImpl;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dao.TrainingTypeDaoImpl;
import com.epam.gym.dao.UserDao;
import com.epam.gym.dao.UserDaoImpl;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.AuthenticationServiceImpl;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TraineeServiceImpl;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainerServiceImpl;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingServiceImpl;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.service.TrainingTypeServiceImpl;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.util.HibernateTransactionExecutor;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Wires the existing plain-Hibernate DAO/service layer (built in the previous module, untouched
 * in its transaction/session handling) into the Spring container as beans, so the REST controllers
 * can get them injected instead of constructing them by hand like {@code Application} used to.
 */
@Configuration
@EnableConfigurationProperties(HibernateProperties.class)
public class BeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BeanConfiguration.class);

    /**
     * Builds the native Hibernate SessionFactory the existing DAO layer runs against, sourcing the
     * JDBC connection from the Spring Boot-autoconfigured {@link DataSource} (itself built from
     * {@code spring.datasource.*} in the active profile's application-{profile}.yml — so switching
     * {@code local}/{@code dev}/{@code stg}/{@code prod} switches the database) and the
     * dialect/ddl-auto/show-sql behavior from {@link HibernateProperties} ({@code app.hibernate.*},
     * also profile-specific). Deliberately native Hibernate rather than spring-orm's
     * LocalSessionFactoryBean/spring-data-jpa, to keep the existing Session-based DAO layer unchanged.
     */
    @Bean(destroyMethod = "close")
    public SessionFactory sessionFactory(DataSource dataSource, HibernateProperties hibernateProperties) {
        log.info("Building Hibernate SessionFactory: dialect={}, ddlAuto={}, showSql={}",
                hibernateProperties.getDialect(), hibernateProperties.getDdlAuto(), hibernateProperties.isShowSql());

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource)
                .applySetting(AvailableSettings.DIALECT, hibernateProperties.getDialect())
                .applySetting(AvailableSettings.HBM2DDL_AUTO, hibernateProperties.getDdlAuto())
                .applySetting(AvailableSettings.SHOW_SQL, String.valueOf(hibernateProperties.isShowSql()))
                .applySetting(AvailableSettings.FORMAT_SQL, String.valueOf(hibernateProperties.isShowSql()))
                .build();

        MetadataSources metadataSources = new MetadataSources(registry)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Trainee.class)
                .addAnnotatedClass(Trainer.class)
                .addAnnotatedClass(Training.class)
                .addAnnotatedClass(TrainingType.class);

        return metadataSources.buildMetadata().buildSessionFactory();
    }

    @Bean
    public TransactionExecutor transactionExecutor(SessionFactory sessionFactory) {
        return new HibernateTransactionExecutor(sessionFactory);
    }

    @Bean
    public UserDao userDao() {
        return new UserDaoImpl();
    }

    @Bean
    public TraineeDao traineeDao() {
        return new TraineeDaoImpl();
    }

    @Bean
    public TrainerDao trainerDao() {
        return new TrainerDaoImpl();
    }

    @Bean
    public TrainingDao trainingDao() {
        return new TrainingDaoImpl();
    }

    @Bean
    public TrainingTypeDao trainingTypeDao() {
        return new TrainingTypeDaoImpl();
    }

    @Bean
    public AuthenticationService authenticationService(UserDao userDao, TransactionExecutor transactionExecutor) {
        return new AuthenticationServiceImpl(userDao, transactionExecutor);
    }

    @Bean
    public TraineeService traineeService(TraineeDao traineeDao, TrainerDao trainerDao, TrainingDao trainingDao,
                                          UserDao userDao, TransactionExecutor transactionExecutor) {
        return new TraineeServiceImpl(traineeDao, trainerDao, trainingDao, userDao, transactionExecutor);
    }

    @Bean
    public TrainerService trainerService(TrainerDao trainerDao, TrainingTypeDao trainingTypeDao,
                                          TrainingDao trainingDao, UserDao userDao,
                                          TransactionExecutor transactionExecutor) {
        return new TrainerServiceImpl(trainerDao, trainingTypeDao, trainingDao, userDao, transactionExecutor);
    }

    @Bean
    public TrainingService trainingService(TraineeDao traineeDao, TrainerDao trainerDao,
                                            TrainingDao trainingDao, TransactionExecutor transactionExecutor) {
        return new TrainingServiceImpl(traineeDao, trainerDao, trainingDao, transactionExecutor);
    }

    @Bean
    public TrainingTypeService trainingTypeService(TrainingTypeDao trainingTypeDao,
                                                     TransactionExecutor transactionExecutor) {
        return new TrainingTypeServiceImpl(trainingTypeDao, transactionExecutor);
    }
}
