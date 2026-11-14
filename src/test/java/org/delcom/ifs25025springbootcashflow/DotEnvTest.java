package org.delcom.ifs25025springbootcashflow;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DotEnvTest {

    @Test
    void loadsKeysIntoSystemPropertiesWithoutOverridingExistingOnes() throws Exception {
        System.setProperty("DOTENV_TEST_EXISTING", "asli");
        Path file = Files.createTempFile("dotenv-test", ".env");
        Files.write(file, List.of(
                "# komentar",
                "",
                "DOTENV_TEST_PLAIN=nilai",
                "DOTENV_TEST_DOUBLE=\"dalam kutip\"",
                "DOTENV_TEST_SINGLE='kutip tunggal'",
                "DOTENV_TEST_MIXED=\"campur'",
                "DOTENV_TEST_MIXED2='campur\"",
                "DOTENV_TEST_SHORT=a",
                "DOTENV_TEST_EXISTING=baru",
                "PATH=jangan-timpa",
                "tanpa-sama-dengan",
                "=tanpa-kunci"));

        DotEnv.load(file);

        assertEquals("nilai", System.getProperty("DOTENV_TEST_PLAIN"));
        assertEquals("dalam kutip", System.getProperty("DOTENV_TEST_DOUBLE"));
        assertEquals("kutip tunggal", System.getProperty("DOTENV_TEST_SINGLE"));
        assertEquals("\"campur'", System.getProperty("DOTENV_TEST_MIXED"));
        assertEquals("'campur\"", System.getProperty("DOTENV_TEST_MIXED2"));
        assertEquals("a", System.getProperty("DOTENV_TEST_SHORT"));
        assertEquals("asli", System.getProperty("DOTENV_TEST_EXISTING"));
        assertNull(System.getProperty("PATH"));
        assertNull(System.getProperty("tanpa-sama-dengan"));
    }

    @Test
    void missingFileIsIgnored() throws Exception {
        DotEnv.load(Path.of("tidak-ada-" + System.nanoTime() + ".env"));
        DotEnv.load();
    }
}
