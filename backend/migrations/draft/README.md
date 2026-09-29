# Draft migrations

Not wired. The app still uses GORM `AutoMigrate` for the existing tables (`organizations`, `users`,
`user_organizations`, `sicils`, `sicil_telefons`); these files add the planned contexts and move to a
real migration tool when F1 starts. Provisional; revised against the live-system inventory.

Apply in order: `0001_common.sql` … `0006_migration.sql`. `sicils`, `users` and `organizations` must exist first.
