       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'SQLINIT'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'ayar.cpy'.
       copy 'carhar.cpy'.
       copy 'depo.cpy'.
       copy 'hareket.cpy'.
       copy 'katlog.cpy'.
       copy 'plasiyer.cpy'.
       copy 'sicil.cpy'.
       copy 'sicilkod.cpy'.
       copy 'sicilpos.cpy'.
       copy 'siciltel.cpy'.
       copy 'stok.cpy'.
       copy 'tipkod.cpy'.
       copy 'users.cpy'.
       copy 'access.cpy'.
       copy 'makbuz.cpy'.
       copy 'odeme.cpy'.
       copy 'ceksenet.cpy'.
       copy 'sayim.cpy'.
       copy 'kasa.cpy'.
       copy 'tip.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'ayar.cpz'.
       copy 'carhar.cpz'.
       copy 'depo.cpz'.
       copy 'hareket.cpz'.
       copy 'katlog.cpz'.
       copy 'plasiyer.cpz'.
       copy 'sicil.cpz'.
       copy 'sicilkod.cpz'.
       copy 'sicilpos.cpz'.
       copy 'siciltel.cpz'.
       copy 'stok.cpz'.
       copy 'tipkod.cpz'.
       copy 'users.cpz'.
       copy 'access.cpz'.
       copy 'makbuz.cpz'.
       copy 'odeme.cpz'.
       copy 'ceksenet.cpz'.
       copy 'sayim.cpz'.
       copy 'kasa.cpz'.
       copy 'tip.cpz'.
       WORKING-STORAGE SECTION.
       01 TARIH.
           02 YIL       PIC 9999.
           02 AY        PIC 99.
           02 GUN       PIC 99.
	   77 DUR    PIC X.
       copy 'status.cpy'.
       PROCEDURE DIVISION.
       BASLA.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==AYAR==
      *                            "%T%"  BY "AYAR"
      *                           ==%R%== BY ==AY-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==CARHAR==
      *                            "%T%"  BY "CARHAR"
      *                           ==%R%== BY ==CH-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==DEPO==
      *                            "%T%"  BY "DEPO"
      *                           ==%R%== BY ==DP-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==HAREKET-SICIL==
      *                            "%T%"  BY "HAREKET-SICIL"
      *                           ==%R%== BY ==HS-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==HAREKET-DETAY==
      *                            "%T%"  BY "HAREKET-DETAY"
      *                           ==%R%== BY ==HD-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==KATLOG==
      *                            "%T%"  BY "KATLOG"
      *                           ==%R%== BY ==KT-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==PLASIYER==
      *                            "%T%"  BY "PLASIYER"
      *                           ==%R%== BY ==PL-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==SICIL==
      *                            "%T%"  BY "SICIL"
      *                           ==%R%== BY ==SC-KAYIT==.
       copy sqlinit.cpy REPLACING ==%T%== BY ==SICILKOD==
                                  "%T%"  BY "SICILKOD"
                                 ==%R%== BY ==SK-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==SICILPOS==
      *                            "%T%"  BY "SICILPOS"
      *                          ==%R%== BY ==SP-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==SICILTEL==
      *                           "%T%"  BY "SICILTEL"
      *                          ==%R%== BY ==SCT-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==STOK==
      *                             "%T%"  BY "STOK"
      *                           ==%R%== BY ==ST-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==STOKADET==
      *                            "%T%"  BY "STOKADET"
      *                           ==%R%== BY ==STA-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==PASSTOK==
      *                            "%T%"  BY "PASSTOK"
      *                           ==%R%== BY ==PAS-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==STOKBAG==
      *                            "%T%"  BY "STOKBAG"
      *                           ==%R%== BY ==SB-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==TIPKOD==
      *                            "%T%"  BY "TIPKOD"
      *                           ==%R%== BY ==TK-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==USERS==
      *                            "%T%"  BY "USERS"
      *                           ==%R%== BY ==US-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==USERACCESS==
      *                            "%T%"  BY "USERACCESS"
      *                           ==%R%== BY ==UA-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==MAKBUZ==
      *                            "%T%"  BY "MAKBUZ"
      *                           ==%R%== BY ==MK-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==ODEME==
      *                            "%T%"  BY "ODEME"
      *                           ==%R%== BY ==OD-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==CEKSENET==
      *                            "%T%"  BY "CEKSENET"
      *                           ==%R%== BY ==CS-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==CEKSENETLOG==
      *                            "%T%"  BY "CEKSENETLOG"
      *                           ==%R%== BY ==CSL-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==SAYIM==
      *                            "%T%"  BY "SAYIM"
      *                           ==%R%== BY ==SY-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==KASA==
      *                            "%T%"  BY "KASA"
      *                           ==%R%== BY ==KS-KAYIT==.
      * copy sqlinit.cpy REPLACING ==%T%== BY ==DTIP==
      *                            "%T%"  BY "DTIP"
      *                           ==%R%== BY ==DTIP-KAYIT==.
	       ACCEPT DUR NO BEEP.
           EXIT PROGRAM.
           STOP RUN.
           
            
