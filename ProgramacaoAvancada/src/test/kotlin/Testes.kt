import org.junit.Test
import kotlin.test.assertEquals

class Fase1Test{
    @Test
    fun testGetKeys(){
        val jObj = JsonObject()
        jObj.addElement(JsonElement("numeros", listOf(1,2,3)))
        jObj.addElement(JsonElement("letra", "a"))
        val keys = jObj.getKeys()
        assertEquals("letra", keys[1])
    }

    @Test
    fun serializeTest() {
        val jObj = JsonObject()
        jObj.addElement(JsonElement("nomes", listOf("Pedro", "Santiago", "Francisca")))
        val serialized = jObj.serialize()
        assertEquals("{\"nomes\" : [\"Pedro\", \"Santiago\", \"Francisca\"]}", serialized)
    }
}

class Fase2Test{
    data class Produto(val name: String, val id: Int)
    @Test
    fun reflexionTest(){
        val produto1 = Produto("pão", 1)
        val jObj = generateJson(produto1)

        assertEquals("{\"Produto\" : {\"id\":1, \"name\":\"pão\"}}", jObj.serialize())
    }
}