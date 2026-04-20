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

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(connection: SQLiteConnection) {
            // exchange_rate is ephemeral (refreshed every 24h). Pre-v5 rows have no business-date
            // for their quote, so rebuilding from scratch is cleaner than guessing a rateDate.
            // User-facing expenses already store homeAmountMinor + exchangeRateUsed so no
            // historical data is lost — only the rate cache is reset.
            connection.execSQL("DROP TABLE IF EXISTS exchange_rate")
            connection.execSQL(
                """CREATE TABLE IF NOT EXISTS exchange_rate (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    baseCode TEXT NOT NULL,
                    targetCode TEXT NOT NULL,
                    rate REAL NOT NULL,
                    rateDate TEXT NOT NULL,
                    fetchedAt INTEGER NOT NULL,
                    source TEXT NOT NULL
                )""".trimIndent()
            )
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_exchange_rate_baseCode_targetCode_rateDate " +
                    "ON exchange_rate (baseCode, targetCode, rateDate)"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(connection: SQLiteConnection) {
            // Groups Phase 0: add opt-in identity + server enrollment tables.
            // Additive — no existing tables touched.
            connection.execSQL(
                """CREATE TABLE IF NOT EXISTS device_identity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    sigPk BLOB NOT NULL,
                    sigSkEncIv BLOB NOT NULL,
                    sigSkEnc BLOB NOT NULL,
                    createdAt INTEGER NOT NULL
                )""".trimIndent()
            )
            connection.execSQL(
                """CREATE TABLE IF NOT EXISTS server_enrollment (
                    id INTEGER PRIMARY KEY NOT NULL,
                    url TEXT NOT NULL,
                    serverSigPk BLOB NOT NULL,
                    fingerprintWords TEXT NOT NULL,
                    fingerprintHex TEXT NOT NULL,
                    enrolledAt INTEGER NOT NULL
                )""".trimIndent()
            )
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
    )
}
