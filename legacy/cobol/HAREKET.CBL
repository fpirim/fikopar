       IDENTIFICATION DIVISION.
       PROGRAM-ID. 'HAREKET'.
       AUTHOR. FIKRET PIRIM.
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SOURCE-COMPUTER. RMCOBOL.
       OBJECT-COMPUTER. RMCOBOL.
       SPECIAL-NAMES.
           CLASS DIGIT IS "1234567890"
           DECIMAL-POINT IS COMMA.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
       copy 'hareket.cpy'.
       copy 'sicil.cpy'.
       copy 'stok.cpy'.
       copy 'carhar.cpy'.
       copy 'ayar.cpy'.
       copy 'katlog.cpy'.
       DATA DIVISION.
       FILE SECTION.
       copy 'hareket.cpz'.
       copy 'sicil.cpz'.
       copy 'stok.cpz'.
       copy 'carhar.cpz'.
       copy 'ayar.cpz'.
       copy 'katlog.cpz'.
       WORKING-STORAGE SECTION.
       01 THS-KAYIT.
           02 THS-KEY.
               03 THS-TIP                PIC 99.
               03 THS-EVRAKNO            PIC X(15).
           02 THS-TARIH.
               03 THS-DYIL               PIC 9999.
               03 THS-DAY                PIC 99.
               03 THS-DGUN               PIC 99.
           02 THS-SAAT.
               03 THS-SA                 PIC 99.
               03 THS-DD                 PIC 99.
               03 THS-SS                 PIC 99.                
           02 THS-FTARIH.
               03 THS-FYIL               PIC 9999.
               03 THS-FAY                PIC 99.
               03 THS-FGUN               PIC 99.
           02 THS-HESAPNO                PIC X(15).
           02 THS-HESAPADI               PIC X(50).
           02 THS-YETKILI                PIC X(40).
           02 THS-ADRES1                 PIC X(40).
           02 THS-ADRES2                 PIC X(40).
           02 THS-ADRES3                 PIC X(40).
           02 THS-MAHALLE                PIC X(20).
           02 THS-ILCE                   PIC X(20).
           02 THS-IL                     PIC X(20).
           02 THS-ULKE                   PIC X(20).
           02 THS-VDA                    PIC X(30).
           02 THS-VNO                    PIC X(10).
           02 THS-TCNO                   PIC X(11).
           02 THS-TSNO                   PIC X(11).
           02 THS-MERSIS                 PIC X(20).
           02 THS-GUN                    PIC 99999.
           02 THS-PLKOD                  PIC 9(4).
           02 THS-DEPONO                 PIC 9999.
           02 THS-DUZEN                  PIC 9(4).
           02 THS-BAG OCCURS 2 TIMES.
               03 THS-BAGTIPI                PIC 99.
               03 THS-BEVRAKNO               PIC X(15).
           02 THS-SONUC                 PIC 99.
        01 TST-KAYIT.
           02 TST-KEY.
               03 TST-KATNO              PIC 9999.
               03 TST-STNO               PIC X(15).
           02 TST-PRCNO                  PIC X(30).
           02 TST-OEMNO                  PIC X(30).
           02 TST-TIPI                   PIC X(30).
           02 TST-CINSI                  PIC X(60).
           02 TST-MARKA                  PIC X(30).
           02 TST-ADET                    PIC S9(12)V9999.
           02 TST-FIYAT                  PIC 9(10)V9999.
           
       01 SCREEN-VARIABLES.
           02 FLAG              PIC 9.
           02 F                 PIC 99.
           02 CIZ               PIC 99.
           02 LIS               PIC 99.
           02 SATIR             PIC 9999.
           02 ILK               PIC 99 VALUE 16.
           02 SON               PIC 99 VALUE 28.
           02 YENI              PIC 9.
           02 T-KOD             PIC S9999.
           02 T-ADI             PIC X(40).
           02 T-KDV             PIC 9(4)V9999.
           02 T-EVRAKNO         PIC X(15).
           02 T-HESAPNO         PIC X(15).
           02 DF-KDV            PIC 9(4)V9999.
           02 DF-ISKONTO1       PIC 9(4)V9999.
           02 DF-ISKONTO2       PIC 9(4)V9999.
           02 ZI-KK             PIC ZZ,ZZZZ.
           02 ZI-ADET           PIC ZZZZZ,ZZZZ.
           02 ZI-FIYAT          PIC ZZZZZZZZZZ,ZZZZ.
           02 SEC               PIC 9.
           02 BAG-ACIKLAMA      PIC X(40).
           02 LAST-DEPONO       PIC 9999 VALUE ZEROS.
           02 BS                PIC 99.
           
           
       01 INPUT-VARIABLES.
	       02 IZLE				PIC 9 VALUE 0.
	       02 SQLQUERY   		PIC X(1024).
           02 E-KATNO           PIC 9999.
           02 E-STNO            PIC X(30).
           02 E-PRCNO           PIC X(30).
           02 E-TIPI            PIC X(30).
           02 E-CINSI           PIC X(60).
           02 E-MARKA           PIC X(30).
           02 E-ISKONTO1        PIC 9(4)V9999.
           02 E-ISKONTO2        PIC 9(4)V9999.
           02 E-KDV             PIC 9(4)V9999.
           02 E-ADET            PIC 9(12)V9999.
           02 E-FIYAT           PIC 9(10)V9999.
           02 Z-ISKONTO1        PIC ZZ.
           02 Z-ISKONTO2        PIC ZZ.
           02 Z-KDV             PIC ZZ.
           02 Z-ADET            PIC ZZZ.ZZZ,ZZ.
           02 Z-FIYAT           PIC Z.ZZZ.ZZZ,ZZ.
           02 Z-TUTAR           PIC Z.ZZZ.ZZZ,ZZ.
           
       01 BILGI.
           02 B-KATNO           PIC ZZZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-STNO            PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-PRCNO           PIC X(11).
           02 FILLER            PIC X VALUE 'º'.
           02 B-TIPI            PIC X(10).
           02 FILLER            PIC X VALUE 'º'.
           02 B-CINSI           PIC X(28).
           02 FILLER            PIC X VALUE 'º'.
           02 B-MARKA           PIC X(7).
           02 FILLER            PIC X VALUE 'º'.
           02 B-ISKONTO1        PIC ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-ISKONTO2        PIC ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-KDV             PIC ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-ADET            PIC ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-FIYAT           PIC Z.ZZZ.ZZZ,ZZ.
           02 FILLER            PIC X VALUE 'º'.
           02 B-TUTAR           PIC Z.ZZZ.ZZZ,ZZ.
       
       77 G-TIP                 PIC 99.
       77 EVRAKNO               PIC X(15).
       77 GORUNTU               PIC 9.
       77 ISLEM                 PIC 9.
       77 DUR               PIC X.
       copy 'numara.var'.
       copy 'toplam.var'.
       copy 'onayvar.cpy'.
       copy 'status.cpy'.
       copy 'win.cpy'.
       LINKAGE SECTION.
       01  MAIN-ARGUMENT.
           02  MAIN-ARGUMENT-SIZE      PIC 9(4) BINARY.
           02  MAIN-ARGUMENT-VALUE.
               03  PIC X OCCURS 1 TO 100
                   DEPENDING ON MAIN-ARGUMENT-SIZE.
       SCREEN SECTION.
       01 SCR.
           02 LINE 1 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'É' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '»' HIGHLIGHT.
           02 LINE 2 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(118) FROM ALL ' ' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 3 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           02 LINE 4 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'EVRAK NO    [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 5 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'DEPO        [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 6 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'HESAP NO    [' HIGHLIGHT. 
           02 PIC X(15) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 7 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'HESAP ADI   [' HIGHLIGHT. 
           02 PIC X(50) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 8 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'YETK˜L˜     [' HIGHLIGHT. 
           02 PIC X(40) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 9 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'ADRES˜      [' HIGHLIGHT. 
           02 PIC X(40) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 10 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              '            [' HIGHLIGHT. 
           02 PIC X(40) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 11 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              '            [' HIGHLIGHT. 
           02 PIC X(40) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 12 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'V.DA˜RES˜   [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 ' - VERG˜ NO.[' HIGHLIGHT. 
           02 PIC X(10) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 ' - S˜C˜L NO.[' HIGHLIGHT. 
           02 PIC X(11) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 4 COLUMN 94 BACKGROUND BLUE FOREGROUND WHITE
              'DšZ. TAR˜H˜  [  /  /    ]' HIGHLIGHT. 
           02 LINE 5 COLUMN 94 BACKGROUND BLUE FOREGROUND WHITE
              'DšZ. SAAT˜   [  :  :  ]' HIGHLIGHT. 
           02 LINE 6 COLUMN 94 BACKGROUND BLUE FOREGROUND WHITE
              'VADE (GšN)   [' HIGHLIGHT. 
           02 PIC X(6) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 7 COLUMN 94 BACKGROUND BLUE FOREGROUND WHITE
              'F˜L.SEVK TAR.[  /  /    ]' HIGHLIGHT. 
           02 LINE 8 COLUMN 94 BACKGROUND BLUE FOREGROUND WHITE
              'DšZENLEYEN   [' HIGHLIGHT. 
           02 PIC X(10) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 8 COLUMN 57 BACKGROUND BLUE FOREGROUND WHITE
              ' MAHALLE/K™Y [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 9 COLUMN 57 BACKGROUND BLUE FOREGROUND WHITE
              ' ˜L€E        [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 10 COLUMN 57 BACKGROUND BLUE FOREGROUND WHITE
              ' žEH˜R       [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 11 COLUMN 57 BACKGROUND BLUE FOREGROUND WHITE
              ' šLKE        [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
          
           02 LINE 31 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'T.S˜C˜L NO. [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 32 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'MERS˜S NO.  [' HIGHLIGHT. 
           02 PIC X(20) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 33 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'BA¦.        [' HIGHLIGHT. 
           02 PIC X(40) FROM ALL ' '. 02 ']' HIGHLIGHT.
           02 LINE 34 COLUMN 3 BACKGROUND BLUE FOREGROUND WHITE
              'PLAS˜YER    [' HIGHLIGHT. 
           02 PIC X(30) FROM ALL ' '. 
           02 '] - HESAP BAK˜YES˜ [' HIGHLIGHT.
           02 PIC X(18) FROM ALL ' '. 02 ']' HIGHLIGHT.
           
           02 LINE 13 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT.
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(28) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT. 
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.  
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT. 02 'Ë' HIGHLIGHT.
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           
           02 LINE 15 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(28) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT. 
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.  
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT. 02 'Î' HIGHLIGHT.
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT.
           02 '¹' HIGHLIGHT.
           
           02 LINE 14 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'º' HIGHLIGHT. 
           02 PIC X(4) FROM 'KT.NO' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(7) FROM 'STOK NO' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(11) FROM ' PAR€A NO' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(10) FROM ' T˜P˜' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(28) FROM ' STOK C˜NS˜' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(7) FROM 'MARKASI' HIGHLIGHT.
           02 'º' HIGHLIGHT. 
           02 PIC X(2) FROM '1%' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(2) FROM '2%' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(2) FROM 'K%' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(10) FROM ' ADET' HIGHLIGHT.
           02 'º' HIGHLIGHT.  
           02 PIC X(12) FROM ' F˜YAT' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 PIC X(12) FROM ' TUTAR' HIGHLIGHT.
           02 'º' HIGHLIGHT.
           02 LINE 29 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'Ì' HIGHLIGHT. 
           02 PIC X(4) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(11) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(28) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(7) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT. 
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(2) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(10) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.  
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT. 02 'Ê' HIGHLIGHT.
           02 PIC X(12) FROM ALL 'Í' HIGHLIGHT. 
           02 '¹' HIGHLIGHT.
           02 LINE 31 COLUMN 91 BACKGROUND BLUE FOREGROUND WHITE
              'TOPLAM       : ' HIGHLIGHT.
           02 PIC X(14) FROM ALL ' ' REVERSE.
           02 LINE 32 COLUMN 91 BACKGROUND BLUE FOREGROUND WHITE
              '˜SKONTO      : ' HIGHLIGHT.
           02 PIC X(14) FROM ALL ' ' REVERSE.
           02 LINE 33 COLUMN 91 BACKGROUND BLUE FOREGROUND WHITE
              'ARA TOPLAM   : ' HIGHLIGHT.
           02 PIC X(14) FROM ALL ' ' REVERSE.
           02 LINE 34 COLUMN 91 BACKGROUND BLUE FOREGROUND WHITE
              'KDV TOPLAM   : ' HIGHLIGHT.
           02 PIC X(14) FROM ALL ' ' REVERSE.
           02 LINE 35 COLUMN 91 BACKGROUND BLUE FOREGROUND WHITE
              'GENEL TOPLAM : ' HIGHLIGHT. 
           02 PIC X(14) FROM ALL ' ' REVERSE.
           02 LINE 36 COLUMN 1 BACKGROUND BLUE FOREGROUND WHITE
              'È' HIGHLIGHT. 02 PIC X(118) FROM ALL 'Í' HIGHLIGHT.
           02 '¼' HIGHLIGHT. 
       01 SCR-TOPLAM.
           02 LINE 31 COLUMN 106 BACKGROUND WHITE FOREGROUND BLACK
               PIC ZZZ.ZZZ.ZZZ,ZZ USING I-TOPLAM-R.
           02 LINE 32 COLUMN 106 BACKGROUND WHITE FOREGROUND BLACK
               PIC ZZZ.ZZZ.ZZZ,ZZ USING I-ISKTOPLAM-R.
           02 LINE 33 COLUMN 106 BACKGROUND WHITE FOREGROUND BLACK
               PIC ZZZ.ZZZ.ZZZ,ZZ USING I-ARATOPLAM.
           02 LINE 34 COLUMN 106 BACKGROUND WHITE FOREGROUND BLACK
               PIC ZZZ.ZZZ.ZZZ,ZZ USING I-KDVTOPLAM-R.
           02 LINE 35 COLUMN 106 BACKGROUND WHITE FOREGROUND BLACK
               PIC ZZZ.ZZZ.ZZZ,ZZ USING I-GENELTOPLAM.
           02 LINE 34 COLUMN 66 BACKGROUND BLUE FOREGROUND BROWN
               PIC Z.ZZZ.ZZZ.ZZZ,ZZ USING T-BAKIYE HIGHLIGHT.
           02 ' '. 
           02 PIC X USING T-DURUM HIGHLIGHT.
           
       01 SCR-BASLIK.
           02 LINE 6 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(15) USING THS-HESAPNO HIGHLIGHT.
           02 LINE 7 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(50) USING THS-HESAPADI HIGHLIGHT.
           02 LINE 8 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(40) USING THS-YETKILI HIGHLIGHT.
           02 LINE 9 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(40) USING THS-ADRES1 HIGHLIGHT.
           02 LINE 10 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(40) USING THS-ADRES2 HIGHLIGHT.
           02 LINE 11 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(40) USING THS-ADRES3 HIGHLIGHT.
           02 LINE 12 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(30) USING THS-VDA HIGHLIGHT.
           02 LINE 12 COLUMN 60 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(10) USING THS-VNO HIGHLIGHT.
           02 LINE 12 COLUMN 84 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(11) USING THS-TCNO HIGHLIGHT.
           02 LINE 8 COLUMN 71 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-MAHALLE HIGHLIGHT.
           02 LINE 9 COLUMN 71 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-ILCE HIGHLIGHT.
           02 LINE 10 COLUMN 71 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-IL HIGHLIGHT.
           02 LINE 11 COLUMN 71 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-ULKE HIGHLIGHT.
           02 LINE 31 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-TSNO HIGHLIGHT.
           02 LINE 32 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                 PIC X(20) USING THS-MERSIS HIGHLIGHT.
           02 SCR-TARIH-AL.      
               03 LINE 4 COLUMN 108 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZ USING THS-DGUN HIGHLIGHT AUTO.
               03 LINE 4 COLUMN 111 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZ USING THS-DAY HIGHLIGHT AUTO.
               03 LINE 4 COLUMN 114 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZZZ USING THS-DYIL HIGHLIGHT AUTO.
           02 SCR-SAAT-AL.      
               03 LINE 5 COLUMN 108 BACKGROUND BLUE FOREGROUND WHITE
                 PIC 99 USING THS-SA HIGHLIGHT AUTO.
               03 LINE 5 COLUMN 111 BACKGROUND BLUE FOREGROUND WHITE
                 PIC 99 USING THS-DD HIGHLIGHT AUTO.
               03 LINE 5 COLUMN 114 BACKGROUND BLUE FOREGROUND WHITE
                 PIC 99 USING THS-SS HIGHLIGHT AUTO.
           02 SCR-GUN-AL.
               03 LINE 6 COLUMN 108 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZ.ZZZ USING THS-GUN HIGHLIGHT AUTO.
           02 SCR-FTARIH-AL.
               03 LINE 7 COLUMN 108 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZ USING THS-FGUN HIGHLIGHT AUTO.
               03 LINE 7 COLUMN 111 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZ USING THS-FAY HIGHLIGHT AUTO.
               03 LINE 7 COLUMN 114 BACKGROUND BLUE FOREGROUND WHITE
                 PIC ZZZZ USING THS-FYIL HIGHLIGHT AUTO.
           02 SCR-BAG.
               03 LINE 33 COLUMN 16 BACKGROUND BLUE FOREGROUND BROWN
                    PIC X(40) USING BAG-ACIKLAMA HIGHLIGHT.
       copy 'onayscr.cpy'.   
       PROCEDURE DIVISION USING MAIN-ARGUMENT.
       START-PROGRAM.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';' 
                INTO G-TIP, EVRAKNO, GORUNTU, ISLEM.
                
       BEGIN-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-CREATE'.
           PERFORM VARYING CIZ FROM 4 BY 1 UNTIL CIZ > 35
                DISPLAY 'º' LINE CIZ POSITION 1
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                SPACES SIZE 118 'º'
           END-PERFORM.
           DISPLAY SCR.
           
      * 01 - ˜RSAL˜YEL˜ SATIž FATURASI
      * 02 - SATIž ˜RSAL˜YES˜
      * 03 - SATIž FATURASI     
      * 
      * 04 - ˜RSAL˜YEL˜ ALIž FATURASI
      * 05 - ALIž ˜RSAL˜YES˜
      * 06 - ALIž FATURASI
      * 
      * 07 - ˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI
      * 08 - ˜ADE €IKIž ˜RSAL˜YES˜
      * 09 - ˜ADE €IKIž FATURASI
      *
      * 10 - ˜RSAL˜YEL˜ ˜ADE ALIž FATURASI
      * 11 - ˜ADE ALIž ˜RSAL˜YES˜
      * 12 - ˜ADE ALIž FATURASI
      *
      * 13 - SATIž FARK FATURASI
      * 14 - ALIž FARK FATURASI
      
           EVALUATE G-TIP
           WHEN 1 DISPLAY '˜RSAL˜YEL˜ SATIž FATURASI' LINE 2 COL 3
           WHEN 2 DISPLAY 'SATIž ˜RSAL˜YES˜' LINE 2 COL 3
           WHEN 3 DISPLAY 'SATIž FATURASI' LINE 2 COL 3
           WHEN 4 DISPLAY '˜RSAL˜YEL˜ ALIž FATURASI' LINE 2 COL 3
           WHEN 5 DISPLAY 'ALIž ˜RSAL˜YES˜' LINE 2 COL 3
           WHEN 6 DISPLAY 'ALIž FATURASI' LINE 2 POSITION 3
           WHEN 7 DISPLAY '˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI' LINE 2 COL 3
           WHEN 8 DISPLAY '˜ADE €IKIž ˜RSAL˜YES˜' LINE 2 COL 3
           WHEN 9 DISPLAY '˜ADE €IKIž FATURASI' LINE 2 COL 3
           WHEN 10 DISPLAY '˜RSAL˜YEL˜ ˜ADE ALIž FATURASI' LINE 2 COL 3
           WHEN 11 DISPLAY '˜ADE ALIž ˜RSAL˜YES˜' LINE 2 COL 3
           WHEN 12 DISPLAY '˜ADE ALIž FATURASI' LINE 2 COL 3
           WHEN 13 DISPLAY 'SATIž FARK FATURASI' LINE 2 COL 3
           WHEN 14 DISPLAY 'ALIž FARK FATURASI' LINE 2 COL 3
           END-EVALUATE.
           
           
      ********************************************************************
      *                                                                  *
      *    BAžLIK B™LšMšNE G˜R˜ž                                         *
      *                                                                  *
      ********************************************************************      
       BASLIK-AL.
           OPEN INPUT SICIL HAREKET-SICIL.
           
       EVRAKNO-AL.
           PERFORM VARYING CIZ FROM 16 BY 1 UNTIL CIZ > 28
                DISPLAY SPACES LINE CIZ POSITION 2 SIZE 4
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    'º' SPACES SIZE 7 'º' SPACES SIZE 11 'º'
                    SPACES SIZE 10 'º' SPACES SIZE 28 'º'
                    SPACES SIZE 7 'º' SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º' SPACES SIZE 10 'º'
                    SPACES SIZE 12 'º' SPACES SIZE 12 
           END-PERFORM.
           DISPLAY SPACES LINE 37 POSITION 1
                    CONTROL 'BCOLOR=BLUE' SIZE 120.
           DISPLAY ' F1 ' LINE 37 POSITION 1
                CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                'EVRAKLAR/S˜C˜LLER' REVERSE LOW 
                ' F2 ' 'DEPO' REVERSE LOW
                ' F3 ' 'AYARLAR' REVERSE LOW.
           MOVE ZEROS TO DX-DEPONO.
           
           INITIALIZE THS-KAYIT.
           MOVE SPACES TO THS-HESAPNO BAG-ACIKLAMA T-DURUM. 
           MOVE G-TIP TO DX-TIP THS-TIP.
           MOVE 2 TO DX-DEPONO.
           PERFORM NUMARA-AL.
           MOVE AY-EVRAKNO TO THS-EVRAKNO.
           PERFORM BASLIK-GOSTER.
           MOVE ZEROS TO I-TOPLAM I-ISKTOPLAM I-KDVTOPLAM T-BAKIYE.
           PERFORM ALT-TOPLAM-OKU-SON.
           DISPLAY SCR-TOPLAM.
           
           IF GORUNTU = 1
            MOVE EVRAKNO TO THS-EVRAKNO
            DISPLAY THS-EVRAKNO LINE 4 POSITION 16
                    CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
            PERFORM EVRAK-OKU THRU EVRAK-OKU-SON
            CLOSE SICIL HAREKET-SICIL 
            GO DETAY-AL.
           
       EVRAKNO-ALT-AL.
           ACCEPT THS-EVRAKNO LINE 4 POSITION 16 NO BEEP UPDATE
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 1 CALL 'HARPEN' USING THS-TIP THS-EVRAKNO
                    CANCEL 'HARPEN' GO EVRAKNO-ALT-AL.
           IF F = 27 CLOSE SICIL HAREKET-SICIL GO STOP-PROGRAM.
           IF F = 2 CALL 'DEPO' CANCEL 'DEPO' GO EVRAKNO-AL.
           IF F = 3 CALL 'AYAR' USING G-TIP CANCEL 'AYAR' GO EVRAKNO-AL.
           IF F = 58 
             IF DX-DEPONO = 1 MOVE 2 TO DX-DEPONO 
                ELSE MOVE 1 TO DX-DEPONO END-IF
             MOVE G-TIP TO DX-TIP
             PERFORM NUMARA-AL MOVE AY-EVRAKNO TO THS-EVRAKNO
             GO EVRAKNO-ALT-AL.
           IF THS-EVRAKNO = SPACES GO EVRAKNO-AL.
           PERFORM EVRAK-OKU THRU EVRAK-OKU-SON.
           IF YENI = 0 CLOSE SICIL HAREKET-SICIL GO DETAY-AL.
       DEPO-AL.
           IF AY-DEPONO NOT = ZEROS AND AY-EVRAKNO = THS-EVRAKNO
                MOVE AY-DEPONO TO THS-DEPONO
                MOVE THS-DEPONO TO T-KOD
                CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0000 0000 0000 0000 0
                CANCEL 'DEPSEC'
                MOVE T-KDV TO DF-KDV
                DISPLAY T-ADI LINE 05 POSITION 16 SIZE 20
                    CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'
                GO HESAPNO-AL.
           
           MOVE THS-DEPONO TO T-KOD.
           MOVE ZEROS TO T-KDV.
           CALL 'DEPSEC' USING 0 T-KOD T-ADI T-KDV 
                  BY CONTENT 0016 0005 0035 0010 0.
           CANCEL 'DEPSEC'.
           IF T-KOD = -1 GO EVRAKNO-AL.
           MOVE T-KOD TO THS-DEPONO.
           MOVE T-KDV TO DF-KDV.
           DISPLAY T-ADI LINE 05 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'.
            
       HESAPNO-AL.
           ACCEPT THS-HESAPNO LINE 6 POSITION 16 NO BEEP UPDATE
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 27 AND AY-DEPONO = ZEROS GO DEPO-AL.
           IF F = 27 AND AY-DEPONO NOT = ZEROS GO EVRAKNO-AL.
           IF F = 1 CALL 'SICPEN' USING THS-HESAPNO
                    CANCEL 'SICPEN' GO HESAPNO-AL.
           PERFORM HESAP-OKU THRU HESAP-OKU-SON.
           IF SC-PLKOD = 5 
            MOVE 'HESAP AKT˜F DE¦˜L !!!' TO ONAY-MESSAGE
            PERFORM WARN-MESSAGE GO HESAPNO-AL.
           IF THS-HESAPNO = SPACES GO HESAPNO-AL.           
       TARIH-AL.
           ACCEPT SCR-TARIH-AL.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-TARIH-AL.
           IF F = 27 GO HESAPNO-AL.
       SAAT-AL.
           ACCEPT SCR-SAAT-AL.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-SAAT-AL.
           IF F = 27 GO TARIH-AL.
       GUN-AL.
           ACCEPT SCR-GUN-AL.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-GUN-AL.
           IF F = 27 GO SAAT-AL.
       FTARIH-AL.
           ACCEPT SCR-FTARIH-AL.
           ACCEPT F FROM ESCAPE KEY.
           DISPLAY SCR-FTARIH-AL.
           IF F = 27 GO GUN-AL.
       DUZEN-AL.
           MOVE THS-DUZEN TO T-KOD.
           CALL 'PLASSEC' USING 0 T-KOD T-ADI
                    BY CONTENT 0095 0009 0118 0015.
           CANCEL 'PLASSEC'.
           IF T-KOD = -1 GO FTARIH-AL.
           MOVE T-KOD TO THS-DUZEN.
           DISPLAY T-ADI LINE 8 POSITION 108 SIZE 10
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'.
           
           CLOSE HAREKET-SICIL. OPEN I-O HAREKET-SICIL.
           IF YENI = 1 
                MOVE THS-KAYIT TO HS-KAYIT
                PERFORM WRITE-HAREKET-SICIL
            ELSE
                MOVE HS-HESAPNO TO TCH-HESAPNO
                MOVE HS-DEPONO TO TCH-DEPONO
                MOVE HS-TIP TO TCH-TIP
                MOVE HS-EVRAKNO TO TCH-EVRAKNO
                MOVE HS-TARIH TO TCH-TARIH
                PERFORM CARHAR-SIL
                IF THS-DEPONO NOT = HS-DEPONO OR THS-DYIL NOT = HS-DYIL
                  PERFORM STOK-ADET-DUZELT THRU STOK-ADET-DUZELT-SON
                END-IF
                MOVE THS-KAYIT TO HS-KAYIT
                PERFORM REWRITE-HAREKET-SICIL
           END-IF.
           CLOSE HAREKET-SICIL SICIL.
           PERFORM INDEX-YAZ.
           OPEN I-O HAREKET-DETAY.
           PERFORM ALT-TOPLAM-AL THRU ALT-TOPLAM-AL-SON.
           PERFORM ALT-TOPLAM-KAYDET.
           CLOSE HAREKET-DETAY.
           GO DETAY-AL.
       INDEX-YAZ.
           OPEN I-O HAREKET-INDEX.
           STRING HS-TIP HS-EVRAKNO 
                DELIMITED BY SIZE INTO HIDX-EVRAKNO.
           STRING HS-TIP HS-HESAPADI 
                DELIMITED BY SIZE INTO HIDX-HESAPADI.
           STRING HS-TIP HS-YETKILI 
                DELIMITED BY SIZE INTO HIDX-YETKILI.
           WRITE HIDX-KAYIT INVALID KEY
            REWRITE HIDX-KAYIT END-REWRITE
           END-WRITE.
           CLOSE HAREKET-INDEX.
       INDEX-SIL.
           OPEN I-O HAREKET-INDEX.
           STRING HS-TIP HS-EVRAKNO 
                DELIMITED BY SIZE INTO HIDX-EVRAKNO.
           READ HAREKET-INDEX INVALID KEY CONTINUE
              NOT INVALID KEY DELETE HAREKET-INDEX
           END-READ.
           CLOSE HAREKET-INDEX.
       
       EVRAK-OKU.
           MOVE ZEROS TO DF-ISKONTO1 DF-ISKONTO2.
           MOVE THS-EVRAKNO TO HS-EVRAKNO.
           MOVE THS-TIP TO HS-TIP.
           READ HAREKET-SICIL INVALID KEY MOVE 1 TO YENI
                PERFORM EVRAK-SIFIRLA PERFORM BASLIK-GOSTER
                GO EVRAK-OKU-SON.
           MOVE HS-KAYIT TO THS-KAYIT.
           MOVE 0 TO YENI.
           MOVE ZEROS TO DF-ISKONTO1 DF-ISKONTO2.
           PERFORM HESAP-OKU THRU HESAP-OKU-SON.
       EVRAK-OKU-SON.
       
       copy 'numara.per'.
       
       BASLIK-GOSTER.
           DISPLAY SCR-BASLIK.
           MOVE THS-DEPONO TO T-KOD.
           CALL 'DEPSEC' USING 1 T-KOD T-ADI T-KDV 
                  BY CONTENT 0000 0000 0000 0000 0.
           CANCEL 'DEPSEC'.
           MOVE T-KDV TO DF-KDV.
           DISPLAY T-ADI LINE 05 POSITION 16 SIZE 20
            CONTROL 'BCOLOR=BLUE, FCOLOR=BROWN'.
           MOVE THS-PLKOD TO T-KOD.
           CALL 'PLASSEC' USING 1 T-KOD T-ADI 
                BY CONTENT 0000 0000 0000 0000.
           CANCEL 'PLASSEC'.
           DISPLAY T-ADI LINE 34 POSITION 16 SIZE 30
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
           MOVE THS-DUZEN TO T-KOD.
           CALL 'PLASSEC' USING 1 T-KOD T-ADI 
                BY CONTENT 0000 0000 0000 0000.
           CANCEL 'PLASSEC'.
           DISPLAY T-ADI LINE 8 POSITION 108 SIZE 10
            CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'. 
           
       EVRAK-SIFIRLA.
           MOVE THS-EVRAKNO TO T-ADI.
           INITIALIZE THS-KAYIT.
           MOVE SPACES TO THS-HESAPNO.
           MOVE T-ADI  TO THS-EVRAKNO.
           MOVE G-TIP  TO THS-TIP.
           ACCEPT THS-TARIH  FROM CENTURY-DATE.
           ACCEPT THS-FTARIH FROM CENTURY-DATE.
           ACCEPT THS-SAAT   FROM TIME.
           
       HESAP-OKU.
           MOVE THS-HESAPNO TO SC-HESAPNO.
           READ SICIL INVALID KEY MOVE SPACES TO THS-HESAPNO 
                GO HESAP-OKU-SON.
           MOVE SC-HESAPADI TO THS-HESAPADI.
           MOVE SC-YETKILI  TO THS-YETKILI.
           MOVE SC-ADRES1   TO THS-ADRES1.
           MOVE SC-ADRES2   TO THS-ADRES2.
           MOVE SC-ADRES3   TO THS-ADRES3.
           MOVE SC-MAHALLE  TO THS-MAHALLE.
           MOVE SC-ILCE     TO THS-ILCE.
           MOVE SC-IL       TO THS-IL.
           MOVE SC-ULKE     TO THS-ULKE.
           MOVE SC-VDA      TO THS-VDA.
           MOVE SC-VNO      TO THS-VNO.
           MOVE SC-TCNO     TO THS-TCNO.
           MOVE SC-TSNO     TO THS-TSNO.
           MOVE SC-MERSIS   TO THS-MERSIS.
           MOVE SC-GUN      TO THS-GUN.
           MOVE SC-PLKOD    TO THS-PLKOD T-KOD.
           MOVE SC-ISKONTO1 TO DF-ISKONTO1.
           MOVE SC-ISKONTO2 TO DF-ISKONTO2.
           PERFORM BASLIK-GOSTER.
       HESAP-OKU-SON.
      ********************************************************************
      *                                                                  *
      *    DETAY B™LšMšNE G˜R˜ž                                          *
      *                                                                  *
      ********************************************************************      
       DETAY-AL.
           OPEN INPUT HAREKET-DETAY.
           DISPLAY SPACES LINE 37 POSITION 1
             CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE' SIZE 120.
             
           IF GORUNTU = 1 OR HS-SONUC NOT = ZEROS
             DISPLAY ' F10 ' LINE 37 POSITION 1 'YAZDIR' REVERSE LOW
             GO DETAY-GIRIS.
             
           DISPLAY
               ' F1 ' LINE 37 POSITION 1 'KATALOG' REVERSE LOW
               ' F2 ' 'SATIR S˜L' REVERSE LOW
               ' F3 ' 'EVRAK S˜L' REVERSE LOW
               ' F5 ' '˜RS.DEN' REVERSE LOW
               ' CS-F5 ' '˜RS.S˜L' REVERSE LOW
               ' F6 ' 'BA¦' REVERSE LOW
               ' CS-F6 ' 'BA¦-S˜L' REVERSE LOW
               ' F8 ' 'BAžLIK' REVERSE LOW
               ' F9 ' 'ANAL˜Z' REVERSE LOW
               ' F10 ' 'YAZDIR' REVERSE LOW.           
       DETAY-GIRIS.
           PERFORM ALT-TOPLAM-AL THRU ALT-TOPLAM-AL-SON.
           PERFORM BAKIYE-OKU THRU BAKIYE-OKU-SON.
           DISPLAY SCR-TOPLAM.
           MOVE SPACES TO BAG-ACIKLAMA.
           PERFORM VARYING F FROM 1 BY 1 UNTIL F > 2
            EVALUATE HS-BAGTIPI(F)
            WHEN 1 MOVE '˜RS.ÿSATIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 2 MOVE 'SATIžÿ˜RS.' TO BAG-ACIKLAMA
            WHEN 3 MOVE 'SATIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 4 MOVE '˜RS.ÿALIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 5 MOVE 'ALIžÿ˜RS.' TO BAG-ACIKLAMA
            WHEN 6 MOVE 'ALIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 7 MOVE '˜RS.ÿ˜ADE €IKIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 8 MOVE '˜ADE €IKIžÿ˜RS.' TO BAG-ACIKLAMA
            WHEN 9 MOVE '˜ADE €IKIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 10 MOVE '˜RS.ÿ˜ADE ALIžÿFAT.' TO BAG-ACIKLAMA
            WHEN 11 MOVE '˜ADE ALIžÿ˜RS.' TO BAG-ACIKLAMA
            WHEN 12 MOVE '˜ADE ALIžÿFAT.' TO BAG-ACIKLAMA
            END-EVALUATE
            IF HS-BAGTIPI(F) NOT = ZEROS
                MOVE BAG-ACIKLAMA TO TRIM-VALUE
                PERFORM TRIM-BLOCK
                   STRING TRIM-VALUE(1:TRIM-SIZE) DELIMITED BY SPACES
                       ',ÿ' DELIMITED BY SPACES
                      HS-BEVRAKNO(F) DELIMITED BY SPACES
                      INTO BAG-ACIKLAMA
            END-IF
           END-PERFORM.
           
           DISPLAY SCR-BAG.
           MOVE ILK TO SATIR.
           MOVE 1 TO HD-SIRA.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
             INVALID KEY GO A-LISTE-SON.
           READ HAREKET-DETAY NEXT.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                GO A-LISTE-SON.
           IF HD-SIRA = ZEROS GO A-LISTE-SON.
           MOVE 0 TO YENI.
       A-LISTE.
           PERFORM AKTAR.
           IF SATIR = ILK PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = SON GO A-LISTE-SON.
           READ HAREKET-DETAY NEXT AT END 
                READ HAREKET-DETAY PREVIOUS GO A-LISTE-SON.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY PREVIOUS GO A-LISTE-SON.
           IF HD-SIRA = ZEROS
                READ HAREKET-DETAY PREVIOUS GO A-LISTE-SON.
           ADD 1 TO SATIR.
           GO A-LISTE.
       A-LISTE-SON.
           MOVE SATIR TO LIS.
           IF SATIR NOT = SON ADD 1 TO SATIR
             PERFORM VARYING CIZ FROM SATIR BY 1 UNTIL CIZ > SON
                 DISPLAY SPACES LINE CIZ POSITION 2 SIZE 4
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    'º' SPACES SIZE 7 'º' SPACES SIZE 11 'º'
                    SPACES SIZE 10 'º' SPACES SIZE 28 'º'
                    SPACES SIZE 7 'º' SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º' SPACES SIZE 10 'º'
                    SPACES SIZE 12 'º' SPACES SIZE 12 
             END-PERFORM SUBTRACT 1 FROM SATIR.
       ILK-SERIT.
           MOVE ILK TO SATIR.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE 1 TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
                INVALID KEY PERFORM YENI-SERITLI 
                            MOVE 1 TO YENI HD-SIRA GO TUS.
           READ HAREKET-DETAY NEXT.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                 PERFORM YENI-SERITLI MOVE 1 TO YENI HD-SIRA GO TUS.
           IF HD-SIRA = ZEROS 
                 PERFORM YENI-SERITLI MOVE 1 TO YENI HD-SIRA GO TUS.
           PERFORM AKTAR.
       TUS.
      *     DISPLAY HD-SIRA LINE 36 POSITION 16 '-' HD-TIP '-' HD-EVRAKNO
      *        '-' HS-EVRAKNO.
           IF YENI = 0
             MOVE HD-STNO TO E-STNO MOVE HD-PRCNO TO E-PRCNO
             MOVE HD-TIPI TO E-TIPI MOVE HD-CINSI TO E-CINSI
             MOVE HD-MARKA TO E-MARKA MOVE HD-ISKONTO1 TO E-ISKONTO1
             MOVE HD-ISKONTO2 TO E-ISKONTO2 MOVE HD-ADET TO E-ADET
             MOVE HD-FIYAT TO E-FIYAT MOVE HD-KDV TO E-KDV
           ELSE
             MOVE SPACES TO E-STNO E-PRCNO E-TIPI E-CINSI E-MARKA
             MOVE ZEROS TO E-ISKONTO1 E-ISKONTO2 E-ADET E-FIYAT E-KDV
           END-IF.
           IF GORUNTU = 1 OR HS-SONUC NOT = 0
                MOVE ' ' TO DUR
                ACCEPT DUR LINE 37 POSITION 1 NO BEEP
                  CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
           ELSE
                ACCEPT E-STNO LINE SATIR POSITION 7 UPDATE 
                 NO BEEP PROMPT '.' CONTROL 'BCOLOR=RED, FCOLOR=WHITE'
           END-IF.
           ACCEPT F FROM ESCAPE KEY.
           IF F = 1 AND GORUNTU = 0 AND HS-SONUC = 0 GO KATLOG-CAGIR.
           IF F = 2 AND GORUNTU = 0 AND YENI = 0 AND HS-SONUC = 0
                GO DELETE-HAREKET-DETAY-OP.
           IF F = 3 AND GORUNTU = 0 AND HS-SONUC = 0 GO DELETE-EVRAK.
           IF F = 8 AND GORUNTU = 0 AND HS-SONUC = 0 
                MOVE ZEROS TO I-TOPLAM I-ISKTOPLAM 
               I-KDVTOPLAM 
               CLOSE HAREKET-DETAY OPEN INPUT HAREKET-SICIL SICIL
                MOVE 0 TO YENI GO DEPO-AL.
           IF F = 27 AND GORUNTU = 0 CLOSE HAREKET-DETAY GO BASLIK-AL.
           IF F = 27 AND GORUNTU = 1 CLOSE HAREKET-DETAY
                    GO STOP-PROGRAM.
           IF F = 52 GO YUKARI-AL.
           IF F = 53 GO ASAGI-AL.
           IF F = 13 AND GORUNTU = 0 AND HS-SONUC = 0 
                GO HAREKET-DETAY-DUZENLE.
           IF F = 54 GO HOME-TUSU.
           IF F = 82 GO END-TUSU.
           IF F = 68 GO PGDN-TUSU.
           IF F = 67 GO PGUP-TUSU.
           IF F = 81 GO CTHO-TUSU.
           IF F = 83 GO CTEN-TUSU.
           IF F = 5 AND GORUNTU = 0 AND HS-BEVRAKNO(1) = SPACES
                    AND HS-SONUC = 0 GO EVRAK-BAGLA.
           IF F = 6 AND GORUNTU = 0 AND HS-BEVRAKNO(2) = SPACES
                    AND HS-SONUC = 0 GO EVRAK-BAGLA.
           IF F = 35 AND HS-SONUC > 0 GO KILIT-AC.
           IF F = 37 AND HS-SONUC = 0 GO KILIT-KAPAT.
           IF F = 35 AND GORUNTU = 0 AND HS-BEVRAKNO(1) NOT = SPACES 
                    AND HS-SONUC = 0 MOVE 1 TO BS GO BAG-SIL.
           IF F = 36 AND GORUNTU = 0 AND HS-BEVRAKNO(2) NOT = SPACES 
                    AND HS-SONUC = 0 MOVE 2 TO BS GO BAG-SIL.
           IF F = 10 GO EVRAK-YAZDIR.
           IF F = 9 CALL 'STKANZ' USING HD-KATNO HD-STNO HS-DYIL 0
                CANCEL 'STKANZ'.
           GO TUS.
       copy 'onayper.cpy'.
       KILIT-KAPAT.
      *
      * YETK˜LEND˜RME KOY
      *
           MOVE 1 TO HS-SONUC.
           OPEN I-O HAREKET-SICIL.
           PERFORM REWRITE-HAREKET-SICIL.
           CLOSE HAREKET-SICIL.
           GO DETAY-GIRIS.
       KILIT-AC.
      *
      * YETK˜LEND˜RME KOY
      *
           MOVE 0 TO HS-SONUC.
           OPEN I-O HAREKET-SICIL.
           PERFORM REWRITE-HAREKET-SICIL.
           CLOSE HAREKET-SICIL.
           GO DETAY-GIRIS.
       EVRAK-YAZDIR.
           MOVE 'EVRAK YAZDIRILACAK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CALL 'CEVRAK' USING HS-TIP HS-EVRAKNO.
           CANCEL 'CEVRAK'.
           CLOSE HAREKET-DETAY. 
           IF GORUNTU = 1 GO STOP-PROGRAM.
           GO BASLIK-AL.
       EVRAK-BAGLA.
           MOVE HS-EVRAKNO TO T-EVRAKNO.
           MOVE HS-HESAPNO TO T-HESAPNO.
           IF F = 5
                EVALUATE HS-TIP 
      *  SATIž FATURASI <=> SATIž ˜RSAL˜YES˜
                WHEN 3 CALL 'EVRSEC' USING 02 00 T-HESAPNO T-EVRAKNO
      *  ALIž FATURASI <=> ALIž ˜RSAL˜YES˜
                WHEN 6 CALL 'EVRSEC' USING 05 00 T-HESAPNO T-EVRAKNO
      *  ˜ADE €IKIž FATURASI <=> ˜ADE €IKIž ˜RSAL˜YES˜
                WHEN 9 CALL 'EVRSEC' USING 08 00 T-HESAPNO T-EVRAKNO
      *  ˜ADE ALIž FATURASI <=> ˜ADE ALIž ˜RSAL˜YES˜
                WHEN 12 CALL 'EVRSEC' USING 11 00 T-HESAPNO T-EVRAKNO
                END-EVALUATE
           END-IF.
           IF F = 6
                EVALUATE HS-TIP 
      *  SATIž FATURASI <=> ˜RSAL˜YEL˜ SATIž FATURASI 
      *  SATIž FATURASI <=> SATIž FATURASI 
                WHEN 3 CALL 'EVRSEC' USING 01 03 T-HESAPNO T-EVRAKNO
      *  ALIž FATURASI <=> ˜RSAL˜YEL˜ ALIž FATURASI 
      *  ALIž FATURASI <=> ALIž FATURASI 
                WHEN 6 CALL 'EVRSEC' USING 04 06 T-HESAPNO T-EVRAKNO
      *  ˜ADE €IKIž FATURASI <=> ˜RSAL˜YEL˜ ˜ADE €IKIž FATURASI 
      *  ˜ADE €IKIž FATURASI <=> ˜ADE €IKIž FATURASI 
                WHEN 9 CALL 'EVRSEC' USING 07 09 T-HESAPNO T-EVRAKNO
      *  ˜ADE ALIž FATURASI <=> ˜RSAL˜YEL˜ ˜ADE ALIž FATURASI 
      *  ˜ADE ALIž FATURASI <=> ˜ADE ALIž FATURASI
                WHEN 12 CALL 'EVRSEC' USING 10 12 T-HESAPNO T-EVRAKNO
                END-EVALUATE
           END-IF.
           IF T-EVRAKNO NOT = SPACES 
                OPEN INPUT HAREKET-SICIL 
                READ HAREKET-SICIL 
                CLOSE HAREKET-SICIL
                GO DETAY-GIRIS 
           END-IF.
           GO TUS.
       BAG-SIL.
           EVALUATE F
           WHEN 35 MOVE ' ˜RSAL˜YE BA¦LANTISI S˜L˜NECEK ?' 
                        TO ONAY-MESSAGE
           WHEN 36 MOVE ' EVRAK BA¦LANTISI S˜L˜NECEK ?' 
                        TO ONAY-MESSAGE
           END-EVALUATE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           OPEN I-O HAREKET-SICIL.
           
           MOVE HS-BEVRAKNO(BS) TO HS-EVRAKNO.
           MOVE HS-BAGTIPI(BS) TO HS-TIP.
           READ HAREKET-SICIL INVALID KEY CONTINUE
              NOT INVALID KEY
               MOVE ZEROS TO HS-BAGTIPI(BS)
               MOVE SPACES TO HS-BEVRAKNO(BS)
               SUBTRACT 1 FROM HS-SONUC
               PERFORM REWRITE-HAREKET-SICIL
           END-READ.
           MOVE G-TIP TO HS-TIP.
           MOVE THS-EVRAKNO TO HS-EVRAKNO.
           READ HAREKET-SICIL INVALID KEY CONTINUE
              NOT INVALID KEY
               MOVE ZEROS TO HS-BAGTIPI(BS)
               MOVE SPACES TO HS-BEVRAKNO(BS)
               PERFORM REWRITE-HAREKET-SICIL
           END-READ.
           CLOSE HAREKET-SICIL.
           GO DETAY-GIRIS.
           
       DELETE-HAREKET-DETAY-OP.
           MOVE 'SATIR S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.
           CLOSE HAREKET-DETAY. OPEN I-O HAREKET-DETAY.
           READ HAREKET-DETAY.
           PERFORM DELETE-HAREKET-DETAY.
           PERFORM ALT-TOPLAM-CIKAR.
           PERFORM ALT-TOPLAM-KAYDET.
           DISPLAY SCR-TOPLAM.
           CLOSE HAREKET-DETAY. OPEN INPUT HAREKET-DETAY.
           MOVE 1 TO FLAG.
           PERFORM STOK-KAYDET THRU STOK-KAYDET-SON.
           GO DETAY-GIRIS.
       
       DELETE-EVRAK.
           MOVE 'EVRAK S˜L˜NECEK EM˜N M˜S˜N˜Z ?' TO ONAY-MESSAGE.
           MOVE 'H' TO E-ONAY.
           PERFORM ONAY-AL THRU ONAY-AL-SON.
           IF E-ONAY = 'H' OR E-ONAY = 'h' GO TUS.

           CLOSE HAREKET-DETAY.
           OPEN I-O HAREKET-SICIL HAREKET-DETAY.
      * ™NCE BA¦LI EVRAK VARSA BA¦ KOPAR     
           PERFORM VARYING F FROM 1 BY 1 UNTIL F > 2
            IF HS-BEVRAKNO(F) NOT = SPACES
                MOVE HS-BEVRAKNO(F) TO HS-EVRAKNO
                MOVE HS-BAGTIPI(F) TO HS-TIP
                READ HAREKET-SICIL INVALID KEY CONTINUE
                    NOT INVALID KEY
                    MOVE ZEROS TO HS-BAGTIPI(F)
                    MOVE SPACES TO HS-BEVRAKNO(F)
                    PERFORM REWRITE-HAREKET-SICIL
                END-READ
            END-IF
           END-PERFORM.
      * BA¦ KOPARMA B˜TT˜ EVRAKI S˜L
           MOVE G-TIP TO HS-TIP.
           MOVE THS-EVRAKNO TO HS-EVRAKNO.
           READ HAREKET-SICIL.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE ZEROS TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
                INVALID KEY GO DELETE-OKU-SON.
       DELETE-OKU.
           READ HAREKET-DETAY NEXT AT END GO DELETE-OKU-SON.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
               READ HAREKET-DETAY PREVIOUS AT END CONTINUE END-READ
               GO DELETE-OKU-SON.
           PERFORM DELETE-HAREKET-DETAY.
           MOVE 1 TO FLAG.
           PERFORM STOK-KAYDET THRU STOK-KAYDET-SON.
           GO DELETE-OKU.
       DELETE-OKU-SON.
           PERFORM DELETE-HAREKET-SICIL.
           CLOSE HAREKET-SICIL HAREKET-DETAY.
           MOVE HS-HESAPNO TO TCH-HESAPNO.
           MOVE HS-DEPONO TO TCH-DEPONO.
           MOVE HS-TIP TO TCH-TIP.
           MOVE HS-EVRAKNO TO TCH-EVRAKNO.
           MOVE HS-TARIH TO TCH-TARIH.
           PERFORM CARHAR-SIL.
           PERFORM INDEX-SIL.
           GO BASLIK-AL.
       KATLOG-CAGIR.
           MOVE 1 TO SEC.
           MOVE ZEROS TO HD-KATNO.
           MOVE SPACES TO HD-STNO MAIN-ARGUMENT-VALUE.
           STRING SEC DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  HD-KATNO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  HD-STNO DELIMITED BY SIZE
                  ';' DELIMITED BY SIZE
                  INTO MAIN-ARGUMENT-VALUE
           END-STRING.       
           MOVE 100 TO MAIN-ARGUMENT-SIZE.
           CALL 'KATLOG' USING MAIN-ARGUMENT.
           CANCEL 'KATLOG'.
           UNSTRING MAIN-ARGUMENT-VALUE DELIMITED BY ';'
                INTO SEC HD-KATNO HD-STNO
           END-UNSTRING.
           IF YENI = 1 
              MOVE HD-KATNO TO E-KATNO
              MOVE HD-STNO TO E-STNO.
              
           GO TUS.
        ALIS-FIYAT-BUL.
           IF G-TIP NOT = 4 AND G-TIP NOT = 5 AND G-TIP NOT = 6
                GO ALIS-FIYAT-BUL-SON.
		   MOVE ALL SPACES TO SQLQUERY.
           STRING "select " DELIMITED BY SIZE
                  "iskonto1," DELIMITED BY SIZE 
                  "maliyet" DELIMITED BY SIZE
                  " from " DELIMITED BY SIZe
                  "windows.win_alimdetay " DELIMITED BY SIZE
                  "where katno = :1" DELIMITED BY SIZE
                  " and stno = ':2'" DELIMITED BY SIZE
                  INTO SQLQUERY.
       
           CALL "exec_scaler" USING IZLE "***remotedns***" "root"
                "root" SQLQUERY E-KATNO E-STNO E-ISKONTO1 E-FIYAT.
           CANCEL "exec_scaler".
        ALIS-FIYAT-BUL-SON.
      * ALIS-FIYAT-BUL.
      *     IF G-TIP NOT = 4 AND G-TIP NOT = 5 AND G-TIP NOT = 6
      *          GO ALIS-FIYAT-BUL-SON.
      *     OPEN INPUT STOKBAG KATLOG STOK.
      *     MOVE E-KATNO TO SB-KATNO.
      *     MOVE E-STNO TO SB-STNO.
      *     MOVE ZEROS TO SB-SIRA.
      *     START STOKBAG KEY IS NOT LESS THAN SB-KEY
      *      INVALID KEY CLOSE STOKBAG KATLOG STOK
      *                  GO ALIS-FIYAT-BUL-SON.
      * ALIS-FIYAT-OKU.
      *     READ STOKBAG NEXT AT END
      *                  CLOSE STOKBAG KATLOG STOK
      *                  GO ALIS-FIYAT-BUL-SON.
      *     IF SB-KATNO NOT = E-KATNO OR SB-STNO NOT = E-STNO
      *                  CLOSE STOKBAG KATLOG STOK
      *                  GO ALIS-FIYAT-BUL-SON.
      *     MOVE SB-BAG-KATNO TO KT-KATNO.
      *     READ KATLOG INVALID KEY GO ALIS-FIYAT-OKU.
      *     IF KT-HESAPNO NOT = HS-HESAPNO GO ALIS-FIYAT-OKU.
      *     MOVE SB-BAG-KATNO TO ST-KATNO.
      *     MOVE SB-BAG-STNO TO ST-STNO.
      *     READ STOK INVALID KEY GO ALIS-FIYAT-OKU.
      *     MOVE SB-ISK TO E-ISKONTO1.
      *     IF DF-KDV = ZEROS
      *          MOVE SB-KDV TO E-KDV.
      *     MOVE ST-FIYAT TO E-FIYAT.
      *     CLOSE STOKBAG KATLOG STOK.
      * ALIS-FIYAT-BUL-SON.
       
       
       HAREKET-DETAY-DUZENLE.
       STOK-OKU.
           MOVE DF-KDV TO E-KDV.
           MOVE DF-ISKONTO1 TO E-ISKONTO1.
           MOVE DF-ISKONTO2 TO E-ISKONTO2.
           IF E-STNO = SPACES MOVE ZEROS TO E-KATNO GO STOK-OKU-SON.
           INITIALIZE TST-KAYIT.
           MOVE E-STNO TO TST-CINSI.
           CALL 'STOKSEC' USING TST-KAYIT HS-DYIL BY CONTENT 0017 0.
           CANCEL 'STOKSEC'.
           IF TST-STNO = SPACES GO TUS.
           IF G-TIP = 1 OR G-TIP = 2 OR G-TIP = 3 OR 
		      G-TIP = 7 OR G-TIP = 8 OR G-TIP = 9
            CALL 'KAMPANYA' USING TST-KATNO TST-STNO TST-FIYAT
                    BY CONTENT SATIR 0006
            CANCEL 'KAMPANYA'
           END-IF.
           MOVE TST-KATNO TO E-KATNO.
           MOVE TST-STNO TO E-STNO.
           MOVE TST-PRCNO TO E-PRCNO.
           MOVE TST-TIPI TO E-TIPI.
           MOVE TST-CINSI TO E-CINSI.
           MOVE TST-MARKA TO E-MARKA.
           MOVE TST-FIYAT TO E-FIYAT.
           PERFORM ALIS-FIYAT-BUL THRU ALIS-FIYAT-BUL-SON.
           CALL 'STKANZ' USING E-KATNO E-STNO HS-DYIL ISLEM.
           CANCEL 'STKANZ'.
           IF ISLEM = 0 GO TUS.
       STOK-OKU-SON.
           PERFORM DUZENLE-GOSTER.
       PARCANO-AL.
           ACCEPT E-PRCNO LINE SATIR POSITION 15 UPDATE PROMPT '.'
             NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO TUS.
       TIPI-AL.    
           ACCEPT E-TIPI LINE SATIR POSITION 27 UPDATE PROMPT '.'
             NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO PARCANO-AL.
       CINSI-AL.    
           ACCEPT E-CINSI LINE SATIR POSITION 38 UPDATE PROMPT '.'
             NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO TIPI-AL.
       MARKA-AL.    
           ACCEPT E-MARKA LINE SATIR POSITION 67 UPDATE PROMPT '.'
             NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO CINSI-AL.
       ISKONTO1-AL.
           MOVE E-ISKONTO1 TO ZI-KK.
           ACCEPT ZI-KK LINE SATIR POSITION 75 UPDATE PROMPT '.'
             NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE ZI-KK TO E-ISKONTO1.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO CINSI-AL.
       ISKONTO2-AL.
           MOVE E-ISKONTO2 TO ZI-KK.
           ACCEPT ZI-KK LINE SATIR POSITION 78 UPDATE PROMPT '.'
            NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE ZI-KK TO E-ISKONTO2.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO ISKONTO1-AL.
           IF DF-KDV NOT = ZEROS GO ADET-AL.
       KDV-AL.
           MOVE E-KDV TO ZI-KK.
           ACCEPT ZI-KK LINE SATIR POSITION 81 UPDATE PROMPT '.'
            NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE ZI-KK TO E-KDV.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO ISKONTO2-AL.
       ADET-AL.
           MOVE E-ADET TO ZI-ADET.
           ACCEPT ZI-ADET LINE SATIR POSITION 84 UPDATE PROMPT '.'
            NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE ZI-ADET TO E-ADET.
           PERFORM DUZENLE-GOSTER.
           IF (F = 27 OR F = 52) AND DF-KDV = ZEROS GO KDV-AL.
           IF (F = 27 OR F = 52) AND DF-KDV NOT = ZEROS GO ISKONTO2-AL.
       FIYAT-AL.
           MOVE E-FIYAT TO ZI-FIYAT.
           ACCEPT ZI-FIYAT LINE SATIR POSITION 95 UPDATE PROMPT '.' 
            NO BEEP CONTROL 'BCOLOR=RED, FCOLOR=WHITE'.
           ACCEPT F FROM ESCAPE KEY.
           MOVE ZI-FIYAT TO E-FIYAT.
           PERFORM DUZENLE-GOSTER.
           IF F = 27 OR F = 52 GO ADET-AL.
           
      * ESK˜ ˜SE TOPLAM €IKAR
            
           IF YENI = 0 PERFORM ALT-TOPLAM-CIKAR
                       MOVE 1 TO FLAG
                       PERFORM STOK-KAYDET THRU STOK-KAYDET-SON.
           
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.         
           MOVE E-KATNO TO HD-KATNO.
           MOVE E-STNO TO HD-STNO.
           MOVE E-PRCNO TO HD-PRCNO.
           MOVE E-TIPI TO HD-TIPI.
           MOVE E-CINSI TO HD-CINSI.
           MOVE E-MARKA TO HD-MARKA.
           MOVE E-ISKONTO1 TO HD-ISKONTO1.
           MOVE E-ISKONTO2 TO HD-ISKONTO2.
           MOVE E-KDV TO HD-KDV.
           MOVE E-ADET TO HD-ADET.
           MOVE E-FIYAT TO HD-FIYAT.
           
           CLOSE HAREKET-DETAY. OPEN I-O HAREKET-DETAY.
           IF YENI = 1
              PERFORM WRITE-HAREKET-DETAY
           ELSE
              PERFORM REWRITE-HAREKET-DETAY
           END-IF.
           PERFORM ALT-TOPLAM-EKLE.
           PERFORM ALT-TOPLAM-KAYDET.
           DISPLAY SCR-TOPLAM.
           CLOSE HAREKET-DETAY. OPEN INPUT HAREKET-DETAY.
           READ HAREKET-DETAY.
           
           MOVE 0 TO FLAG.
           PERFORM STOK-KAYDET THRU STOK-KAYDET-SON.
           
           PERFORM AKTAR.
           MOVE 0 TO YENI.
           GO ASAGI-AL.
       
       DUZENLE-GOSTER.
           MOVE E-ISKONTO1 TO Z-ISKONTO1.
           MOVE E-ISKONTO2 TO Z-ISKONTO2.
           MOVE E-KDV TO Z-KDV.
           MOVE E-ADET TO Z-ADET.
           MOVE E-FIYAT TO Z-FIYAT.
           COMPUTE I-TUTAR ROUNDED = E-ADET * E-FIYAT.
           MOVE I-TUTAR TO Z-TUTAR.
           DISPLAY E-KATNO LINE SATIR POSITION 2 SIZE 4
                 CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' 'º'  
                    E-STNO SIZE 7 'º' E-PRCNO SIZE 11 'º' 
                    E-TIPI SIZE 10 'º' E-CINSI SIZE 28 'º' 
                    E-MARKA SIZE 7 'º' Z-ISKONTO1 'º' Z-ISKONTO2 
                    'º' Z-KDV 'º' Z-ADET 'º' Z-FIYAT 'º' Z-TUTAR.
       CTHO-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI SUBTRACT 1 FROM HD-SIRA.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE 1 TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY.
           READ HAREKET-DETAY NEXT.
           MOVE ILK TO SATIR.
           GO A-LISTE.
       CTEN-TUSU.
           IF YENI = 1 GO TUS.
           IF LIS NOT = SON GO END-TUSU.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           MOVE 9999999999 TO HD-SIRA.
           START HAREKET-DETAY KEY IS NOT GREATER THAN HD-KEY.
           READ HAREKET-DETAY NEXT.
           MOVE SON TO SATIR.
           PERFORM Y-LISTE THRU Y-LISTE-SON.
           COMPUTE F = SON - ILK.
           PERFORM F TIMES
              READ HAREKET-DETAY NEXT
           END-PERFORM.
           PERFORM AKTAR.
           MOVE SON TO SATIR LIS.
           GO TUS.
           
       PGUP-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI SUBTRACT 1 FROM HD-SIRA
            ELSE
              PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
             PERFORM SATIR TIMES
               READ HAREKET-DETAY PREVIOUS
             END-PERFORM.
           READ HAREKET-DETAY PREVIOUS AT END
                READ HAREKET-DETAY NEXT 
                MOVE ILK TO SATIR
                PERFORM AKTAR PERFORM SERITLI
                GO TUS.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY NEXT 
                MOVE ILK TO SATIR
                PERFORM AKTAR PERFORM SERITLI
                GO TUS.
           IF HD-SIRA = ZEROS READ HAREKET-DETAY NEXT
                MOVE ILK TO SATIR
                PERFORM AKTAR PERFORM SERITLI
                GO TUS.
           MOVE SON TO SATIR.
       PG-OKU.
           IF SATIR = ILK GO PG-OKU-SON.
           READ HAREKET-DETAY PREVIOUS AT END 
                    READ HAREKET-DETAY NEXT 
                    GO PG-OKU-SON.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY NEXT GO PG-OKU-SON.
           IF HD-SIRA = ZEROS READ HAREKET-DETAY NEXT
                READ HAREKET-DETAY NEXT GO PG-OKU-SON.
           SUBTRACT 1 FROM SATIR.
           GO PG-OKU.
       PG-OKU-SON.
           MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           COMPUTE SATIR = LIS - ILK.
               PERFORM SATIR TIMES
                   READ HAREKET-DETAY PREVIOUS
               END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR.
           GO TUS.
       PGDN-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
           PERFORM SATIR TIMES
              READ HAREKET-DETAY NEXT
           END-PERFORM.
           READ HAREKET-DETAY NEXT AT END
              READ HAREKET-DETAY PREVIOUS
              MOVE LIS TO SATIR 
              PERFORM AKTAR PERFORM SERITLI 
              GO TUS.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY PREVIOUS
                MOVE LIS TO SATIR 
                PERFORM AKTAR PERFORM SERITLI 
                GO TUS.
           IF HD-SIRA = ZEROS READ HAREKET-DETAY NEXT
                READ HAREKET-DETAY PREVIOUS
                MOVE LIS TO SATIR 
                PERFORM AKTAR PERFORM SERITLI 
                GO TUS.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITSIZ.
           COMPUTE F = SATIR + 1.
           MOVE F TO SATIR.
           READ HAREKET-DETAY NEXT AT END 
             READ HAREKET-DETAY PREVIOUS MOVE ILK TO SATIR.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
             READ HAREKET-DETAY PREVIOUS MOVE ILK TO SATIR.
           IF HD-SIRA = ZEROS 
             READ HAREKET-DETAY PREVIOUS MOVE ILK TO SATIR.
           PERFORM A-LISTE THRU A-LISTE-SON.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       END-TUSU.
           IF YENI = 1 GO TUS.
           PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - LIS.
              PERFORM SATIR TIMES
                 READ HAREKET-DETAY NEXT
              END-PERFORM.
           MOVE LIS TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.

       HOME-TUSU.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI SUBTRACT 1 FROM HD-SIRA
           ELSE
              PERFORM SERITSIZ.
           COMPUTE SATIR = SATIR - ILK.
           PERFORM SATIR TIMES
                  READ HAREKET-DETAY PREVIOUS
           END-PERFORM.
           MOVE ILK TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       YUKARI-AL.
           IF YENI = 1 AND SATIR = ILK GO TUS.
           IF YENI = 1 PERFORM YENI-SERITSIZ SUBTRACT 1 FROM SATIR
               MOVE 0 TO YENI SUBTRACT 1 FROM HD-SIRA PERFORM SERITLI 
               GO TUS.
           READ HAREKET-DETAY PREVIOUS AT END 
                READ HAREKET-DETAY NEXT GO TUS.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY NEXT GO TUS.
           IF HD-SIRA = ZEROS 
                READ HAREKET-DETAY NEXT GO TUS.
           IF SATIR = ILK PERFORM A-LISTE THRU A-LISTE-SON
                COMPUTE SATIR = LIS - ILK
                PERFORM SATIR TIMES
                    READ HAREKET-DETAY PREVIOUS
                END-PERFORM
                MOVE ILK TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           SUBTRACT 1 FROM SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           MOVE 0 TO YENI.
           GO TUS.
       ASAGI-AL.
           IF YENI = 1 GO TUS.
           READ HAREKET-DETAY NEXT AT END 
                 READ HAREKET-DETAY PREVIOUS GO ASAGI-YENI-AL.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                 READ HAREKET-DETAY PREVIOUS GO ASAGI-YENI-AL.
           IF HD-SIRA = ZEROS 
                 READ HAREKET-DETAY PREVIOUS GO ASAGI-YENI-AL.
           IF SATIR = SON PERFORM Y-LISTE THRU Y-LISTE-SON
                 COMPUTE F = SON - ILK
                 PERFORM F TIMES
                      READ HAREKET-DETAY NEXT
                 END-PERFORM
                 MOVE SON TO SATIR PERFORM AKTAR GO TUS.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM AKTAR. PERFORM SERITLI.
           GO TUS.
       ASAGI-YENI-AL.
           IF GORUNTU = 1 OR HS-SONUC NOT = ZEROS GO TUS.
           MOVE 1 TO YENI.
           IF SATIR = SON 
                   SUBTRACT 1 FROM SATIR
                   PERFORM Y-LISTE THRU Y-LISTE-SON
                   COMPUTE F = SON - ILK - 1
                   PERFORM F TIMES
                      READ HAREKET-DETAY NEXT
                   END-PERFORM
                   MOVE SON TO SATIR PERFORM AKTAR 
                   PERFORM YENI-SERITLI
                   GO TUS
           END-IF.
           PERFORM SERITSIZ.
           ADD 1 TO SATIR.
           PERFORM YENI-SERITLI.
           MOVE SPACES TO E-STNO E-PRCNO E-TIPI E-CINSI E-MARKA.
           MOVE ZEROS TO E-ISKONTO1 E-ISKONTO2 E-ADET E-FIYAT.
           GO TUS.
       Y-LISTE.
           PERFORM AKTAR.
           IF SATIR = SON PERFORM SERITLI ELSE PERFORM SERITSIZ.
           IF SATIR = ILK GO Y-LISTE-SON.
           READ HAREKET-DETAY PREVIOUS AT END READ HAREKET-DETAY NEXT 
                GO Y-LISTE-SON.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO
                READ HAREKET-DETAY NEXT GO Y-LISTE-SON.
           IF HD-SIRA = ZEROS READ HAREKET-DETAY NEXT GO Y-LISTE-SON.
           SUBTRACT 1 FROM SATIR.
           GO Y-LISTE.
       Y-LISTE-SON.
       AKTAR.
           MOVE HD-KATNO TO B-KATNO.
           MOVE HD-STNO TO B-STNO.
           MOVE HD-PRCNO TO B-PRCNO.
           MOVE HD-TIPI TO B-TIPI.
           MOVE HD-CINSI TO B-CINSI.
           MOVE HD-MARKA TO B-MARKA.
           MOVE HD-ISKONTO1 TO B-ISKONTO1.
           MOVE HD-ISKONTO2 TO B-ISKONTO2.
           MOVE HD-KDV TO B-KDV.
           MOVE HD-ADET TO B-ADET.
           MOVE HD-FIYAT TO B-FIYAT.
           COMPUTE I-TUTAR ROUNDED = HD-ADET * HD-FIYAT.
           MOVE I-TUTAR TO B-TUTAR.
       SERITLI.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE' LOW.
       SERITSIZ.
           DISPLAY BILGI LINE SATIR POSITION 2
              CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'.
       YENI-SERITLI.
           ADD 1 TO HD-SIRA.
           DISPLAY SPACES LINE SATIR POSITION 2 SIZE 4
                 CONTROL 'BCOLOR=WHITE, FCOLOR=BLUE, LOW'
                    'º' SPACES SIZE 7 'º' SPACES SIZE 11 'º'
                    SPACES SIZE 10 'º' SPACES SIZE 28 'º'
                    SPACES SIZE 7 'º' SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º' SPACES SIZE 10 'º'
                    SPACES SIZE 12 'º' SPACES SIZE 12.
       YENI-SERITSIZ.
           DISPLAY SPACES LINE SATIR POSITION 2 SIZE 4
                 CONTROL 'BCOLOR=BLUE, FCOLOR=WHITE'
                    'º' SPACES SIZE 7 'º' SPACES SIZE 11 'º'
                    SPACES SIZE 10 'º' SPACES SIZE 28 'º'
                    SPACES SIZE 7 'º' SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º'
                    SPACES SIZE 2 'º' SPACES SIZE 10 'º'
                    SPACES SIZE 12 'º' SPACES SIZE 12.

       STOK-ADET-DUZELT.
           IF HS-TIP = 3 OR HS-TIP = 6 OR HS-TIP = 9 OR HS-TIP = 12
              OR HS-TIP = 13 OR HS-TIP = 14          
                GO STOK-ADET-DUZELT-SON. 
           OPEN INPUT HAREKET-DETAY.
           MOVE 1 TO HD-SIRA.
           MOVE HS-TIP TO HD-TIP.
           MOVE HS-EVRAKNO TO HD-EVRAKNO.
           START HAREKET-DETAY KEY IS NOT LESS THAN HD-KEY
             INVALID KEY GO STOK-ADET-DUZELT-SON.
           OPEN I-O STOKADET INPUT STOK.
       STOK-ADET-OKU.
           READ HAREKET-DETAY NEXT AT END GO STOK-ADET-OKU-SON.
           IF HD-TIP NOT = HS-TIP OR HD-EVRAKNO NOT = HS-EVRAKNO 
                GO STOK-ADET-OKU-SON.
           IF HD-SIRA = ZEROS GO STOK-ADET-OKU.
           IF HD-KATNO = ZEROS OR HD-STNO = SPACES GO STOK-ADET-OKU.
           MOVE HD-KATNO TO STA-KATNO ST-KATNO.
           MOVE HD-STNO TO STA-STNO ST-STNO.
           READ STOK INVALID KEY GO STOK-ADET-OKU.
           MOVE HS-DEPONO TO STA-DEPONO.
           MOVE HS-DYIL TO STA-YIL.
           READ STOKADET INVALID KEY MOVE ZEROS TO STA-GIR STA-CIK
                    PERFORM WRITE-STOKADET
           END-READ.
           EVALUATE HS-TIP
           WHEN 1 WHEN 2 WHEN 7 WHEN 8 SUBTRACT HD-ADET FROM STA-CIK
           WHEN 4 WHEN 5 WHEN 10 WHEN 11 SUBTRACT HD-ADET FROM STA-GIR
           END-EVALUATE.
           IF STA-GIR = ZEROS AND STA-CIK = ZEROS
            PERFORM DELETE-STOKADET
           ELSE 
            PERFORM REWRITE-STOKADET
           END-IF.
           MOVE THS-DEPONO TO STA-DEPONO.
           MOVE THS-DYIL TO STA-YIL.
           READ STOKADET INVALID KEY MOVE ZEROS TO STA-GIR STA-CIK
                    PERFORM WRITE-STOKADET
           END-READ.
           EVALUATE HS-TIP
           WHEN 1 WHEN 2 WHEN 7 WHEN 8 ADD HD-ADET TO STA-CIK
           WHEN 4 WHEN 5 WHEN 10 WHEN 11 ADD HD-ADET TO STA-GIR
           END-EVALUATE.
           IF STA-GIR = ZEROS AND STA-CIK = ZEROS
            PERFORM DELETE-STOKADET
           ELSE
            PERFORM REWRITE-STOKADET 
           END-IF.
           GO STOK-ADET-OKU.
       STOK-ADET-OKU-SON.
           CLOSE STOKADET STOK.       
       STOK-ADET-DUZELT-SON.
           CLOSE HAREKET-DETAY.
       
       STOK-KAYDET.
	       DISPLAY HS-TIP LINE 33 POSITION 1.
           IF HS-TIP = 3 OR HS-TIP = 6 OR HS-TIP = 9 OR HS-TIP = 12
              OR HS-TIP = 13 OR HS-TIP = 14          
                GO STOK-KAYDET-SON. 
           IF HD-KATNO = ZEROS OR HD-STNO = SPACES 
                GO STOK-KAYDET-SON.
           OPEN I-O STOKADET INPUT STOK.
           MOVE HD-KATNO TO STA-KATNO ST-KATNO.
           MOVE HD-STNO TO STA-STNO ST-STNO.
           READ STOK INVALID KEY CLOSE STOKADET STOK GO STOK-KAYDET-SON.
           MOVE HS-DEPONO TO STA-DEPONO.
           MOVE HS-DYIL TO STA-YIL.
           READ STOKADET INVALID KEY MOVE ZEROS TO STA-GIR STA-CIK
                    PERFORM WRITE-STOKADET
           END-READ.
           EVALUATE HS-TIP
           WHEN 1 WHEN 2 WHEN 7 WHEN 8
                         IF FLAG = 0 ADD HD-ADET TO STA-CIK
                         ELSE SUBTRACT HD-ADET FROM STA-CIK END-IF
           WHEN 4 WHEN 5 WHEN 10 WHEN 11
                         IF FLAG = 0 ADD HD-ADET TO STA-GIR
                         ELSE SUBTRACT HD-ADET FROM STA-GIR END-IF
           END-EVALUATE.
           IF STA-GIR = ZEROS AND STA-CIK = ZEROS
            PERFORM DELETE-STOKADET
           ELSE
            PERFORM REWRITE-STOKADET
           END-IF.
           CLOSE STOKADET STOK.
       STOK-KAYDET-SON.

       
        
       copy 'toplam.per'.
               
               
             
       STOP-PROGRAM.
           MOVE WCB TO NESTED-WCB (1).
           DISPLAY NESTED-WCB (1)
              LINE 1 POSITION 1 CONTROL 'WINDOW-REMOVE'.
           EXIT PROGRAM.
           STOP RUN.
       copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-SICIL==
                                  "%T%"  BY "HAREKET-SICIL"
                                 ==%R%== BY ==HS-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==HAREKET-DETAY==
                                  "%T%"  BY "HAREKET-DETAY"
                                 ==%R%== BY ==HD-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==STOKADET==
                                  "%T%"  BY "STOKADET"
                                 ==%R%== BY ==STA-KAYIT==.
       copy fileop.cpy REPLACING ==%T%== BY ==AYAR==
                                  "%T%"  BY "AYAR"
                                 ==%R%== BY ==AY-KAYIT==.

       
