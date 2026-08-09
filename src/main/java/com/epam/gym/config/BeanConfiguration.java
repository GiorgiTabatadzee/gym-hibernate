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
import com.epam.gym.util.HibernateTransactionExecutor;
import com.epam.gym.util.HibernateUtil;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the existing plain-Hibernate DAO/service layer (built in the previous module, untouched
 * in its transaction/session handling) into the Spring container as beans, so the REST controllers
 * can get them injected instead of constructing them by hand like {@code Application} used to.
 */
@Configuration
public class BeanConfiguration {

    @Bean(destroyMethod = "close")
    public SessionFactory sessionFactory() {
        return HibernateUtil.getSessionFactory();
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
