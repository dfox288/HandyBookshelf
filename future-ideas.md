# Handy Bookshelf — Future Ideas

Feature proposals from two perspectives: a technical/redstone player and a builder/decorator.

## Consensus Picks (independently proposed by both)

### Enchantment-Level Color-Coded Glint
Tint the glint overlay based on enchantment power/rarity:
- Level I-II: default vanilla purple shimmer
- Level III-IV: blue/cyan shifted
- Level V: gold/amber
- Treasure enchants (Mending, Swift Sneak, Soul Speed): crimson/red

Implementation: modulate vertex color in `renderOpaqueQuad` (currently hardcoded white). Add `slotGlintColor` to render state. Low-medium effort. Needs a policy for multi-enchant books (use highest level?).

### Library Index Mode (Show All Labels at Once)
Keybind-toggled mode where ALL enchanted slots within range show their labels simultaneously, not just the aimed one. Smaller text (~60% scale), compact format. Like a card catalog for your enchanting room.

Implementation: in `extractRenderState`, populate `slotName[]` for all enchanted slots when mode is active. Add keybind via Fabric's `KeyBindingHelper`. Medium effort. Main challenge is visual clutter with many labels — needs distance fading and label count cap.

## Quick Wins

### Show Name Tags for All Items
Currently only enchanted books, written books, and custom-named items show labels. Extend to all non-empty slots (regular books show "Book", quills show "Book and Quill"). Literally removing an `else if` condition. Trivial. Add config toggle for "show all" vs "only named/enchanted".

### Glint Pulse / Breathing Animation
Gentle opacity oscillation (70%-100% over 3-4 seconds) with per-slot phase offset based on block position hash. Keeps the shimmer feeling alive rather than static. Low effort — sine wave on the alpha in `extractRenderState`, new `slotGlintAlpha` float array.

## Medium Effort

### Enchanting Particle Ambiance
Enchanted slots occasionally emit floating enchanting-table glyph particles (~1 per 3 seconds per slot). Handled via `ClientTickEvents.END_WORLD_TICK`, not the BER. Needs particle density config slider (0-200%) and search radius cap (16-24 blocks) for performance.

### Multi-Enchantment Count Badge
Small number ("3") rendered in the corner of slots containing multi-enchant books. Visible without aiming — fills the info gap between glint (yes/no) and name tag (full details). Uses `submitText` at smaller scale, positioned at bottom-right of slot bounds. Only shown when count >= 2.

### Slot Occupancy Indicator (3/6)
Shows how many slots are filled per shelf. Useful for finding shelves with room in large libraries. Can work client-only from blockstate (`slot_N_occupied`) without the mixin. Low-medium effort.

## Ambitious

### "Find My Enchantment" Search/Highlight Mode
Hold an enchanted book in hand, and all bookshelves containing the same enchantment highlight with a distinct color. Avoids needing a custom UI — uses item-in-hand as the query. High effort: needs client-side scan of loaded block entities, new render state for search highlights, keybind to toggle.

### Enchantment Category Icons
Small 8x8 sprites next to name tag text showing enchantment category (sword for damage, shield for protection, pickaxe for tools, etc.). Needs custom sprite sheet texture and enchantment-to-category mapping. Medium-high effort.

### Comparator Signal Preview
When holding a redstone comparator, show the current signal strength (1-6) on the bookshelf face. Bridges decorative and redstone uses. Medium-high effort — need to verify `lastInteractedSlot` is synced via the existing mixin.

### Redstone Comparator Power Output
Total enchantment levels drive comparator signal strength instead of slot count. Server-side mixin. Amazing for adventure map puzzles but gameplay-altering — must default to OFF. High effort, scope-creep risk.
