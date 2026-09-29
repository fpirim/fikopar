<?php
set_time_limit(0);
ini_set('memory_limit', '-1');

function start_time() {
    $mtime = microtime(); 
    $mtime = explode(" ",$mtime); 
    $mtime = $mtime[1] + $mtime[0]; 
    $GLOBALS['starttime'] = $mtime; 
}
function end_time($msg) {
    $mtime = microtime(); 
    $mtime = explode(" ",$mtime); 
    $mtime = $mtime[1] + $mtime[0]; 
    $endtime = $mtime; 
    $totaltime = ($endtime - $GLOBALS['starttime']); 
    echo "$msg in #". number_format($totaltime, 6) ." seconds.\n"; 
}
function execute($cmd) {
    $proc = proc_open($cmd, [['pipe','r'],['pipe','w'],['pipe','w']], $pipes);
    while(($line = fgets($pipes[1])) !== false) {
        fwrite(STDOUT,$line);
    }
    while(($line = fgets($pipes[2])) !== false) {
        fwrite(STDERR,$line);
    }
    fclose($pipes[0]);
    fclose($pipes[1]);
    fclose($pipes[2]);
    return proc_close($proc);
}
function isConnectionActive() {
	if(!$link = mysqli_connect(getenv("FIKOPAR_STATUS_DB_HOST") ?: "localhost", getenv("FIKOPAR_STATUS_DB_USER") ?: "fikopar", getenv("FIKOPAR_STATUS_DB_PASS") ?: "")) {
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
	    echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
	    echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	    exit;
	}
	$link->query("SET NAMES 'utf8'");
	$link->query("SET CHARACTER SET utf8_turkish_ci");

	if(!$stmt = $link->query("select active from b2b.wb_connection where id = 2")) {
		echo "Error create select statement [" . $link->error . "]" . PHP_EOL;
		exit();	
	}
	$row = $stmt->fetch_assoc();
	$link->close();
	if($row['active'] == "1")
		return true;
	return false;
}
$processes = Array ( 
    Array ( "fileName" => "DATA\\TIPKOD.DAT",
            "lastModified" => 0,
            "command" => "tipkod_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\PLASIYER.DAT",
            "lastModified" => 0,
            "command" => "plasiyer_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\SICIL.DAT",
            "lastModified" => 0,
            "command" => "sicil_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\SICILTEL.DAT",
            "lastModified" => 0,
            "command" => "siciltel_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\SICILKOD.DAT",
            "lastModified" => 0,
            "command" => "sicilkod_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\HARSIC.DAT",
            "lastModified" => 0,
            "command" => "harsic_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\HARDET.DAT",
            "lastModified" => 0,
            "command" => "hardet_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\STKADT.DAT",
            "lastModified" => 0,
            "command" => "stkadt_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\CARHAR.DAT",
            "lastModified" => 0,
            "command" => "carhar_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\CEKSENET.DAT",
            "lastModified" => 0,
            "command" => "ceksenet_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\CSNLOG.DAT",
            "lastModified" => 0,
            "command" => "csnlog_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\MAKBUZ.DAT",
            "lastModified" => 0,
            "command" => "makbuz_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\ODEME.DAT",
            "lastModified" => 0,
            "command" => "odeme_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\DEPO.DAT",
            "lastModified" => 0,
            "command" => "depo_sqlaktar.php" ),
    Array ( "fileName" => "DATA\\KASA.DAT",
            "lastModified" => 0,
            "command" => "kasa_sqlaktar.php" ));
while(1) {
	while(isConnectionActive()) {
		echo "Veri aktariliyor ... bitmesi bekleniyor ...\n";
		usleep(1000*10000);	
	}
	if(!$link = mysqli_connect(getenv("FIKOPAR_REMOTE_DB_HOST") ?: "db.example.com", getenv("FIKOPAR_REMOTE_DB_USER") ?: "fikopar", getenv("FIKOPAR_REMOTE_DB_PASS") ?: "")) {
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
	    echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
	    echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	    exit;
	}
	$link->query("SET NAMES 'utf8'");
	$link->query("SET CHARACTER SET utf8_turkish_ci");
	$link->query("update b2b.wb_connection set message = DATE_FORMAT(CURRENT_TIMESTAMP(),'%Y%m%d%H%i%s'), active = 1 where id = 3;");
	$link->query("COMMIT");

    foreach($processes as $key => $process) {
        if(file_exists($process['fileName'])) {
            $lastModified = filemtime($process['fileName']);
            echo "$process[fileName] last modified $lastModified == $process[lastModified] \n";
            if($lastModified != $process['lastModified']) {
                start_time();
                echo "INFO : ($process[command]) Started. \n";
                execute("C:\\php\\php.exe $process[command]");
                end_time("INFO : ($process[command]) Done");
                $process['lastModified'] = $lastModified;
                $processes[$key] = $process;
            } else {
               echo "INFO : $process[fileName] does not change.\n";
            }
        } else {
            echo "ERROR : $process[fileName] does not exists !!!\n";
        }
        echo "\n";
    }
	$link->query("update b2b.wb_connection set active = 0 where id = 3;");
	$link->query("COMMIT");
	$link->close();
    start_time();
    echo "INFO : Program going to sleep ... ";
    usleep(1000*10000);
    end_time("end sleep");
}
?>