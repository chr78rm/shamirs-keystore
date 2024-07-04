package de.christofreichardt.jca.shamir;

import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import java.nio.file.Path;
import scala.Tuple2;
import scala.collection.immutable.IndexedSeq;
import scala.math.BigInt;

public class ShamirsFacade {

    public char[] mergeSlices(Path[] paths) {
        return SecretMerging.apply(paths).password();
    }

    static public record CertificationResult(int falsified, int verified) {
    }

    static public class Splitter {
        final SecretSharing secretSharing;

        public Splitter(int shares, int threshold, CharSequence password) {
            this.secretSharing = new SecretSharing(shares, threshold, password);
        }

        public CertificationResult certified() {
            SecretSharing.CertificationResult certificationResult = this.secretSharing.certified();
            return new CertificationResult(certificationResult.falsified(), certificationResult.verified());
        }

        public CertificationResult saveCertifiedPartition(int[] sizes, Path path) {
            SecretSharing.CertificationResult certificationResult = this.secretSharing.saveCertifiedPartition(sizes, path);
            return new CertificationResult(certificationResult.falsified(), certificationResult.verified());
        }

        public void savePartition(int[] sizes, Path path) {
            this.secretSharing.savePartition(sizes, path);
        }

        @Override
        public String toString() {
            return this.secretSharing.toString();
        }
    }

    static public class Merger {
        final Splitter splitter;

        public Merger(Splitter splitter) {
            this.splitter = splitter;
        }

        public char[] password() {
            int threshold = this.splitter.secretSharing.threshold();
            IndexedSeq<Tuple2<BigInt, BigInt>> sharePoints = this.splitter.secretSharing.sharePoints().take(threshold).toIndexedSeq();
            BigInt prime = this.splitter.secretSharing.prime();
            SecretMerging secretMerging = new SecretMerging(sharePoints, prime);
            return secretMerging.password();
        }
    }
}
