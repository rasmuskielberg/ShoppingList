package junior.shoppinglist.Data

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*


class ShoppingListDB (val shoppingListHelper: ShoppingListHelper = ShoppingListHelper.instance): AnkoLogger {

    //Sets the format of the date and time
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val shoppingListTypes = shoppingListHelper.use {
        select(DB.ListTypeTable.tableName)
                .parseList(rowParser { id: Int, ltn: String, ca: String ->
                    ShoppingListType(id, ltn, ca)
                })
    }


    fun getShoppingLists(): List<ShoppingListComplete>?  {
        val empty = shoppingListHelper.use {
            select(DB.ShoppingListTable.tableName, DB.ShoppingListTable.id)
                    .limit(1)
                    .parseOpt(IntParser)
        }
        if(empty != null)
        {
            return shoppingListHelper.use {
                select(DB.ShoppingListTable.tableName + "," + DB.ListTypeTable.tableName,
                        "${DB.ShoppingListTable.tableName}.${DB.ShoppingListTable.id}",
                        DB.ShoppingListTable.listName,
                        DB.ListTypeTable.listTypeName,
                        DB.ShoppingListTable.updatedAt)
                        .where("${DB.ShoppingListTable.tableName}.${DB.ShoppingListTable.listTypeId} = " +
                                "${DB.ListTypeTable.tableName}.${ junior.shoppinglist.Data.DB.ListTypeTable.id}")
                        .orderBy(DB.ShoppingListTable.updatedAt, SqlOrderDirection.DESC)
                        .parseList(rowParser {
                            id: Int, ln: String, ltn: String, ua: String ->
                            ShoppingListComplete(id, ln, ltn, ua)
                        })
            }
        }
        return null
    }


  /*  fun getItems(shoppingListId: Int)
            = shoppingListHelper.use {
        val listType_Id = select(DB.ShoppingListTable.tableName, DB.ShoppingListTable.listTypeId).whereSimple("${DB.ShoppingListTable.id} = ?", shoppingListId.toString()).parseSingle(IntParser)
        select(DB.ItemsTable.tableName,
                DB.ItemsTable.itemName)
                .where("${DB.ItemsTable.itemTypeId} = $listType_Id")
                .parseList(rowParser(ina -> ))
    }*/


    fun getShoppingListAddedItems(shoppingListId: Int) : List<ShoppingListItemsAdded>? {
        val empty = shoppingListHelper.use {
            select(DB.ItemsAddedTable.tableName, DB.ItemsAddedTable.id)
                    .whereSimple("${DB.ItemsAddedTable.shoppingListId} = ?", shoppingListId.toString())
                    .limit(1)
                    .parseOpt(IntParser)
        }
        if (empty != null) {
            return shoppingListHelper.use {
                select(DB.ItemsAddedTable.tableName + "," + DB.ItemsTable.tableName + "," + DB.ItemTypesTable.tableName,
                        "${DB.ItemsAddedTable.tableName}.${DB.ItemsAddedTable.id}",
                        DB.ItemsAddedTable.shoppingListId,
                        DB.ItemsTable.itemName,
                        DB.ItemTypesTable.itemTypeName,
                        DB.ItemsAddedTable.itemQuantity,
                        DB.ItemsAddedTable.bought,
                        "${DB.ItemsAddedTable.tableName}.${DB.ItemsAddedTable.updatedAt}")
                        .whereSimple("${DB.ItemsAddedTable.tableName}.${DB.ItemsAddedTable.itemId} = " +
                                "${DB.ItemsTable.tableName}.${DB.ItemsTable.id} AND " +
                                "${DB.ItemsTable.tableName}.${DB.ItemsTable.itemTypeId} = " +
                                "${DB.ItemTypesTable.tableName}.${DB.ItemTypesTable.id} AND " +
                                "${DB.ItemsAddedTable.shoppingListId} = ?", shoppingListId.toString())
                        .orderBy(DB.ItemsAddedTable.bought)
                        .orderBy(DB.ItemsTable.itemTypeId)
                        .orderBy(DB.ItemsTable.itemName)
                        .parseList(rowParser { id: Int, lid: Int, itn: String, itTn: String, itq: Double?, bo: Int, ua: String ->
                            ShoppingListItemsAdded(id, lid, itn, itTn, itq, bo, ua)
                        })
            }
        }
        return null
    }


    fun insertShoppingListType(typeName: String) {
        val createdAt = simpleDateFormat.format(Date())
        shoppingListHelper.use {
            transaction {
                var maxId = select(DB.ListTypeTable.tableName, "coalesce(max(${DB.ListTypeTable.id}), 0)").parseOpt(IntParser)?: 1
                maxId = if (maxId >= 10000) maxId + 1 else 10000
                insert(DB.ListTypeTable.tableName,
                        DB.ListTypeTable.id to maxId,
                        DB.ListTypeTable.listTypeName to typeName,
                        DB.ListTypeTable.createdAt to createdAt
                )
            }
        }
    }


    fun insertShoppingList(listName: String, listTypeId: Int) {
        val createdAt = simpleDateFormat.format(Date())
        shoppingListHelper.use {
            transaction {
                var maxId = select(DB.ShoppingListTable.tableName, "coalesce(max(${DB.ShoppingListTable.id}), 0)").parseOpt(IntParser)?: 1
                maxId = if (maxId >= 10000) maxId + 1 else 10000
                insert(DB.ShoppingListTable.tableName,
                        DB.ShoppingListTable.id to maxId,
                        DB.ShoppingListTable.listName to listName,
                        DB.ShoppingListTable.listTypeId to listTypeId,
                        DB.ShoppingListTable.createdAt to createdAt,
                        DB.ShoppingListTable.updatedAt to createdAt
                )
            }
        }
    }


    fun insertItem(itemName: String, itemTypeId: Int): Int {
        val createdAt = simpleDateFormat.format(Date())
        var maxId = -1
        shoppingListHelper.use {
            transaction {
                maxId = select(DB.ItemsTable.tableName, "coalesce(max(${DB.ItemsTable.id}), 0)").parseOpt(IntParser)?: 1
                maxId = if (maxId >= 50000) maxId + 1 else 50000
                insert(DB.ItemsTable.tableName,
                        DB.ItemsTable.id to maxId,
                        DB.ItemsTable.itemName to itemName,
                        DB.ItemsTable.itemTypeId to itemTypeId,
                        DB.ItemsTable.createdAt to createdAt
                )
            }
        }
        return maxId
    }


    fun insertItemAdded(itemName: String, shoppingListId: Int, quantity: Double?) {
        val createdAt = simpleDateFormat.format(Date())
        shoppingListHelper.use {
            transaction {
                var itemId = select(DB.ItemsTable.tableName, DB.ItemsTable.id).whereSimple("${DB.ItemsTable.itemName} = ?", itemName).parseOpt(IntParser)
                if (itemId == null) {
                    val shoppingListTypeId = select(DB.ShoppingListTable.tableName, DB.ShoppingListTable.listTypeId).whereSimple("${DB.ShoppingListTable.id} = ?", shoppingListId.toString()).parseSingle(IntParser)
                    val itemTypeId = select(DB.ItemTypesTable.tableName, DB.ItemTypesTable.id).whereSimple("${DB.ItemTypesTable.listTypeId}  = ? AND ${DB.ItemTypesTable.itemTypeName} = ? LIMIT 1", shoppingListTypeId.toString(),"Diverse").parseSingle(IntParser)
                    itemId = insertItem(itemName, itemTypeId)
                }
                var maxId = select(DB.ItemsAddedTable.tableName, "coalesce(max(${DB.ItemsAddedTable.id}), 0)").parseOpt(IntParser)?: 1
                maxId = if (maxId >= 10000) maxId + 1 else 10000
                insert(DB.ItemsAddedTable.tableName,
                        DB.ItemsAddedTable.id to maxId,
                        DB.ItemsAddedTable.shoppingListId to shoppingListId,
                        DB.ItemsAddedTable.itemId to itemId,
                        DB.ItemsAddedTable.itemQuantity to quantity,
                        DB.ItemsAddedTable.createdAt to createdAt,
                        DB.ItemsAddedTable.updatedAt to createdAt
                )
            }
        }
        updateShoppingList(shoppingListId)
    }


    fun updateShoppingList(id: Int, listName: String? = null, listTypeId: Int? = null) {
        val updatedAt = simpleDateFormat.format(Date())

            shoppingListHelper.use {
                if (listName != null && listTypeId != null) {
                update(DB.ShoppingListTable.tableName,
                        DB.ShoppingListTable.listName to listName,
                        DB.ShoppingListTable.listTypeId to listTypeId,
                        DB.ShoppingListTable.updatedAt to updatedAt
                ).whereSimple("${DB.ShoppingListTable.id} = ?", id.toString())
                        .exec()
                } else {
                 update(DB.ShoppingListTable.tableName,
                        DB.ShoppingListTable.updatedAt to updatedAt)
                         .whereSimple("${DB.ShoppingListTable.id} = ?", id.toString())
                         .exec()
                }
            }
    }


    fun updateItemAddedBought(id: Int, shoppingListId: Int) {
        val updatedAt = simpleDateFormat.format(Date())
        shoppingListHelper.use {
            var bought = select(DB.ItemsAddedTable.tableName, DB.ItemsAddedTable.bought).whereSimple("${DB.ItemsAddedTable.id} = ?", id.toString()).parseSingle(IntParser)
            bought = if (bought == 0) 1 else 0
            update(DB.ItemsAddedTable.tableName,
                    DB.ItemsAddedTable.bought to bought,
                    DB.ItemsAddedTable.updatedAt to updatedAt
            ).whereSimple("${DB.ItemsAddedTable.id} = ?", id.toString())
                    .exec()
        }
        updateShoppingList(shoppingListId)
    }


    fun deleteShoppingList(id: Int) {
        shoppingListHelper.use {
            delete(DB.ShoppingListTable.tableName, "${DB.ShoppingListTable.id} = $id")
        }
    }


    fun deleteItemAdded(id: Int) {
        shoppingListHelper.use {
            delete(DB.ItemsAddedTable.tableName, "${DB.ItemsAddedTable.id} = $id")
        }
    }
}