# Wandr

A cross-platform social travel app built with **Kotlin Multiplatform + Compose Multiplatform**, targeting **Android** and **Web (wasmJs)**.

Users create a profile with interests, browse a map of events in cities they want to visit, join events, create their own, and connect with people who share similar interests. Think "Meetup for spontaneous travelers who don't know anyone in the city."

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform 1.10.3 |
| Shared logic | Kotlin Multiplatform 2.3.20 |
| Backend | Supabase (PostgreSQL + Auth + Storage + Realtime) |
| Networking | Ktor Client 3.4.1 |
| Serialization | kotlinx.serialization 1.8.0 |
| Image loading | Coil3 3.1.0 |
| Navigation | Navigation Compose 2.9.2 |
| Coroutines | kotlinx.coroutines 1.10.1 |

---

## Getting Started

### 1. Clone the repo

```bash
git clone <repo-url>
cd Hackitall-GIP
```

### 2. Set up environment variables

Copy `.env.example` to `.env` and fill in your Supabase credentials:

```bash
cp .env.example .env
```

```env
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your_anon_public_key_here
```

Find these in your Supabase dashboard → **Project Settings → API**.

> `.env` is gitignored — never commit it. `SupabaseConfig.kt` is generated at build time from these values.

### 3. Set up Supabase RLS policies

Run the following in **Supabase → SQL Editor**:

```sql
-- Profiles
create policy "Profiles are publicly readable" on profiles for select using (true);
create policy "Users can insert own profile" on profiles for insert with check (auth.uid() = id);
create policy "Users can update own profile" on profiles for update using (auth.uid() = id);

-- Interests (read-only, seeded)
create policy "Interests are publicly readable" on interests for select using (true);

-- Profile interests
create policy "Users can manage own interests" on profile_interests
  for all using (auth.uid() = profile_id);

-- Events
create policy "Public events are readable" on events for select using (is_public = true);
create policy "Authenticated users can create events" on events for insert with check (auth.uid() = creator_id);
create policy "Creators can update own events" on events for update using (auth.uid() = creator_id);

-- Event participants
create policy "Participants are readable" on event_participants for select using (true);
create policy "Users can join events" on event_participants for insert with check (auth.uid() = profile_id);
create policy "Users can leave events" on event_participants for delete using (auth.uid() = profile_id);

-- Travel groups
create policy "Open groups are readable" on travel_groups for select using (is_open = true);
create policy "Authenticated users can create groups" on travel_groups for insert with check (auth.uid() = creator_id);

-- Travel group members
create policy "Members are readable" on travel_group_members for select using (true);
create policy "Users can join groups" on travel_group_members for insert with check (auth.uid() = profile_id);
create policy "Users can leave groups" on travel_group_members for delete using (auth.uid() = profile_id);

-- Friendships
create policy "Users can see own friendships" on friendships for select
  using (auth.uid() = requester_id or auth.uid() = receiver_id);
create policy "Users can send friend requests" on friendships for insert
  with check (auth.uid() = requester_id);
create policy "Users can update friendship status" on friendships for update
  using (auth.uid() = receiver_id);
create policy "Users can remove friendships" on friendships for delete
  using (auth.uid() = requester_id or auth.uid() = receiver_id);
```

### 4. Run the web app

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Open `http://localhost:8080`.

### 5. Run the Android app

```bash
./gradlew :composeApp:assembleDebug
```

Or use the run configuration in Android Studio / IntelliJ.

---

## Project Structure

```
composeApp/src/commonMain/kotlin/org/example/project/
├── data/
│   ├── model/                  # Data classes mirroring DB tables
│   │   ├── Profile.kt          # profiles table + ProfileUpdate
│   │   ├── Interest.kt         # interests table
│   │   ├── ProfileInterest.kt  # profile_interests junction
│   │   ├── Event.kt            # events table + EventInsert + EventParticipant
│   │   ├── TravelGroup.kt      # travel_groups + TravelGroupInsert + TravelGroupMember
│   │   ├── Friendship.kt       # friendships table + FriendshipStatusUpdate
│   │   └── ProfileUpdate.kt    # partial update payload for profiles
│   ├── remote/
│   │   ├── SupabaseClient.kt   # Supabase singleton (Auth, Postgrest, Storage, Realtime)
│   │   └── SupabaseConfig.kt   # GENERATED at build time from .env — do not edit
│   └── repository/
│       ├── AuthRepository.kt   # sign up, sign in, sign out, session
│       ├── UserRepository.kt   # profile CRUD, avatar upload, interests
│       ├── EventRepository.kt  # fetch/create/join/leave events
│       ├── TravelGroupRepository.kt  # group management
│       └── FriendshipRepository.kt   # friend requests, accept, block
├── viewmodel/
│   ├── AuthViewModel.kt        # auth state + session flow
│   ├── ProfileViewModel.kt     # profile + interests state
│   ├── EventViewModel.kt       # events list, selected event, participants
│   ├── TravelGroupViewModel.kt # groups list, members, group events
│   └── FriendshipViewModel.kt  # friends, pending requests, status checks
└── App.kt                      # temporary auth smoke-test screen
```

---

## Architecture

**MVVM** with a strict layering rule:

```
Screen (Composable)
  └── ViewModel  — holds StateFlow, calls repository
        └── Repository  — only layer that touches Supabase
              └── Supabase (Ktor HTTP under the hood)
```

- Screens observe `StateFlow` and recompose on change
- ViewModels wrap all repository calls in `runCatching` — never throw into the UI
- Repositories return data directly and throw on error
- `SupabaseConfig` is injected at build time from `.env` so keys never enter source control

---

## Database Schema

| Table | Description |
|---|---|
| `profiles` | Public user profiles, extends `auth.users` via trigger |
| `interests` | Seeded list of 10 interests (Art, Music, Climbing, etc.) |
| `profile_interests` | Many-to-many: user ↔ interests |
| `events` | Events with location (lat/lng), date, max participants, linked interests |
| `event_participants` | Many-to-many: user ↔ events, with join status |
| `travel_groups` | Groups travelling to a destination, open or closed |
| `travel_group_members` | Many-to-many: user ↔ groups, with role (admin/member) |
| `friendships` | Directional friendship with status (pending/accepted/blocked) |

Storage bucket: `avatars` (public) — path format: `{user_id}/avatar.jpg`

---

## What's Done

- [x] KMP project setup (Android + wasmJs targets)
- [x] All dependencies configured (Supabase, Ktor, Coil3, Navigation, Coroutines)
- [x] `.env`-based secret injection via Gradle — no keys in source
- [x] All data models with `@Serializable` + `@SerialName` matching DB schema
- [x] `SupabaseClient` singleton with Auth, Postgrest, Storage, Realtime
- [x] `AuthRepository` + `AuthViewModel`
- [x] `UserRepository` + `ProfileViewModel`
- [x] `EventRepository` + `EventViewModel`
- [x] `TravelGroupRepository` + `TravelGroupViewModel`
- [x] `FriendshipRepository` + `FriendshipViewModel`
- [x] Temporary auth test screen (sign up / sign in / sign out)

## What's Next

- [ ] App theme (colors, typography, dark/light)
- [ ] Navigation skeleton (bottom nav + routes)
- [ ] Auth screens (Login, Register, Interest picker)
- [ ] Explore screen (map + event list)
- [ ] Event detail screen
- [ ] Profile screen
- [ ] Travel group screens
- [ ] Friends screen
