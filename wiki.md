## Creating Custom Particles

Particle configs go in `src/main/resources/assets/inventory_particles/iparticles/` directory with `.json` or `.json5` extension.

### File Structure

```
src/main/resources/assets/inventory_particles/
├── iparticles/                 ← Particle configs (.json or .json5)
│   ├── drip/
│   │   ├── water_drip.json
│   │   ├── lava_drip.json5
│   │   └── ...
│   └── ...
├── textures/iparticles/        ← Particle texture atlases (*.png)
│   ├── drip/
│   │   ├── water_drip_0.png
│   │   ├── water_drip_1.png
│   │   └── ...
│   └── ...
└── textures/spawn_areas/       ← Spawn area masks (*.png)
    ├── standard.png
    ├── lava_bucket_drip.png
    └── ...
```

Texture references in configs use the `inventory_particles:` namespace prefix. Example: `"inventory_particles:drip/water_drip_0.png"` refers to `textures/iparticles/drip/water_drip_0.png`.

---

## — Main Options —

### `life_time` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/required.png?raw=true" width="60px" alt="status"/>

> Type: `Integer` | Example: `300` | Positive only

Specifies the lifetime of the particle in ticks (game ticks, where 20 ticks = 1 second).

---

### `animation_type` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/optional.png?raw=true" width="60px" alt="status"/>

> Type: `String` | Example: `random_static` | Default Value: `random`

Defines how the particle's textures animate over time.

Possible values:
- `"stretch"`: Plays each texture to fit the `life_time`.
- `"onetime"`: Plays an animation for the duration of `life_time` at the speed of `animation_speed`, and then dies.
- `"loop"`: Loops the animation continuously.
- `"random"`: Chooses a random texture each time for the duration of `animation_speed`.
- `"random_static"`: Chooses a random texture once when the particle spawns and keeps it static.

---

### `animation_speed` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/optional.png?raw=true" width="60px" alt="status"/>

> Type: `Double` | Example: `1.0` | Default Value: `1.0` | Positive only

Controls the speed of the texture animation in game ticks. `1.0` is normal speed, `2.0` is twice as fast, `0.5` is half speed.

---

### `size` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/optional.png?raw=true" width="60px" alt="status"/>

> Type: `Object` | Example: `{"width": 16, "height": 16}` | Default Value: `{"width": 8, "height": 8}`

Defines the size of the particle. Can be static or dynamic:

#### Static Size:
```json
{
  "width": 8.0,
  "height": 8.0
}
```

#### Dynamic Size:
```json
"size": {
  {
    "sizes": {
      "0": {"width": 4.0, "height": 4.0},
      "50": {"width": 8.0, "height": 8.0},
      "100": {"width": 4.0, "height": 4.0}
    },
    "interpolation": "linear_interpolation"
  }
}
```
- `sizes`: An object that stores other tick-size objects.
- - `tick-size object`
- - - The key is always a number indicating the tick at which the size should be set (taking into account `interpolation`)
- - - The value is similar to a static size.
- `interpolation`: Type of interpolation between sizes. Possible: `ease_in_sine`, `ease_in_expo`, and other from https://nicmulvaney.com/easing/

---

### `textures` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/required.png?raw=true" width="60px" alt="status"/>

> Type: `String Array` | Example: `["inventory_particles:drip/water_drip_0.png"]`

List of strings that indticates texture paths for your particle. Can be item texture or atlas texture:

#### Item Texture
```json
"textures": [
	"minecraft:stick"
],
```
- Renders provided item as particle's texture
- Use tooltip with F3+H to get your item identifier

#### Standard (Mod) Atlas Texture
```json
"textures": [
	"inventory_particles:void/void_1.png"
],
```
- Renders texture from mod's atlas. Every texture must be in `textures/iparticles/..`

#### Another Atlas Texture
```json
"textures": [
	{
		"atlas": "minecraft:decorated_pot",
		"sprite": "the_name_of_sprite_from_this_atlas"
	}
],
```
- Renders texture from another atlas. Every texture must be in `textures/iparticles/..`

### `holders` <img src="https://github.com/LopyMine/PatPat/blob/master/img/wiki/required.png?raw=true" width="60px" alt="status"/>

> Type: `Array of Objects` | Example: See below | Default Value: `[]`

List of items that trigger particle spawning. Each holder defines when and where particles appear on an item. At least one holder is required for particles to spawn.

#### Holder Fields:

##### `name`

> Type: `String` | Example: `"Water Bucket Drip"` | Default Value: Auto-generated

Optional name for the holder, used for debugging. Helps identify configurations in logs.

##### `item`

> Type: `String` | Example: `"minecraft:water_bucket"` or `"#minecraft:buckets"` | Required

Item ID or tag to trigger particles:
- Item ID: `"minecraft:water_bucket"` — spawns particles only on this item.
- Tag: `"#minecraft:buckets"` — spawns particles on all items in the tag (use `#` prefix).

##### `nbt_conditions_match`

> Type: `String` | Example: `"any"` | Default Value: `"any"`

How to combine NBT conditions when multiple are specified:
- `"all"`: All conditions must match.
- `"any"`: At least one condition must match.
- `"none"`: None of the conditions must match.

##### `nbt_conditions`

> Type: `Array of Objects` | Example: See structure below | Default Value: `[]`

Conditions based on item's NBT data. Allows particles only on items with specific enchantments, damage, or custom data. Each object represents an NBT node to check.

###### NBT Condition Fields:
- `this_name`: Name of the NBT tag to check (e.g., `"Damage"`, `"Enchantments"`, `"CustomModelData"`).
- `this_type`: Type of the tag: `"int"`, `"string"`, `"list"`, `"compound"`, `"double"`, `"float"`, `"long"`.
- `check_value`: Array of values to match against. String comparison.
- `next_match` (optional): How to combine nested conditions (`"all"`, `"any"`, `"none"`).
- `next` (optional): Array of nested conditions for deeper NBT paths.

**Example — Enchanted items only:**
```json
"nbt_conditions": [
  {
    "this_name": "Enchantments",
    "this_type": "list",
    "check_value": [""]
  }
]
```

**Example — Damaged tools:**
```json
"nbt_conditions": [
  {
    "this_name": "Damage",
    "this_type": "int",
    "check_value": ["0"],
    "next_match": "none"
  }
]
```
This shows particles only if Damage ≠ 0.

**Example — Custom data (nested):**
```json
"nbt_conditions": [
  {
    "this_name": "tag",
    "this_type": "compound",
    "check_value": [""],
    "next": [
      {
        "this_name": "example_key",
        "this_type": "string",
        "check_value": ["example_value"]
      }
    ]
  }
]
```

##### `spawn_area`

> Type: `String` | Example: `"lava_bucket_drip.png"` | Default Value: `"standard.png"`

Name of a PNG file in the `textures/spawn_areas/` directory. The image acts as a mask:
- **White pixels** (`#FFFFFF`) — particles spawn here.
- **Black pixels** (`#000000`) — particles do not spawn.
- **Gray pixels** — partial spawn chance (transparency).

Size of the mask determines spawn region on the item. Load the item texture as reference, then draw white areas where you want particles.

**Example files:**
- `"standard.png"` — uniform spawning across entire item.
- `"bucket_drip.png"` — spawning only at bucket bottom edge.
- `"spawn_egg.png"` — spawning on spawn egg surface.

##### `spawn_count`

> Type: `Array of Integers` | Example: `[1, 2]` | Default Value: `[0, 0]`

Range of particles spawned per event: `[min, max]`. A random integer between min and max is chosen each spawn.

- `[0, 0]` — no particles spawn (default, likely unintended).
- `[1, 1]` — exactly 1 particle each spawn.
- `[1, 3]` — 1 to 3 particles randomly each spawn.
- `[2, 5]` — 2 to 5 particles for dense effect.

##### `spawn_frequency`

> Type: `Array of Integers` | Example: `[20, 80]` | Default Value: `[0, 0]`

Ticks between spawn events: `[min, max]`. A random interval is chosen each time a particle spawns, determining when the next spawn occurs (20 ticks = 1 second).

- `[20, 40]` — spawns every 1–2 seconds.
- `[10, 80]` — spawns every 0.5–4 seconds (varied effect).
- `[80, 80]` — spawns every 4 seconds (fixed rate).

##### `color`

> Type: `String` or `Object` | Example: `"#FF0000"` | Default Value: `""`

Tints the particle. Options:

**Static color (hex):**
```json
"color": "#FF0000"
```
Red particles. Use 6-digit hex code.

**White (default):**
```json
"color": ""
```
No tint applied.

**From item NBT:**
```json
"color": "nbt"
```
Reads color from item's `display.color` NBT (used by leather armor, dyed items). If not present, defaults to white.

**List of colors (animated/random):**
```json
"color": "nbt_list"
```
Reads color list from item NBT and cycles through or randomly picks colors.

##### `speed_coefficient`

> Type: `Double` | Example: `0.2` | Default Value: `0.0`

Multiplier for particle physics speed. Affects `impulse`, `acceleration`, and `max` values in physics.

- `0.0` — no additional speed (particles move only by physics base settings).
- `0.3` — particles move 30% faster than configured.
- `1.0` — full speed as configured.
- `-0.5` — negative multiplier reverses movement direction (rare).

#### Holder Example — Complete:

```json
"holders": [
  {
    "name": "Lava Bucket Drip",
    "item": "minecraft:lava_bucket",
    "spawn_area": "lava_bucket_drip.png",
    "spawn_count": [1, 2],
    "spawn_frequency": [20, 80],
    "speed_coefficient": 0.2,
    "color": "#FF6600",
    "nbt_conditions_match": "any",
    "nbt_conditions": []
  },
  {
    "name": "Enchanted Tools Glint",
    "item": "#c:tools",
    "spawn_area": "standard.png",
    "spawn_count": [0, 2],
    "spawn_frequency": [30, 60],
    "speed_coefficient": 0.1,
    "color": "",
    "nbt_conditions_match": "all",
    "nbt_conditions": [
      {
        "this_name": "Enchantments",
        "this_type": "list",
        "check_value": [""]
      }
    ]
  }
]
```

### `physics`

> Type: `Object` | Example: See structure below | Default Value: No movement (static particles)

Defines how particles move and rotate over their lifetime. Both `base` (movement) and `rotation` subsections are optional.

#### `base`

Movement physics for the particle in 2D space. Controls X/Y motion and angular rotation.

##### `x_speed`

> Type: `Object` | Controls horizontal (left-right) movement.

- `impulse`: Initial speed range `[min, max]` in pixels/tick. Applied once at spawn.
- `impulse_bidirectional`: If `true`, impulse can be positive or negative (left or right). If `false`, always positive.
- `acceleration`: Constant acceleration in pixels/tick². Applied every tick.
- `acceleration_bidirectional`: If `true`, acceleration can flip direction each tick.
- `max_acceleration`: Maximum acceleration magnitude `[min, max]`.
- `max`: Maximum speed magnitude `[min, max]`.
- `braking`: Deceleration per tick (0–1). Reduces speed each frame. `0.05` = 5% slowdown per tick.
- `turbulence`: Random speed variation `[min, max]` applied per tick (like wind).
- `cursor_impulse_inherit_coefficient`: How much particle inherits cursor movement (0–1). `1.0` = full inheritance.

**Example — still horizontal:**
```json
"x_speed": {
  "impulse": [0.0, 0.0],
  "impulse_bidirectional": false,
  "acceleration": 0.0,
  "acceleration_bidirectional": false,
  "max_acceleration": [-100.0, 100.0],
  "max": [-100.0, 100.0],
  "braking": 0.0,
  "turbulence": [0.0, 0.0],
  "cursor_impulse_inherit_coefficient": 1.0
}
```

**Example — drifting horizontally:**
```json
"x_speed": {
  "impulse": [-0.5, 0.5],
  "impulse_bidirectional": true,
  "acceleration": 0.0,
  "braking": 0.02,
  "turbulence": [-0.1, 0.1],
  "cursor_impulse_inherit_coefficient": 1.0
}
```
Random initial drift ±0.5 px/tick, slight turbulence, gentle braking.

##### `y_speed`

> Type: `Object` | Controls vertical (up-down) movement. Same structure as `x_speed`.

Common use: gravity/falling motion.

**Example — gravity (dripping):**
```json
"y_speed": {
  "impulse": [0.0, 0.0],
  "impulse_bidirectional": false,
  "acceleration": 0.3,
  "acceleration_bidirectional": false,
  "max_acceleration": [-100.0, 100.0],
  "max": [-100.0, 100.0],
  "braking": 0.0,
  "turbulence": [-0.1, 0.1],
  "cursor_impulse_inherit_coefficient": 1.0
}
```
Constant downward acceleration (gravity), small turbulence to add randomness. Used in milk/lava drips.

**Example — floating upward:**
```json
"y_speed": {
  "impulse": [0.1, 0.3],
  "impulse_bidirectional": false,
  "acceleration": -0.1,
  "braking": 0.05,
  "turbulence": [0.0, 0.0]
}
```
Particles spawn with upward velocity, gradually slow and fall back down.

##### `angle_speed`

> Type: `Object` | Controls rotation of the particle in degrees per tick.

- `impulse`: Rotation speed at spawn `[min, max]` degrees/tick.
- `impulse_bidirectional`: If `true`, can spin clockwise or counter-clockwise.
- All other fields same as `x_speed`.

**Example — spinning particle:**
```json
"angle_speed": {
  "impulse": [2.0, 5.0],
  "impulse_bidirectional": true,
  "acceleration": 0.0,
  "braking": 0.0,
  "turbulence": [0.0, 0.0]
}
```
Particle rotates 2–5 degrees per tick in random direction.

#### `rotation`

Rotation settings for the visual representation (particle body and texture).

##### `particle`

> Type: `Object` | Rotates the entire particle sprite.

- `spawn_angle`: Initial rotation `[min, max]` in degrees (0–360).
- `rotate_in_movement_direction`: If `true`, particle rotates to face its movement direction (auto-orient).
- `speed`: Speed config (same structure as `x_speed`) controlling rotation speed.

**Example — static orientation:**
```json
"particle": {
  "spawn_angle": [0.0, 0.0],
  "rotate_in_movement_direction": false,
  "speed": {
    "impulse": [0.0, 0.0],
    "acceleration": 0.0,
    "braking": 0.0,
    "turbulence": [0.0, 0.0]
  }
}
```
Particle never rotates, always upright (0°).

**Example — auto-orient to movement:**
```json
"particle": {
  "spawn_angle": [0.0, 0.0],
  "rotate_in_movement_direction": true,
  "speed": { ... }
}
```
Particle rotates to face the direction it's moving (like a falling leaf).

##### `texture`

> Type: `Object` | Rotates only the texture inside the particle (particle frame stays still).

Same structure as `particle`. Useful for spinning effects without moving the sprite boundary.

**Example — spinning texture:**
```json
"texture": {
  "spawn_angle": [0.0, 360.0],
  "rotate_in_movement_direction": false,
  "speed": {
    "impulse": [1.0, 3.0],
    "impulse_bidirectional": true,
    "acceleration": 0.0,
    "braking": 0.0,
    "turbulence": [0.0, 0.0]
  }
}
```
Texture starts at random angle, spins 1–3 degrees/tick in either direction.

#### Physics Example — Complete (Falling Leaf):

```json
"physics": {
  "base": {
    "x_speed": {
      "impulse": [-0.3, 0.3],
      "impulse_bidirectional": true,
      "acceleration": 0.0,
      "braking": 0.05,
      "turbulence": [-0.1, 0.1],
      "cursor_impulse_inherit_coefficient": 1.0
    },
    "y_speed": {
      "impulse": [0.0, 0.1],
      "impulse_bidirectional": false,
      "acceleration": 0.15,
      "braking": 0.0,
      "turbulence": [-0.05, 0.05],
      "cursor_impulse_inherit_coefficient": 1.0
    },
    "angle_speed": {
      "impulse": [1.0, 3.0],
      "impulse_bidirectional": true,
      "acceleration": 0.0,
      "braking": 0.0,
      "turbulence": [0.0, 0.0]
    }
  },
  "rotation": {
    "particle": {
      "spawn_angle": [0.0, 360.0],
      "rotate_in_movement_direction": false,
      "speed": {
        "impulse": [0.0, 0.0],
        "acceleration": 0.0,
        "braking": 0.0,
        "turbulence": [0.0, 0.0]
      }
    },
    "texture": {
      "spawn_angle": [0.0, 360.0],
      "rotate_in_movement_direction": false,
      "speed": {
        "impulse": [1.0, 2.0],
        "impulse_bidirectional": true,
        "acceleration": 0.0,
        "braking": 0.0,
        "turbulence": [0.0, 0.0]
      }
    }
  }
}
```
Result: Particle drifts side to side, accelerates downward (gravity), rotates particle and texture for organic falling leaf effect.

---

## Tips and Best Practices

### Testing Particles In-Game
1. Create your config file in `iparticles/` folder.
2. Place particle texture files in `textures/iparticles/`.
3. Place spawn area masks in `textures/spawn_areas/` (if using custom masks).
4. Reload resource pack in-game (F3+T in vanilla, or mod reload).
5. Hold the item in your inventory to see particles.

Use `/ip particles reload` command (if available) to reload configs without full resource pack reload.

### Common Issues

**Particles don't appear:**
- Verify `spawn_count` is not `[0, 0]` (default).
- Check `spawn_frequency` is not `[0, 0]`.
- Confirm item ID or tag in `holders.item` matches your inventory item.
- Verify texture path is correct and file exists.

**Textures look wrong:**
- Check texture path ends with `.png`.
- Verify texture atlas file exists in `textures/iparticles/`.
- Use 16×16 or 32×32 PNG files for best results.
- Ensure alpha channel is enabled for transparency.

**Spawn area mask doesn't work:**
- Verify spawn area file exists in `textures/spawn_areas/`.
- Use pure white (`#FFFFFF`) for active areas, pure black (`#000000`) for inactive.
- Test with `"standard.png"` first to confirm other settings work.

**Physics don't feel right:**
- Start with `acceleration: 0.3` for gravity-like effect.
- Use `braking: 0.05` to slow particles down over time.
- Test `turbulence: [-0.1, 0.1]` for randomness without chaos.
- Adjust `speed_coefficient` in holder to scale all physics uniformly.

### Performance Tips
- Keep `spawn_count` low (1–2) to avoid lag on weak systems.
- Use `spawn_frequency` with longer gaps (20+ ticks) for smoother effect.
- Reuse textures across multiple particles to save memory.
- Avoid complex physics (many speed configs) on frequently-spawned items.

### Texture Guidelines
- Use semi-transparent PNG for soft edges and blending.
- Match texture style to item (sparkles for gems, drips for liquids).
- Provide multiple frames (3–5) for `random` or `loop` animation types.
- Keep filesize small; 64×64 PNG is usually sufficient.

### NBT Condition Examples

**Enchanted items only:**
```json
"nbt_conditions_match": "any",
"nbt_conditions": [
  {
    "this_name": "Enchantments",
    "this_type": "list",
    "check_value": [""]
  }
]
```

**Specific damage range:**
```json
"nbt_conditions_match": "all",
"nbt_conditions": [
  {
    "this_name": "Damage",
    "this_type": "int",
    "check_value": ["0"],
    "next_match": "none"
  }
]
```

**Specific custom model data:**
```json
"nbt_conditions": [
  {
    "this_name": "CustomModelData",
    "this_type": "int",
    "check_value": ["12345"]
  }
]
```
