       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'USERADD'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'users.cpy'.
       copy 'access.cpy'.
              SELECT USEROKU ASSIGN TO DISK, 'USERS.CFG'
                ORGANIZATION LINE SEQUENTIAL.
              SELECT ACCESSOKU ASSIGN TO DISK, 'ACCESS.CFG'
                ORGANIZATION LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       copy 'users.cpz'.
       copy 'access.cpz'.
       FD USEROKU.
       01 UO-KAYIT.
           02 UO-NAME             PIC X(20).
           02                     PIC X.
           02 UO-PASS             PIC X(20).
           02                     PIC X.
           02 UO-ACTIVE           PIC 9.
       FD ACCESSOKU.
       01 AO-KAYIT.
           02 AO-NAME             PIC X(20).
           02                     PIC X.
           02 AO-CODE             PIC X(50).
           02                     PIC X.
           02 AO-READ             PIC 9.
           02 AO-WRITE            PIC 9.
           02 AO-UPDATE           PIC 9.
           02 AO-DELETE           PIC 9.
       WORKING-STORAGE SECTION. 
       copy 'status.cpy'.
       copy 'fileop.var'.
       PROCEDURE DIVISION.
       BASLA.
           PERFORM TRUNCATE-USERS.
		   PERFORM TRUNCATE-USERACCESS.
		   OPEN I-O USERS USERACCESS.
           OPEN INPUT USEROKU ACCESSOKU.
           READ USEROKU NEXT. READ ACCESSOKU NEXT.
           READ USEROKU NEXT. READ ACCESSOKU NEXT.
       USER-AKTAR.
           MOVE SPACES TO UO-NAME UO-PASS.
           MOVE ZEROS TO UO-ACTIVE.
           READ USEROKU NEXT AT END GO USER-AKTAR-SON.
           IF UO-NAME = SPACES GO USER-AKTAR.
           MOVE UO-NAME TO US-NAME.
           MOVE UO-PASS TO US-PASS.
           MOVE UO-ACTIVE TO US-ACTIVE.
           PERFORM WRITE-USERS.
           GO USER-AKTAR.
       USER-AKTAR-SON.
       
       ACCESS-AKTAR.
           MOVE SPACES TO AO-NAME AO-CODE.
           MOVE ZEROS TO AO-READ AO-WRITE AO-UPDATE AO-DELETE.
           READ ACCESSOKU NEXT AT END GO STOP-PROGRAM.
           IF AO-NAME = SPACES GO ACCESS-AKTAR.
           MOVE AO-NAME TO UA-NAME.
           MOVE AO-CODE TO UA-CODE.
           MOVE AO-READ TO UA-READ.
           MOVE AO-WRITE TO UA-WRITE.
           MOVE AO-UPDATE TO UA-UPDATE.
           MOVE AO-DELETE TO UA-DELETE.
           PERFORM WRITE-USERACCESS.
           GO ACCESS-AKTAR.
       STOP-PROGRAM.
           CLOSE USERS USERACCESS.
           CLOSE USEROKU ACCESSOKU.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==USERS==
                                  "%T%"  BY "USERS"
                                 ==%R%== BY ==US-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==USERACCESS==
                                  "%T%"  BY "USERACCESS"
                                 ==%R%== BY ==UA-KAYIT==.							
           