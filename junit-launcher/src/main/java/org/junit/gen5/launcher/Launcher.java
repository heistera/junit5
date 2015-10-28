
package org.junit.gen5.launcher;

import org.junit.gen5.engine.*;

import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class Launcher {

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();


	public void registerTestPlanExecutionListeners(TestPlanExecutionListener... testListeners) {
		listenerRegistry.registerTestPlanExecutionListeners(testListeners);
		listenerRegistry.registerTestExecutionListeners(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlan.addTests(testEngine.discoverTests(specification));
		}
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		TestPlan plan = discover(specification);
		execute(plan);
	}

	private void execute(TestPlan testPlan) {
		listenerRegistry.notifyTestPlanExecutionListeners(
				testPlanExecutionListener -> testPlanExecutionListener.testPlanExecutionStarted(testPlan.getTests().size())
		);

		TestExecutionListener compositeListener = listenerRegistry.getCompositeTestExecutionListener();

		for (TestEngine testEngine : lookupAllTestEngines()) {
			testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine), compositeListener);
		}

		listenerRegistry.notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionFinished);
	}

}
