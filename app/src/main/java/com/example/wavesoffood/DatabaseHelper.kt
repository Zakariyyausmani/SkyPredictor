import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "waves_of_food.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_FAVORITES = "favorites"
        const val TABLE_WEATHER = "weather"

        // Favorites table columns
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_COUNTRY = "country"

        // Weather table columns
        const val COLUMN_CITY_ID = "city_id"
        const val COLUMN_TEMP = "temp"
        const val COLUMN_STATUS = "status"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the favorites table
        val createFavoritesTable = ("CREATE TABLE $TABLE_FAVORITES ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_COUNTRY TEXT)")

        // Create the weather table
        val createWeatherTable = ("CREATE TABLE $TABLE_WEATHER ("
                + "$COLUMN_CITY_ID INTEGER, "
                + "$COLUMN_TEMP TEXT, "
                + "$COLUMN_STATUS TEXT, "
                + "$COLUMN_UPDATED_AT TEXT, "
                + "FOREIGN KEY($COLUMN_CITY_ID) REFERENCES $TABLE_FAVORITES($COLUMN_ID) ON DELETE CASCADE)")

        db.execSQL(createFavoritesTable)
        db.execSQL(createWeatherTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WEATHER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }
}
