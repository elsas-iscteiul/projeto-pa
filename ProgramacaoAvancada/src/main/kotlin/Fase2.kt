import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class Ignore

data class Aluno(val nome: String,
                 val numero: Int,
                 @Ignore
                 val curso: String
                 )


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

fun generateJson(key: String, value: Any?): JsonObject{
    val jObj = JsonObject()
    when(value){
        is Int? ->  jObj.addElement(JsonElement(key, value))
        is String ->  jObj.addElement(JsonElement(key, value))
        is Boolean -> jObj.addElement(JsonElement(key, value))
    }
    return jObj
}


fun generateJson(a: Any) : JsonObject{
    val c: KClass<Any> = a::class as KClass<Any>
    try {
        validateDataClass(c)
    } catch (e: Exception){
        println(e.message)
        return JsonObject()

    }
    val key = c.simpleName
    val values = c.declaredMemberProperties
    val jObject = JsonObject()
    values.forEach {
        if(!it.hasAnnotation<Ignore>()) {
            when {
                it.get(a) is Int -> jObject.addElement(JsonElement(it.name, it.get(a) as Int))
                it.get(a) is Boolean -> jObject.addElement(JsonElement(it.name, it.get(a) as Boolean))
                it.get(a) is Collection<*> -> jObject.addElement(JsonElement(it.name, it.get(a) as Collection<*>))
                it.get(a) is String -> jObject.addElement(JsonElement(it.name, it.get(a) as String))
            }
        }
    }
    val finalValue = JsonObject()
    finalValue.addElement(JsonElement(c.simpleName!!, jObject))
    return finalValue
}

fun validateDataClass(c: KClass<Any>){
    if(c.declaredMemberProperties.isEmpty())
        throw IllegalArgumentException("Necessarios os argumentos Key e Value")
}


fun main(){
    //val a = Aluno("Pedro", 99233, "MEI")
    //val jObject = generateJson(a)
    //val mapTest = mapOf("Joao" to listOf<Int>(1,2,3), "Manuela" to listOf(4,5,6))
    //val jObject = generateJson(mapTest)
    val intTester = generateJson("idade" , null)
    val textSerializer = VisitorTextSerialize()
    intTester.accept(textSerializer)
    println(textSerializer.serializedText)

}