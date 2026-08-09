package com.epam.gym.config;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import com.epam.gym.util.TransactionExecutor;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/** Direct calls to each @Bean method — cheaper and more focused than booting the full context. */
class BeanConfigurationTest {

    private final BeanConfiguration config = new BeanConfiguration();

    @Test
    void allBeanMethodsProduceNonNullInstances() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        TransactionExecutor tx = config.transactionExecutor(sessionFactory);
        assertNotNull(tx);

        UserDao userDao = config.userDao();
        TraineeDao traineeDao = config.traineeDao();
        TrainerDao trainerDao = config.trainerDao();
        TrainingDao trainingDao = config.trainingDao();
        TrainingTypeDao trainingTypeDao = config.trainingTypeDao();
        assertNotNull(userDao);
        assertNotNull(traineeDao);
        assertNotNull(trainerDao);
        assertNotNull(trainingDao);
        assertNotNull(trainingTypeDao);

        AuthenticationService authenticationService = config.authenticationService(userDao, tx);
        assertNotNull(authenticationService);

        TraineeService traineeService = config.traineeService(traineeDao, trainerDao, trainingDao, userDao, tx);
        assertNotNull(traineeService);

        TrainerService trainerService =
                config.trainerService(trainerDao, trainingTypeDao, trainingDao, userDao, tx);
        assertNotNull(trainerService);

        TrainingService trainingService = config.trainingService(traineeDao, trainerDao, trainingDao, tx);
        assertNotNull(trainingService);

        TrainingTypeService trainingTypeService = config.trainingTypeService(trainingTypeDao, tx);
        assertNotNull(trainingTypeService);
    }
}
