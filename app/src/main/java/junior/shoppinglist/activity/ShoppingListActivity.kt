package junior.shoppinglist.activity

import android.app.ListActivity
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle

import junior.shoppinglist.R
import kotlinx.android.synthetic.main.activity_shopping_list.*
import org.jetbrains.anko.onClick
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import junior.shoppinglist.Data.ShoppingListDB
import junior.shoppinglist.Data.ShoppingListItemsAdded
import kotlinx.android.synthetic.main.the_shoppinglist.view.*
import org.jetbrains.anko.toast
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item.view.*


class ShoppingListActivity : ListActivity() {

    var shoppingListId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)
        shoppingListId = intent.getIntExtra("shoppingListId", -2)
        val shopping_List_Name = intent.getStringExtra("shoppingListName")
        shoppingListName.tag = shoppingListId
        shoppingListName.text = shopping_List_Name

        add_item.setOnClickListener {
            itemPopup(shoppingListId)
        }

        // Gesture
        val gd = GestureDetector(MyGestureDetector())
        getListView()?.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    /* Gets the items from the database and populates the list view with the adapter */
    fun updateList() {
        val items = ShoppingListDB().getShoppingListAddedItems(shoppingListName.tag as Int)
        /*val footer = layoutInflater.inflate(R.layout.add_item_button, listView, false)
        list.addFooterView(footer)

        footer.setOnClickListener {
            itemPopup(shoppingListId)
        }*/

        if (items != null) {
            listAdapter = ItemsArrayAdapt(items)
        } else {
            listAdapter = null
        }
    }

    /* Makes the add item popup window */
    fun itemPopup(listId: Int) {
        // retrieve display dimensions
        val displayRectangle = Rect()
        val window = this@ShoppingListActivity.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val itemPopup = LayoutInflater.from(this).inflate(R.layout.item, null, false)
        itemPopup.minimumWidth = (displayRectangle.width() * 0.8f).toInt()
        itemPopup.minimumHeight = 150
        itemPopup.add_item_search.requestFocus()

        val test = PopupWindow(itemPopup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        test.showAsDropDown(itemPopup, Gravity.TOP, 400, 0)

/*      // Cant make a popup (dropdown list of suggestions) in a popup
        val items = arrayListOf<String>("Mælk", "Smør", "Sukker", "Toiletpapir")
        //val items = ShoppingListDB().getItems(shoppingListId)
        val adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line, items)
        val autoComText = itemPopup.add_item_search as AutoCompleteTextView
        autoComText.setAdapter(adapter)
*/

        itemPopup.findViewById(R.id.close_new_item).onClick {
            test.dismiss()
        }

        val itemSearchEditText = itemPopup.findViewById(R.id.add_item_search) as EditText

        // On return key insert the item in the database
        itemSearchEditText.setOnEditorActionListener { _, actionId, event ->
           if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)
           {
               test.dismiss()
               val item = itemSearchEditText.text.toString().trim()
               if (!item.isNullOrBlank()) {
                   val quantityEditText = itemPopup.findViewById(R.id.add_item_quantity) as EditText
                   val quantity = quantityEditText.text.toString().trim().toDoubleOrNull()
                   ShoppingListDB().insertItemAdded(item, listId, quantity)
                   updateList()
                   toast("$item ${getString(R.string.added_to_list)}")
               }
           }
            false
        }
    }

    /* List view on item click
    * updates the item in the database as bought */
    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        ShoppingListDB().updateItemAddedBought(v!!.tag as Int, shoppingListId)
        updateList()
    }

    /* Array adapter used for the list view */
    inner class ItemsArrayAdapt(items: List<ShoppingListItemsAdded>): ArrayAdapter<ShoppingListItemsAdded>(this,
            R.layout.the_shoppinglist, R.id.item_name, items){

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            val item = getItem(position)
            view.tag = item.ItemAddedId
            val itemText =
                    if (item.quantity != null) {
                        val quantity = item.quantity
                        val q : Any
                        if (quantity % 1 == 0.0) {
                            q = quantity.toInt()
                        }else {
                            q = quantity
                        }
                            "$q ${item.itemName}" }
                    else item.itemName
            val itemView = view.item_name
            itemView.text = itemText
            // If item is bought it makes a line through the text
            if (item.bought == 1) {
                itemView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                itemView.paintFlags = 0
            }
            return view
        }
    }

    /* GestureDetector that register movement,swipe and touch on screen */
    inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
    private var mLastOnDownEvent: MotionEvent? = null
    private val swipeMaxOffPath = 100
    private val SwipeThresholdVelocity = 100
    private val SwipeMinDistance = 150

        override fun onDown(e: MotionEvent): Boolean {
            //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
            mLastOnDownEvent = e
            return super.onDown(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if ( e1 == null || e2 == null) return false
            val dX = e2.x - e1.x
            val dY = e1.y - e2.y
            val position = listView.pointToPosition(Math.round(e1.x), Math.round(e1.y))

            if (Math.abs(dY) < swipeMaxOffPath && Math.abs(velocityX) >= SwipeThresholdVelocity && Math.abs(dX) >= SwipeMinDistance) {
                if (dX > 0) {
                    //Swipe Right
                    showDeleteButton(position, 1)
                } else {
                    // Swipe Left
                    showDeleteButton(position, 2)
                }
                return true
            } else
                if (Math.abs(dX) < swipeMaxOffPath && Math.abs(velocityY) >= SwipeThresholdVelocity && Math.abs(dY) >= SwipeMinDistance) {
                    if (dY > 0) {
                        // Swipe UP
                    } else {
                        // Swipe DOWN
                    }
                    return true
                }
            return false
        }

        /*Shows / remove delete button on swipe.
        * deletes item if swiped right twice*/
        private fun showDeleteButton(position: Int, show: Int): Boolean {
            val child = list?.getChildAt(position)
            if (child != null) {
                val deleteButton = child.findViewById(R.id.delete_item_button) as ImageButton?
                if (deleteButton != null)
                    if (show == 1) {
                        if (deleteButton.visibility == View.VISIBLE)
                        {
                            val id: Int? = child.tag as? Int
                            if (id != null) {
                                ShoppingListDB().deleteItemAdded(id)
                                updateList()
                            }
                        } else {
                            deleteButton.visibility = View.VISIBLE
                        }
                    } else {
                        deleteButton.visibility = View.GONE
                    }
                return true
            }
            return false
        }
    }
}