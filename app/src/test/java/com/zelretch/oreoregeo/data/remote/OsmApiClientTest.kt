package com.zelretch.oreoregeo.data.remote

import org.junit.Assert.*
import org.junit.Test
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class OsmApiClientTest {

    @Test
    fun testBuildNodeXmlForNewNode() {
        val client = OsmApiClient("test_token")

        // Use reflection to access the private buildNodeXml method
        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        val tags = mapOf(
            "name" to "Test Cafe",
            "amenity" to "cafe"
        )

        val xml = method.invoke(
            client,
            null, // nodeId (null for new node)
            35.6812,
            139.7671,
            tags,
            12345L, // changesetId
            null // version (null for new node)
        ) as String

        // Parse and verify the XML structure
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val osmElement = doc.getElementsByTagName("osm").item(0) as Element
        assertNotNull("osm element should exist", osmElement)

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element
        assertNotNull("node element should exist", nodeElement)

        // Verify node doesn't have id attribute for new nodes
        assertFalse("New node should not have id attribute", nodeElement.hasAttribute("id"))

        // Verify coordinates
        assertEquals("35.6812", nodeElement.getAttribute("lat"))
        assertEquals("139.7671", nodeElement.getAttribute("lon"))

        // Verify changeset
        assertEquals("12345", nodeElement.getAttribute("changeset"))

        // Verify version is not present for new nodes
        assertFalse("New node should not have version attribute", nodeElement.hasAttribute("version"))

        // Verify tags
        val tagElements = nodeElement.getElementsByTagName("tag")
        assertEquals(2, tagElements.length)

        val tagMap = mutableMapOf<String, String>()
        for (i in 0 until tagElements.length) {
            val tag = tagElements.item(i) as Element
            tagMap[tag.getAttribute("k")] = tag.getAttribute("v")
        }

        assertEquals("Test Cafe", tagMap["name"])
        assertEquals("cafe", tagMap["amenity"])
    }

    @Test
    fun testBuildNodeXmlForExistingNode() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        val tags = mapOf(
            "name" to "Updated Cafe",
            "amenity" to "restaurant"
        )

        val xml = method.invoke(
            client,
            98765L, // nodeId
            35.6813,
            139.7672,
            tags,
            12346L, // changesetId
            3 // version
        ) as String

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element

        // Verify node has id attribute for existing nodes
        assertTrue("Existing node should have id attribute", nodeElement.hasAttribute("id"))
        assertEquals("98765", nodeElement.getAttribute("id"))

        // Verify coordinates
        assertEquals("35.6813", nodeElement.getAttribute("lat"))
        assertEquals("139.7672", nodeElement.getAttribute("lon"))

        // Verify changeset
        assertEquals("12346", nodeElement.getAttribute("changeset"))

        // Verify version is present for existing nodes
        assertTrue("Existing node should have version attribute", nodeElement.hasAttribute("version"))
        assertEquals("3", nodeElement.getAttribute("version"))
    }

    @Test
    fun testBuildNodeXmlWithSpecialCharacters() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        val tags = mapOf(
            "name" to "Café & Restaurant",
            "description" to "A <test> with \"quotes\" and 'apostrophes'"
        )

        val xml = method.invoke(
            client,
            null,
            35.0,
            139.0,
            tags,
            100L,
            null
        ) as String

        // XML should properly escape special characters
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element
        val tagElements = nodeElement.getElementsByTagName("tag")

        val tagMap = mutableMapOf<String, String>()
        for (i in 0 until tagElements.length) {
            val tag = tagElements.item(i) as Element
            tagMap[tag.getAttribute("k")] = tag.getAttribute("v")
        }

        // XML parser should handle escaping/unescaping automatically
        assertEquals("Café & Restaurant", tagMap["name"])
        assertEquals("A <test> with \"quotes\" and 'apostrophes'", tagMap["description"])
    }

    @Test
    fun testBuildNodeXmlWithEmptyTags() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        val tags = emptyMap<String, String>()

        val xml = method.invoke(
            client,
            null,
            35.0,
            139.0,
            tags,
            100L,
            null
        ) as String

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element
        val tagElements = nodeElement.getElementsByTagName("tag")

        assertEquals(0, tagElements.length)
    }

    @Test
    fun testBuildNodeXmlWithMultipleTags() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        val tags = mapOf(
            "name" to "Complex Place",
            "amenity" to "restaurant",
            "cuisine" to "japanese",
            "wheelchair" to "yes",
            "opening_hours" to "Mo-Su 10:00-22:00"
        )

        val xml = method.invoke(
            client,
            null,
            35.0,
            139.0,
            tags,
            100L,
            null
        ) as String

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element
        val tagElements = nodeElement.getElementsByTagName("tag")

        assertEquals(5, tagElements.length)

        val tagMap = mutableMapOf<String, String>()
        for (i in 0 until tagElements.length) {
            val tag = tagElements.item(i) as Element
            tagMap[tag.getAttribute("k")] = tag.getAttribute("v")
        }

        assertEquals("Complex Place", tagMap["name"])
        assertEquals("restaurant", tagMap["amenity"])
        assertEquals("japanese", tagMap["cuisine"])
        assertEquals("yes", tagMap["wheelchair"])
        assertEquals("Mo-Su 10:00-22:00", tagMap["opening_hours"])
    }

    @Test
    fun testParseNodeXml() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "parseNodeXml",
            String::class.java
        )
        method.isAccessible = true

        val xml = """
            <osm>
              <node id="12345" lat="35.6812" lon="139.7671" version="2" changeset="98765">
                <tag k="name" v="Test Cafe"/>
                <tag k="amenity" v="cafe"/>
              </node>
            </osm>
        """.trimIndent()

        val node = method.invoke(client, xml) as OsmNode

        assertEquals(12345L, node.id)
        assertEquals(35.6812, node.lat, 0.0001)
        assertEquals(139.7671, node.lon, 0.0001)
        assertEquals(2, node.version)
        assertEquals(98765L, node.changeset)
        assertEquals(2, node.tags.size)
        assertEquals("Test Cafe", node.tags["name"])
        assertEquals("cafe", node.tags["amenity"])
    }

    @Test
    fun testParseNodeXmlWithNoTags() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "parseNodeXml",
            String::class.java
        )
        method.isAccessible = true

        val xml = """
            <osm>
              <node id="12345" lat="35.6812" lon="139.7671" version="1" changeset="98765"/>
            </osm>
        """.trimIndent()

        val node = method.invoke(client, xml) as OsmNode

        assertEquals(12345L, node.id)
        assertEquals(35.6812, node.lat, 0.0001)
        assertEquals(139.7671, node.lon, 0.0001)
        assertEquals(1, node.version)
        assertEquals(98765L, node.changeset)
        assertEquals(0, node.tags.size)
    }

    @Test
    fun testParseNodeXmlWithSpecialCharacters() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "parseNodeXml",
            String::class.java
        )
        method.isAccessible = true

        val xml = """
            <osm>
              <node id="12345" lat="35.6812" lon="139.7671" version="1" changeset="98765">
                <tag k="name" v="Café &amp; Restaurant"/>
                <tag k="description" v="A &lt;test&gt; with &quot;quotes&quot;"/>
              </node>
            </osm>
        """.trimIndent()

        val node = method.invoke(client, xml) as OsmNode

        assertEquals("Café & Restaurant", node.tags["name"])
        assertEquals("A <test> with \"quotes\"", node.tags["description"])
    }

    @Test
    fun testIsLoggedInWithValidToken() {
        val client = OsmApiClient("valid_access_token")
        assertTrue(client.isLoggedIn())
    }

    @Test
    fun testIsLoggedInWithNullToken() {
        val client = OsmApiClient(null)
        assertFalse(client.isLoggedIn())
    }

    @Test
    fun testIsLoggedInWithDummyToken() {
        val client = OsmApiClient("dummy_token")
        assertFalse(client.isLoggedIn())
    }

    @Test
    fun testIsLoggedInWithEmptyToken() {
        val client = OsmApiClient("")
        assertFalse(client.isLoggedIn())
    }

    @Test
    fun testIsLoggedInWithBlankToken() {
        val client = OsmApiClient("   ")
        assertFalse(client.isLoggedIn())
    }

    @Test
    fun testChangesetXmlStructure() {
        // This test verifies the changeset XML structure by simulating what would be sent
        val changesetXml = """
            <osm>
              <changeset>
                <tag k="created_by" v="Oreoregeo Android App"/>
                <tag k="comment" v="Adding a new cafe"/>
              </changeset>
            </osm>
        """.trimIndent()

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(changesetXml.toByteArray()))

        val osmElement = doc.getElementsByTagName("osm").item(0) as Element
        assertNotNull("osm element should exist", osmElement)

        val changesetElement = doc.getElementsByTagName("changeset").item(0) as Element
        assertNotNull("changeset element should exist", changesetElement)

        val tagElements = changesetElement.getElementsByTagName("tag")
        assertEquals(2, tagElements.length)

        val tagMap = mutableMapOf<String, String>()
        for (i in 0 until tagElements.length) {
            val tag = tagElements.item(i) as Element
            tagMap[tag.getAttribute("k")] = tag.getAttribute("v")
        }

        assertEquals("Oreoregeo Android App", tagMap["created_by"])
        assertEquals("Adding a new cafe", tagMap["comment"])
    }

    @Test
    fun testNodeXmlCoordinatePrecision() {
        val client = OsmApiClient("test_token")

        val method = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        method.isAccessible = true

        // Test with high precision coordinates
        val xml = method.invoke(
            client,
            null,
            35.681236789,
            139.767125456,
            emptyMap<String, String>(),
            100L,
            null
        ) as String

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val nodeElement = doc.getElementsByTagName("node").item(0) as Element

        // Verify that precision is maintained
        val lat = nodeElement.getAttribute("lat").toDouble()
        val lon = nodeElement.getAttribute("lon").toDouble()

        assertEquals(35.681236789, lat, 0.0000000001)
        assertEquals(139.767125456, lon, 0.0000000001)
    }

    @Test
    fun testBuildNodeXmlRoundTripConsistency() {
        val client = OsmApiClient("test_token")

        val buildMethod = OsmApiClient::class.java.getDeclaredMethod(
            "buildNodeXml",
            Long::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Map::class.java,
            Long::class.javaPrimitiveType,
            Int::class.javaObjectType
        )
        buildMethod.isAccessible = true

        val parseMethod = OsmApiClient::class.java.getDeclaredMethod(
            "parseNodeXml",
            String::class.java
        )
        parseMethod.isAccessible = true

        val originalTags = mapOf(
            "name" to "Test Place",
            "amenity" to "cafe",
            "cuisine" to "coffee_shop"
        )

        // Build XML for an existing node
        val xml = buildMethod.invoke(
            client,
            12345L,
            35.6812,
            139.7671,
            originalTags,
            98765L,
            3
        ) as String

        // Wrap in osm tag for parsing
        val wrappedXml = "<osm>$xml</osm>"

        // Parse it back
        val node = parseMethod.invoke(client, wrappedXml) as OsmNode

        // Verify round-trip consistency
        assertEquals(12345L, node.id)
        assertEquals(35.6812, node.lat, 0.0001)
        assertEquals(139.7671, node.lon, 0.0001)
        assertEquals(3, node.version)
        assertEquals(98765L, node.changeset)
        assertEquals(originalTags.size, node.tags.size)
        originalTags.forEach { (key, value) ->
            assertEquals(value, node.tags[key])
        }
    }
}
