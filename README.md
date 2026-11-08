# Icy's Lib

Shared code for my mods. Right now that means guidebooks: each mod ships its own book art and a
`book.json`, and the lib draws it.

## Using it

```groovy
repositories {
    mavenLocal {
        content { includeGroup 'icy.lib' }
    }
}

dependencies {
    implementation "icy.lib:icys_lib-neoforge-${minecraft_version}:${icys_lib_version}"
}
```

and in `neoforge.mods.toml`:

```toml
[[dependencies.${mod_id}]]
modId="icys_lib"
type="required"
versionRange="[${icys_lib_version},)"
ordering="AFTER"
side="BOTH"
```

Run `gradlew publishToMavenLocal` here first.

## Books

A book `mymod:guide` lives at `assets/mymod/icys_lib/books/guide/`:

- `book.json`: canvas size, texture folder, text style and the layer list
- `entries/<category>/<name>.json`: content the `list`, `choice` and `items` layers read

Layers are drawn in order. Art can be full-canvas PNGs with only the piece opaque; the lib finds each
piece's bounds and click mask from its alpha, so most layers need no coordinates.

Layer types: `image`, `button`, `choice`, `list`, `text`, `items`. Any layer takes `when`
(`{"tab": "structures"}`, a list of values, or `""` for unset) and `unless`, the same shape, which
hides the layer when any listed key matches. Texture and text strings take
`${key}` from the book's state, with a `fallback`.

A `choice` takes its options from an `options` map, or from entries: give it a `category`, an optional
`where` and an `option` sprite like `"mob_${mob}_selected"`, filled in with each entry's name.

A `text` with `"on": <sprite>` places its `area` relative to that sprite's bounds and is skipped when the
sprite is missing, so a label can follow art that moves with the state.

`book.json` can take a `bend`: `top` is a list of `[x, drop]` points tracing how far the page's top edge
dips below flat, and `fade` is the `[y, y]` range over which that dip eases to nothing. Text (letter by
letter) and items then follow the page curve. Give a `text` or `items` layer `"bend": false` to keep it
straight, e.g. text that sits on a piece of art.

A `list` lays its entries out along `step`; give it `columns` to wrap them into a grid and `scale` to
draw its frames and icons smaller or larger. Give a grid `rows` and only that many rows show, with the
mouse wheel moving through the rest a row at a time. A `text` with a `scrollbar` scrolls too, a couple of
lines per notch, instead of cutting off what doesn't fit. Either takes
`"scrollbar": {"area": [x, y, w, h], "track": "#RRGGBB", "thumb": "#RRGGBB"}`, drawn only when there's
more to see. A `recipe` layer
draws an entry's `recipe` field (nine slots, each an item id, a list of ids to cycle through, or `""`) into
a 3x3 grid at `at` with `cell` spacing, puts the entry's icon item at `result`, and draws its `art` only
when the entry has a recipe. Give it a `key` and it sets that state to `yes` or `""`, so other layers can
lay out differently for entries without one.

Items are drawn at the largest size up to their `size` whose pixels land on whole screen pixels, centered
in their box, so they stay sharp at every scale.

An entry's `icon` is an `item` (a list of ids cycles, in step with recipe slots) or a `texture` drawn at
item size.

In game: `/icys_lib book <id>` opens a book, `/icys_lib debug` outlines click areas and shows canvas
coordinates.
