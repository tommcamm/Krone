package com.sofato.krone.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migrations {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE recurring_expense ADD COLUMN dayOfMonth INTEGER DEFAULT NULL")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            val newCurrencies = listOf(
                "('GBP', 'British Pound', '£', 2, 'BEFORE', 0, 4)",
                "('SEK', 'Swedish Krona', 'kr', 2, 'AFTER', 0, 5)",
                "('NOK', 'Norwegian Krone', 'kr', 2, 'AFTER', 0, 6)",
                "('CHF', 'Swiss Franc', 'CHF', 2, 'BEFORE', 0, 7)",
                "('PLN', 'Polish Zloty', 'zł', 2, 'AFTER', 0, 8)",
                "('CZK', 'Czech Koruna', 'Kč', 2, 'AFTER', 0, 9)",
                "('ISK', 'Icelandic Króna', 'kr', 0, 'AFTER', 0, 10)",
                "('TRY', 'Turkish Lira', '₺', 2, 'BEFORE', 0, 11)",
                "('JPY', 'Japanese Yen', '¥', 0, 'BEFORE', 0, 12)",
                "('CAD', 'Canadian Dollar', 'CA\$', 2, 'BEFORE', 0, 13)",
                "('AUD', 'Australian Dollar', 'A\$', 2, 'BEFORE', 0, 14)",
                "('THB', 'Thai Baht', '฿', 2, 'BEFORE', 0, 15)",
            )
            for (values in newCurrencies) {
                connection.execSQL(
                    "INSERT OR IGNORE INTO currency (code, name, symbol, decimalPlaces, symbolPosition, isEnabled, sortOrder) VALUES $values"
                )
            }
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(connection: SQLiteConnection) {
            // Recreate budget_allocation to change FK onDelete to CASCADE and add month index.
            // SQLite doesn't support ALTER TABLE for foreign key changes.
            connection.execSQL(
                """CREATE TABLE IF NOT EXISTS budget_allocation_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    month TEXT NOT NULL,
                    allocatedAmountMinor INTEGER NOT NULL,
                    currencyCode TEXT NOT NULL,
                    FOREIGN KEY (categoryId) REFERENCES category(id) ON DELETE CASCADE
                )""".trimIndent()
            )
            connection.execSQL(
                "INSERT INTO budget_allocation_new (id, categoryId, month, allocatedAmountMinor, currencyCode) " +
                    "SELECT id, categoryId, month, allocatedAmountMinor, currencyCode FROM budget_allocation"
            )
            connection.execSQL("DROP TABLE budget_allocation")
            connection.execSQL("ALTER TABLE budget_allocation_new RENAME TO budget_allocation")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_budget_allocation_categoryId ON budget_allocation (categoryId)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_budget_allocation_month ON budget_allocation (month)")

            // Add composite index on recurring_expense(isActive, nextDate)
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_expense_isActive_nextDate ON recurring_expense (isActive, nextDate)")
            // Add unique index on monthly_snapshot.month
            connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_monthly_snapshot_month ON monthly_snapshot (month)")
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
