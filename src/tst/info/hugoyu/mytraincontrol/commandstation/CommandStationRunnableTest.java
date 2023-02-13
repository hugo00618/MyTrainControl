package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.DccThrottle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

class CommandStationRunnableTest {

    private CommandStation commandStation;
    private CommandStationRunnable sut;

    @Mock
    DccThrottle throttle;

    @Mock
    Trainset trainset;

    private SetThrottleTask throttleTask;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        commandStation = CommandStation.getNewInstance();
        sut = new CommandStationRunnable(commandStation);

        throttleTask = new SetThrottleTask(throttle, 0);
    }

    @Test
    public void testGetNextAvailableToExecuteTaskWaitForTask() throws InterruptedException {
        Future<AbstractCommandStationTask> future = constructGetNextAvailableToExecuteTaskFuture();

        Thread.sleep(1000);
        assertFalse(future.isDone());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskNextTaskImmediatelyAvailable() throws InterruptedException, ExecutionException {
        commandStation.addTask(throttleTask);

        Future<AbstractCommandStationTask> future = constructGetNextAvailableToExecuteTaskFuture();
        Thread.sleep(100);
        assertTrue(future.isDone());
        assertEquals(throttleTask, future.get());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskNextTaskDelayedByNextAvailableExecutionTime() throws InterruptedException {
        sut.nextAvailableExecutionTime = System.currentTimeMillis() + 10 * 1000;
        commandStation.addTask(throttleTask);

        Thread.sleep(1000);
        assertFalse(constructGetNextAvailableToExecuteTaskFuture().isDone());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskNextTaskNotDelayedBynNextAvailableHighCurrentExecutionTime() {
        sut.nextAvailableHighCurrentExecutionTime = System.currentTimeMillis() + 10 * 1000;
        commandStation.addTask(throttleTask);

        assertFalse(constructGetNextAvailableToExecuteTaskFuture().isDone());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskNextTaskDelayedBynNextAvailableHighCurrentExecutionTime() throws InterruptedException {
        sut.nextAvailableExecutionTime = System.currentTimeMillis();
        sut.nextAvailableHighCurrentExecutionTime = System.currentTimeMillis() + 10 * 1000;
        // high current consumption task
        commandStation.addTask(new SetThrottleTask(throttle,0, 100, null));

        Thread.sleep(1000);
        assertFalse(constructGetNextAvailableToExecuteTaskFuture().isDone());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskReturnsNewlyAddedTask() throws ExecutionException, InterruptedException {
        sut.nextAvailableExecutionTime = System.currentTimeMillis();
        sut.nextAvailableHighCurrentExecutionTime = System.currentTimeMillis() + 10 * 1000;

        // high current consumption task
        SetThrottleTask highConsumptionTask = new SetThrottleTask(throttle,0, 100, null);
        commandStation.addTask(highConsumptionTask);

        commandStation.addTask(throttleTask);

        Future<AbstractCommandStationTask> future = constructGetNextAvailableToExecuteTaskFuture();
        Thread.sleep(100);
        assertTrue(future.isDone());
        assertEquals(throttleTask, future.get());
        assertEquals(1, commandStation.getTasks().size()); // high consumption task
        assertEquals(highConsumptionTask, commandStation.getTasks().peek());
    }

    @Test
    public void testGetNextAvailableToExecuteTaskReturnsWaitedCurrentTask() throws ExecutionException, InterruptedException {
        sut.nextAvailableExecutionTime = System.currentTimeMillis();
        sut.nextAvailableHighCurrentExecutionTime = System.currentTimeMillis() + 2 * 1000;

        // high current consumption task available to be executed 2 secs after
        SetThrottleTask highConsumptionTask = new SetThrottleTask(throttle,0, 100, null);
        commandStation.addTask(highConsumptionTask);

        // task delayed for 10 secs
        SetSpeedTask setSpeedTask = new SetSpeedTask(trainset, System.currentTimeMillis(), 10 * 1000);
        commandStation.addTask(setSpeedTask);

        Future<AbstractCommandStationTask> future = constructGetNextAvailableToExecuteTaskFuture();
        Thread.sleep(3000);
        assertTrue(future.isDone());
        assertEquals(highConsumptionTask, future.get());
        assertEquals(1, commandStation.getTasks().size()); // delayed set speed task
        assertEquals(setSpeedTask, commandStation.getTasks().peek());
    }

    private Future<AbstractCommandStationTask> constructGetNextAvailableToExecuteTaskFuture() {
        CompletableFuture<AbstractCommandStationTask> future = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> future.complete(sut.getNextAvailableTask()));
        return future;
    }
}