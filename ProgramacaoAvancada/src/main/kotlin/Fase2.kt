
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class Ignore

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class DifferentName(val name: String)

@DifferentName("Estudante")
data class Aluno(
    @DifferentName("Identificaçao")
    val nome: String,
    val numero: Int,
    @Ignore
    val curso: String
    )


fun generateJson(m : Map<*,*>): JsonObject{
    val jObject = JsonObject()
    m.forEach{
        jObject.addElement(JsonElement(it.key.toString(), it.value))
    }
    return jObject
}

fun generateJson(key: String, value: Any?): JsonObject{
    val jObj = JsonObject()
    jObj.addElement(JsonElement(key, value))
    return jObj
}

fun generateJson(value: Collection<*>): JsonArray{
    return JsonArray(value)
}


fun generateJson(a: Any) : JsonObject{
    val c: KClass<Any> = a::class as KClass<Any>
    try {
        validateDataClass(c)
    } catch (e: Exception){
        println(e.message)
        return JsonObject()

    }
    var key = c.simpleName!!
    if(c.hasAnnotation<DifferentName>()) {
        key = c.findAnnotation<DifferentName>()!!.name
    }
    val values = c.declaredMemberProperties
    val jObject = JsonObject()
    values.forEach {
        if(!it.hasAnnotation<Ignore>()) {
           if(it.hasAnnotation<DifferentName>()){
               val name = it.findAnnotation<DifferentName>()!!.name
               jObject.addElement(JsonElement(name, it.get(a)))
           }
            else{
               jObject.addElement(JsonElement(it.name, it.get(a)))
            }
        }
    }
    val finalValue = JsonObject()
    finalValue.addElement(JsonElement(key, jObject))
    return finalValue
}

fun validateDataClass(c: KClass<Any>){
    if(c.declaredMemberProperties.isEmpty())
        throw IllegalArgumentException("Necessarios os argumentos Key e Value")
}


fun main(){
    val a = Aluno("Pedro", 99233, "MEI")
    val jObject = generateJson(a)
    println(jObject.serialize())
    //val mapTest = mapOf("Joao" to listOf<Int>(1,2,3), "Manuela" to listOf(4,5,6))
    //val jObject = generateJson(mapTest)
    val intTester = generateJson("idade" , null)
    val arrTester = generateJson(listOf(intTester, 2, 5, jObject))


}