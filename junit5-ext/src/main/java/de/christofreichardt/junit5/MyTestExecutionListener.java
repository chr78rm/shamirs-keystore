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
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 *
 * @author christof.reichardt
 */
public class MyTestExecutionListener implements TestExecutionListener, Traceable {

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
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		System.out.printf("Testplan execution has been finished ...\n");

		TracerFactory.getInstance().closePoolTracer();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "executionStarted(TestIdentifier testIdentifier)");

		try {
			tracer.logMessage(LogLevel.INFO, String.format("%s started ...", testIdentifier.getDisplayName()),
					getClass(), "executionStarted(TestIdentifier testIdentifier)");
			tracer.out().printfIndentln("testIdentifier.getUniqueId() = %s", testIdentifier.getUniqueId());
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
		} finally {
			tracer.wayout();
		}
	}

	@Override
	public AbstractTracer getCurrentTracer() {
		return TracerFactory.getInstance().getCurrentPoolTracer();
	}

}
