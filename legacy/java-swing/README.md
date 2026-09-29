# java-swing (~2015–)

Desktop client used next to the COBOL ERP: order entry, pending orders, purchasing, warehouse transfers, stock count, stock cards, labels, exchange rates, statements, cheque/note and receipt lists.

Swing with a small in-house component set (`tr.fikopar.swing`), one controller/model/view/report package per module, Hibernate 4 on MySQL, JasperReports. `db/models/cobol` maps the tables mirrored from COBOL (read-only); `db/models/windows` are tables owned by this app.

Connection settings are `${FIKOPAR_DB_*}` placeholders in `src/META-INF/hibernate.cfg.xml`. It won't build as-is; see [DEPENDENCIES.md](DEPENDENCIES.md).
