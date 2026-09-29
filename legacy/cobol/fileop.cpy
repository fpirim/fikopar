       WRITE-%T%.
           CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "%T%" "INSERT" %R%.
           CANCEL "executesql".
           WRITE %R% INVALID KEY
                MOVE 0 TO WVAR 
                CALL "rollback"
           NOT INVALID KEY 
                MOVE 1 TO WVAR
                CALL "commit"
           END-WRITE.
       END-WRITE-%T%.
       REWRITE-%T%.
           CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "%T%" "UPDATE" %R%.
           CANCEL "executesql".
           REWRITE %R% INVALID KEY
                MOVE 0 TO WVAR 
                CALL "rollback"
           NOT INVALID KEY 
                MOVE 1 TO WVAR
                CALL "commit"
           END-REWRITE.
       END-REWRITE-%T%.
       DELETE-%T%.
           CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "%T%" "DELETE" %R%.
           CANCEL "executesql".
           DELETE %T% INVALID KEY
                MOVE 0 TO WVAR 
                CALL "rollback"
           NOT INVALID KEY 
                MOVE 1 TO WVAR
                CALL "commit"
           END-DELETE.
       END-DELETE-%T%.
	   TRUNCATE-%T%.
	       CALL "executesql" USING "***remotedns***" "USER" "PASS" 
             "COBOL" "%T%" "DELETEALL" "not used parameter!!".
           CANCEL "executesql".
		   OPEN OUTPUT %T%.
		   CLOSE %T%.
		   CALL "commit".
       END-TRUNCATE-%T%.
	   
	   
	   