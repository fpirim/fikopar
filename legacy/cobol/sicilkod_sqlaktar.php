<?php
set_time_limit(0);
include 'RMReader.php';
include 'status.php';

$data = RMReader::read('DATA\\SICILKOD.DAT');
$data_size = count($data);

$db = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

if($db->connect_errno > 0){
    die('Unable to connect to database [' . $db->connect_error . ']');
}

$sql = "CREATE TABLE IF NOT EXISTS SICILKOD (
        hesapno varchar(15),
        kod int(4),
        PRIMARY KEY (hesapno,kod)
    );";

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}

$dbex = new mysqli(getenv('FIKOPAR_DB_HOST') ?: 'localhost:3306', getenv('FIKOPAR_DB_USER') ?: 'fikopar', getenv('FIKOPAR_DB_PASS') ?: '', getenv('FIKOPAR_DB_NAME') ?: 'fikopar');

$update_query = "update sicilkod set hesapno = ?, kod = ? where hesapno = ? and kod = ?";
$insert_query = "insert into sicilkod values (?,?)";

$delete_query = "delete from sicilkod where hesapno = ? and kod = ?";

if(!$update_stmt = $dbex->prepare($update_query))
    die("Error create update statement [" . $dbex->error . "]");

$update_stmt->bind_param("sisi", $hesapno, $kod, $hesapno, $kod);

if(!$insert_stmt = $dbex->prepare($insert_query))
    die("Error create insert statement [" . $dbex->error . "]");

$insert_stmt->bind_param("si", $hesapno, $kod);

if(!$delete_stmt = $dbex->prepare($delete_query))
    die("Error create delete statement [" . $dbex->error . "]");

$delete_stmt->bind_param("si", $hesapno, $kod);

$dbex->query("START TRANSACTION");


$sql = 'select * from sicilkod';

if(!$result = $db->query($sql)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
     $key = sprintf("%-15s%4s", toCP857($row['hesapno']), str_pad(number_format($row['kod'],0,'',''),4,'0',STR_PAD_LEFT));
     $val = sprintf("%-15s%4s", toCP857($row['hesapno']), str_pad(number_format($row['kod'],0,'',''),4,'0',STR_PAD_LEFT));
     if(isset($data[$key])) {
         if($data[$key] == $val) {
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
           continue;
         } else {
           $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
           $kod = floatVal(substr($data[$key],15,4));
           $update_stmt->execute();
           unset($data[$key]);
           show_status($data_size - count($data), $data_size);
         }
     } else {
         $hesapno = $row['hesapno']; $kod = $row['kod'];
         $delete_stmt->execute();
     }
}

foreach($data as $key => $val) {
   $hesapno = (string)  rtrim(toUTF8(substr($data[$key],0,15)));
   $kod = floatVal(substr($data[$key],15,4));
   $insert_stmt->execute();
   unset($data[$key]);           
   show_status($data_size - count($data), $data_size);
}
$update_stmt->close();
$insert_stmt->close();
$delete_stmt->close();
$dbex->query("COMMIT");
$dbex->close();
$db->close();


function toCP857($data) {
    return iconv('UTF-8','CP857', $data);
}
function toUTF8($data) {
    return iconv('CP857','UTF-8', $data);
}
?>
