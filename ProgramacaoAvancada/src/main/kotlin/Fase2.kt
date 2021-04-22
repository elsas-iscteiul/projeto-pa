import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties

data class Aluno(val nome: String, val numero: Int)

fun generateJson(name: String, col : Collection<*>): JsonObject{
    val finalValue = JsonObject()
    finalValue.addElement(JsonElement(name, col))
    return finalValue
}

fun generateJson(m : Map<*,*>): JsonObject{
    val jObject = JsonObject()
    m.forEach{
        when{
            it.value is Int -> jObject.addElement(JsonElement(it.key.toString(), it.value as Int))
            it.value is Boolean -> jObject.addElement(JsonElement(it.key.toString(), it.value as Boolean))
            it.value is String -> jObject.addElement(JsonElement(it.key.toString(), it.value as String))
            it.value is Collection<*> -> jObject.addElement(JsonElement(it.key.toString(), it.value as Collection<*>))
        }
    }
    return jObject
}



fun generateJson(a: Any) : JsonObject{
    val c: KClass<Any> = a::class as KClass<Any>
    val key = c.simpleName
    val values = c.declaredMemberProperties
    val jObject = JsonObject()
    values.forEach {
        when{
            it.get(a) is Int -> jObject.addElement(JsonElement(it.name, it.get(a) as Int))
            it.get(a) is Boolean -> jObject.addElement(JsonElement(it.name, it.get(a) as Boolean))
            it.get(a) is Collection<*> -> jObject.addElement(JsonElement(it.name, it.get(a) as Collection<*>))
            it.get(a) is String -> jObject.addElement(JsonElement(it.name, it.get(a) as String))
        }
    }
    val finalValue = JsonObject()
    finalValue.addElement(JsonElement(c.simpleName!!, jObject))
    return finalValue
}



fun main(){
    //val a = Aluno("Pedro", 99233)
    //val jObject = generateJson(a)
    val mapTest = mapOf("Joao" to listOf<Int>(1,2,3), "Manuela" to listOf(4,5,6))
    val jObject = generateJson(mapTest)
    val textSerializer = VisitorTextSerialize()
    jObject.accept(textSerializer)
    println(textSerializer.serializedText)

}