
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

@Target(AnnotationTarget.PROPERTY)
annotation class Inject
annotation class InjectAdd

class Injector {
    companion object {
        val map: MutableMap<String, MutableList<KClass<*>>> = mutableMapOf()

        init {
            val scanner = Scanner(File("di.properties"))
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                val parts = line.split("=")
                val props = parts[1].split(",")
                props.forEach {
                    map.getOrPut(parts[0], ::mutableListOf).add(Class.forName(it).kotlin)
                }
            }
            scanner.close()
        }

        fun <T : Any> create(type: KClass<T>): T {
            val o: T = type.createInstance()
            type.declaredMemberProperties.forEach {
                if (it.hasAnnotation<Inject>()) {
                    it.isAccessible = true
                    val key = type.simpleName + "." + it.name
                    val obj = map[key]!![0].createInstance()
                    (it as KMutableProperty<*>).setter.call(o, obj)
                }

                if (it.hasAnnotation<InjectAdd>()) {
                    it.isAccessible = true
                    val key = type.simpleName + "." + it.name
                    val listOfActions = it.getter.call(o) as MutableList<Any>
                    map[key]!!.forEach{
                        val obj = it.createInstance()
                        listOfActions.add(obj)
                    }

                }
            }

            return o
        }

    }
}

interface Action{
    val name: String
    fun execute(window: JsonTree)
}

class ToText : Action {
    override val name: String
        get() = "Escrever em Ficheiro"

    override fun execute(window: JsonTree) {
        val file = File("SerializedText.txt")
        file.appendText("\n")
        file.appendText(window.text.text)

    }
}

