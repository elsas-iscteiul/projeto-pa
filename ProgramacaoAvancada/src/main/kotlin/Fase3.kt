
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.*



fun main() {
    val aluno = Aluno("Maria", 69123, "METI")
    val jObj = generateJson(aluno)
    val arrEx = listOf(1,2,3,jObj)
    val arrJson = generateJson(arrEx)

    val jt = Injector.create(JsonTree::class)
    jt.open(arrJson)
}


class JsonTree {

    val shell: Shell
    val tree: Tree
    val text: Text
    val search: Text
    lateinit var e: Element

    @InjectAdd
    val actions = mutableListOf<Action>()

    val allItems: MutableList<TreeItem> = mutableListOf()

    init {

        shell = Shell(Display.getDefault())
        shell.setSize(800, 500)
        shell.text = "JsonTree"
        shell.layout = FillLayout()

        tree = Tree(shell, SWT.NONE)


        text = Text(shell, SWT.NONE)
        text.editable = false

        search = Text(shell, SWT.NONE)
        search.setTextLimit(50)


        tree.addSelectionListener(object : SelectionAdapter(){
            override fun widgetSelected(e: SelectionEvent) {
                text.setText(tree.selection.first().getData("name").toString())
            }
        })

        search.addModifyListener(object: ModifyListener{
            override fun modifyText(p0: ModifyEvent) {
                allItems.forEach {
                    if(search.text == it.text){
                        it.background = Color(0,0,0)
                    }
                    else{
                        it.background = null
                    }
                }
            }
        })

    }



    fun open(e: Element) {
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

                root.text = "object"
                currentTreeItem = root
                root.setData("name", jo.serialize())

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

                ja.children.forEach {
                    if(!(it is JsonObject)){
                        val node = TreeItem(currentTreeItem, SWT.NONE)
                        node.text = it.toString()
                        node.setData("name", it.toString())
                    }
                }
            }

            override fun visit(je: JsonElement) {
                val node = TreeItem(currentTreeItem, SWT.NONE)
                node.text = je.field
                node.setData("name", je.serialize())

            }

        })


        actions.forEach { action ->
            val button = Button(shell, SWT.PUSH)
            button.text = action.name
            button.addSelectionListener(object : SelectionAdapter(){
                override fun widgetSelected(e: SelectionEvent) {
                    action.execute(this@JsonTree)
                }
            })
        }

        allItems.add(tree.topItem)
        getAllItems(tree.topItem, allItems)
        tree.expandAll()
        shell.pack()
        shell.open()
        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }
}


// auxiliares para varrer a árvore

fun Tree.expandAll() = traverse { it.expanded = true }

fun Tree.traverse(visitor: (TreeItem) -> Unit) {
    fun TreeItem.traverse() {
        visitor(this)
        items.forEach {
            it.traverse()
        }
    }
    items.forEach { it.traverse() }
}

fun getAllItems(currentItem: TreeItem, allItems: MutableList<TreeItem>){
    currentItem.items.forEach {
        allItems.add(it)
        getAllItems(it, allItems)
    }

}