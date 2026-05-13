# Expense Tracker App 💰

A modern Android Expense Tracker application built using **Kotlin**, **MVVM Architecture**, **Room Database**, and **Material Design**.
This app helps users manage daily expenses, track spending habits, and visualize expenses category-wise through interactive charts.

---

# ✨ Features

## 🏠 Home Dashboard

* Beautiful modern UI
* Fixed header and summary card
* Displays:

  * Total spending
  * Transaction count
  * Daily average
  * Total categories
* Shows only the latest 10 transactions

---

## ➕ Add Expense

* Add new expenses easily
* Smart validations:

  * Title accepts only text
  * Amount accepts numeric values
* Current date selected automatically
* Calendar picker for custom date selection
* Default category preselected as **Others**
* Example hints for better user guidance

---

## 📊 Statistics Screen

* Interactive Pie Chart using **MPAndroidChart**
* Category-wise expense visualization
* Dynamic category summary list
* Consistent colors across the app

---

## 📜 Transaction History

* View all transactions
* Swipe left/right to delete expenses
* Edit existing expenses

---

## 🎨 Modern UI

* Material Design components
* Smooth layouts and animations
* Responsive and clean interface

---

# 🛠️ Tech Stack

* **Kotlin**
* **MVVM Architecture**
* **Room Database**
* **LiveData**
* **ViewModel**
* **Hilt Dependency Injection**
* **Navigation Component**
* **RecyclerView**
* **MPAndroidChart**
* **Material Components**

---

# 📂 Project Structure

```text
com.example.expensetracker
│
├── adapter
├── data
│   ├── local
│   └── repository
├── di
├── ui
│   ├── home
│   ├── statistics
│   ├── alltransactions
│   ├── addexpense
│   └── settings
├── utils
└── viewmodel
```

---

# 📸 Screenshots

*Add your screenshots here*

| Home Screen | Statistics Screen |
| ----------- | ----------------- |
| Screenshot  | Screenshot        |

---

# 🚀 Installation

1. Clone the repository

```bash
git clone (https://github.com/emaanfaatima88-ux/Expense_Tracker.git)
```

2. Open project in Android Studio

3. Sync Gradle

4. Run the app on emulator or physical device

---

# 📦 Dependencies

```gradle
implementation("androidx.room:room-runtime")
implementation("com.google.dagger:hilt-android")
implementation("androidx.navigation:navigation-fragment-ktx")
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

---

# 🔮 Future Improvements

* Budget planning
* Monthly reports
* Dark mode
* Cloud backup
* Expense filters
* Export to PDF/Excel
* Authentication system

---

# 👨‍💻 Developer

Developed by **Eman**

---

# ⭐ Support

If you like this project, consider giving it a ⭐ on GitHub.
