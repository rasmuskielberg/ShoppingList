package junior.shoppinglist.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import junior.shoppinglist.Data.*

import junior.shoppinglist.R
import junior.shoppinglist.Data.ShoppingListComplete
import junior.shoppinglist.app.AddListDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.shoppinglists.view.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), AddListDialog.Callback, AnkoLogger {

    lateinit var shListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar
        myToolbar.title = ""
        myToolbar.setLogo(R.drawable.shoppinglist_logo)
        setSupportActionBar(myToolbar)

        shListView = sh_listview

        // Gesture
        val gd = GestureDetector(MyGestureDetector())
        shListView.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    /* Gets the lists from the database and populates the list view with the adapter */
    fun updateList() {
        val items = ShoppingListDB().getShoppingLists()

        if (items != null) {
            val adapter = ShoppingListsArrayAdapt(items)
            shListView.adapter = adapter

            shListView.onItemClick { _, view, _, _ ->
                if (view != null && view.tag is Int) {
                    startActivity(intentFor<ShoppingListActivity>
                    ("shoppingListId" to view.tag, "shoppingListName" to view.shoppingListName.text))
                }
            }
        } else {
            shListView.adapter = null
        }
    }

    /* Creates the menu in the toolbar */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /* Handles the menu item selection */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                longToast("settings pressed")
                return true
            }
            R.id.add_list -> {
                AddListDialog().show(fragmentManager, "")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /* Implements the callback for AddListDialog*/
    override fun onAddList(listName: String, listTypeId: Int) {
        ShoppingListDB().insertShoppingList(listName, listTypeId)
        updateList()
    }

    /*Array adapter for shoppinglists
    * populates the list with a id tag, list name, list type and a last update date*/
    inner class ShoppingListsArrayAdapt(items: List<ShoppingListComplete>): ArrayAdapter<ShoppingListComplete>(this,
            R.layout.shoppinglists, R.id.shoppingListName, items){

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            val item = getItem(position)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",  Locale.getDefault())
            val date = simpleDateFormat.parse(item.updatedAt)
            val srcSimpleDateFormat = SimpleDateFormat("d MMM",  Locale.getDefault())
            val dateStr = srcSimpleDateFormat.format(date)

            view.tag = item.id
            view.shoppingListName.text = item.listName
            view.shoppingListType.text = item.listTypeName
            view.shoppingListUpdatedAt.text = dateStr
            return view
        }
    }

    /* GestureDetector that register movement,swipe and touch on screen */
    inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        private val swipeMaxOffPath = 100
        private val SwipeThresholdVelocity = 100
        private val SwipeMinDistance = 150

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if ( e1 == null || e2 == null) return false
            val dX = e2.x - e1.x
            val dY = e1.y - e2.y
            val position = shListView.pointToPosition(Math.round(e1.x), Math.round(e1.y))

            if (Math.abs(dY) < swipeMaxOffPath && Math.abs(velocityX) >= SwipeThresholdVelocity && Math.abs(dX) >= SwipeMinDistance) {
                if (dX > 0) {
                    //Swipe Right
                    showDeleteButton(position, 1)
                } else {
                    // Swipe Left
                    showDeleteButton(position, 2)
                }
                return true
            }
            return false
        }

        /*Shows / remove delete button on swipe.
        * deletes list if swiped right twice*/
        private fun showDeleteButton(position: Int, show: Int): Boolean {
            val child = shListView.getChildAt(position)
            if (child != null) {
                val deleteButton = child.findViewById(R.id.delete_item_button) as ImageButton?
                if (deleteButton != null)
                    if (show == 1) {
                        if (deleteButton.visibility == View.VISIBLE)
                        {
                            val id: Int? = child.tag as? Int
                            if (id != null) {
                                ShoppingListDB().deleteShoppingList(id)
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