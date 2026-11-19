# Kids Budget

Kids Budget is an Android app for a parent who tracks allowance for younger children. You keep the phone and the records. You add each child, give them accounts, and log money in and money out while you talk through it together.

The app is a ledger for you to use with a younger child. It is not a kids' app for them to run on their own.

## What you can track

- A profile for each child, with a name and a color
- Accounts such as Saving, Spending, and Giving, or any name you choose
- Money received (allowance, a gift, pay for a chore) and money spent
- A total for each child, a balance for each account, an activity list, and a chart of the balance over time

Records stay in a database on the phone.

## How to use it

1. Open the app. The first screen lists your children.
2. Tap **+**. Enter the child's name, pick a color, and tap **Save**. Leave **Create Saving, Spending, and Giving accounts** checked to start with those three accounts.
3. Tap the child. On a phone, their page opens full screen. On a wide screen, such as a tablet, the list stays on the left and the child stays on the right.
4. The top of the page shows their total balance, which is the sum of every account.
5. Tap **Add account** for another account. Pick Saving, Spending, or Giving, or type a different name.
6. Tap **+** to log a transaction. This button appears after the child has at least one account. Choose the account, choose **Received** or **Spent**, enter the amount and an optional memo, set the date, and tap **Save**.
7. Received amounts raise the balance. Spent amounts lower it. The chart is the running total of every transaction, oldest first.
8. Open the **⋮** menu on an account or a transaction to delete that item. Deleting an account also deletes its transactions.
9. Use the pencil to change the child's name or color. Use the trash icon to delete the child, their accounts, and every transaction. The app asks you to confirm.

Amounts are stored in cents. The entry field shows a dollar sign. Balances on screen use your phone's currency format.

## Back up the data

Open the **⋮** menu on the children list.

- **Export data** writes a JSON file. The suggested name looks like `kids-budget-2026-11-19.json`. Keep that file somewhere you control.
- **Import data** reads a Kids Budget backup and replaces every child, account, and transaction already in the app. The app asks you to confirm before it does that.

The file format is version 1 and is labeled `kids-budget`. A backup from a newer version of the app will not import.

## Build and run

You need JDK 17, Android Studio with Android SDK 36, and a device or emulator running Android 8.0 (API 26) or newer.

Open this folder in Android Studio and run the **app** configuration.

From the command line:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The debug APK is written under `app/build/outputs/apk/debug/`.
