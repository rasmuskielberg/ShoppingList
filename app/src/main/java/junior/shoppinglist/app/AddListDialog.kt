package junior.shoppinglist.app

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import junior.shoppinglist.Data.ShoppingListDB
import junior.shoppinglist.Data.ShoppingListType
import junior.shoppinglist.R
import kotlinx.android.synthetic.main.list_type_spinner_item.view.*
import org.jetbrains.anko.ctx


class AddListDialog : DialogFragment() {

    // Interface for the callback ability
    interface Callback {
        fun onAddList(listName: String, listTypeId: Int)
    }

    lateinit private var callback: Callback

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback= context as Callback
    }

    /* Makes the dialog box with the items and the buttons */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val lf = LayoutInflater.from(activity).inflate(R.layout.dialog_add_list, null)
        val listName = lf.findViewById(R.id.listName) as EditText
        // Makes the spinner (drop down menu)
        val listTypeSpinner = lf.findViewById(R.id.spinnerListTypes) as Spinner
        listTypeSpinner.adapter = ListTypeAdapter(ShoppingListDB().shoppingListTypes)

        return builder.setTitle(R.string.add_list)
                .setView(lf)
                .setPositiveButton(R.string.add) { _, _ ->
                    if (!listName.text.toString().isNullOrBlank())
                        callback.onAddList(listName.text.toString().trim(), listTypeSpinner.shoppingListTypeName.tag as Int)
                }
                .setNegativeButton(R.string.button_close, null)
                .create()
    }

    /* Array adapter for the spinner */
    inner class ListTypeAdapter(listTypes: List<ShoppingListType>): ArrayAdapter<ShoppingListType>(ctx,
            R.layout.list_type_spinner_item, R.id.shoppingListTypeName, listTypes) {

        /* Sets the view for the closed spinner */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            val item = getItem(position)

            view.shoppingListTypeName.tag = item.id
            view.shoppingListTypeName.text = item.listTypeName
            return view
        }

        /* Sets the view for the open spinner */
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val dropDownView = super.getDropDownView(position, convertView, parent)
            val item = getItem(position)

            dropDownView.shoppingListTypeName.tag = item.id
            dropDownView.shoppingListTypeName.text = item.listTypeName
            return dropDownView
        }
    }
}
