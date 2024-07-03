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

    static public class Splitter {
        final SecretSharing secretSharing;

        public Splitter(int shares, int threshold, CharSequence password) {
            this.secretSharing = new SecretSharing(shares, threshold, password);
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
