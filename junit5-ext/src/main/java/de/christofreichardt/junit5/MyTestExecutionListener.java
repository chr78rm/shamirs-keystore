/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2024, Christof Reichardt
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @author christof.reichardt
 */
public class MyTestExecutionListener implements TestExecutionListener, Traceable {

    int succeeded;
    int failed;
    int aborted;
    int containerFailed;
    int containerSucceeded;
    int containerAborted;
    
//    boolean shouldClose = false;

    class Test {

        final TestIdentifier testIdentifier;
        final LocalDateTime startTime;
        LocalDateTime endTime;
        TestExecutionResult.Status status;

        public Test(TestIdentifier testIdentifier, LocalDateTime startTime) {
            this.testIdentifier = testIdentifier;
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Optional<Duration> getDuration() {
            return this.endTime != null ? Optional.of(Duration.between(this.startTime, this.endTime)) : Optional.empty();
        }

        public TestIdentifier getTestIdentifier() {
            return testIdentifier;
        }

        public Optional<TestExecutionResult.Status> getStatus() {
            return this.status != null ? Optional.of(this.status) : Optional.empty();
        }

        public void setStatus(TestExecutionResult.Status status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("%s[container=%b, test=%b, start=%s, end=%s, duration=%s, status=%s]",
                    this.testIdentifier.getDisplayName(),
                    this.testIdentifier.isContainer(),
                    this.testIdentifier.isTest(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this.startTime),
                    this.endTime != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this.endTime) : "-",
                    getDuration().isPresent() ? getDuration().get() : "-",
                    getStatus().isPresent() ? getStatus().get() : "-");
        }
    }

    class Summary {

        final String id;

        final Map<String, Test> tests = new HashMap<>();

        public Summary(String id) {
            this.id = id;
        }

        void addTest(Test test) {
            this.tests.put(test.getTestIdentifier().getUniqueId(), test);
        }

        void setStatus(String id, TestExecutionResult.Status status) {
            Test test = this.tests.get(id);
            test.setStatus(status);
            test.setEndTime(LocalDateTime.now());
        }

        long getContainerCount() {
            return this.tests.values().stream()
                    .filter(test -> test.getTestIdentifier().isContainer())
                    .count();
        }

        long getTestCount() {
            return this.tests.values().stream()
                    .filter(test -> test.getTestIdentifier().isTest())
                    .count();
        }

        long getSuccessful() {
            return this.tests.values().stream()
                    .filter(test -> test.getStatus().isPresent() && test.getStatus().get() == TestExecutionResult.Status.SUCCESSFUL)
                    .count();
        }

        long getFailed() {
            return this.tests.values().stream()
                    .filter(test -> test.getStatus().isPresent() && test.getStatus().get() == TestExecutionResult.Status.FAILED)
                    .count();
        }

        long getAborted() {
            return this.tests.values().stream()
                    .filter(test -> test.getStatus().isPresent() && test.getStatus().get() == TestExecutionResult.Status.ABORTED)
                    .count();
        }

        @Override
        public String toString() {
            return String.format("id = %s, containers = %d, tests = %d, successful = %d, failed = %d, aborted = %d",
                    this.id, getContainerCount(), getTestCount(), getSuccessful(), getFailed(), getAborted());
        }

        void printSummary(PrintStream out) {
            out.println("-------------------------------------");
            out.printf("%s, (uniqueId = %s)\n", MyTestExecutionListener.this.id2DisplayName.get(this.id), this.id);
            out.printf("containers = %d, tests = %d, successful = %d, failed = %d, aborted = %d\n",
                    getContainerCount(), getTestCount(), getSuccessful(), getFailed(), getAborted());
            out.println("-------------------------------------");
            this.tests.values().forEach(test -> out.printf("%s\n", test));
            out.println();
        }
    }

    final Map<String, Summary> summaries = new LinkedHashMap<>();
    final Map<String, String> id2DisplayName = new HashMap<>();
    
    static final public String TRACE_CONFIG_PROPERTY = "de.christofreichardt.junit5.traceConfig";
    final String traceConfig = System.getProperty(TRACE_CONFIG_PROPERTY);
    static final public String SUMMARY_PROPERTY = "de.christofreichardt.junit5.summary";
    final String summary = System.getProperty(SUMMARY_PROPERTY);

    public MyTestExecutionListener() {
        
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.printf("%s: Testplan execution has been started ...\n", Thread.currentThread().getName());
        System.out.printf("Using %s = %s...\n", TRACE_CONFIG_PROPERTY, this.traceConfig);
        System.out.printf("Using %s = %s...\n", SUMMARY_PROPERTY, this.summary);

        try {
            if (this.traceConfig != null) {
                TracerFactory.getInstance().reset();
                try {
                    try (InputStream resourceAsStream = MyTestExecutionListener.class.getClassLoader().getResourceAsStream(this.traceConfig)) {
                        if (resourceAsStream != null) {
                            TracerFactory.getInstance().readConfiguration(resourceAsStream);
                        }
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                TracerFactory.getInstance().openPoolTracer();
                AbstractTracer tracer = getCurrentTracer();
                tracer.initCurrentTracingContext();
            }
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
                    testPlan.getChildren(parent).forEach(testIdentifier -> {
                        walk(testIdentifier);
                    });
                }
            }

            TestPlanWalker testPlanWalker = new TestPlanWalker();
            testPlan.getRoots().forEach(testIdentifier -> {
                testPlanWalker.walk(testIdentifier);
            });
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.printf("Testplan execution has been finished ...\n");

        boolean shouldClose = false;
        AbstractTracer tracer = getCurrentTracer();
        PrintStream out = tracer.out();
        try {
            if (this.summary != null) {
                try {
                    out = new PrintStream(Files.newOutputStream(Paths.get(this.summary)));
                    shouldClose = true;
                } catch (IOException ex) {
                    out = tracer.out();
                }
            }
            out.println();
            out.printf("-> Summary\n");
            out.printf("=====================================\n");
            out.println();
            for (Map.Entry<String, Summary> entry : this.summaries.entrySet()) {
                entry.getValue().printSummary(out);
            }
            out.println();
            out.printf("Tests--------------------------------\n");
            out.printf("succeeded: %d\n", this.succeeded);
            out.printf("   failed: %d\n", this.failed);
            out.printf("  aborted: %d\n", this.aborted);
            out.printf("-------------------------------------\n");
            out.println();
            out.printf("Container----------------------------\n");
            out.printf("succeeded: %d\n", this.containerSucceeded);
            out.printf("   failed: %d\n", this.containerFailed);
            out.printf("  aborted: %d\n", this.containerAborted);
            out.printf("-------------------------------------\n");
            out.println();
        } finally {
            if (shouldClose) {
                out.close();
            }
        }

        if (this.traceConfig != null) {
            TracerFactory.getInstance().closePoolTracer();
        }
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
                summary.addTest(new Test(testIdentifier, LocalDateTime.now()));
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
                summary.setStatus(testIdentifier.getUniqueId(), testExecutionResult.getStatus());
            }

            if (testIdentifier.isTest()) {
                switch (testExecutionResult.getStatus()) {
                    case SUCCESSFUL:
                        this.succeeded++;
                        break;
                    case FAILED:
                        this.failed++;
                        break;
                    case ABORTED:
                        this.aborted++;
                        break;
                    default:
                        break;
                }
            } else if (testIdentifier.isContainer()) {
                switch (testExecutionResult.getStatus()) {
                    case SUCCESSFUL:
                        this.containerSucceeded++;
                        break;
                    case FAILED:
                        this.containerFailed++;
                        break;
                    case ABORTED:
                        this.containerAborted++;
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
