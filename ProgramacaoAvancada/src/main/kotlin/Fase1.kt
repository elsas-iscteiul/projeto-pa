abstract class Element(){
     open var fieldValue: Any? = null
}

class JsonElement(val field: String) : Element() {
    override var fieldValue: Any? = null
    constructor(field: String, dataType: Any?) : this(field) {
        when(dataType){
            is Int? -> fieldValue = dataType
            is String -> fieldValue = dataType
            is Boolean -> fieldValue = dataType
            is Double -> fieldValue = dataType
            is JsonObject -> fieldValue = dataType
            is JsonArray -> fieldValue = dataType
        }
        if(dataType is Collection<*>){
            val jArr = JsonArray()
            dataType.forEach {
                if (it != null) {
                    jArr.addElement(it)
                }
            }
            fieldValue = jArr
        }
    }


}

class JsonArray() : Element(){

    constructor(col : Collection<*>) : this(){
        col.forEach {
            if (it != null) {
                children.add(it)
            }
        }
    }

    val children = mutableListOf<Any>()

    fun addElement(a : Any){
        children.add(a)
    }

    fun accept(v: Visitor) {
        v.visit(this)
    }

    fun serialize() : String{
        val serializer = VisitorTextSerialize()
        this.accept(serializer)
        return serializer.serializedText
    }
}

//VER JSON ARRAY

//JsonObject = {} pode ter elementos la dentro, neste caso JsonDataType
class JsonObject  {
    val children : MutableList<Element> = mutableListOf()

    fun addElement(e : Element){
        children.add(e)
    }

    fun accept(v: Visitor) {
        children.forEach {
            if( it is JsonElement){
                if(it.fieldValue is JsonObject)
                    (it.fieldValue as JsonObject).accept(v)
                if(it.fieldValue is JsonArray)
                    ((it.fieldValue as JsonArray).accept(v))
            }
        }
        v.visit(this)
    }

    fun serialize(): String {
        val serializer = VisitorTextSerialize()
        this.accept(serializer)
        return serializer.serializedText
    }

    fun getKeys(): MutableList<String> {
        val keysGetter = VisitorReturnKeys()
        this.accept(keysGetter)
        return keysGetter.found
    }
}

interface Visitor{
    fun visit(jo: JsonObject)
    fun visit(ja: JsonArray)
}

class VisitorTextSerialize: Visitor{
    var serializedText = ""

    override fun visit(jo: JsonObject) {

        val everyValue = jo.children.joinToString {
            (if (it is JsonElement ){
                "\"" + it.field + "\"" + " : " + parseType(it.fieldValue)
            }
            else {
                "[" + parseType(it.fieldValue) + "]"
            }).toString()

        }
        serializedText = "{$everyValue}"
    }


    override fun visit(ja: JsonArray) {
        val everyValue = ja.children.joinToString {
            parseType(it)
        }
        serializedText = "[$everyValue]"
    }

}

fun parseType(fieldValue: Any?): String {
    if (fieldValue is JsonObject){
        var parsedString = "{"
        parsedString += fieldValue.children.joinToString(separator = ", ") {
            (if (it is JsonElement ){
                "\"" + it.field + "\"" + " : " + parseType(it.fieldValue)
            }
            else {
                "[" + parseType(it.fieldValue) + "]"
            }).toString()
        }
        return parsedString + "}"
    }
    if (fieldValue is JsonArray){
        var parsedString = "["
        parsedString += fieldValue.children.joinToString(separator = ", ") {
                "${parseType(it)}"
        }
        return parsedString + "]"
    }
    return when(fieldValue){
        is String -> "\"$fieldValue\""
        else -> "$fieldValue"
    }
}

class VisitorReturnKeys : Visitor {

    var found = mutableListOf<String>()

    override fun visit(jo: JsonObject) {
        jo.children.forEach {
            if(it is JsonElement)
                found.add(it.field)
        }

    }

    override fun visit(ja: JsonArray) {
        return
    }
}

fun main(){
    val test1 = JsonElement("nome","Paulo")
    val test2 = JsonElement("idade", 15)
    val test3 = JsonElement("nome", "Francisca")
    val test4 = JsonElement("listapessoas", arrayListOf<String>("A","B","C"))
    val test5 = JsonElement("Testenull", null)


    val jsonObject1 = JsonObject()
    jsonObject1.addElement(test1)

    val jObject = JsonObject()
    jObject.addElement(test1)
    jObject.addElement(test2)
    jObject.addElement(test3)

    val test6 = JsonElement("Pessoas", jObject)
    jsonObject1.addElement(test6)

    val jobjectList = mutableListOf<JsonObject>()
    jobjectList.add(jsonObject1)
    jobjectList.add(jObject)


    val jObj3 = JsonObject()
    jObj3.addElement(JsonElement("TesteArrayJOBJECTS", jobjectList))




    println(jObj3.serialize())


    val jArrayTest = JsonArray(arrayListOf("joao",2,3,4))
    println(jArrayTest.serialize())


}