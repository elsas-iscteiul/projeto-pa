abstract class JsonElement(){
    abstract fun accept(visitor: Visitor)
    abstract fun serialize() : String

    open var value: Any? = null
}



fun isValidType(dataType: Any?): Boolean {
    var valid  = false
    when(dataType){
        is Int? -> valid = true
        is String -> valid = true
        is Boolean -> valid = true
        is Double -> valid = true
        is JsonObject -> valid = true
        is JsonArray -> valid = true
    }
    return valid
}

class JsonData(var name: String) : JsonElement() {


    override var value: Any? = null
    constructor(name: String, dataType: Any?) : this(name) {
        if(isValidType(dataType))
            value = dataType
        if(dataType is Collection<*>){
            val jArr = JsonArray()
            dataType.forEach {
                if (it != null) {
                    jArr.addElement(it)
                }
            }
            value = jArr
        }
    }

    override fun accept(v: Visitor) {
        v.visit(this)
        if(value is JsonObject)
            (value as JsonObject).accept(v)
        if(value is JsonArray)
            (value as JsonArray).accept(v)

    }

    override fun serialize() : String {
       return " \"${this.name}\" : ${parseType(this.value)}"
    }

}

class JsonArray() : JsonElement(){

    constructor(col : Collection<*>) : this(){
        col.forEach {
            if (it != null) {
                children.add(it)
            }
        }
    }

    val children = mutableListOf<Any>()

    fun addElement(a : Any){
        if(isValidType(a))
        children.add(a)
    }

    override fun accept(v: Visitor) {
        v.visit(this)
        children.forEach {
            if(it is JsonObject)
                it.accept(v)
        }
        v.endVisit()
    }

    override fun serialize(): String {
        val everyValue = this.children.joinToString {
            parseType(it)
        }
        return "[$everyValue]"
    }


}

//VER JSON ARRAY


class JsonObject  : JsonElement() {
    val children : MutableList<JsonData> = mutableListOf()

    fun addElement(jd : JsonData){
        children.add(jd)
    }

    override fun accept(v: Visitor) {
        v.visit(this)
        children.forEach {
            it.accept(v)
        }
        v.endVisit()


    }

    override fun serialize(): String {
        val everyValue = this.children.joinToString {
            (if (it is JsonData ){
                "\"" + it.name + "\"" + " : " + parseType(it.value)
            }
            else {
                "[" + parseType(it.value) + "]"
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
    fun visit(je: JsonData)
    fun endVisit()
}



fun parseType(value: Any?): String {
    if (value is JsonObject){
        var parsedString = "{"
        parsedString += value.children.joinToString(separator = ", ") {
            (if (it is JsonData ){
                "\"" + it.name + "\"" + " : " + parseType(it.value)
            }
            else {
                "[" + parseType(it.value) + "]"
            }).toString()
        }
        return parsedString + "}"
    }
    if (value is JsonArray){
        var parsedString = "["
        parsedString += value.children.joinToString(separator = ", ") {
                "${parseType(it)}"
        }
        return parsedString + "]"
    }
    return when(value){
        is String -> "\"$value\""
        else -> "$value"
    }
}

class VisitorReturnKeys : Visitor {

    var found = mutableListOf<String>()

    override fun visit(jo: JsonObject) {
        jo.children.forEach {
            if(it is JsonData)
                found.add(it.name)
        }

    }

    override fun visit(ja: JsonArray) {
        return
    }

    override fun visit(je: JsonData){}
    override fun endVisit() {
    }
}

fun main(){
    val test1 = JsonData("nome","Paulo")
    val test2 = JsonData("idade", 15)
    val test3 = JsonData("nome", "Francisca")

    val a = JsonData("1", 1)
    val b = JsonData("2", 2)




    val jsonObject1 = JsonObject()

    val jObject = JsonObject()
    jObject.addElement(test1)
    jObject.addElement(test2)
    jObject.addElement(test3)


    val test6 = JsonData("Pessoas", jObject)
    jsonObject1.addElement(test6)

    println(jsonObject1.serialize())


}