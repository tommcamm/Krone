package com.sofato.krone.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.sofato.krone.domain.model.SymbolPosition

class KroneDatabaseCallback : RoomDatabase.Callback() {

    /** Escape single quotes for SQL string literals. */
    private fun String.esc(): String = replace("'", "''")

    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)
        seedCurrencies(connection)
        seedCategories(connection)
    }

    private fun seedCurrencies(connection: SQLiteConnection) {
        data class CurrencySeed(
            val code: String,
            val name: String,
            val symbol: String,
            val decimalPlaces: Int,
            val symbolPosition: SymbolPosition,
            val isEnabled: Boolean,
            val sortOrder: Int,
        )

        val currencies = listOf(
            CurrencySeed("DKK", "Danish Krone", "kr", 2, SymbolPosition.AFTER, true, 0),
            CurrencySeed("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 1),
            CurrencySeed("USD", "US Dollar", "$", 2, SymbolPosition.BEFORE, true, 2),
            CurrencySeed("NZD", "New Zealand Dollar", "NZ$", 2, SymbolPosition.BEFORE, false, 3),
            CurrencySeed("GBP", "British Pound", "£", 2, SymbolPosition.BEFORE, false, 4),
            CurrencySeed("SEK", "Swedish Krona", "kr", 2, SymbolPosition.AFTER, false, 5),
            CurrencySeed("NOK", "Norwegian Krone", "kr", 2, SymbolPosition.AFTER, false, 6),
            CurrencySeed("CHF", "Swiss Franc", "CHF", 2, SymbolPosition.BEFORE, false, 7),
            CurrencySeed("PLN", "Polish Zloty", "zł", 2, SymbolPosition.AFTER, false, 8),
            CurrencySeed("CZK", "Czech Koruna", "Kč", 2, SymbolPosition.AFTER, false, 9),
            CurrencySeed("ISK", "Icelandic Króna", "kr", 0, SymbolPosition.AFTER, false, 10),
            CurrencySeed("TRY", "Turkish Lira", "₺", 2, SymbolPosition.BEFORE, false, 11),
            CurrencySeed("JPY", "Japanese Yen", "¥", 0, SymbolPosition.BEFORE, false, 12),
            CurrencySeed("CAD", "Canadian Dollar", "CA$", 2, SymbolPosition.BEFORE, false, 13),
            CurrencySeed("AUD", "Australian Dollar", "A$", 2, SymbolPosition.BEFORE, false, 14),
            CurrencySeed("THB", "Thai Baht", "฿", 2, SymbolPosition.BEFORE, false, 15),
        )

        for (c in currencies) {
            connection.execSQL(
                "INSERT INTO currency (code, name, symbol, decimalPlaces, symbolPosition, isEnabled, sortOrder) " +
                    "VALUES ('${c.code.esc()}', '${c.name.esc()}', '${c.symbol.esc()}', ${c.decimalPlaces}, '${c.symbolPosition.name}', ${if (c.isEnabled) 1 else 0}, ${c.sortOrder})"
            )
        }
    }

    private fun seedCategories(connection: SQLiteConnection) {
        data class CategorySeed(
            val name: String,
            val iconName: String,
            val colorHex: String,
            val sortOrder: Int,
        )

        val categories = listOf(
            CategorySeed("Groceries", "ShoppingCart", "#FF4CAF50", 0),
            CategorySeed("Eating out", "Restaurant", "#FFFF5722", 1),
            CategorySeed("Coffee & drinks", "LocalCafe", "#FF795548", 2),
            CategorySeed("Transport", "DirectionsBus", "#FF2196F3", 3),
            CategorySeed("Shopping", "ShoppingBag", "#FFE91E63", 4),
            CategorySeed("Entertainment", "TheaterComedy", "#FF9C27B0", 5),
            CategorySeed("Health", "LocalPharmacy", "#FFEF5350", 6),
            CategorySeed("Gifts", "CardGiftcard", "#FFFF9800", 7),
            CategorySeed("Household", "Home", "#FF607D8B", 8),
            CategorySeed("Other", "MoreHoriz", "#FF9E9E9E", 9),
        )

        for (c in categories) {
            connection.execSQL(
                "INSERT INTO category (name, iconName, colorHex, isCustom, sortOrder, isArchived) " +
                    "VALUES ('${c.name.esc()}', '${c.iconName.esc()}', '${c.colorHex.esc()}', 0, ${c.sortOrder}, 0)"
            )
        }
    }
}
