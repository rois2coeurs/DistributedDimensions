# DistributedDimensions

A Minecraft plugin system that splits a world across **one Paper server per dimension**, coordinated by a Velocity proxy.

```
[Player] ──► [Velocity Proxy (dd-velocity)]
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
  [Overworld]  [Nether]   [End]
  (dd-paper)  (dd-paper) (dd-paper)
```

Crossing a portal feels identical to vanilla: inventory, XP, hunger, gamemode, and chat are preserved across all servers in real time.

---

## How it works

1. Player enters a portal → the Paper plugin cancels the vanilla event
2. The full player state (inventory, XP, hunger, gamemode…) is serialized and sent to Velocity via a plugin message channel
3. Velocity routes the player to the target server and forwards the message
4. The target server deserializes the state, finds a safe spawn location (or builds a portal), and teleports the player

Nether coordinate scaling (1:8 ratio) and End platform spawning are handled automatically.

---

## Modules

| Module | Description |
|---|---|
| `dd-common` | Shared library: `Dimension` enum, `LocationData`, `Channels`, `DimensionSwitchService` interface |
| `dd-paper` | Paper plugin — portal detection, state serialization, player teleportation, dimension guard |
| `dd-velocity` | Velocity plugin — player routing, state forwarding, cross-server chat |

- **Paper API:** 1.21.11 · **Java:** 21
- **Velocity API:** 3.5.0-SNAPSHOT

---

## Requirements

- Java 21
- A Velocity 3.x proxy
- Three Paper 1.21.11 servers (one per dimension)

---

## Build & deploy

```bash
make all      # build + deploy JARs to .dev/
make build    # mvn clean package only
make deploy   # copy JARs to .dev/*/plugins/
make start    # docker compose up -d
make stop     # docker compose stop
make logs     # docker compose logs -f
```

A ready-to-use Docker Compose environment is included in `.dev/`.  
Connect with a Minecraft 1.21.11 client on `localhost:25565`.

---

## Configuration

Each Paper server declares its dimension in `plugins/DistributedDimensions/config.yml`:

```yaml
world: OVERWORLD   # OVERWORLD | NETHER | END
debug: false
```

The Velocity plugin reads `plugins/distributed-dimensions/config.toml`:

```toml
debug = false
```

---

## Roadmap

- Potion effects not transferred on dimension switch
- Player data is in-memory only (reset on server reload)
- No timeout handling when target server is offline
- No payload validation on received plugin messages
