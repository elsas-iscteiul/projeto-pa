class JsonElement(val field: String) {
    var fieldValue: Any? = null
    constructor(field: String, dataType: String) : this(field) {fieldValue = dataType}
    constructor(field: String, dataType: Int?) : this(field) { fieldValue = dataType} //Podia criar outro construtor para o tipo null, mas estou a receber ca os nulls
    constructor(field: String, dataType: Boolean) : this(field){fieldValue = dataType}
    constructor(field: String, dataType: Collection<*>) : this(field){fieldValue = dataType}
    constructor(field: String, dataType: JsonObject) : this(field){fieldValue = dataType}


}

//JsonObject = {} pode ter elementos la dentro, neste caso JsonDataType
class JsonObject  {
    val children : MutableList<JsonElement> = mutableListOf()

    fun addElement(je : JsonElement){
        children.add(je)
    }

    var fieldValue: Any? = children

    fun accept(v: Visitor) {
        children.forEach {
            if(it.fieldValue is JsonObject)
                (it.fieldValue as JsonObject).accept(v)
        }
        v.visit(this)
    }

    fun serialize(): String {
        val serializer = VisitorTextSerialize()
        this.accept(serializer)
        return serializer.serializedText
    }
}

interface Visitor{
    fun visit(jo: JsonObject)
}

class VisitorTextSerialize: Visitor{
    var serializedText = ""

    override fun visit(jo: JsonObject) {
        val everyValue = jo.children.joinToString { "\"" + it.field + "\"" + " : " + parseType(it.fieldValue) }
        serializedText = "{$everyValue}"
    }

}

fun parseType(fieldValue: Any?): String {
    if (fieldValue is JsonObject){
        var parsedString = "{"
        parsedString += fieldValue.children.joinToString(separator = ", ") {
            "\"" + it.field + "\"" + ":" + parseType(it.fieldValue)
        }
        return parsedString + "}"
    }
    if (fieldValue is Collection<*>){ //Pode ser ArrayList pq e um datatype do tipo array ou porque e children de JsonObject
        var parsedString = "["
        parsedString += fieldValue.joinToString(separator = ", ") {
            if (it is String)
                "\"$it\""
            else
                "$it"
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
            found.add(it.field)
        }

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

    val stringFind = VisitorReturnKeys()
    val textSerializer = VisitorTextSerialize()

    jsonObject1.accept(textSerializer)
    jsonObject1.accept(stringFind)
    println(textSerializer.serializedText)

    stringFind.found.forEach { println(it) }




}