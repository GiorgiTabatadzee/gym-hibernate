package com.epam.gym.service;

import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.FakeTransactionExecutor;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    private TrainingTypeServiceImpl service;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        service = new TrainingTypeServiceImpl(trainingTypeDao, new FakeTransactionExecutor());
        cardio = new TrainingType("Cardio");
        cardio.setId(1L);
    }

    @Test
    void create_savesNewTrainingType() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Cardio"))).thenReturn(Optional.empty());
        when(trainingTypeDao.save(any(Session.class), any(TrainingType.class))).thenReturn(cardio);

        TrainingType created = service.create("Cardio");

        assertEquals("Cardio", created.getTrainingTypeName());
    }

    @Test
    void create_throwsValidationException_onBlankName() {
        assertThrows(ValidationException.class, () -> service.create(" "));
    }

    @Test
    void create_throwsValidationException_whenAlreadyExists() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Cardio"))).thenReturn(Optional.of(cardio));
        assertThrows(ValidationException.class, () -> service.create("Cardio"));
    }

    @Test
    void getByName_returnsMatch() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Cardio"))).thenReturn(Optional.of(cardio));
        assertEquals(cardio, service.getByName("Cardio"));
    }

    @Test
    void getByName_throwsEntityNotFound_whenMissing() {
        when(trainingTypeDao.findByName(any(Session.class), eq("Unknown"))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getByName("Unknown"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAll_delegatesToSessionQuery() {
        Session session = org.mockito.Mockito.mock(Session.class);
        Query<TrainingType> query = org.mockito.Mockito.mock(Query.class);
        when(session.createQuery(eq("FROM TrainingType"), eq(TrainingType.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(cardio));

        TrainingTypeServiceImpl serviceWithRealSession = new TrainingTypeServiceImpl(trainingTypeDao,
                new com.epam.gym.util.TransactionExecutor() {
                    @Override
                    public <T> T executeInTransaction(java.util.function.Function<Session, T> work) {
                        return work.apply(session);
                    }
                });

        List<TrainingType> all = serviceWithRealSession.getAll();

        assertEquals(1, all.size());
    }
}
