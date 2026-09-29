      *********************************************************************
      * $Header:   U:\C85\DEV\VERIFY\VCS\WIN.CPV   5.0   15 Sep 1990  1:44:32   BILL  $
      * 								  *
      *   WIN.CPY  -  Copy library for window test programs.              *
      *                                                                   *
      *********************************************************************

       77  WINDOW-LINE                  PIC 999 BINARY.
       77  WINDOW-POSITION              PIC 999 BINARY.
       77  WINDOW-STATUS                PIC 999.
       01  CREATE-WINDOW-CONTROL-PHRASE.
           03  FILLER                   PIC X(14) VALUE "WINDOW-CREATE".
           03  WINDOW-CONTROL-PHRASE    PIC X(100).

       01  WCB.
           03  WCB-HANDLE               PIC 999 BINARY  VALUE 0.
           03  WCB-NUM-ROWS             PIC 999 BINARY.
           03  WCB-NUM-COLS             PIC 999 BINARY.
           03  WCB-LOCATION-REFERENCE   PIC X.
               88  WCB-LOCATION-SCREEN-RELATIVE   VALUE "S".
               88  WCB-LOCATION-WINDOW-RELATIVE   VALUE "W".
           03  WCB-BORDER-SWITCH        PIC X.
               88 WCB-BORDER-ON                   VALUE "Y"
                                                  WHEN FALSE "N".
           03  WCB-BORDER-TYPE          PIC 9.
           03  WCB-BORDER-CHAR          PIC X.
           03  WCB-FILL-SWITCH          PIC X.
               88 WCB-FILL-ON                     VALUE "Y"
                                                  WHEN FALSE "N".
           03  WCB-FILL-CHAR            PIC X.
           03  WCB-TITLE-LOCATION       PIC X.
               88 WCB-TITLE-TOP                   VALUE "T".
               88 WCB-TITLE-BOTTOM                VALUE "B".
           03  WCB-TITLE-POSITION       PIC X.
               88 WCB-TITLE-CENTER                VALUE "C".
               88 WCB-TITLE-LEFT                  VALUE "L".
               88 WCB-TITLE-RIGHT                 VALUE "R".
           03  WCB-TITLE-LENGTH         PIC 999 BINARY.
           03  WCB-TITLE                PIC X(64).
       77 NESTED-INDEX          PIC 99 BINARY VALUE 0.
       01 NESTED-WINDOWS.
           03 NESTED-WCB        PIC X(120) OCCURS 9.
       