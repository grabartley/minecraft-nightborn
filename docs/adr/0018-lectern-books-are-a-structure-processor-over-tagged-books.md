# ADR 0018: Lectern books are a structure processor over tagged books

## Context

Every lectern in a vampire structure must hold a written book, drawn at random from a shelf for that kind of structure (HOME-27). Books and shelves must be data a data pack can change (COMPAT-5), shared code must not know any one structure, the text must read in the player's language, and structures that already exist must not change.

The templates are written by the `tools/structures` generator (ADR 0017), which runs with no game and no registries. Most lecterns it places hold a blank book and quill, and the crypt's are empty.

## Decision

**A book is an entry in the `nightborn:lore_book` registry.** Each file under `data/<namespace>/nightborn/lore_book/` gives a `title`, an `author` and a list of `pages`. The title and pages are text components, so a data pack can write plain text or translation keys. The registry is not synced, because only the server builds the book.

**A shelf is a tag over that registry.** `data/nightborn/tags/nightborn/lore_book/crypt.json` lists the crypt's books, and so on for each kind. A tag lets a data pack add one book to a shelf without restating the rest, and one book can sit on several shelves.

**A structure processor fills the lectern.** `nightborn:lectern_book` names a shelf. When a piece is placed it turns every lectern block into one holding a book picked from that shelf, whatever the template put there, and leaves the lectern alone when the shelf is empty. Each kind has a processor list under `worldgen/processor_list/vampire/`, and every element of its template pools names that list. A processor runs only while a structure generates, so worlds that already hold a structure keep what they have. The templates keep their blank book as a fallback for a pack that removes the processor.

**The pick is seeded by where the lectern stands.** Vanilla gives a processor a random seeded from the block's position when the placement has none of its own. A structure is placed one chunk at a time and a piece can be placed more than once, so the pick must come out the same each time for the same lectern. Two lecterns in one house are picked separately and can match.

**The book is a vanilla written book.** Pages are translatable text, which the client resolves when the book is opened, so they read in the player's language with no client code. A written book's own title and author are plain strings and cannot be translated. The title the player sees is therefore set as the item's name, which is a text component and wins over the written title. The written title holds the server's English title cut to the 32 characters vanilla allows. The author stays a plain string: authors are proper names and are the same in every language.

## Consequences

- A new kind of structure gets books by adding a tag and a processor list. No code changes.
- A data pack can add, remove or rewrite books, move them between shelves, or point a structure at a different shelf.
- A client without the mod's language files would see translation keys. The mod is required on the client, so this does not happen in play.
- Author names are shown in Latin script in every language.
- The shipped pages are kept short enough to fit one page of the book screen in every language, and a test holds each page to a length limit. The limit is a character count, not the game's own line wrapping, so the final check is by eye in game.
- The generator's templates and its tests are untouched.
