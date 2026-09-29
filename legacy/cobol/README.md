# cobol (2010–)

Character-mode ERP in RM/COBOL: account cards, current-account movements, stock and catalogue, cash, cheques/notes, receipts, trial balance and statements. `MENU.CBL` is the entry point. Files are CP857 (`iconv -f CP857 -t UTF-8`).

- `*.CBL` programs, `*.cpy` FILE-CONTROL entries, `*.cpz` FD record layouts, `PROP/` print layouts, `LIB/` C helpers.
- `aktarma.php` watched the `.DAT` files and ran the matching `*_sqlaktar.php`, which decoded the RM/COBOL indexed file (`RMReader.php`) and reloaded a MySQL table. The PHP B2B portal and the Java Swing client read that copy; COBOL stayed the system of record.

Not included: `DATA/` (customer data), build output and side experiments (`mysql*/`, `address/`, `dumpvar/`), screenshots. DB access now comes from `FIKOPAR_*` env vars; `CALL "executesql"` credentials are `USER`/`PASS` placeholders.
