# Dependencies

Removed from `lib/` (see `.classpath`):

| Library | Version | Coordinates |
|---|---|---|
| Hibernate ORM + c3p0 | 4.3.10.Final | `org.hibernate:hibernate-core`, `hibernate-c3p0` |
| c3p0 | 0.9.2.1 | `com.mchange:c3p0` |
| JasperReports | 6.1.0 | `net.sf.jasperreports:jasperreports` |
| Apache POI | 3.13 | `org.apache.poi:poi` |
| MySQL Connector/J | 5.1.36 | `mysql:mysql-connector-java` |
| SQLite JDBC | 3.7.2 | `org.xerial:sqlite-jdbc` |
| log4j | 1.2.15 | `log4j:log4j` |
| Barbecue | 1.5-beta1 | `net.sourceforge.barbecue:barbecue` |
| J/Invoke | 3.0.3 | commercial |
| JIDE Common Layer | | `com.jidesoft:jide-oss` |

Vendored sources removed: `swing/jdatepicker` (JDatePicker, BSD-2), `swing/treetable` and `test/` (JTreeTable sample, Sapient Public License), `swing/component/FDatepicker.java` (© Mark Lasby). Compiled `.jasper` files, the company logo and a sound file are also left out.
