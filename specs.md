# Krone — Personal Budget Tracker for Android

> Working title. Danish currency reference, short, memorable.

---

## 1. Vision & Differentiators

The budgeting app space is crowded with apps that are either paywalled, ugly, over-engineered, or abandoned. Krone differentiates by being:

- **Completely offline-first** — no account, no server, no signup. Works on day one.
- **Open source (GPLv3 or MPL-2.0)** — hackable, auditable, trustworthy.
- **Denmark-aware** — understands Danish expense patterns (MobilePay-style categories, A-kasse, SU, betalingsservice, typical Danish rent/utility structures).
- **Multi-currency native** — DKK as home currency, with first-class EUR, USD, NZD support and an open interface for adding any currency. Daily exchange rates from a free API, cached locally.
- **Actually polished** — Material Design 3 with dynamic color (Material You), fluid animations, proper typography, edge-to-edge design, predictive back gesture support.
- **Savings-aware** — doesn't treat "money left over" as the goal. Actively tracks savings targets, investment contributions (ASK, pension, brokerage), and shows them as first-class citizens alongside expenses.
- **On-device smart** — optional ML-powered insights (category prediction, anomaly detection, trend forecasting) that run entirely on-device. No data leaves the phone. Ever.
- **Opinionated about budgeting** — not a blank spreadsheet. It guides users toward a working budget with sensible defaults.

### What "not yet another budgeting app" means concretely

| Most apps do this poorly           | Krone does this instead                                                                 |
|------------------------------------|-----------------------------------------------------------------------------------------|
| Dump you on a blank screen         | Guided onboarding: income → fixed costs → savings goals → spending money. 4 steps.      |
| Require manual entry for everything | Smart recurring: set it once, it auto-posts monthly. Edit only when it changes.         |
| Show a pie chart and call it a day | Daily remaining budget ("you can spend X kr today"), trend lines, streak tracking.       |
| Hide features behind a paywall     | Everything is free. Forever. It's open source.                                          |
| Ignore locale                      | DKK default, multi-currency with live rates, Danish expense categories.                 |
| No export path                     | CSV and JSON export. Your data is yours.                                                |
| Treat savings as an afterthought   | Savings & investments are a core budget category with dedicated tracking and graphs.     |
| Single currency only               | Log expenses in any currency; everything converts to your home currency automatically.   |
| Cloud-dependent "intelligence"     | On-device ML for category suggestions, spending anomalies, and forecasts. Fully offline. |

---

## 2. Target User

- Lives in Denmark (or any DKK-economy context), but may travel or have expenses in EUR/USD/NZD.
- Has a regular income (salary, SU, freelance).
- Wants to understand where money goes each month.
- May be actively saving — ASK (Aktiesparekonto), pension top-ups, emergency fund, or investment accounts.
- Not a finance nerd — wants clarity, not spreadsheets.
- Ranges from university students on SU to working professionals.
- Non-technical. Will not read a manual or watch a tutorial.

---

## 3. Core Features (MVP — v1.0)

### 3.1 Onboarding Flow

A guided setup that builds the user's first budget in under 2 minutes:

1. **Home currency** — defaults to DKK. User can pick any supported currency. Additional currencies can be enabled for expense logging.
2. **Monthly net income** — single number input in home currency. Support for multiple income sources (job + SU, dual income household). If an income source is in a foreign currency, the user enters it in that currency and the app converts at the current rate.
3. **Fixed monthly expenses** — pre-populated category suggestions relevant to Denmark:
    - Rent (husleje)
    - Electricity (el)
    - Heating (varme) — separated because Danish utilities often bill these independently
    - Water (vand)
    - Internet & mobile
    - Insurance (forsikring)
    - Transport (rejsekort/monthly pass/car costs)
    - A-kasse / union fees
    - Subscriptions (streaming, gym, etc.)
    - Loan repayments
4. **Savings & investment goals** — encouraged but skippable:
    - Emergency fund target
    - ASK (Aktiesparekonto) monthly contribution
    - Investment account / pension top-up
    - Custom savings goal (holiday, new computer, etc.)
5. **Result screen** — shows: total fixed costs, total savings allocated, remaining "free" money, suggested daily budget (remaining ÷ days in month).

Users can skip and adjust later. No dead ends.

### 3.2 Monthly Budget View

- **Budget overview card** — income vs. fixed costs vs. savings vs. discretionary. Three-segment stacked bar.
- **Category breakdown** — each category shows: budgeted amount, spent so far, remaining, percentage used. Visual progress bars.
- **Savings progress** — distinct visual treatment (green/teal ring or progress arc) showing how savings targets are tracking this month.
- **Monthly calendar heatmap** — shows spending intensity per day (light to dark). Quickly spot heavy-spending days.
- **Recurring expenses auto-post** — fixed expenses and savings contributions appear automatically on their set date each month. User only intervenes if something changes.
- **Currency breakdown chip** — if multi-currency expenses exist this month, a small expandable chip shows the split (e.g., "DKK 12,400 · EUR 230 · USD 45").

### 3.3 Daily Budget View (the killer feature)

This is the primary screen most users will interact with daily:

- **"You can spend X kr today"** — large, prominent number in home currency. Calculated as: (monthly free budget − spent so far this month − remaining savings contributions) ÷ remaining days in month.
- **Quick-add expense** — amount + optional category + optional currency + optional note. Minimal friction. One-handed use. Currency defaults to home currency; a small toggle switches to recently-used foreign currencies.
- **Today's transactions list** — what was spent today, editable. Foreign-currency expenses show both original and converted amounts.
- **Rolling daily average** — "you've been spending avg Y kr/day this month" with trend arrow (up/down vs. last month).
- **Streak counter** — days in a row under daily budget. Gentle gamification, not aggressive.
- **ML suggestion chip** (optional, opt-in) — after entering an amount, the app suggests a category based on patterns. Single tap to accept, swipe to dismiss.

### 3.4 Expense Entry

- **Quick add** — amount field with large numpad, category picker (grid of icons), optional note. Submit in 3 taps maximum.
- **Currency selector** — small flag/code chip next to the amount field. Defaults to home currency. Tap to switch. Recently-used currencies appear first. Conversion rate shown inline ("≈ 1,234 kr").
- **Category system** — predefined set with icons, user can add custom categories. Suggested defaults:
    - Groceries (dagligvarer)
    - Eating out
    - Coffee & drinks
    - Transport
    - Shopping / clothing
    - Entertainment
    - Health / pharmacy
    - Gifts
    - Household
    - Other
- **ML auto-categorization** (opt-in) — after the user has ~50 expenses, the on-device model starts suggesting categories when an amount is entered. The suggestion appears as a highlighted chip; user confirms or overrides. Every override trains the model further.
- **Recurring expense management** — add/edit/pause/delete. Shows next occurrence date. Supports foreign-currency recurring expenses (converted at the rate on the posting date).
- **Split expense** — optional, for shared costs (useful for couples/roommates).

### 3.5 Savings & Investment Tracking

This is a first-class feature, not buried in settings.

#### 3.5.1 Savings Buckets

Each bucket represents a savings or investment goal:

- **Label** — user-defined (e.g., "Emergency fund", "ASK", "Nordnet monthly", "Holiday 2026").
- **Type** — one of: Emergency fund, Investment (ASK, pension, brokerage), Goal-based (target amount + optional deadline), Recurring transfer (open-ended monthly amount).
- **Monthly contribution** — how much the user commits to this bucket per month. Auto-posts like a recurring expense.
- **Target amount** (optional) — for goal-based buckets. The app calculates projected completion date.
- **Current balance** (manual entry) — the user can periodically update the actual balance in the account. This is not auto-synced (no bank API in v1) but enables tracking growth over time.
- **Currency** — savings buckets can be in any supported currency (e.g., an NZD savings account, a USD brokerage).

#### 3.5.2 Savings in the Budget

Savings contributions are treated as **non-negotiable allocations**, similar to rent — they are subtracted from income before calculating discretionary spending. This is the "pay yourself first" model.

The daily budget formula becomes:

```
daily_budget = (income − fixed_costs − total_savings_contributions − spent_so_far) ÷ remaining_days
```

If a user skips a savings contribution one month, they can mark it as "skipped" (not deleted) so the app doesn't penalize their daily budget but still tracks the gap.

#### 3.5.3 Savings Visualizations

- **Savings dashboard tab** — dedicated screen showing all buckets:
    - Progress bar per bucket (contributed vs. target).
    - Net worth equivalent view: sum of all bucket balances in home currency.
    - Monthly contribution totals: stacked bar showing how much goes where.
- **Goal countdown** — for goal-based buckets: "At current rate, you'll reach your target in 7 months."
- **Historical savings chart** — line chart showing total savings balance over time (updated when user enters balance snapshots).
- **Savings rate metric** — percentage of income going to savings this month, with trend vs. previous months. Displayed prominently: "You're saving 22% of your income this month."
- **Investment growth (simple)** — if the user enters balance updates for investment buckets (ASK, brokerage), the app can show actual vs. contributed amounts, giving a rough picture of returns. This is not portfolio tracking — it's "I put in 50,000 kr and it's now worth 54,200 kr."

### 3.6 Graphs & Insights

- **Monthly spending by category** — donut chart (not pie — donut with the remaining budget in the center).
- **Spending trend line** — last 6–12 months, total spending over time.
- **Category comparison** — bar chart comparing this month vs. last month per category.
- **Daily cumulative spend** — line chart showing spend accumulation through the month vs. the "ideal" linear budget line. Shows if you're ahead or behind.
- **Savings vs. spending ratio** — area chart showing the proportion of income that went to savings vs. spending over the last 12 months. Goal: the savings area should grow.
- **Multi-currency breakdown** — if the user has foreign-currency expenses, a small chart shows spending by currency with converted totals.
- **Top 3 insights** — auto-generated plain-language summaries: "You spent 34% more on eating out this month compared to last month", "Your grocery spending has been stable for 3 months", "You've saved 18% more than last quarter."
- **ML-powered insights** (opt-in, see section 8):
    - **Anomaly detection** — "Unusual: you spent 890 kr on Transport today, your average is 45 kr."
    - **Trend forecasting** — "At your current pace, you'll overshoot your grocery budget by ~600 kr this month."
    - **Seasonal patterns** — "Based on your history, electricity costs typically rise by 30% in November."

### 3.7 Multi-Currency System

#### 3.7.1 Supported Currencies (v1)

Launch with first-class support for:

- **DKK** (Danish Krone) — default home currency
- **EUR** (Euro) — critical for Denmark (pegged, used for EU purchases)
- **USD** (US Dollar)
- **NZD** (New Zealand Dollar)

The currency system is designed as an **open registry**: adding a new currency is a single entry in a configuration table (code, symbol, name, decimal places, locale). Contributors can add currencies via PR. The UI dynamically adapts to whatever currencies are in the registry.

#### 3.7.2 Exchange Rates

**Data source:** [Frankfurter API](https://www.frankfurter.app/) — free, open-source, no API key required, based on European Central Bank reference rates. Updated daily on ECB business days.

Alternative fallback: [exchangerate.host](https://exchangerate.host/) or [Open Exchange Rates](https://openexchangerates.org/) free tier (1,000 requests/month).

**Rate fetching strategy:**

1. On app open (if online and rates are >24h old): fetch latest rates for all enabled currencies.
2. Store rates in a local Room table: `exchange_rate` (base_currency, target_currency, rate, fetched_at).
3. If offline: use the most recent cached rate. Display a small indicator: "Rate from 2 days ago."
4. Rates are always relative to a base currency (EUR for Frankfurter). The app normalizes to the user's home currency.
5. **Never block the UI** on a rate fetch. Show cached data immediately, update in background.

**Rate usage:**

- When a user enters a foreign-currency expense, the app converts to home currency using the rate for that day (or nearest available).
- Historical expenses keep their original amount AND the converted amount at the time of entry. Exchange rates change, but a 50 EUR dinner on March 3rd should always show the DKK equivalent at the March 3rd rate, not today's rate.
- The monthly budget view aggregates everything in home currency.
- A small "≈" prefix indicates converted amounts to distinguish them from native-currency amounts.

#### 3.7.3 Currency UX

- **Expense entry:** The numpad has a small currency chip (e.g., `DKK ▼`). Tapping it shows a bottom sheet with enabled currencies, most-recently-used first. Selection is sticky — if you're logging a bunch of EUR expenses in a row, it stays on EUR until you switch back.
- **Settings → Currencies:** Toggle currencies on/off. Add new currencies from the full registry. Set home currency (with confirmation, since this changes all display amounts).
- **All amounts are stored in their original currency** in the database. The `expense` table has both `amount` (original) and `home_amount` (converted) columns, plus `currency_code` and `exchange_rate_used`.

### 3.8 Data Management

- **CSV export** — all transactions (with both original and home-currency amounts), compatible with Excel/Google Sheets.
- **JSON export** — full database export for developers/backup.
- **Import from CSV** — to migrate from other apps or spreadsheets.
- **Google Drive backup** (see section 5 for details) — encrypted, optional, user-initiated or auto-scheduled.
- **Delete all data** — single action, with confirmation.

---

## 4. Tech Stack

### 4.1 Language & Framework

| Decision         | Choice                        | Rationale                                                                 |
|------------------|-------------------------------|---------------------------------------------------------------------------|
| Language         | **Kotlin**                    | First-class Android language, null safety, coroutines, modern idioms.     |
| UI Framework     | **Jetpack Compose**           | Declarative, less boilerplate, Material 3 native support, future of Android UI. |
| Min SDK          | **API 26 (Android 8.0)**      | Covers ~97% of active devices. Allows java.time APIs without desugaring. |
| Target SDK       | **API 35 (Android 15)**       | Latest, required for Play Store.                                         |
| Build system     | **Gradle with Kotlin DSL**    | Standard, version catalogs for dependency management.                    |
| Architecture     | **MVVM + Clean Architecture** | ViewModels + UseCases + Repository pattern. Testable, maintainable.      |

### 4.2 Key Libraries

| Purpose                  | Library                              | Notes                                                    |
|--------------------------|--------------------------------------|----------------------------------------------------------|
| Local database           | **Room**                             | SQLite abstraction, type-safe queries, migration support. |
| Dependency injection     | **Hilt**                             | Standard for Android, reduces boilerplate.               |
| Navigation               | **Compose Navigation**               | Type-safe navigation with compose-destinations or official lib. |
| Charts                   | **Vico** or **YCharts**              | Compose-native charting. Vico is well-maintained and good-looking. |
| Date/time                | **kotlinx-datetime**                 | Multiplatform, clean API.                                |
| Preferences              | **DataStore (Preferences)**          | Replaces SharedPreferences, async, type-safe.            |
| Backup/sync              | **Google Drive API (REST)**          | For optional cloud backup. See section 5.                |
| Networking               | **Ktor Client**                      | Lightweight, Kotlin-native HTTP client for exchange rate fetching. Minimal footprint. |
| JSON parsing             | **kotlinx.serialization**            | Pairs with Ktor, no reflection, fast.                    |
| On-device ML             | **ONNX Runtime (Android)**           | Lightweight inference engine for custom models. See section 8. |
| On-device ML (alt)       | **ML Kit (Custom Model)**            | Google's ML Kit for simpler use cases (classification). Google flavor only. |
| Testing                  | **JUnit 5 + Turbine + Compose Test** | Unit + flow + UI testing.                                |
| CSV                      | **kotlin-csv** or **Apache Commons** | Lightweight CSV parsing.                                 |
| Animations               | **Compose Animation APIs**           | Built-in, spring-based, interruptible.                   |
| Splash screen            | **Core Splashscreen API**            | Official API, smooth cold-start transition.              |
| Work scheduling          | **WorkManager**                      | For background rate fetching and model retraining.       |

### 4.3 Project Structure

```
app/
├── data/
│   ├── db/                  # Room database, DAOs, entities, migrations
│   ├── repository/          # Repository implementations
│   ├── network/             # Exchange rate API client (Ktor)
│   ├── datastore/           # User preferences (DataStore)
│   └── ml/                  # ML model loading, inference wrappers
├── domain/
│   ├── model/               # Domain models (Budget, Expense, Category, Currency, SavingsBucket...)
│   ├── usecase/             # Business logic (CalculateDailyBudget, ConvertCurrency, GetMLPrediction...)
│   └── repository/          # Repository interfaces
├── ui/
│   ├── theme/               # Material 3 theme, colors, typography
│   ├── onboarding/          # Onboarding flow screens
│   ├── dashboard/           # Daily view (main screen)
│   ├── budget/              # Monthly budget view
│   ├── expenses/            # Expense entry, list, edit
│   ├── savings/             # Savings buckets, investment tracking, goals
│   ├── insights/            # Charts, analytics, ML-powered insights
│   ├── currency/            # Currency management, rate display
│   ├── settings/            # Settings, export, backup
│   └── components/          # Shared composables (cards, inputs, charts, currency chips)
├── worker/                  # WorkManager workers (rate sync, ML retraining, recurring posting)
├── di/                      # Hilt modules
└── util/                    # Extensions, formatters, currency utils, constants
```

---

## 5. Data & Sync Strategy

### 5.1 Local-First Architecture

All data lives in a **Room (SQLite) database** on device. The app must function perfectly with zero network connectivity, always. Network is used only for exchange rates and optional backup.

**Core tables:**

- `currency` — code (PK, e.g., "DKK"), name, symbol, decimal_places, is_enabled, sort_order
- `exchange_rate` — id, base_code, target_code, rate (Double), fetched_at, source (e.g., "frankfurter")
- `income` — id, amount_minor (Long), currency_code, label, is_recurring, recurrence_rule, start_date
- `category` — id, name, icon, color, is_custom, sort_order
- `budget_allocation` — id, category_id, month (YYYY-MM), allocated_amount_minor, currency_code
- `expense` — id, amount_minor (Long), currency_code, home_amount_minor (Long), exchange_rate_used (Double), category_id, note, date, created_at, is_recurring_instance, ml_category_suggestion (nullable), ml_suggestion_accepted (Boolean)
- `recurring_expense` — id, amount_minor, currency_code, category_id, label, recurrence_rule, next_date, is_active
- `savings_bucket` — id, label, type (ENUM: emergency_fund, investment, goal, recurring), currency_code, monthly_contribution_minor, target_amount_minor (nullable), deadline (nullable), current_balance_minor, balance_updated_at, is_active, sort_order
- `savings_balance_snapshot` — id, bucket_id, balance_minor, recorded_at (for historical tracking)
- `savings_contribution` — id, bucket_id, amount_minor, date, is_auto_posted, is_skipped
- `monthly_snapshot` — id, month, total_income_minor, total_fixed_minor, total_variable_minor, total_savings_minor, home_currency_code (denormalized for fast historical queries)
- `ml_model_metadata` — id, model_type (e.g., "category_classifier"), version, trained_at, sample_count, accuracy_estimate

All `_minor` columns store amounts in the smallest currency unit (øre, cents, etc.) as `Long` to avoid floating-point issues.

Use Room's `@TypeConverter` for dates, enums, and custom types. Plan migrations from day one — use a migration testing strategy.

### 5.2 Google Drive Backup (the Android equivalent of iCloud sync)

There is no true "iCloud for Android" that syncs app data transparently. The realistic options:

| Option                           | Verdict                                                              |
|----------------------------------|----------------------------------------------------------------------|
| Auto Backup (system)             | Limited to 25MB, unreliable, user can't control it. Not suitable.    |
| Google Drive App Data folder     | Hidden folder, app-specific. Good for backup. Not visible to user.   |
| Google Drive regular folder      | Visible to user, they can manage the file. More transparent.         |
| WebDAV / self-hosted             | Too complex for non-tech users. Could be a v2 plugin.               |

**Recommended approach: Google Drive App Data folder** with option to also export to a visible Drive folder.

Implementation:
1. User taps "Backup to Google Drive" or enables auto-backup (daily/weekly).
2. App exports the Room database as an encrypted JSON file (or encrypted SQLite copy).
3. Uploads to the app's hidden App Data folder on Google Drive via the Drive REST API.
4. Restore: download, decrypt, replace local database.
5. Encryption: AES-256, key derived from a user-chosen passphrase. Without the passphrase, the backup file is meaningless.

**Important:** This is backup/restore, not real-time sync. True multi-device sync with conflict resolution is a massive engineering effort — don't attempt it for v1. Be honest with users: "This backs up your data. To use on a new phone, restore from backup."

### 5.3 Exchange Rate Sync

- **WorkManager** schedules a daily background job to fetch rates.
- Job constraints: requires network, not on low battery.
- Fetches rates for all enabled currencies in a single API call (Frankfurter supports this).
- Stores in `exchange_rate` table with timestamp.
- On failure: retry with exponential backoff. Never block the user.
- Rate history is kept (not overwritten) so historical conversions remain accurate.
- Rate freshness indicator in the UI: green dot (< 24h), yellow (1–3 days), grey (> 3 days).

### 5.4 Data Export

- **CSV:** One file per table or a single denormalized transactions file. Includes both original and home-currency amounts, currency codes. Headers in English. UTF-8 with BOM for Excel compatibility.
- **JSON:** Full database dump, versioned schema. Useful for developers and for backup interoperability.
- **Sharing:** Use Android's share sheet to send export files via email, messaging, etc.

---

## 6. UX & Design Guidelines

### 6.1 Design System

- **Material Design 3** with **dynamic color** (Material You). The app picks up the user's wallpaper colors.
- Provide a fallback color scheme (a calm blue-green palette) for devices without dynamic color support.
- **Typography:** Use the default Material 3 type scale. No custom fonts — keep APK lean and native-feeling.
- **Icons:** Material Symbols (outlined style). Consistent weight and optical size.
- **Spacing:** Follow the 8dp grid strictly.
- **Dark mode:** Full support from day one. Not an afterthought.
- **Edge-to-edge:** Draw behind system bars. Use `WindowInsets` correctly.
- **Currency & savings visual language:**
    - Foreign-currency amounts: slightly muted color + "≈" prefix for converted values.
    - Savings-related elements: green/teal accent to visually distinguish from spending.
    - Investment buckets: subtle chart-line icon or growth arrow to differentiate from emergency fund / goal savings.

### 6.2 Interaction Principles

- **One-handed usability** — primary actions in the bottom half of the screen. The quick-add FAB is in the bottom-right corner.
- **3-tap expense entry** — open → amount → category → done. Note and currency are optional, not blocking.
- **Swipe to delete** with undo snackbar (never a confirmation dialog for common actions).
- **Haptic feedback** on key actions (expense added, budget reset, streak milestone, savings goal reached).
- **Predictive back gesture** support (Android 14+).
- **Large screen support** — adaptive layouts for tablets and foldables using `WindowSizeClass`.
- **Bottom navigation** — 4 tabs: Dashboard (daily), Budget (monthly), Savings, Insights.

### 6.3 Accessibility

- Minimum touch target: 48dp.
- All interactive elements have content descriptions.
- Support for TalkBack. Currency amounts announce both original and converted values.
- Sufficient color contrast ratios (WCAG AA minimum).
- Don't rely on color alone to convey information (pair with icons or text). Particularly important for savings vs. spending visual distinction.
- Respect system font size scaling (don't hardcode `sp` values that break at large font sizes).

### 6.4 Animations & Polish

- **Shared element transitions** between list items and detail screens.
- **Spring-based animations** for number changes (daily budget counter, savings progress).
- **Skeleton loading** (shimmer) if any data takes time to compute.
- **Smooth chart animations** — data points animate in on screen entry.
- **Micro-interactions** — subtle scale on button press, progress bar fills smoothly, savings milestone celebration (confetti or gentle pulse, not obnoxious).
- **Currency switch animation** — smooth cross-fade when toggling between currencies in expense entry.

---

## 7. Denmark-Specific Considerations

### 7.1 Currency & Formatting

- **Home currency default:** DKK (kr). Changeable during onboarding or in settings.
- **DKK display format:** `1.234,56 kr` (period as thousands separator, comma as decimal).
- **Other currencies** follow their standard locale formatting: EUR uses `€1.234,56`, USD uses `$1,234.56`, NZD uses `$1,234.56`.
- Use `java.text.NumberFormat` with the appropriate `Locale` per currency for consistent formatting. A `CurrencyFormatter` utility class abstracts this.
- All amounts stored as `Long` in the smallest unit (øre, cents) to avoid floating-point issues. The `currency` table's `decimal_places` column tells the formatter how to convert.
- **DKK ↔ EUR special case:** Denmark pegs DKK to EUR at roughly 7.46. The app can display a note in currency settings: "DKK is pegged to EUR — rate is very stable." This helps users understand why the rate barely changes.

### 7.2 Default Categories Reflecting Danish Life

Pre-populated budget categories should feel familiar to someone living in Denmark:

- **Bolig (Housing):** Husleje, ejerbolig (mortgage), a conto varme, a conto vand, el, boligforsikring.
- **Transport:** Rejsekort, DSB/metro monthly pass, car (insurance, benzin, parking, vægtafgift).
- **Forsikring:** Indboforsikring, ulykkesforsikring, sundhedsforsikring.
- **Fagforening & A-kasse.**
- **Pension (voluntary contributions).**
- **Groceries** — Netto, Rema, Føtex, Lidl context (not part of app, just informing category defaults).
- **Subscriptions** — streaming, gym, phone plan.

The category system uses English labels (app is English-only) but the defaults are informed by Danish expense patterns. For instance, heating is a separate category from electricity — in many countries this would be merged into "utilities."

### 7.3 Income Handling

- Danish salaries are typically paid monthly on a fixed date (often the last business day).
- SU is paid on a specific date each month.
- The app should allow setting "income day" so it knows when the budget month resets.
- Consider: budget month ≠ calendar month. If you're paid on the 25th, your "budget month" might run 25th to 24th.

### 7.4 Denmark-Relevant Savings Defaults

During onboarding, the savings step can offer pre-labeled bucket templates:

- **ASK (Aktiesparekonto)** — standard Danish tax-advantaged investment account. 2026 contribution limit should be noted if known at build time, or fetched via a simple config update.
- **Pension (ratepension / aldersopsparing top-up)** — voluntary contributions beyond employer.
- **Emergency fund** — generic, with a suggested target of 3× monthly expenses.
- **Feriepenge buffer** — some workers receive holiday pay as a lump sum; budgeting for "holiday months" where spending spikes.
- **Custom** — user-defined label and parameters.

### 7.5 Seasonal Awareness

- Danish expenses vary seasonally: heating costs spike in winter (October–March), electricity varies.
- The insights engine could note: "Your electricity spending typically rises in November — consider budgeting 200 kr extra."
- The ML system (section 8) can learn these seasonal patterns automatically after 12+ months of data.

---

## 8. On-Device Machine Learning

### 8.1 Philosophy

All ML features are:
- **Opt-in** — clearly toggled in settings under "Smart Features." Off by default.
- **Fully on-device** — no data is sent anywhere. Models are trained and run locally.
- **Transparent** — the app explains what the model does and what data it uses, in plain language.
- **Gracefully degradable** — if ML is off, the app works identically, just without suggestions.
- **Not an LLM** — these are lightweight, purpose-built models. No generative AI, no hallucinations, no prompt engineering. The user gets deterministic, explainable outputs.

### 8.2 Feature: Category Auto-Suggestion

**What it does:** When the user enters an expense amount (and optionally a note), the model suggests a category.

**How it works:**

- **Model type:** Lightweight gradient-boosted tree or small neural network (< 1 MB).
- **Input features:** Amount (bucketed), time of day, day of week, day of month, note text (if provided — simple TF-IDF or character n-gram embedding), recent category history.
- **Output:** Probability distribution over categories. Show top suggestion if confidence > 70%.
- **Training data:** The user's own expense history. Model retrains weekly (or when >20 new expenses are added) via a WorkManager background job.
- **Minimum data:** Model activates after ~50 categorized expenses. Before that, the feature is hidden.
- **Runtime:** ONNX Runtime for inference. Training can use a lightweight on-device framework or pre-compute features and use a simple Kotlin-native decision tree implementation.

**UX:** A colored chip appears below the amount field: `🏷️ Groceries?` — tap to accept, tap X to dismiss and pick manually. Every interaction (accept or override) improves the model.

### 8.3 Feature: Spending Anomaly Detection

**What it does:** Flags unusual transactions.

**How it works:**

- **Model type:** Statistical (no ML model needed). Per-category rolling mean and standard deviation over the last 90 days.
- **Trigger:** An expense that is > 2 standard deviations above the category mean.
- **UX:** A subtle banner on the daily view: "Heads up: your 890 kr Transport expense is unusually high (avg: 47 kr)." Dismissable. Not alarming.
- **Value:** Catches accidental double-charges, forgotten subscriptions, or genuinely unusual spending.

### 8.4 Feature: Monthly Spend Forecasting

**What it does:** Predicts total spending for the month based on current trajectory.

**How it works:**

- **Model type:** Simple linear extrapolation for v1. Can upgrade to a time-series model (exponential smoothing or a tiny LSTM) in v2 if enough data accumulates.
- **Input:** Daily cumulative spending through the month, historical monthly totals, day-of-month spending patterns.
- **Output:** Projected end-of-month total, shown as a dashed line extension on the daily cumulative chart.
- **Confidence band:** Light shaded area around the projection showing likely range.
- **UX:** On the insights screen: "At your current pace, you'll spend ~14,200 kr this month (budget: 12,000 kr)." If the projection exceeds the budget, the daily budget view can show a gentle amber warning.

### 8.5 Feature: Seasonal Pattern Detection

**What it does:** After 12+ months of data, identifies recurring seasonal patterns.

**How it works:**

- **Model type:** Month-over-month comparison per category with simple regression.
- **Trigger:** When a category shows a consistent pattern across the same months in different years (e.g., heating always spikes in November).
- **UX:** Insight card: "Historically, your heating costs rise by ~40% in October. Consider budgeting 500 kr extra." Shown once per season, dismissable.
- **Requirement:** Minimum 12 months of data. This is a long-game feature — it rewards consistent use.

### 8.6 Optional: Local LLM for Natural Language Notes (v2+, experimental)

**If and only if** a suitable small on-device LLM becomes practical (< 100 MB, fast inference on mid-range phones):

- The user could type a natural-language note ("coffee with friends at Paludan") and the model extracts: category (Coffee & drinks), note (Paludan), and possibly suggests amount from context.
- This is **strictly opt-in**, clearly labeled as experimental, and requires the user to download the model separately.
- **NOT in v1.** Listed here only to ensure the architecture doesn't prevent it. The expense entry pipeline already has a hook for ML-assisted input.

### 8.7 Technical Implementation

| Concern              | Approach                                                                                 |
|----------------------|------------------------------------------------------------------------------------------|
| Inference engine     | **ONNX Runtime (onnxruntime-android)** — ~3 MB, runs on CPU, no Google dependency.       |
| Model format         | ONNX for trained models. Simple statistical models (anomaly, forecast) are pure Kotlin.  |
| Training             | Feature extraction in Kotlin → train a small scikit-learn model → convert to ONNX. Initial model ships with the app (trained on synthetic/aggregated data), then retrains on-device. |
| On-device retraining | For simple models (decision trees, linear): pure Kotlin implementation. For neural: export features, retrain on next app update cycle or use federated-learning-style on-device fine-tuning (stretch goal). |
| Storage              | Models stored in app internal storage. Metadata in `ml_model_metadata` table.            |
| Battery impact       | Inference: negligible (< 1ms per prediction). Retraining: constrained to charging + idle via WorkManager. |
| APK size impact      | ONNX Runtime: ~3 MB. Models: < 1 MB each. Total ML overhead: ~5 MB.                     |
| FOSS flavor          | ONNX Runtime is open source (MIT). Fully compatible with F-Droid.                        |

---

## 9. Extensibility & Hackability

### 9.1 Plugin-Friendly Architecture

While v1 won't have a formal plugin system, the architecture should make it easy to add:

- **New chart types** — charting is abstracted behind a composable interface.
- **New export formats** — export logic is in a repository, adding OFX/QIF is a new implementation.
- **New categories** — fully user-configurable, stored in database, not hardcoded.
- **New currencies** — single row in the `currency` table + optional locale formatter. Contributors can add via PR.
- **New savings bucket types** — the type enum is extensible; new types can have custom visualizations.
- **Budget strategies** — the "daily budget" calculation is a `UseCase`. Alternative strategies (envelope method, 50/30/20 rule) can be additional use cases behind a strategy pattern.
- **New ML models** — the ML pipeline has a clean interface: `suspend fun predict(input: ExpenseInput): CategorySuggestion?`. New model types plug in without touching UI code.
- **Exchange rate providers** — behind an interface: `suspend fun fetchRates(base: String, targets: List<String>): Map<String, Double>`. Swapping Frankfurter for another source is a single implementation change.
- **Theming** — Material 3 dynamic color + user-selectable accent colors.

### 9.2 Developer Experience

- **Clear README** — setup instructions, architecture overview, contribution guidelines.
- **CONTRIBUTING.md** — code style (ktlint), PR process, issue templates.
- **Architecture Decision Records (ADRs)** — document major decisions (why Room over Realm, why not KMP, why ONNX over TFLite, etc.).
- **CI/CD** — GitHub Actions: lint, test, build APK on every PR. Release builds via tags.
- **F-Droid ready** — no proprietary dependencies in the core app. Google Drive backup is an optional flavor.
- **ML contribution guide** — how to train a new model, export to ONNX, and register it in the app.

### 9.3 Build Flavors

```kotlin
productFlavors {
    create("foss") {
        // No Google dependencies. Pure open source. F-Droid compatible.
        // Backup: local file export only.
        // ML: ONNX Runtime (MIT license, fully FOSS).
        // Exchange rates: Frankfurter API (open source).
    }
    create("google") {
        // Includes Google Drive backup.
        // ML: ONNX Runtime + optionally ML Kit for enhanced features.
        // Distributed via Play Store.
    }
}
```

This ensures F-Droid inclusion (which requires no proprietary code) while still offering Google Drive backup for Play Store users.

---

## 10. Development Roadmap

### Phase 1 — Foundation (Weeks 1–4)

- Project setup: Gradle, Hilt, Room, Compose, CI.
- Database schema (including currency, savings, ML tables) and migrations framework.
- Theme and design system (Material 3, dynamic color, dark mode).
- Core domain models and use cases.
- Currency registry and formatter utility.
- Basic expense CRUD (add, list, edit, delete) with currency support.
- Category management.

### Phase 2 — Budget Engine (Weeks 5–8)

- Onboarding flow (including currency selection and savings goals).
- Monthly budget setup and allocation.
- Recurring expenses engine (auto-posting, next-date calculation).
- Savings buckets: CRUD, monthly auto-posting, balance snapshots.
- Daily budget calculation (income − fixed − savings − spent ÷ remaining days).
- Dashboard (daily view) with quick-add.

### Phase 3 — Multi-Currency & Exchange Rates (Weeks 9–10)

- Ktor client setup and Frankfurter API integration.
- WorkManager background rate sync.
- Currency selector in expense entry.
- Conversion logic and display (original + home amount).
- Rate freshness indicator.
- Currency settings screen.

### Phase 4 — Insights & Visualizations (Weeks 11–14)

- Charts: donut, trend line, daily cumulative, category comparison.
- Savings dashboard: progress bars, goal countdowns, savings rate, historical chart.
- Multi-currency breakdown chart.
- Auto-generated text insights.
- Animations and micro-interactions.
- Accessibility audit and fixes.
- Streak tracking.

### Phase 5 — On-Device ML (Weeks 15–17)

- ONNX Runtime integration.
- Category auto-suggestion model: feature extraction, initial synthetic model, inference pipeline.
- Anomaly detection (statistical, pure Kotlin).
- Monthly spend forecasting (linear extrapolation).
- ML settings screen (opt-in toggles, transparency info).
- WorkManager job for background retraining.
- On-device retraining pipeline for category classifier.

### Phase 6 — Data & Distribution (Weeks 18–20)

- CSV/JSON export and import (with multi-currency columns).
- Google Drive backup and restore (google flavor).
- Settings screen (income day, custom categories, theme, currencies, ML).
- Play Store listing, screenshots, description.
- F-Droid metadata and reproducible builds.
- Beta testing with real users in Denmark.

### Phase 7 — v1.0 Release

- Bug fixes from beta.
- Performance profiling (startup time, scroll performance, database query speed, ML inference time).
- ML model tuning based on beta feedback.
- v1.0 stable release.

---

## 11. Post-v1 Ideas (Backlog)

These are explicitly out of scope for v1 but the architecture should not prevent them:

- **Bank integration via Open Banking (PSD2)** — auto-import transactions from Danish banks. Complex regulatory requirements but hugely valuable.
- **Widgets** — Glance (Jetpack) widget showing daily remaining budget on home screen.
- **Wear OS companion** — quick-add from smartwatch.
- **Shared budgets** — for couples or roommates.
- **Envelope budgeting mode** — alternative to the daily-budget approach.
- **Receipt photo attachment** — attach a photo to an expense.
- **Notifications** — daily spending summary, weekly recap, approaching budget limit.
- **WebDAV sync** — self-hosted alternative to Google Drive.
- **Localization** — Danish, German, etc. (v1 is English-only but string resources should use `strings.xml` from day one).
- **Local LLM note parsing** — see section 8.6.
- **Crypto/non-fiat currency support** — if demand exists. Architecture supports it (custom decimal places, volatile rates).
- **Portfolio tracking integration** — connect to Nordnet/Saxo via API for real balance updates on investment buckets.

---

## 12. Quality Gates (Definition of Done for v1)

- [ ] All screens work in light and dark mode.
- [ ] Dynamic color works on Pixel devices and gracefully falls back elsewhere.
- [ ] Expense entry in ≤ 3 taps (home currency) or ≤ 4 taps (foreign currency).
- [ ] App cold-starts in < 1 second on mid-range device.
- [ ] Database migrations are tested.
- [ ] TalkBack navigation works on all screens, including currency and savings flows.
- [ ] No hardcoded strings — everything in `strings.xml`.
- [ ] CSV export opens correctly in Excel and Google Sheets with proper currency columns.
- [ ] Google Drive backup/restore round-trips without data loss (including savings and currency data).
- [ ] Exchange rates fetch successfully and degrade gracefully when offline.
- [ ] ML category suggestion accuracy > 70% after 100 categorized expenses (measured in beta).
- [ ] ML inference < 5ms per prediction on a mid-range device.
- [ ] Unit test coverage > 80% on domain layer (including currency conversion and savings calculations).
- [ ] UI tests for onboarding, expense entry, savings bucket creation, and currency switching.
- [ ] No crashes in 1 week of daily use by 3+ beta testers.
- [ ] F-Droid build compiles without proprietary dependencies.
- [ ] APK size < 20 MB (increased from 15 MB to accommodate ONNX Runtime).
- [ ] ProGuard/R8 rules verified — no reflection issues at runtime, ONNX models not stripped.

---

## 13. License Recommendation

**MPL-2.0 (Mozilla Public License 2.0):**

- Requires modifications to MPL-licensed files to stay open.
- Allows combining with proprietary code (important if someone wants to fork and add a proprietary feature).
- More permissive than GPL but still protects the core.
- Compatible with F-Droid.
- Used by Firefox, Syncthing, and other well-known open-source projects.

Alternative: **GPLv3** if stronger copyleft is desired (all derivative work must be open source). This is a values decision for the project owner.

---

## Appendix A: Key Technical Decisions & Rationale

**Why not Kotlin Multiplatform (KMP)?**
The brief says Android-only and native feel. KMP adds complexity for no benefit unless iOS is planned. If iOS becomes a goal later, the clean architecture with domain/data separation makes KMP migration straightforward — the domain and data layers can be extracted into a shared module.

**Why not Flutter or React Native?**
Native Kotlin + Compose gives the best Material 3 support, smallest APK, best performance, and access to all Android APIs without bridges. For an app that must feel premium and native, cross-platform frameworks add friction.

**Why Room over Realm or SQLDelight?**
Room is the official Jetpack persistence library, has first-class Compose and Flow support, well-tested migration system, and the largest community. SQLDelight is excellent but Room has better tooling integration. Realm's future is uncertain after the MongoDB acquisition shifts.

**Why store amounts as Long (minor units)?**
Floating-point arithmetic causes rounding errors with currency. Storing as the smallest unit (øre for DKK, cents for EUR/USD/NZD) and doing integer math eliminates this. The `currency` table's `decimal_places` column ensures the formatter knows how to convert for any currency (most are 2, but some like JPY are 0). This is standard practice in fintech.

**Why not a monthly subscription model?**
The brief says free and open source. Revenue, if needed, could come from a voluntary donation model (like Signal) or a "pro" flavor with cosmetic extras (icon packs, custom themes). But the core app must never paywall functionality.

**Why Frankfurter API for exchange rates?**
Free, no API key, open source, backed by ECB data. Reliable for daily rates. Limitations: ECB only publishes rates on business days, and only for major currencies. This is fine for our use case (daily budgeting, not forex trading). If a currency isn't supported by ECB, the app can fall back to exchangerate.host or accept user-entered rates.

**Why ONNX Runtime over TensorFlow Lite?**
ONNX Runtime is MIT-licensed (fully FOSS-compatible), lighter (~3 MB vs. ~5 MB for TFLite), and supports a wider range of model formats. TFLite is excellent but adds a soft Google dependency. For the FOSS build flavor, ONNX is the cleaner choice. Both can be supported via a common interface if needed later.

**Why not a cloud-based ML service?**
The app's core promise is "your data never leaves your phone." Sending expense data to a cloud ML endpoint would violate this promise and require internet access. On-device ML is more private, works offline, and for our use cases (category classification, anomaly detection) the models are small enough that on-device performance is excellent.

**Why opt-in for ML features?**
Some users are uncomfortable with any form of "AI" in their apps, even on-device. Making it opt-in respects that. It also means the app works perfectly without ML — the features are enhancements, not dependencies. The settings screen clearly explains what each ML feature does and that all processing happens locally.
