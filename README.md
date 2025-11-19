# 📱 **SeniorBuddy – Real-Time Ride Support for Senior Citizens**

SeniorBuddy is a real-time Android application designed to **connect senior citizens with verified community volunteers** for safe and reliable rides.
Built using **Kotlin, Jetpack Compose, and Firebase**, the app ensures seamless interactions, instant updates, and secure login flows for both Seniors and Volunteers.

---

## 🚀 **Key Features**

### 👴 Senior User Features

* 📍 Request a ride with pickup & drop locations
* 🚗 Select vehicle type (auto, bike, car)
* ❤️ Mention special assistance needs
* 💰 Suggest fare for the ride
* 🔔 Live updates on volunteer acceptance & ride progress

### 🤝 Volunteer Features

* 📡 View ride requests **in real-time**
* ✔️ Accept available ride requests instantly
* 📋 Manage ongoing & completed rides
* 🔄 Update ride status (Accepted → In-Progress → Completed)

### 🔐 Authentication & Security

* 🔑 Firebase Authentication with **role-based login**
* 🧓 Senior and 🙋 Volunteer dashboards
* 🔒 Firestore rules for secure data access

### ☁️ Backend – Firebase Firestore

* ⚡ Real-time updates for all ride requests
* 🔄 Bi-directional syncing between Senior & Volunteer apps
* 📁 Fully cloud-based, scalable NoSQL database

---

## 🏗️ **Tech Stack**

| Category         | Technologies                                     |
| ---------------- | ------------------------------------------------ |
| **Frontend**     | Kotlin, Jetpack Compose, Material 3              |
| **Backend**      | Firebase Firestore, Firebase Auth                |
| **Architecture** | MVVM, Coroutines, StateFlow                      |
| **Tools**        | Android Studio, GitHub, Emulator/Physical Device |

---

## 📸 **App Screenshots**

*(Images resized to look clean and consistent)*

<p align="center">
  <img src="https://github.com/user-attachments/assets/fbf4af8e-9df5-497c-a32c-ae63e3d2c292" width="250" />
  <img src="https://github.com/user-attachments/assets/311009d7-617f-4cc3-a1a7-439702f52c2a" width="250" />
  <img src="https://github.com/user-attachments/assets/2579cf20-5d4e-4a98-9899-45a17b661b1c" width="250" />
</p>

---

## 🧩 **How It Works**

1. **Senior logs in** → opens dashboard
2. Enters **ride details** and submits
3. Request is stored in **Firestore (real-time)**
4. **Volunteers instantly see** new request
5. Volunteer accepts → Senior receives update
6. Ride progresses through status stages
7. Ride completes → both dashboards update

---

## 📂 **Project Structure (Simplified)**

```
SeniorBuddy/
 ├── ui/
 │   ├── senior/
 │   ├── volunteer/
 │   ├── components/
 ├── data/
 │   ├── firestore/
 │   ├── auth/
 ├── model/
 ├── viewmodel/
 └── utils/
```

---

## 🛠️ **Setup Instructions**

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/SeniorBuddy.git
   ```
2. Open in **Android Studio**
3. Add your Firebase `google-services.json`
4. Enable:

   * Firebase Authentication
   * Cloud Firestore
5. Run the project!

---

## ❤️ **Contributions**

Feel free to open issues or submit pull requests.
All contributions are welcome!

---

## ⭐ **Support the Project**

If you like SeniorBuddy, give this repo a **⭐ star** — it motivates future updates!

---
