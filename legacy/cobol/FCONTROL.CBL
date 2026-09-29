       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'FCONTROL'.
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
           
       copy 'status.cpy'.
	   copy 'fileop.var'.
       PROCEDURE DIVISION.
       DECLARATIVES.
       I-O-MAIN-ERROR SECTION.
           USE AFTER STANDARD EXCEPTION PROCEDURE ON CARHAR DEPO
            KATLOG PLASIYER SICIL SICILKOD SICILPOS SICILTEL STOK
            STOKARA TIPKOD USERS STOKADET HAREKET-SICIL MAKBUZ
            HAREKET-DETAY HAREKET-INDEX USERACCESS ODEME PASSTOK
            CEKSENET CEKSENETLOG STOKBAG SAYIM KASA AYAR DTIP.
       I-O-AYAR-ERROR-ROUTINE.
           IF ST-CARHAR = '35' OPEN OUTPUT CARHAR CLOSE CARHAR.
           IF ST-AYAR = '35' OPEN OUTPUT AYAR 
           MOVE SPACES TO AY-EVRAKNO
           PERFORM VARYING AY-DEPONO FROM 1 BY 1 UNTIL AY-DEPONO = 3
            PERFORM VARYING AY-TIP FROM 1 BY 1 UNTIL AY-TIP = 22
            EVALUATE AY-TIP
             WHEN 1 WHEN 2 WHEN 3
                     EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'A{6}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'Z-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Mal Satçü òülemi' TO AY-ACIKLAMA
             WHEN 4 WHEN 5 WHEN 6
                     EVALUATE AY-DEPONO
                     WHEN 1 MOVE SPACES TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'A-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Mal Alçü òülemi' TO AY-ACIKLAMA
             WHEN 7 WHEN 8 WHEN 9
                     EVALUATE AY-DEPONO
                     WHEN 1 MOVE SPACES TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'I-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Mal òade Äçkçü òülemi' TO AY-ACIKLAMA
             WHEN 10 WHEN 11 WHEN 12
                     EVALUATE AY-DEPONO
                     WHEN 1 MOVE SPACES TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'I-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Mal òade Alçü òülemi' TO AY-ACIKLAMA
             WHEN 13 EVALUATE AY-DEPONO
                     WHEN 1 MOVE SPACES TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'F-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Fiyat Farkç Äçkçü òülemi' TO AY-ACIKLAMA
             WHEN 14 EVALUATE AY-DEPONO
                     WHEN 1 MOVE SPACES TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'F-{YIL}{6}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'EVRAK.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'Fiyat Farkç Alçü òülemi' TO AY-ACIKLAMA
             WHEN 15 MOVE SPACES TO AY-EVRAKFORMAT
                     MOVE 'EKSTRE.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'CARò HESAP KARTI EKSTRESò' TO AY-ACIKLAMA
             WHEN 16 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'TA{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'TA{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'MAKBUZ.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'TAHSòLAT MAKBUZU' TO AY-ACIKLAMA
             WHEN 17 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'TE{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'TE{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'MAKBUZ.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'TEDòYE MAKBUZU' TO AY-ACIKLAMA
             WHEN 18 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'AC{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'AC{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'CEKSENET.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'ALACAK ÄEKò òûLEMò' TO AY-ACIKLAMA
             WHEN 19 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'BC{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'BC{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'CEKSENET.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'BORÄ ÄEKò òûLEMò' TO AY-ACIKLAMA
             WHEN 20 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'AS{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'AS{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'CEKSENET.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'ALACAK SENETò òûLEMò' TO AY-ACIKLAMA
             WHEN 21 EVALUATE AY-DEPONO
                     WHEN 1 MOVE 'BS{YIL}0{8}' TO AY-EVRAKFORMAT
                     WHEN 2 MOVE 'BS{YIL}9{8}' TO AY-EVRAKFORMAT
                     END-EVALUATE
                     MOVE 'CEKSENET.DUZ' TO AY-YAZDOS
                     MOVE 'LPT2:' TO AY-YAZPORT
                     MOVE 'BORÄ SENETò òûLEMò' TO AY-ACIKLAMA
            END-EVALUATE
			PERFORM WRITE-AYAR
            END-PERFORM
           END-PERFORM
           CLOSE AYAR.
           IF ST-DEPO = '35' OPEN OUTPUT DEPO 
              MOVE ZEROS TO DP-NO MOVE '(BOû)' TO DP-ADI
              PERFORM WRITE-DEPO
              MOVE 1 TO DP-NO MOVE '1.HESAP' TO DP-ADI
              PERFORM WRITE-DEPO
              MOVE 2 TO DP-NO MOVE '2.HESAP' TO DP-ADI
              PERFORM WRITE-DEPO
              CLOSE DEPO.
           IF ST-HAREKET-SICIL = '35' OPEN OUTPUT HAREKET-SICIL 
                                      CLOSE HAREKET-SICIL.
           IF ST-HAREKET-DETAY = '35' OPEN OUTPUT HAREKET-DETAY
                                      CLOSE HAREKET-DETAY.
           IF ST-HAREKET-INDEX = '35' OPEN OUTPUT HAREKET-INDEX
                                      CLOSE HAREKET-INDEX.
           IF ST-USERACCESS = '35' OPEN OUTPUT USERACCESS
                                   CLOSE USERACCESS.
           IF ST-KATLOG = '35' OPEN OUTPUT KATLOG CLOSE KATLOG.
           IF ST-PLASIYER = '35' OPEN OUTPUT PLASIYER
                MOVE ZEROS TO PL-NO MOVE '(BOû)' TO PL-ADI
                PERFORM WRITE-PLASIYER
                CLOSE PLASIYER.
           IF ST-SICIL = '35' OPEN OUTPUT SICIL CLOSE SICIL.
           IF ST-SICILKOD = '35' OPEN OUTPUT SICILKOD CLOSE SICILKOD.
           IF ST-SICILPOS = '35' OPEN OUTPUT SICILPOS CLOSE SICILPOS.
           IF ST-SICILTEL = '35' OPEN OUTPUT SICILTEL CLOSE SICILTEL.
           IF ST-STOK = '35' OPEN OUTPUT STOK CLOSE STOK.
           IF ST-PASSTOK = '35' OPEN OUTPUT PASSTOK CLOSE PASSTOK.
           IF ST-STOKARA = '35' OPEN OUTPUT STOKARA CLOSE STOKARA.
           IF ST-TIPKOD = '35' OPEN OUTPUT TIPKOD 
                MOVE ZEROS TO TK-KOD MOVE '(BOû)' TO TK-TIP
                PERFORM WRITE-TIPKOD
                CLOSE TIPKOD.
           IF ST-USERS = '35' OPEN OUTPUT USERS CLOSE USERS.
           IF ST-STOKADET = '35' OPEN OUTPUT STOKADET CLOSE STOKADET.
           IF ST-STOKARA = '35' OPEN OUTPUT STOKARA CLOSE STOKARA.
           IF ST-MAKBUZ = '35' OPEN OUTPUT MAKBUZ CLOSE MAKBUZ.
           IF ST-ODEME = '35' OPEN OUTPUT ODEME 
            MOVE ZEROS TO OD-NO MOVE '(BOû)' TO OD-ADI 
				PERFORM WRITE-ODEME
            MOVE 1 TO OD-NO MOVE 'Peüin' TO OD-ADI PERFORM WRITE-ODEME
            CLOSE ODEME.
           IF ST-CEKSENET = '35' OPEN OUTPUT CEKSENET CLOSE CEKSENET.
           IF ST-CEKSENETLOG = '35' OPEN OUTPUT CEKSENETLOG 
                    CLOSE CEKSENETLOG.
           IF ST-STOKBAG = '35' OPEN OUTPUT STOKBAG CLOSE STOKBAG.
           IF ST-SAYIM = '35' OPEN OUTPUT SAYIM CLOSE SAYIM.
           IF ST-KASA = '35' OPEN OUTPUT KASA CLOSE KASA.
           IF ST-DTIP = '35' OPEN OUTPUT DTIP
            PERFORM VARYING DTIP-NO FROM 1 BY 1 UNTIL DTIP-NO = 22
            EVALUATE DTIP-NO
            WHEN 1  MOVE 'òRSALòYELò SATIû FATURASI' TO DTIP-ADI
            WHEN 2  MOVE 'SATIû òRSALòYESò' TO DTIP-ADI
            WHEN 3  MOVE 'SATIû FATURASI' TO DTIP-ADI
            WHEN 4  MOVE 'òRSALòYELò ALIû FATURASI' TO DTIP-ADI
            WHEN 5  MOVE 'ALIû òRSALòYESò' TO DTIP-ADI
            WHEN 6  MOVE 'ALIû FATURASI' TO DTIP-ADI
            WHEN 7  MOVE 'òRSALòYELò òADE ÄIKIû FATURASI' TO DTIP-ADI
            WHEN 8  MOVE 'òADE ÄIKIû òRSALòYESò' TO DTIP-ADI
            WHEN 9  MOVE 'òADE ÄIKIû FATURASI' TO DTIP-ADI
            WHEN 10 MOVE 'òRSALòYELò òADE ALIû FATURASI' TO DTIP-ADI
            WHEN 11 MOVE 'òADE ALIû òRSALòYESò' TO DTIP-ADI
            WHEN 12 MOVE 'òADE ALIû FATURASI' TO DTIP-ADI
            WHEN 13 MOVE 'SATIû FARK FATURASI' TO DTIP-ADI
            WHEN 14 MOVE 'ALIû FARK FATURASI' TO DTIP-ADI
            WHEN 15 MOVE 'CARò HESAP KARTI EKSTRESò' TO DTIP-ADI
            WHEN 16 MOVE 'TAHSòLAT MAKBUZU' TO DTIP-ADI
            WHEN 17 MOVE 'TEDòYE MAKBUZU' TO DTIP-ADI
            WHEN 18 MOVE 'ALACAK ÄEKò òûLEMò' TO DTIP-ADI
            WHEN 19 MOVE 'BORÄ ÄEKò òûLEMò' TO DTIP-ADI
            WHEN 20 MOVE 'ALACAK SENETò òûLEMò' TO DTIP-ADI
            WHEN 21 MOVE 'BORÄ SENETò òûLEMò' TO DTIP-ADI
            END-EVALUATE
			PERFORM WRITE-DTIP
			END-PERFORM
            CLOSE DTIP.
           GO I-O-AYAR-ERROR-ROUTINE-SON.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==CARHAR==
                                  "%T%"  BY "CARHAR"
                                 ==%R%== BY ==CH-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==DEPO==
                                  "%T%"  BY "DEPO"
                                 ==%R%== BY ==DP-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-SICIL==
                                  "%T%"  BY "HAREKET-SICIL"
                                 ==%R%== BY ==HS-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-DETAY==
                                  "%T%"  BY "HAREKET-DETAY"
                                 ==%R%== BY ==HD-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==KATLOG==
                                  "%T%"  BY "KATLOG"
                                 ==%R%== BY ==KT-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==PLASIYER==
                                  "%T%"  BY "PLASIYER"
                                 ==%R%== BY ==PL-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==SICIL==
                                  "%T%"  BY "SICIL"
                                 ==%R%== BY ==SC-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==SICILKOD==
                                  "%T%"  BY "SICILKOD"
                                 ==%R%== BY ==SC-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==SICILPOS==
                                  "%T%"  BY "SICILPOS"
                                 ==%R%== BY ==SP-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==SICILTEL==
                                  "%T%"  BY "SICILTEL"
                                 ==%R%== BY ==SCT-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==STOK==
                                  "%T%"  BY "STOK"
                                 ==%R%== BY ==ST-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==PASSTOK==
                                  "%T%"  BY "PASSTOK"
                                 ==%R%== BY ==PAS-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==STOKBAG==
                                  "%T%"  BY "STOKBAG"
                                 ==%R%== BY ==SB-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==TIPKOD==
                                  "%T%"  BY "TIPKOD"
                                 ==%R%== BY ==TK-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==USERS==
                                  "%T%"  BY "USERS"
                                 ==%R%== BY ==US-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==USERACCESS==
                                  "%T%"  BY "USERACCESS"
                                 ==%R%== BY ==UA-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==MAKBUZ==
                                  "%T%"  BY "MAKBUZ"
                                 ==%R%== BY ==MK-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==ODEME==
                                  "%T%"  BY "ODEME"
                                 ==%R%== BY ==OD-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==CEKSENET==
                                  "%T%"  BY "CEKSENET"
                                 ==%R%== BY ==CS-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==CEKSENETLOG==
                                  "%T%"  BY "CEKSENETLOG"
                                 ==%R%== BY ==CSL-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==SAYIM==
                                  "%T%"  BY "SAYIM"
                                 ==%R%== BY ==SY-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==KASA==
                                  "%T%"  BY "KASA"
                                 ==%R%== BY ==KS-KAYIT==.
	   copy fileop.cpy REPLACING ==%T%== BY ==DTIP==
                                  "%T%"  BY "DTIP"
                                 ==%R%== BY ==DTIP-KAYIT==.			
       I-O-AYAR-ERROR-ROUTINE-SON.
       END DECLARATIVES.
       MAIN SECTION.
       START-PROGRAM.
           ACCEPT TARIH FROM CENTURY-DATE.
           OPEN INPUT AYAR. CLOSE AYAR.
           OPEN INPUT CARHAR. CLOSE CARHAR.
           OPEN INPUT DEPO. CLOSE DEPO.
           OPEN INPUT HAREKET-SICIL. CLOSE HAREKET-SICIL.
           OPEN INPUT HAREKET-DETAY. CLOSE HAREKET-DETAY.
           OPEN INPUT HAREKET-INDEX. CLOSE HAREKET-INDEX.
           OPEN INPUT USERACCESS. CLOSE USERACCESS.
           OPEN INPUT KATLOG. CLOSE KATLOG.
           OPEN INPUT PLASIYER. CLOSE PLASIYER.
           OPEN INPUT SICILKOD. CLOSE SICILKOD.
           OPEN INPUT SICILPOS. CLOSE SICILPOS.
           OPEN INPUT SICILTEL. CLOSE SICILTEL.
           OPEN INPUT SICIL. CLOSE SICIL.
           OPEN INPUT STOK. CLOSE STOK.
           OPEN INPUT PASSTOK. CLOSE PASSTOK.
           OPEN INPUT STOKARA. CLOSE STOKARA.
           OPEN INPUT TIPKOD. CLOSE TIPKOD.
           OPEN INPUT USERS. CLOSE USERS.
           OPEN INPUT STOKADET. CLOSE STOKADET.
           OPEN INPUT STOKARA. CLOSE STOKARA.
           OPEN INPUT MAKBUZ. CLOSE MAKBUZ.
           OPEN INPUT ODEME. CLOSE ODEME.
           OPEN INPUT CEKSENET. CLOSE CEKSENET.
           OPEN INPUT CEKSENETLOG. CLOSE CEKSENETLOG.
           OPEN INPUT STOKBAG. CLOSE STOKBAG.
           OPEN INPUT SAYIM. CLOSE SAYIM.
           OPEN INPUT KASA. CLOSE KASA.
           OPEN INPUT DTIP. CLOSE DTIP.
           EXIT PROGRAM.
           STOP RUN.
	       
            
