
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*



fun main() {
    val aluno = Aluno("Maria", 69123, "METI")
    val jObj = generateJson(aluno)
    val arrEx = listOf(1,2,3,jObj)
    val arrJson = generateJson(arrEx)
    arrJson.addElement("Subaru")
    val example0 = JsonData("modified", true)
    val example1 = JsonData("exists", null)
    val anotherObj = JsonObject()
    anotherObj.addElement(example0)
    anotherObj.addElement(example1)
    arrJson.addElement(anotherObj)

    val jt = JsonTreeVanilla()
    jt.open(arrJson)
}


class JsonTreeVanilla() {

    val shell: Shell
    val tree: Tree
    val text: Text
    val search: Text
    lateinit var e: JsonElement
    val display: Display = Display.getDefault()


    val allItems: MutableList<TreeItem> = mutableListOf()


    fun getSerializedText(): String{
        return text.text
    }

    fun refresh(){
        allItems.forEach {
            val type = it.getData("element")
            if(type is JsonData) {
                it.text = type.toString()
            }

            it.setData("name", (type as JsonElement).serialize())
            text.setText(tree.selection.first().getData("name").toString())

        }
    }

    fun getSelectedElement(): JsonElement {
        return tree.selection.first().getData("element") as JsonElement
    }

    init {

        shell = Shell(Display.getDefault())
        shell.text = "JsonTree"
        shell.layout = GridLayout(2, false)


        tree = Tree(shell, SWT.NONE)


        text = Text(shell, SWT.SINGLE)
        text.layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
        text.editable = false



        search = Text(shell, SWT.NONE)
        search.setTextLimit(100)
        search.layoutData = GridData(SWT.FILL, SWT.FILL, false, true)


        tree.addSelectionListener(object : SelectionAdapter(){
            override fun widgetSelected(e: SelectionEvent) {
                text.setText(tree.selection.first().getData("name").toString())
            }
        })

        search.addModifyListener(object: ModifyListener{
            override fun modifyText(p0: ModifyEvent) {
                allItems.forEach {
                    if(it.text.contains(search.text) && !search.text.isEmpty()){
                        it.background = Color(0,0,0)
                    }
                    else{
                        it.background = null
                    }
                }
            }
        })

    }



    fun open(e: JsonElement) {
        this.e = e
        e.accept(object : Visitor{
            var currentTreeItem: TreeItem? = null
            override fun visit(jo: JsonObject) {
                var root: TreeItem
                if(currentTreeItem == null){
                    root = TreeItem(tree, SWT.NONE)
                }
                else{
                    root = TreeItem(currentTreeItem, SWT.NONE)
                }

                currentTreeItem = root
                root.setData("name", jo.serialize())
                root.setData("element", jo)
                root.text = "object"


            }

            override fun visit(ja: JsonArray) {
                var root: TreeItem
                if(currentTreeItem == null)
                    root = TreeItem(tree, SWT.NONE)
                else
                    root = TreeItem(currentTreeItem, SWT.NONE)

                root.text = "arr"
                currentTreeItem = root
                root.setData("name", ja.serialize())
                root.setData("element", ja)

                ja.children.forEach {
                    if(!(it is JsonObject)){
                        val node = TreeItem(currentTreeItem, SWT.NONE)
                        node.setData("element", it)
                        node.setData("name", it.toString())
                        node.text = parseType(node.getData("element"))
                    }
                }
            }

            override fun visit(jd: JsonData) {
                val node = TreeItem(currentTreeItem, SWT.NONE)
                node.text = jd.name
                node.setData("element", jd)
                node.setData("name", jd.serialize())

            }

            override fun endVisit() {
                currentTreeItem = currentTreeItem?.parentItem
            }

        })






        allItems.add(tree.topItem)
        getAllItems(tree.topItem, allItems)



        tree.expandAll()
        shell.pack()
        shell.open()

        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }
}





