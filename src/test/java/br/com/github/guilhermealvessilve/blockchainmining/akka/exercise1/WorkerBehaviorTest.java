package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise1;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;
import br.com.github.guilhermealvessilve.blockchainmining.utils.BlocksData;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WorkerBehaviorTest {

    @Test
    void shouldMiningFailIfNonceNotInRange() {
        final BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.newInstance());
        final TestInbox<HashResult> testInbox = TestInbox.create();
        final var block = BlocksData.getNextBlock(0, "0");
        final var message = new WorkerBehavior.Command(0, 5, block, testInbox.getRef());
        testActor.run(message);
        final List<CapturedLogEvent> allLogEntries = testActor.getAllLogEntries();

        assertEquals(1, allLogEntries.size());
        assertEquals("null", allLogEntries.get(0).message());
        assertEquals(Level.DEBUG, allLogEntries.get(0).level());
    }

    @Test
    void shouldMiningPassesIfNonceInRange() {
        final BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.newInstance(
                () -> new HashResult(82701, "0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b")));
        final TestInbox<HashResult> testInbox = TestInbox.create();
        final var block = BlocksData.getNextBlock(0, "0");
        final var message = new WorkerBehavior.Command(82700, 5, block, testInbox.getRef());
        testActor.run(message);
        final List<CapturedLogEvent> allLogEntries = testActor.getAllLogEntries();

        assertEquals(1, allLogEntries.size());
        final String expectedResult = "82701 : 0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b";
        assertEquals(expectedResult, allLogEntries.get(0).message());
        assertEquals(Level.DEBUG, allLogEntries.get(0).level());
    }

    @Test
    void shouldReceiveNoMessageMiningFailIfNonceNotInRange() {
        final BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.newInstance());
        final TestInbox<HashResult> testInbox = TestInbox.create();
        final var block = BlocksData.getNextBlock(0, "0");
        final var message = new WorkerBehavior.Command(0, 5, block, testInbox.getRef());
        testActor.run(message);

        assertFalse(testInbox.hasMessages());
    }

    @Test
    void shouldReceiveMessageMiningPassesIfNonceInRange() {
        final BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.newInstance(
                () -> new HashResult(82701, "0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b")));
        final TestInbox<HashResult> testInbox = TestInbox.create();
        final var block = BlocksData.getNextBlock(0, "0");
        final var message = new WorkerBehavior.Command(82700, 5, block, testInbox.getRef());
        testActor.run(message);

        final var expectedHasResult = new HashResult(82701, "0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b");
        testInbox.expectMessage(expectedHasResult);
    }
}
