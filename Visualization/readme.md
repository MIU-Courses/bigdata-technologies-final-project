# Tableau Visualization
## Set up to Connect Tableau to HBase

1.	Download and Install Tableau Desktop
Link: https://www.tableau.com/products/desktop/download 
2.	Download and Install  HBase ODBC Driver
Link: https://www.cdata.com/drivers/hbase/download/odbc/
3.	To connect HBase, In part “To a Server” click “more”, then select “Other Databases (ODBC)”. In tab “Connect Using”, choose “Driver” then select “CData ODBC Driver for Apache HBase” . Click “Connect” then input Server (Check this information from VMWare or Docker – REST HBase, in my case is 127.0.0.1) and Port(Check this information from VMWare or Docker – REST HBase, in my case is 8070) in tab “Connection Attributes”.
4.	To public in cloud, From Dashboard Screen, Choose File -> Share -> login to Tableau Cloud  












