abstract class Element(){
    abstract fun accept(visitor: Visitor)
    abstract fun serialize() : String

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

    override fun accept(v: Visitor) {
        v.visit(this)
        if(fieldValue is JsonObject)
            (fieldValue as JsonObject).accept(v)
        if(fieldValue is JsonArray)
            (fieldValue as JsonArray).accept(v)

    }

    override fun serialize() : String {
       return "${this.field} : ${parseType(this.fieldValue)}"
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

    override fun accept(v: Visitor) {
        v.visit(this)
        children.forEach {
            if(it is JsonObject)
                it.accept(v)
        }
    }

    override fun serialize(): String {
        val everyValue = this.children.joinToString {
            parseType(it)
        }
        return "[$everyValue]"
    }


}

//VER JSON ARRAY

//JsonObject = {} pode ter elementos la dentro, neste caso JsonDataType
class JsonObject  : Element() {
    val children : MutableList<Element> = mutableListOf()

    fun addElement(e : Element){
        children.add(e)
    }

    override fun accept(v: Visitor) {
        v.visit(this)
        children.forEach {
            it.accept(v)
        }


    }

    override fun serialize(): String {
        val everyValue = this.children.joinToString {
            (if (it is JsonElement ){
                "\"" + it.field + "\"" + " : " + parseType(it.fieldValue)
            }
            else {
                "[" + parseType(it.fieldValue) + "]"
            }).toString()

        }
        return "{$everyValue}"
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
    fun visit(je: JsonElement)
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

    override fun visit(je: JsonElement){}
}

fun main(){
    val test1 = JsonElement("nome","Paulo")
    val test2 = JsonElement("idade", 15)
    val test3 = JsonElement("nome", "Francisca")

    val a = JsonElement("1", 1)
    val b = JsonElement("2", 2)




    val jsonObject1 = JsonObject()

    val jObject = JsonObject()
    jObject.addElement(test1)
    jObject.addElement(test2)
    jObject.addElement(test3)


    val test6 = JsonElement("Pessoas", jObject)
    jsonObject1.addElement(test6)

    println(jsonObject1.serialize())


}