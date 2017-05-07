package junior.shoppinglist.Data
object DB {
    object ListTypeTable {
        val tableName = "ShoppingListType"
        val id = "_id"
        val listTypeName = "ListTypeName"
        val createdAt = "CreatedAt"
    }

    object ShoppingListTable {
        val tableName = "ShoppingLists"
        val id = "_id"
        val listName = "ListName"
        val listTypeId = "ListTypeID"
        val createdAt = "CreatedAt"
        val updatedAt = "UpdatedAt"
    }

    object ItemTypesTable {
        val tableName = "ItemTypes"
        val id = "_id"
        val itemTypeName = "ItemTypeName"
        val listTypeId = "ListTypeId"
        val createdAt = "CreatedAt"
    }

    object ItemsTable {
        val tableName = "Items"
        val id = "_id"
        val itemName = "ItemName"
        val itemTypeId = "ItemTypeID"
        val createdAt = "CreatedAt"
    }

    object ItemsAddedTable {
        val tableName = "ItemsAdded"
        val id = "_id"
        val shoppingListId = "ShoppingListID"
        val itemId = "ItemID"
        val itemQuantity = "ItemQuantity"
        val bought = "Bought"
        val createdAt = "CreatedAt"
        val updatedAt = "UpdatedAt"
    }
}