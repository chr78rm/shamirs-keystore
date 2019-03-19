package de.ngda.jca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import scala.Predef;
import scala.collection.Iterable;
import scala.collection.JavaConverters;
import scala.collection.immutable.IndexedSeq;
import scala.collection.mutable.Buffer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShamirsKeystoreUnit implements Traceable {
	
	@BeforeAll
	void systemProperties() {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "systemProperties()");

		try {
			String[] propertyNames = System.getProperties().stringPropertyNames().toArray(new String[0]);
			Arrays.sort(propertyNames);
			for (String propertyName : propertyNames) {
				tracer.out().printfIndentln("%s = %s", propertyName, System.getProperty(propertyName));
			}
		} finally {
			tracer.wayout();
		}
	}

	@BeforeAll
	void setupProvider() {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "setupProvider()");

		try {
			Security.addProvider(new Provider());
			assertThat(Security.getProvider(Provider.NAME)).isNotNull();
		} finally {
			tracer.wayout();
		}
	}
	
	@Test
	@DisplayName("SecretMerging-1")
	void secretMerging_1() {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "secretMerging_1()");

		try {
			List<Path> paths = new ArrayList<>();
			paths.add(Paths.get("..", "shamirs-secret-sharing", "json", "partition-3-1.json"));
			paths.add(Paths.get("..", "shamirs-secret-sharing", "json", "partition-3-2.json"));
			Iterable<Path> iterable = JavaConverters.collectionAsScalaIterable(paths);
			SecretMerging secretMerging = SecretMerging.apply(iterable);
			
			tracer.out().printfIndentln("secretMerging.secretBytes() = (%s)", secretMerging.secretBytes().mkString(","));
		} finally {
			tracer.wayout();
		}
	}
	
	@Test
	@DisplayName("RoundTrip-1")
	void roundTrip_1() {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "roundTrip_1()");

		try {
			String myPassword = "Dies-ist-streng-geheim";
			final int SHARES = 8;
			final int THRESHOLD = 4;
			SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, myPassword.getBytes(StandardCharsets.UTF_8));
			SecretMerging secretMerging = new SecretMerging(secretSharing.sharePoints(), secretSharing.prime());
			assertThat(secretMerging.password()).isEqualTo(myPassword.toCharArray());
		} finally {
			tracer.wayout();
		}
	}

	@Test
	@DisplayName("RoundTrip-2")
	void roundTrip_2() {
		AbstractTracer tracer = getCurrentTracer();
		tracer.entry("void", this, "roundTrip_1()");

		try {
			String myPassword = "Dies-ist-streng-geheim";
			final int SHARES = 8;
			final int THRESHOLD = 4;
			SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, myPassword.getBytes(StandardCharsets.UTF_8));
			final int[] SIZES = {4,2,2};
			secretSharing.savePartition(SIZES, Paths.get("json", "roundtrip-2", "partition"));
			Path[] paths_1 = {Paths.get("json", "roundtrip-2", "partition-0.json")};
			assertThat(SecretMerging.apply(paths_1).password()).isEqualTo(myPassword.toCharArray());
			Path[] paths_2 = {Paths.get("json", "roundtrip-2", "partition-1.json"), Paths.get("json", "roundtrip-2", "partition-2.json")};
			assertThat(SecretMerging.apply(paths_2).password()).isEqualTo(myPassword.toCharArray());
			Path[] paths_3 = {Paths.get("json", "roundtrip-2", "partition-1.json")};
			Throwable catched = catchThrowable(() -> SecretMerging.apply(paths_3).password());
			assertThat(catched).isInstanceOf(IllegalArgumentException.class);
		} finally {
			tracer.wayout();
		}
	}

	@Override
	public AbstractTracer getCurrentTracer() {
		return TracerFactory.getInstance().getCurrentPoolTracer();
	}

}
