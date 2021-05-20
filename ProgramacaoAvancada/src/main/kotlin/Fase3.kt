
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.*



fun main() {
    val aluno = Aluno("Maria", 69123, "METI")
    val jObj = generateJson(aluno)
    val arrEx = listOf(1,2,3,jObj)
    val arrJson = generateJson(arrEx)
    arrJson.addElement("Subaru")
    val example = JsonData("name", "Magali")
    val anotherObj = JsonObject()
    anotherObj.addElement(example)
    arrJson.addElement(anotherObj)


    val jt = Injector.create(JsonTree::class)
    jt.open(arrJson)
}


class JsonTree() {

    val shell: Shell
    val tree: Tree
    val text: Text
    val search: Text
    lateinit var e: JsonElement
    val display: Display = Display.getDefault()

    @Inject
    lateinit var modifier : Modifier

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
                root.text = "object"
                root.setImage(Image(display, modifier.getImgLoc(jo)))

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
                root.setImage(Image(display, modifier.getImgLoc(ja)))

                ja.children.forEach {
                    if(!(it is JsonObject) && modifier.toShow(it)){
                        val node = TreeItem(currentTreeItem, SWT.NONE)
                        node.text = parseType(it)
                        node.setData("name", it.toString())
                    }
                }
            }

            override fun visit(jd: JsonData) {
                val node = TreeItem(currentTreeItem, SWT.NONE)
                //node.text = jd.name
                node.text = modifier.setText(jd)
                node.setData("name", jd.serialize())
                node.setImage(Image(display, modifier.getImgLoc(jd)))

            }

            override fun endVisit() {
                currentTreeItem = currentTreeItem?.parentItem
            }

        })


        actions.forEach { action ->
            val button = Button(shell, SWT.PUSH)
            button.text = action.name
            button.addSelectionListener(object : SelectionAdapter(){
                override fun widgetSelected(e: SelectionEvent) {
                    action.execute(text.text)
                }
            })
        }



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