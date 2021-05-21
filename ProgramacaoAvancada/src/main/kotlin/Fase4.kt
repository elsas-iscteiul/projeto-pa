
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text
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

interface Modifier{
    fun getImgLoc(e: JsonElement) : String
    fun toShow(a: Any?) : Boolean
    fun setText(jsonData: JsonData) : String
}

class ToText : Action {
    override val name: String
        get() = "Escrever em Ficheiro"

    override fun execute(window: JsonTree) {
        val file = File("SerializedText.txt")
        file.appendText(window.getSerializedText())
        file.appendText("\n")

    }
}

class Edit: Action{
    override val name: String
        get()  = "Editar nome"


    override fun execute(window: JsonTree) {
        val shell = Shell(Display.getDefault())
        shell.setSize(300, 200)
        shell.text = "Name Edit"
        shell.layout = FillLayout()
        val editField = Text(shell, SWT.NONE)
        val button = Button(shell, SWT.PUSH)
        button.text = "EDIT"
        button.addSelectionListener(object : SelectionAdapter(){
            override fun widgetSelected(e: SelectionEvent) {
                val element = window.getSelectedElement()
                if (element is JsonData)
                    element.name = editField.text
                window.refresh()
                shell.dispose()

            }
        })

        shell.pack()
        shell.open()
        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        shell.dispose()
    }

}



class Visualizer : Modifier{
    override fun getImgLoc(e: JsonElement) : String{
        when(e){
            is JsonObject -> return "json-object-icon.jpg"
            is JsonData -> return "json-element-icon.png"
            is JsonArray -> return "json-array-icon.jpg"
        }
        return ""
    }

    override fun setText(jsonData: JsonData) : String{
        val modifiedText = "\"name\" :  \"${jsonData.name}\""
        return modifiedText
    }

    override fun toShow(a: Any?) : Boolean{
        var show = false
        if(a is JsonObject)
            show = true

        return show
    }

}