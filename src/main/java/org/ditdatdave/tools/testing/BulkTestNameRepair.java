package org.ditdatdave.tools.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * This class will take a directory of test classes and rename (where applicable) the files to ensure
 * they end with `Test`.  It will also rename the class references in the file to match the new name.
 *
 * <br>
 *
 * This solves a (self-inflicted) problem where tests have been created without following the class file naming
 * convention.  These tests may be executable in an IDE (which is looking for the @Test annotation) but
 * fail to run using the Maven surefire plugin or similar.  Renaming a few by hand is trivial, but when there are
 * potentially hundreds it is time-consuming.
 */
public class BulkTestNameRepair {

    static class Pair {

        private Path path;
        private String content;

        public static Pair mkPair(Path path) {
            try {
                Pair pair = new Pair();
                pair.path = path;
                pair.content = new String(Files.readAllBytes(path));
                return pair;
            } catch (Exception e) {
                return null;
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Path root = Paths.get("../waltz-dev/waltz-common/src/test/java/foo");
        Files.walk(root)
                .filter(Files::isRegularFile)
                .map(Pair::mkPair)
                .filter(Objects::nonNull)
                .filter(p -> p.content.contains("@Test"))
                .filter(p -> ! p.path.toString().toLowerCase().endsWith("test.java"))
                .forEach(p -> {
                    try {
                        String className = p.path.getFileName().toString().replaceAll("\\.java$", "");
                        String newClassName = className + "Test";
                        String updatedContent = p.content.replaceAll(className, newClassName);
                        Files.write(p.path, updatedContent.getBytes());
                        Files.move(p.path, p.path.getParent().resolve(newClassName + ".java"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

}
