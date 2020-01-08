package org.hawkular.alerts.rest

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Import/Export REST tests.
 *
 * @author Lucas Ponce
 */
@QuarkusTest
class ImportExportITest extends AbstractQuarkusITestBase {

    @Test
    void importExportTest() {
        String basePath = new File(".").canonicalPath
        String toImport = new File(basePath + "/src/test/resources/import/alerts-data.json").text

        def resp = client.post(path: "import/delete", body: toImport)
        assertEquals(200, resp.status)

        resp = client.get(path: "export")
        assertEquals(200, resp.status)

        // Original definitions from alerts-data.json should be in the backend
        assertEquals(9, resp.data.triggers.size())
        assertEquals(1, resp.data.actions.size())
    }

    @Test
    void importGroupTest() {
        String basePath = new File(".").canonicalPath
        String toImport = new File(basePath + "/src/test/resources/import/groups-data.json").text

        def resp = client.post(path: "import/delete", body: toImport)
        assertEquals(200, resp.status)

        resp = client.get(path: "export")
        assertEquals(200, resp.status)

        assertEquals(3, resp.data.triggers.size())
    }


}
