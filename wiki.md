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

## — Config Options —

### `life_time` 

> Type: `Integer` | Example: `300` | Positive only

Specifies the lifetime of the particle in ticks (game ticks, where 20 ticks = 1 second).

---

### `animation_type` 

> Type: `String` | Example: `random_static` | Default Value: `random`

Defines how the particle's textures animate over time.

Possible values:
- `"stretch"`: Plays each texture to fit the `life_time`.
- `"onetime"`: Plays an animation for the duration of `life_time` at the speed of `animation_speed`, and then dies.
- `"loop"`: Loops the animation continuously.
- `"random"`: Chooses a random texture each time for the duration of `animation_speed`.
- `"random_static"`: Chooses a random texture once when the particle spawns and keeps it static.

---

### `animation_speed` 

> Type: `Double` | Example: `1.0` | Default Value: `1.0` | Positive only

Controls the speed of the texture animation in game ticks. `1.0` is normal speed, `2.0` is twice as fast, `0.5` is half speed.

---

### `size` 

> Type: `Object` | Example: `{"width": 16, "height": 16}` | Default Value: `{"width": 8, "height": 8}`

Defines the size of the particle. Can be static or dynamic:

#### Example — Static Size:
```json
{
  "width": 8.0,
  "height": 8.0
}
```

#### Example — Dynamic Size:
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

### `textures` 

> Type: `String Array` | Example: `["inventory_particles:drip/water_drip_0.png"]`

List of strings that indicates texture paths for your particle. Can be item texture or atlas texture:

#### Example — Item Texture
```json
"textures": [
	"minecraft:stick"
],
```
- Renders provided item as particle's texture
- Use tooltip with F3+H to get your item identifier

#### Example — Standard (Mod) Atlas Texture
```json
"textures": [
	"inventory_particles:void/void_1.png"
],
```
- Renders texture from mod's atlas. Every texture must be in `textures/iparticles/..`

#### Example — Another Atlas Texture
```json
"textures": [
	{
		"atlas": "minecraft:decorated_pot",
		"sprite": "the_name_of_sprite_from_this_atlas"
	}
],
```
- Renders texture from another atlas.

### `holders` 

> Type: `Array of Objects` | Example: See below | Default Value: `[]`

List of items that trigger particle spawning. Each holder defines when and where particles appear on an item. At least one holder is required for particles to spawn.

---

#### Holder Fields:

##### `name`

> Type: `String` | Example: `"Water Bucket Drip"` | Default Value: UnknownParticle@123456

Optional name for the holder, used for debugging. Helps identify configurations in logs.

##### `item`

> Type: `String` | Example: `"minecraft:water_bucket"` or `"#minecraft:buckets"` | Required

Item ID or tag to trigger particles:
- Item ID: `"minecraft:water_bucket"` — spawns particles only on this item.
- Tag: `"#minecraft:buckets"` — spawns particles on all items in the tag (use `#` prefix).

##### `spawn_area`

> Type: `String` | Example: `"lava_bucket_drip.png"` | Default Value: Just left top corner without offsets

Name of a PNG file in the `textures/spawn_areas/` directory. The image acts as a mask where particles may spawn.

Size of the mask determines spawn region on the item.

**Example files:**
- `"bucket_drip.png"` — spawning only at bucket bottom edge.
- `"spawn_egg.png"` — spawning on spawn egg surface.

##### `spawn_count`

> Type: `Array of Integers` | Example: `[1, 2]` | Default Value: `[0, 0]`

Range of particles spawned per spawn event: `[min, max]`. A random integer between min and max is chosen each spawn.

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

##### `speed_coefficient`

> Type: `Double` | Example: `0.2` | Default Value: `0.0`

Scales particle spawn count based on cursor movement speed. The formula is:
```
spawn_count = config_spawn_count * speed_coefficient * sqrt(cursor_speed)
```

This means particles spawn more frequently when the cursor moves faster:
- `0.0` — spawn count ignores cursor speed (static spawning).
- `0.2` — spawn count scales moderately with cursor speed.
- `1.0` — spawn count scales fully with cursor speed.

##### `color`

> Type: `String` or `Object` | Example: `"#FF0000"` | Default Value: `""`

Tints the particle. Options:

**Example — Static color (hex value, #AARRGGBB):**
```json
"color": "#FFFF0000"
```

This will color particle to red. Something like `"#DDFF0000"` will make particle little transparent.

**Example — White (default):**
```json
"color": ""
```
No tint applied.

**Example — From item NBT:**
```json
"color": "nbt"
```
Reads color from item's `display.color` NBT (used by leather armor, dyed items). If not present, defaults to white.

**Example — List of colors (animated/random):**
```json
"color": "nbt_list"
```
Reads color list from item NBT and cycles through or randomly picks colors.

> The difference between "nbt" and "nbt_list" lies in how the final color is obtained.
> "nbt" blends all available colors into one, whereas "nbt_list" leaves them as they are.

**Example — Advanced color modes (gradients, animations):**

Advanced color modes allow complex color animations by combining multiple colors with different interpolation modes.

```json
"color": {
  "mode": "gradient",
  "values": ["#FF000000", "#FFFF0000", "#FFFFFF00", "#FF00FF00", "#FF0000FF", "#FF000000"],
  "speed": 20
}
```

Available modes:
- `"random"`: Picks random color from `values` each tick.
- `"random_static"`: Picks random color once at spawn.
- `"gradient"`: Smoothly interpolates between colors in order. `speed` is the duration in ticks for full cycle.
- `"gradient_loop"`: Same as gradient but loops infinitely.
- `"gradient_bounce"`: Interpolates back and forth between colors.
- `"gradient_random_static"`: Gradient that starts at random position in the color sequence.
- `"mixed"`: Blends all colors together.

**Example — Rainbow gradient:**
```json
"color": {
  "mode": "gradient_loop",
  "values": ["#FFFF0000", "#FFFF7F00", "#FFFFFF00", "#FF00FF00", "#FF0000FF", "#FF8B00FF"],
  "speed": 40
}
```
Cycles through rainbow colors, taking 40 ticks for a full loop.

**Example — Fade in-out:**
```json
"color": {
  "mode": "gradient_bounce",
  "values": ["#00FFFFFF", "#FFFFFFFF"],
  "speed": 20
}
```
Fades from transparent white to opaque white and back, where the first hex digit is alpha (00=transparent, FF=opaque).

##### `nbt_conditions_match`

> Type: `String` | Example: `"all"` | Default Value: `"any"`

How to combine NBT conditions when multiple are specified:
- `"all"`: All conditions must match.
- `"any"`: At least one condition must match.
- `"none"`: None of the conditions must match.

##### `nbt_conditions`

> Type: `Array of Objects` | Example: See structure below | Default Value: `[]`

Conditions based on item's NBT data. Allows particles only on items with specific enchantments, damage, or custom data. Each object represents an NBT node to check.

###### NBT Node Fields:
- `this_name`: Name of the NBT tag to check (e.g., `"Damage"`, `"Enchantments"`, `"CustomModelData"`).
- `this_type`: Type of the NBT tag to check: `"int"`, `"string"`, `"list"`, `"compound"`, `"double"`, `"float"`, `"long"`.
- `check_value`: Array of values to match against. It uses string comparison.
- `next_match` (optional): How to combine nested conditions (`"all"`, `"any"`, `"none"`).
- `next` (optional): Array of nested conditions for deeper NBT paths.

**Example — Enchanted items only:**

Why it works: Not all items necessarily have the "Enchantments" tag.
If an item has any enchantment, it also has this tag.
Also, note that we are not checking for specific enchantments here.

```json
"nbt_conditions": [
  {
    "this_name": "Enchantments",
    "this_type": "list"
  }
]
```

**Example — Damaged tools:**

Here, we are checking the specific value of the NBT tag.
If the Damage value is 0, it returns true; if the value is 10, it returns false.
If the value is not of the int type, it also returns false.
If there is no value or tag at all, it also returns false.

```json
"nbt_conditions": [
  {
    "this_name": "Damage",
    "this_type": "int",
    "check_value": ["0"]
  }
]
```

**Example — More nbt nodes (nested):**

To return true here, it must find the "tag", then find in it the "example_key" (of type string), and only then check if the value is "example_value".
If something fails, it will return false.

```json
"nbt_conditions": [
  {
    "this_name": "tag",
    "this_type": "compound",
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

**Example — Specific custom model data:**
```json
"nbt_conditions": [
  {
    "this_name": "CustomModelData",
    "this_type": "int",
    "check_value": ["12345"]
  }
]
```

#### Example — Holder:

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
    "color": {
      "mode": "gradient_loop",
      "values": ["#FFFFFF00", "#FFFFFFFF", "#FF00FF00"],
      "speed": 20
    },
    "nbt_conditions_match": "all",
    "nbt_conditions": [
      {
        "this_name": "Enchantments",
        "this_type": "list"
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
- `impulse_bidirectional`: If `true`, impulse can be positive or negative (left or right). If `false`, always positive. In math, it basically does `randomValue(impulseRange) * random(1, -1)`
- `acceleration`: Constant acceleration in pixels/tick². Applied every tick.
- `acceleration_bidirectional`: If `true`, acceleration can flip direction each tick. In math, it basically does `acceleration * random(1, -1)`
- `max_acceleration`: Maximum acceleration speed `[min, max]`.
- `max`: Maximum global speed magnitude `[min, max]`.
- `braking`: Deceleration per tick (0–1). Reduces speed each frame. `0.05` = 5% slowdown per tick.
- `turbulence`: Random speed variation `[min, max]` applied per tick (like wind).
- `cursor_impulse_inherit_coefficient`: How much particle inherits cursor speed (0–1). `1.0` = full inheritance.

**Example — No particle movement:**
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

**Example — Drifting:**
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
It does random initial drift ±0.5 px/tick, slight turbulence, gentle braking.

##### `y_speed`

> Type: `Object` | Controls vertical (up-down) movement. Same structure as `x_speed`.

**Example — Gravity/Dripping:**
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

**Example — Floating upward:**
```json
"y_speed": {
  "impulse": [0.1, 0.3],
  "impulse_bidirectional": false,
  "acceleration": -0.1,
  "turbulence": [0.0, 0.0]
}
```
Particles spawn with upward velocity, gradually slow and fall back down.

##### `angle_speed`

> Type: `Object` | Controls particle movement direction via angle-based physics.

This applies speed in the direction the particle is facing (its angle). Uses trigonometry to convert angle into X/Y velocity components.

Fields used:
- `impulse`: Initial angle-based speed `[min, max]`. Applied at spawn.
- `impulse_bidirectional`: If `true`, impulse can be positive or negative.
- `acceleration`: Constant acceleration in the angle direction.
- `acceleration_bidirectional`: If `true`, acceleration can flip direction.
- `braking`: Deceleration applied to angle-based movement.
- `turbulence`: Random variation in angle-based speed.

Fields NOT used in angle_speed (ignored):
- `max_acceleration`
- `max`
- `cursor_impulse_inherit_coefficient`

**Example — movement in rotated direction:**
```json
"angle_speed": {
  "impulse": [1.0, 2.0],
  "impulse_bidirectional": false,
  "acceleration": 0.1,
  "braking": 0.02,
  "turbulence": [0.0, 0.0]
}
```
Particle moves in direction of its angle with 1–2 px/tick initial speed and slight acceleration. Combine with `particle.rotate_in_movement_direction: true` for particles that face their movement.

#### `rotation`

Rotation settings for the visual representation (particle body and texture).

##### `particle`

> Type: `Object` | Rotates the entire particle, where it actually looks.

- `spawn_angle`: Initial rotation `[min, max]` in degrees (0–360).
- `rotate_in_movement_direction`: If `true`, particle rotates to face its movement direction (auto-orient).
- `speed`: Speed config (same structure as `x_speed`) controlling rotation speed.

**Example — Static orientation:**
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

**Example — Auto-orient to movement:**
```json
"particle": {
  "spawn_angle": [0.0, 0.0],
  "rotate_in_movement_direction": true,
  "speed": { ... }
}
```
Particle rotates to face the direction it's moving (like a falling leaf).

##### `texture`

> Type: `Object` | Rotates only the particle sprite separate from particle itself.

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

#### Example — Physics:

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
5. Hold the item in your inventory to see particles. .
