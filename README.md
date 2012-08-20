## geoimport - an automated GEO Import system in Scala, Play 2.0 and Akka
This application allows the user to configure automated downloads and
merges of various organisms from the Gene Expression Omnibus (GEO).

### Dependencies:

* Scala 2.9.2
* Play 2.0.2
* ScalaQuery 0.10 (automatically downloaded)
* isb-dataformats (included)
* MySQL

### Installation:

1. Download and install the recommended version of the Play framework
2. Clone or fork the current source code of geoimport
3. create a MySQL database geoimport and import the database file
   database/geoimport.sql (e.g. mysql -u root -p geoimport < geomimport.sql)
4. cd geoimport
5. enter "play" and then "run"

