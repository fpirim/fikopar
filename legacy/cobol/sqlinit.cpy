           DISPLAY "%T%" " start reading ...".
		   OPEN INPUT %T%.
		   CALL "executesql" USING "***remotedns***" "USER" "PASS"
		     "COBOL" "%T%" "TRUNCATE" "not used parameter!!".
           CANCEL "executesql".	
           INITIALIZE %R%.
	   WRITE-%T%.
           READ %T% NEXT AT END GO END-WRITE-%T%.
		   DISPLAY %R%.
		   CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "%T%" "INSERT" %R%.
           CANCEL "executesql".
           GO WRITE-%T%.
       END-WRITE-%T%.
           CALL "commit".
		   CLOSE %T%.
		   DISPLAY "%T%" " end.".