# UI Layer - Quick Start Guide

## ✅ What You Have Now

Your complete UI foundation is ready! Here's what was created:

### 📁 Directory Structure
```
ui/
├── theme/
│   ├── Colors.kt       - Color palette (travel-themed blues/oranges)
│   ├── Typography.kt   - Text styles (Material 3)
│   └── Theme.kt        - Main theme wrapper
├── components/
│   ├── InterestChip.kt - Pill-shaped interest tags
│   ├── UserAvatar.kt   - Circular profile pictures
│   └── EventCard.kt    - Event list item cards
└── screens/
    ├── AuthScreen.kt          - Login/Signup screen
    ├── ProfileScreen.kt       - User profile display
    └── CreateEventScreen.kt   - Event creation form
```

---

## 🚀 How to Use These Files

### Step 1: Update App.kt to use your theme

Replace the content in `App.kt` with this:

```kotlin
package org.example.project

import androidx.compose.runtime.*
import org.example.project.ui.theme.TravelCompanionTheme
import org.example.project.ui.screens.AuthScreen
import org.example.project.viewmodel.AuthViewModel

@Composable
fun App() {
    TravelCompanionTheme {
        val authViewModel = remember { AuthViewModel() }
        
        AuthScreen(
            viewModel = authViewModel,
            onAuthSuccess = {
                // TODO: Navigate to main screen
                println("Login successful!")
            }
        )
    }
}
```

### Step 2: Test Individual Components

You can test components in isolation by creating preview functions:

```kotlin
// In any screen file, add:
@Composable
fun PreviewEventCard() {
    TravelCompanionTheme {
        EventCard(
            event = Event(
                id = "1",
                title = "Sunset Hike",
                locationName = "Mount Peak",
                dateTime = "2024-04-04T18:00:00",
                description = "Join us for an evening hike!",
                lat = 52.3676,
                lng = 4.9041,
                maxParticipants = 10,
                keyInterests = listOf(1, 2, 3)
            )
        )
    }
}
```

---

## 🎨 Theme Customization

### Colors
Edit `theme/Colors.kt` to change your app's look:
- **Primary** (blue) - Main brand color, buttons
- **Secondary** (orange) - Accents, highlights
- **Accent** (green) - Success states, nature activities

Interest colors are pre-mapped:
- Art → Pink
- Music → Purple
- Sports → Green
- Food → Red
- Tech → Blue
- Nature → Light Green
- Culture → Orange
- Nightlife → Deep Purple

### Typography
Edit `theme/Typography.kt` if you want custom fonts:
1. Add font files to `commonMain/composeResources/font/`
2. Create `FontFamily` in Typography.kt
3. Replace `FontFamily.Default`

---

## 🔌 Connecting to ViewModels

Your screens are ready to connect to existing ViewModels!

### AuthScreen - ALREADY CONNECTED ✅
```kotlin
AuthScreen(
    viewModel = AuthViewModel(),
    onAuthSuccess = { /* navigate */ }
)
```

### ProfileScreen - Connect like this:
```kotlin
val profileViewModel = remember { ProfileViewModel() }
val profile by profileViewModel.currentProfile.collectAsState()

profile?.let {
    ProfileScreen(
        profile = it,
        interests = listOf("Art", "Hiking", "Music"),
        onEditClick = { /* open edit dialog */ }
    )
}
```

### CreateEventScreen - Connect like this:
```kotlin
val eventViewModel = remember { EventViewModel() }

CreateEventScreen(
    onBackClick = { /* navigate back */ },
    onCreateClick = { formData ->
        // Convert formData to EventInsert
        val eventInsert = EventInsert(
            creatorId = currentUserId,
            title = formData.title,
            description = formData.description,
            locationName = formData.locationName,
            dateTime = formData.dateTime,
            maxParticipants = formData.maxParticipants,
            lat = 0.0, // TODO: Get from map picker
            lng = 0.0,
            keyInterests = formData.selectedInterests.mapIndexed { i, _ -> i }
        )
        eventViewModel.createEvent(eventInsert)
    }
)
```

---

## 📝 Next Steps (Priority Order)

### IMMEDIATE (Do This Now):
1. ✅ Test the theme - Update App.kt to use `TravelCompanionTheme`
2. ✅ Run the app - See your AuthScreen in action
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
   ```

### SHORT TERM (Next 2 hours):
3. **Create a simple navigation system**
   - Install Voyager: `implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")`
   - Create screens: Auth → Profile → CreateEvent
   
4. **Add EventList screen** (missing from your original list!)
   ```kotlin
   LazyColumn {
       items(events) { event ->
           EventCard(event = event, onEventClick = { /* details */ })
       }
   }
   ```

5. **Integrate Map view**
   - You'll need Google Maps SDK
   - Show events as markers
   - Use `event.lat` and `event.lng`

### MEDIUM TERM (After basic flow works):
6. Add date/time picker for CreateEventScreen
7. Add image upload for UserAvatar
8. Add search/filter for events
9. Create EventDetailsScreen (when you click an EventCard)

---

## 🐛 Troubleshooting

### "Unresolved reference: Color"
- Add import: `import androidx.compose.ui.graphics.Color`

### "Cannot find TravelCompanionTheme"
- Make sure all theme files compiled successfully
- Clean and rebuild: `./gradlew clean build`

### "ViewModel not found"
- ViewModels are in `org.example.project.viewmodel`
- They're already created by your teammates!

### "Supabase errors"
- Create `.env` file in project root
- Add your Supabase credentials (check `.env.example`)

---

## 💡 Pro Tips

1. **Use Material 3 tokens** - `MaterialTheme.colorScheme.primary` instead of hardcoded colors
2. **Consistent spacing** - Use multiples of 4dp (4, 8, 12, 16, 24, 32)
3. **Test on both platforms** - Run on Android emulator AND web browser
4. **Keep it simple** - For hackathon, functionality > perfection

---

## 🎯 Hackathon Demo Flow (Make THIS Perfect)

```
1. Auth Screen (login/signup) 
   ↓
2. Event List Screen (map or list view)
   ↓
3. Click "Create Event" button
   ↓
4. Create Event Screen (fill form)
   ↓
5. Back to Event List (see your new event!)
   ↓
6. Click event → Show details (participants, join button)
   ↓
7. Profile Screen (show joined events)
```

Focus on making this ONE flow beautiful and functional!

---

## 🤝 Questions?

- Check existing ViewModels in `/viewmodel` - they have all the backend logic
- Look at data models in `/data/model` - they define your data structure
- Test components individually before combining them
- Ask your teammates about backend API if needed

Good luck! 🚀
