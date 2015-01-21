package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;

public class ItemDataAccess extends DataAccess {

    public ItemDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    public void addItem(Module module, Item item) {
        getDatabase().insert(ItemTable.TABLE_NAME, null, buildContentValues(module, item));
    }

    public void addOrUpdateItem(Module module, Item item) {
        if (updateItem(module, item) < 1) {
            addItem(module, item);
        }
    }

    public Item getItem(String id) {
        Cursor cursor = getDatabase().query(
                ItemTable.TABLE_NAME,
                new String[]{
                        ItemTable.COLUMN_ID,
                        ItemTable.COLUMN_POSITION,
                        ItemTable.COLUMN_TITLE,
                        ItemTable.COLUMN_TYPE,
                        ItemTable.COLUMN_AVAILABLE_FROM,
                        ItemTable.COLUMN_AVAILABLE_TO,
                        ItemTable.COLUMN_LOCKED,
                        ItemTable.COLUMN_VISITED,
                        ItemTable.COLUMN_COMPLETED,
                },
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();
        Item item = buildItem(cursor);

        cursor.close();

        return item;
    }

    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<Item>();

        String selectQuery = "SELECT * FROM " + ItemTable.TABLE_NAME;

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Item item = buildItem(cursor);
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return itemList;
    }

    public List<Item> getAllItemsForModule(Module module) {
        List<Item> itemList = new ArrayList<Item>();

        String selectQuery = "SELECT * FROM " + ItemTable.TABLE_NAME + " WHERE " + ItemTable.COLUMN_MODULE_ID + " = \'" + module.id + "\'";

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Item item = buildItem(cursor);
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return itemList;
    }

    private Item buildItem(Cursor cursor) {
        Item item = new Item();

        item.id = cursor.getString(0);
        item.position = cursor.getInt(1);
        item.title = cursor.getString(2);
        item.type = cursor.getString(3);
        item.available_from = cursor.getString(4);
        item.available_to = cursor.getString(5);
        item.locked = cursor.getInt(6) != 0;
        item.progress.visited = cursor.getInt(7) != 0;
        item.progress.completed = cursor.getInt(8) != 0;

        return item;
    }

    private ContentValues buildContentValues(Module module, Item item) {
        ContentValues values = new ContentValues();
        values.put(ItemTable.COLUMN_ID, item.id);
        values.put(ItemTable.COLUMN_POSITION, item.position);
        values.put(ItemTable.COLUMN_TITLE, item.title);
        values.put(ItemTable.COLUMN_TYPE, item.type);
        values.put(ItemTable.COLUMN_AVAILABLE_FROM, item.available_from);
        values.put(ItemTable.COLUMN_AVAILABLE_TO, item.available_to);
        values.put(ItemTable.COLUMN_LOCKED, item.locked);
        values.put(ItemTable.COLUMN_VISITED, item.progress.visited);
        values.put(ItemTable.COLUMN_COMPLETED, item.progress.completed);
        values.put(ItemTable.COLUMN_MODULE_ID, module.id);

        return values;
    }

    public int getItemsCount() {
        String countQuery = "SELECT * FROM " + ItemTable.TABLE_NAME;
        Cursor cursor = getDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public int updateItem(Module module, Item item) {
        int affected = getDatabase().update(
                ItemTable.TABLE_NAME,
                buildContentValues(module, item),
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(item.id)});

        return affected;
    }

    public void deleteItem(Item item) {
        getDatabase().delete(
                ItemTable.TABLE_NAME,
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(item.id)});
    }

}