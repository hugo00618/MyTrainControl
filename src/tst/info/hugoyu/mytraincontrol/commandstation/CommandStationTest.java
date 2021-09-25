package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class CommandStationTest {

    @Mock
    Trainset trainset1, trainset2;

    CommandStation instance;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        instance = CommandStation.getNewInstance();
    }

    @Test
    public void addTaskSort() {
        long t1 = System.currentTimeMillis();
        long t2 = t1 + 100;
        instance.addTask(new SetSpeedTask(trainset2, t2));
        instance.addTask(new SetSpeedTask(trainset1, t1));

        List<AbstractCommandStationTask> tasks = getTasksList();
        assertEquals(2, tasks.size());
        assertEquals(trainset1, tasks.get(0).getTrainset());
        assertEquals(trainset2, tasks.get(1).getTrainset());
    }

    @Test
    public void addTaskSortDelayed() {
        long t1 = System.currentTimeMillis();
        long t2 = t1 + 100;
        instance.addTask(new SetSpeedTask(trainset1, t1, 5000));
        instance.addTask(new SetSpeedTask(trainset2, t2));

        List<AbstractCommandStationTask> tasks = getTasksList();
        assertEquals(2, tasks.size());

        assertEquals(trainset2, tasks.get(0).getTrainset());
        assertEquals(t2, tasks.get(0).getTaskCreationTime());
        assertEquals(t2, tasks.get(0).getScheduledExecutionTime());
        assertFalse(tasks.get(0).isDelayedTask());

        assertEquals(trainset1, tasks.get(1).getTrainset());
        assertEquals(t1, tasks.get(1).getTaskCreationTime());
        assertEquals(t1 + 5000, tasks.get(1).getScheduledExecutionTime());
        assertTrue(tasks.get(1).isDelayedTask());
    }

    @Test
    public void addTaskDedupe() {
        long t1 = System.currentTimeMillis();
        long t2 = t1 + 100;

        instance.addTask(new SetSpeedTask(trainset1, t1, 5000));
        instance.addTask(new SetSpeedTask(trainset2, t2, 2500));

        List<AbstractCommandStationTask> tasks = getTasksList();
        assertEquals(2, tasks.size());

        assertEquals(trainset2, tasks.get(0).getTrainset());
        assertEquals(t2, tasks.get(0).getTaskCreationTime());
        assertEquals(t2 + 2500, tasks.get(0).getScheduledExecutionTime());

        assertEquals(trainset1, tasks.get(1).getTrainset());
        assertEquals(t1, tasks.get(1).getTaskCreationTime());
        assertEquals(t1 + 5000, tasks.get(1).getScheduledExecutionTime());

        long t3 = t2 + 100;
        instance.addTask(new SetSpeedTask(trainset1, t3));

        tasks = getTasksList();
        assertEquals(2, tasks.size());

        assertEquals(trainset1, tasks.get(0).getTrainset());
        assertEquals(t1, tasks.get(0).getTaskCreationTime());
        assertEquals(t3, tasks.get(0).getScheduledExecutionTime());
        assertTrue(tasks.get(0).isDelayedTask());

        assertEquals(trainset2, tasks.get(1).getTrainset());
        assertEquals(t2, tasks.get(1).getTaskCreationTime());
        assertEquals(t2 + 2500, tasks.get(1).getScheduledExecutionTime());
    }

    private List<AbstractCommandStationTask> getTasksList() {
        PriorityQueue<AbstractCommandStationTask> tasks = new PriorityQueue<>(instance.getTasks());
        List<AbstractCommandStationTask> tasksList = new ArrayList<>();
        while (!tasks.isEmpty()) {
            tasksList.add(tasks.poll());
        }
        return tasksList;
    }

}