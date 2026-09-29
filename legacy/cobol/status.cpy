       01 FILE-CONTROL-STATUS-VARS.
           02 ST-AYAR           PIC XX.
           02 ST-CARHAR         PIC XX.
           02 ST-DEPO           PIC XX.
           02 ST-HAREKET-SICIL  PIC XX.
           02 ST-HAREKET-DETAY  PIC XX.
           02 ST-HAREKET-INDEX  PIC XX.
           02 ST-KATLOG         PIC XX.
           02 ST-PLASIYER       PIC XX.
           02 ST-SICIL          PIC XX.
           02 ST-SICILKOD       PIC XX.
           02 ST-SICILPOS       PIC XX.
           02 ST-SICILTEL       PIC XX.
           02 ST-STOK           PIC XX.
           02 ST-TIPKOD         PIC XX.
           02 ST-USERACCESS     PIC XX.
           02 ST-USERS          PIC XX.
           02 ST-STOKADET       PIC XX.
           02 ST-STOKARA        PIC XX.
           02 ST-MAKBUZ         PIC XX.
           02 ST-ODEME          PIC XX.
           02 ST-PASSTOK        PIC XX.
           02 ST-CEKSENET       PIC XX.
           02 ST-CEKSENETLOG    PIC XX.
           02 ST-STOKBAG        PIC XX.
           02 ST-SAYIM          PIC XX.
           02 ST-KASA           PIC XX.
           02 ST-DTIP           PIC XX.
       01 TEMP-DOSYA.
           02 FILLER        PIC X(16) VALUE 'C:\WINDOWS\TEMP\'.
           02 TSAAT         PIC 9(8).
           02 FILLER        PIC X(4) VALUE '.TMP'.
       01 EXLS-PARAM.
           02 FILLER            PIC X(36) VALUE 
                'wscript.exe run.vbs "c:\php\php.exe '.
           02 EXLS-PHP          PIC X(15).
           02 FILLER            PIC X VALUE ' '.
           02 EXLS-DOSYA.
               03 FILLER        PIC X(16) VALUE 'C:\WINDOWS\TEMP\'.
               03 EXLS-TANIM    PIC X(5).
               03 FILLER        PIC X(3) VALUE '___'.
               03 EXLS-SAAT     PIC 9(8).
               03 FILLER        PIC X(4) VALUE '.XLS'.
           02 FILLER            PIC X VALUE '"'.
