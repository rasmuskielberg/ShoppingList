package junior.shoppinglist.Data

data class ShoppingListType(val id: Int, val listTypeName: String, val createdAt: String)

data class ShoppingList (val id: Int, val listName: String, val listTypeId: Int, val createdAt: String, val updatedAt: String)

data class ShoppingListComplete (val id: Int, val listName: String, val listTypeName: String, val updatedAt: String)

data class ItemType (val id: Int, val itemTypeName: String, val listTypeId: Int, val createdAt: String)

data class Item (val id: Int, val itemName: String, val itemTypeId: Int, val createdAt: String)

data class ItemNames(val itemName: String)

data class ItemAdded (val id: Int, val shoppingListId: Int, val itemId: Int, val quantity: Double?, val bought: Int, val createdAt: String, val updatedAt: String)

data class ShoppingListItemsAdded(val ItemAddedId: Int, val shoppingListId: Int, val itemName: String, val itemTypeName: String, val quantity: Double?, val bought: Int, val updatedAt: String)