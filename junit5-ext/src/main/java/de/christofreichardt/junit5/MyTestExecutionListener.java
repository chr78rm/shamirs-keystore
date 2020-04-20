/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2020, Christof Reichardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.christofreichardt.junit5;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 *
 * @author christof.reichardt
 */
public class MyTestExecutionListener implements TestExecutionListener, Traceable {

	class Summary {
		final String id;

		int containers;
		int tests;
		int successful;
		int failed;
		int aborted;

		public Summary(String id) {
			this.id = id;
		}

		void incrementContainers() {
			this.containers++;
		}

		void incrementTests() {
			this.tests++;
		}

		void incrementsSuccessful() {
			this.successful++;
		}

		void incrementFailed() {
			this.failed++;
		}

		void incrementAborted() {
			this.aborted++;
		}

		@Override
		public String toString() {
			return String.format("id = %s, containers = %d, tests = %d, successful = %d, failed = %d, aborted = %d",
					this.id, this.containers, this.tests, this.successful, this.failed, this.aborted);
		}

		void printSummary(PrintStream out) {
			out.printf("%s, (uniqueId = %s)\n", MyTestExecutionListener.this.id2DisplayName.get(this.id), this.id);
			out.printf("containers = %d, tests = %d, successful = %d, failed = %d, aborted = %d\n\n",
					this.containers, this.tests, this.successful, this.failed, this.aborted);
		}
	}

	Map<String, Summary> summaries = new LinkedHashMap<>();
	Map<String, String> id2DisplayName = new HashMap<>();

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		System.out.printf("%s: Testplan execution has been started ...\n", Thread.currentThread().getName());
		System.out.printf("Using de.christofreichardt.junit5.traceConfig = %s...\n", System.getProperty("de.christofreichardt.junit5.traceConfig"));

		try {
			TracerFactory.getInstance().reset();
			if (System.getProperties().containsKey("de.christofreichardt.junit5.traceConfig")) {
				InputStream resourceAsStream = MyTestExecutionListener.class.getClassLoader()
						.getResourceAsStream(System.getProperty("de.christofreichardt.junit5.traceConfig"));
				if (resourceAsStream != null) {
					TracerFactory.getInstance().readConfiguration(resourceAsStream);
				}
			}
			TracerFactory.getInstance().openPoolTracer();
			AbstractTracer tracer = getCurrentTracer();
			tracer.initCurrentTracingContext();
		} catch (TracerFactory.Exception ex) {
			throw new RuntimeException("Tracer configuration failed,", ex);
		}

		traceTestPlan(testPlan);
	}
	void traceTestPlan(TestPlan testPlan) {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "traceTestPlan(TestPlan testPlan)");

		try {
			class TestPlanWalker {

				void walk(TestIdentifier parent) {
					tracer.out().printfIndentln("uniqueId = %s, displayName = %s", parent.getUniqueId(), parent.getDisplayName());
					MyTestExecutionListener.this.id2DisplayName.put(parent.getUniqueId(), parent.getDisplayName());
					for (TestIdentifier testIdentifier : testPlan.getChildren(parent)) {
						walk(testIdentifier);
					}
				}
			}

			TestPlanWalker testPlanWalker = new TestPlanWalker();
			for (TestIdentifier testIdentifier : testPlan.getRoots()) {
				testPlanWalker.walk(testIdentifier);
			}
		} finally {
			tracer.wayout();
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		System.out.printf("Testplan execution has been finished ...\n");

		AbstractTracer tracer = getCurrentTracer();
		tracer.out().println();
		tracer.out().printfIndentln("-> Summary");
		tracer.out().printfIndentln("==========");
		this.summaries.entrySet()
				.forEach(entry -> entry.getValue().printSummary(tracer.out()));

		TracerFactory.getInstance().closePoolTracer();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "executionStarted(TestIdentifier testIdentifier)");

		try {
			tracer.logMessage(LogLevel.INFO, String.format("%s started ...", testIdentifier.getDisplayName()),
					getClass(), "executionStarted(TestIdentifier testIdentifier)");
			tracer.out().printfIndentln("testIdentifier.getUniqueId() = %s, testIdentifier.getParentId() = %s", testIdentifier.getUniqueId(), testIdentifier.getParentId().orElse("[]"));
			tracer.out().printfIndentln("testIdentifier.isContainer() = %b, testIdentifier.isTest() = %b",
					testIdentifier.isContainer(), testIdentifier.isTest());

			if (testIdentifier.getParentId().isPresent()) {
				String parentId = testIdentifier.getParentId().get();
				Summary summary = this.summaries.getOrDefault(parentId, new Summary(parentId));
				if (testIdentifier.isContainer()) {
					summary.incrementContainers();
				}
				if (testIdentifier.isTest()) {
					summary.incrementTests();
				}
				this.summaries.putIfAbsent(parentId, summary);
			}
		} finally {
			tracer.wayout();
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this,
				"executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)");

		try {
			tracer.out().printfIndentln("%s[%s] finished with status %s.", testIdentifier.getDisplayName(),
					testIdentifier.getUniqueId(), testExecutionResult.getStatus());

			if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
				testExecutionResult.getThrowable().ifPresent(throwable -> {
					tracer.out().printfIndentln("%s: %s", throwable.getClass().getName(), throwable.getMessage());
					do {
						StackTraceElement[] stackTraceElements = throwable.getStackTrace();
						for (StackTraceElement stackTraceElement : stackTraceElements) {
							tracer.out().printfIndentln("  at %s.%s(%s:%d)", stackTraceElement.getClassName(),
									stackTraceElement.getMethodName(), stackTraceElement.getFileName(),
									stackTraceElement.getLineNumber());
						}
						throwable = throwable.getCause();
						if (throwable != null) {
							tracer.out().printfIndentln("caused by = %s: %s", throwable.getClass().getName(),
									throwable.getMessage());
						}
					} while (throwable != null);
				});
			}

			if (testIdentifier.getParentId().isPresent()) {
				Summary summary = this.summaries.get(testIdentifier.getParentId().get());
				switch (testExecutionResult.getStatus()) {
					case SUCCESSFUL:
						summary.incrementsSuccessful();
						break;
					case FAILED:
						summary.incrementFailed();
						break;
					case ABORTED:
						summary.incrementAborted();
						break;
					default:
						break;
				}
			}
		} finally {
			tracer.wayout();
		}
	}

	@Override
	public AbstractTracer getCurrentTracer() {
		return TracerFactory.getInstance().getCurrentPoolTracer();
	}

}
