package junior.shoppinglist.Data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import junior.shoppinglist.app.*
import kotlinx.android.synthetic.main.shoppinglists.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.*
import org.jetbrains.anko.info

class ShoppingListHelper(context: Context = App.instance) : ManagedSQLiteOpenHelper(
        context,
        DB_NAME,
        null,
        DB_VERSION), AnkoLogger {

    companion object {
        val DB_VERSION = 12
        val DB_NAME = "ShoppingList"
        val instance by lazy { ShoppingListHelper() }
    }

    /* Creating Tables */
    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(DB.ListTypeTable.tableName, true,
                DB.ListTypeTable.id to INTEGER + PRIMARY_KEY,
                DB.ListTypeTable.listTypeName to TEXT + NOT_NULL + UNIQUE,
                DB.ListTypeTable.createdAt to TEXT + NOT_NULL)

        db.createTable(DB.ShoppingListTable.tableName, true,
                DB.ShoppingListTable.id to INTEGER + PRIMARY_KEY,
                DB.ShoppingListTable.listName to TEXT + NOT_NULL,
                DB.ShoppingListTable.listTypeId to INTEGER + NOT_NULL,
                DB.ShoppingListTable.createdAt to TEXT + NOT_NULL,
                DB.ShoppingListTable.updatedAt to TEXT,
                "" to FOREIGN_KEY(DB.ShoppingListTable.listTypeId, DB.ListTypeTable.tableName, DB.ListTypeTable.id))

        db.createTable(DB.ItemTypesTable.tableName, true,
                DB.ItemTypesTable.id to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                DB.ItemTypesTable.itemTypeName to TEXT + NOT_NULL,
                DB.ItemTypesTable.listTypeId to INTEGER,
                DB.ItemTypesTable.createdAt to TEXT + NOT_NULL,
                "" to FOREIGN_KEY(DB.ItemTypesTable.listTypeId, DB.ListTypeTable.tableName, DB.ListTypeTable.id))

        db.createTable(DB.ItemsTable.tableName, true,
                DB.ItemsTable.id to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                DB.ItemsTable.itemName to TEXT + NOT_NULL,
                DB.ItemsTable.itemTypeId to INTEGER + NOT_NULL,
                DB.ItemsTable.createdAt to TEXT + NOT_NULL,
                "" to FOREIGN_KEY(DB.ItemsTable.itemTypeId, DB.ItemTypesTable.tableName, DB.ItemTypesTable.id))

        db.createTable(DB.ItemsAddedTable.tableName, true,
                DB.ItemsAddedTable.id to INTEGER + PRIMARY_KEY,
                DB.ItemsAddedTable.shoppingListId to INTEGER + NOT_NULL,
                DB.ItemsAddedTable.itemId to INTEGER + NOT_NULL,
                DB.ItemsAddedTable.itemQuantity to REAL,
                DB.ItemsAddedTable.bought to INTEGER + DEFAULT ("0"),
                DB.ItemsAddedTable.createdAt to TEXT + NOT_NULL,
                DB.ItemsAddedTable.updatedAt to TEXT,
                "" to FOREIGN_KEY(DB.ItemsAddedTable.shoppingListId, DB.ShoppingListTable.tableName, DB.ShoppingListTable.id),
                "" to FOREIGN_KEY(DB.ItemsAddedTable.itemId, DB.ItemsTable.tableName, DB.ItemsTable.id))

        initData(db)
        info("Database tables created")
    }

    /* Upgrading database */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DB.ItemsAddedTable.tableName)
        db.execSQL("DROP TABLE IF EXISTS " + DB.ItemsTable.tableName)
        db.execSQL("DROP TABLE IF EXISTS " + DB.ItemTypesTable.tableName)
        db.execSQL("DROP TABLE IF EXISTS " + DB.ShoppingListTable.tableName)
        db.execSQL("DROP TABLE IF EXISTS " + DB.ListTypeTable.tableName)
        // Create tables again
        onCreate(db)
    }

    /* Creating initial data in database */
    private fun initData(db: SQLiteDatabase) {
        val shoppingListTypes: MutableList<ShoppingListType> = arrayListOf()

        shoppingListTypes.add(ShoppingListType(1, "Dagligvareliste", "2000-01-01 00:00:00"))
        shoppingListTypes.add(ShoppingListType(2, "Byggemarkedsliste", "2000-01-01 00:00:00"))
        shoppingListTypes.add(ShoppingListType(3, "Div liste", "2000-01-01 00:00:00"))
        shoppingListTypes.add(ShoppingListType(4, "Alt", "2000-01-01 00:00:00"))

        shoppingListTypes.forEach {(id, listTypeName, createdAt) -> db.insert(DB.ListTypeTable.tableName,
                DB.ListTypeTable.id to id,
                DB.ListTypeTable.listTypeName to listTypeName,
                DB.ListTypeTable.createdAt to createdAt)
        }

        val shoppingLists: MutableList<ShoppingList> = arrayListOf()

        shoppingLists.add(ShoppingList(1, "Mandag", 1, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        shoppingLists.add(ShoppingList(2, "Salat", 1, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))

        shoppingLists.forEach { (id, listName, listTypeId, createdAt, updatedAt) -> db.insert(DB.ShoppingListTable.tableName,
                DB.ShoppingListTable.id to id,
                DB.ShoppingListTable.listName to listName,
                DB.ShoppingListTable.listTypeId to listTypeId,
                DB.ShoppingListTable.createdAt to createdAt,
                DB.ShoppingListTable.updatedAt to updatedAt)
        }

        val itemTypes: MutableList<ItemType> = arrayListOf()

        itemTypes.add(ItemType(1, "Mejeri", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(2, "Kolonial", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(3, "Husholdning", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(4, "Frost", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(5, "Kød", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(6, "Frugt & grønt", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(7, "Kølevare", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(8, "Diverse", 1, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(9, "Diverse", 2, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(10, "Diverse", 3, "2000-01-01 00:00:00"))
        itemTypes.add(ItemType(11, "Diverse", 4, "2000-01-01 00:00:00"))

        itemTypes.forEach { (id, itemTypeName, listTypeId, createdAt) -> db.insert(DB.ItemTypesTable.tableName,
                DB.ItemTypesTable.id to id,
                DB.ItemTypesTable.itemTypeName to itemTypeName,
                DB.ItemTypesTable.listTypeId to listTypeId,
                DB.ItemTypesTable.createdAt to createdAt)
        }

        val items: MutableList<Item> = arrayListOf()

        items.add(Item(1, "Mælk", 1, "2000-01-01 00:00:00"))
        items.add(Item(2, "Smør", 1, "2000-01-01 00:00:00"))
        items.add(Item(3, "Ost", 1, "2000-01-01 00:00:00"))
        items.add(Item(4, "Hakket Oksekød", 5, "2000-01-01 00:00:00"))
        items.add(Item(5, "Hakket Svinekød", 5, "2000-01-01 00:00:00"))
        items.add(Item(6, "OkseMørbrad", 5, "2000-01-01 00:00:00"))
        items.add(Item(7, "Nakkeksteg", 5, "2000-01-01 00:00:00"))
        items.add(Item(8, "Nakkekoteletter", 5, "2000-01-01 00:00:00"))
        items.add(Item(9, "Majs på dåse", 2, "2000-01-01 00:00:00"))
        items.add(Item(10, "Hakket Tomater", 2, "2000-01-01 00:00:00"))
        items.add(Item(11, "Flåede Tomater", 2, "2000-01-01 00:00:00"))
        items.add(Item(12, "Toiletpapir", 3, "2000-01-01 00:00:00"))
        items.add(Item(13, "Køkkenrulle", 3, "2000-01-01 00:00:00"))
        items.add(Item(14, "Opvaskemiddel", 3, "2000-01-01 00:00:00"))
        items.add(Item(15, "Opvasketabs", 3, "2000-01-01 00:00:00"))
        items.add(Item(16, "Is", 4, "2000-01-01 00:00:00"))
        items.add(Item(17, "Frosne jordbær", 4, "2000-01-01 00:00:00"))
        items.add(Item(18, "Frosne grøntsager", 4, "2000-01-01 00:00:00"))
        items.add(Item(19, "Frosne fiskepinde", 4, "2000-01-01 00:00:00"))
        items.add(Item(20, "Æbler", 6, "2000-01-01 00:00:00"))
        items.add(Item(21, "Pærer", 6, "2000-01-01 00:00:00"))
        items.add(Item(22, "Bananer", 6, "2000-01-01 00:00:00"))
        items.add(Item(23, "Tomater", 6, "2000-01-01 00:00:00"))
        items.add(Item(24, "Agurk", 6, "2000-01-01 00:00:00"))
        items.add(Item(25, "Brocoli", 6, "2000-01-01 00:00:00"))
        items.add(Item(26, "Salat", 6, "2000-01-01 00:00:00"))
        items.add(Item(27, "Peberfrugt", 6, "2000-01-01 00:00:00"))
        items.add(Item(28, "Frisk pasta", 7, "2000-01-01 00:00:00"))
        items.add(Item(29, "Pasta", 2, "2000-01-01 00:00:00"))
        items.add(Item(30, "Leverpostej", 7, "2000-01-01 00:00:00"))
        items.add(Item(31, "Skinkesalat", 7, "2000-01-01 00:00:00"))
        items.add(Item(32, "Tærtedej", 7, "2000-01-01 00:00:00"))


        items.forEach { (id, itemName, itemTypeId, createdAt) -> db.insert(DB.ItemsTable.tableName,
                DB.ItemsTable.id to id,
                DB.ItemsTable.itemName to itemName,
                DB.ItemsTable.itemTypeId to itemTypeId,
                DB.ItemsTable.createdAt to createdAt
        )}

        val itemsAdded: MutableList<ItemAdded> = arrayListOf()

        itemsAdded.add(ItemAdded(1, 1, 1, 2.00, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(2, 1, 2, 1.00, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(3, 1, 22, null, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(4, 1, 30, null, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(5, 2, 26, null, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(6, 2, 24, null, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(7, 2, 23, 10.00, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(8, 2, 25, null, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))
        itemsAdded.add(ItemAdded(9, 2, 27, 2.00, 0, "2000-01-01 00:00:00", "2000-01-01 00:00:00"))

        itemsAdded.forEach { (id, shoppingListId, itemId, quantity, bought, createdAt, updatedAt) -> db.insert(DB.ItemsAddedTable.tableName,
                DB.ItemsAddedTable.id to id,
                DB.ItemsAddedTable.shoppingListId to shoppingListId,
                DB.ItemsAddedTable.itemId to itemId,
                DB.ItemsAddedTable.itemQuantity to quantity,
                DB.ItemsAddedTable.bought to bought,
                DB.ItemsAddedTable.createdAt to createdAt,
                DB.ItemsAddedTable.updatedAt to updatedAt
                )}

        info("Data inserted")
    }


}